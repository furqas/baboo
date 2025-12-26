package com.furqas.upload_service.service.impl

import com.furqas.upload_service.client.MetadataClient
import com.furqas.upload_service.client.StorageClient
import com.furqas.upload_service.dto.ChunkUploadResponse
import com.furqas.upload_service.dto.CreateVideoRequest
import com.furqas.upload_service.dto.InitiateUploadRequest
import com.furqas.upload_service.dto.InitiateUploadResponse
import com.furqas.upload_service.dto.TranscodeJobEvent
import com.furqas.upload_service.dto.UpdateVideoRequest
import com.furqas.upload_service.exceptions.InvalidChunkException
import com.furqas.upload_service.exceptions.InvalidVideoUploadException
import com.furqas.upload_service.exceptions.UploadNotFoundException
import com.furqas.upload_service.model.UploadState
import com.furqas.upload_service.model.enums.UploadStatus
import com.furqas.upload_service.producers.TranscoderJobProducer
import com.furqas.upload_service.service.UploadService
import lombok.RequiredArgsConstructor
import org.springframework.boot.jackson.autoconfigure.JacksonProperties
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@RequiredArgsConstructor
@Service
class UploadServiceImpl(
    private val storageClient: StorageClient,
    private val metadataClient: MetadataClient,
    private final val producer: TranscoderJobProducer,
    private val redisTemplate: RedisTemplate<String, UploadState>,
    private final val CHUNK_SIZE: Long = 5 * 1024 * 1024 // 5MB
): UploadService {

    override fun initiateUpload(
        request: InitiateUploadRequest
    ): InitiateUploadResponse {

        validateVideoFile(request.fileName, request.fileSize, request.contentType)

        val uploadId = UUID.randomUUID()
        val videoId = UUID.randomUUID()

        val totalChunks = (request.fileSize / CHUNK_SIZE) + if (request.fileSize % CHUNK_SIZE > 0) 1 else 0

        val uploadState = UploadState(
            id = uploadId,
            videoId = videoId,
            userId = request.userId,
            fileName = request.fileName,
            fileSize = request.fileSize,
            totalChunks = totalChunks.toInt(),
            uploadedChunks = 0,
            status = UploadStatus.INITIATED,
            createdAt = LocalDateTime.now()
        )
        redisTemplate.opsForValue().set(
            "upload:$uploadId",
            uploadState,
        )

        metadataClient.createVideo(
            CreateVideoRequest(
                accountId = request.userId,
                title = request.fileName.substringBeforeLast('.'),
                status = "PROCESSING",
                description = "",
                visibility = "PRIVATE",
                videoId = videoId,
            )
        )

        return InitiateUploadResponse(
            uploadId = uploadId,
            videoId = videoId,
            chunkSize = CHUNK_SIZE,
            totalChunks = totalChunks.toInt()
        )
    }

    override fun uploadChunk(
        uploadId: String,
        chunkNumber: Int,
        totalChunks: Int,
        data: ByteArray
    ): ChunkUploadResponse {
        val state = redisTemplate.opsForValue().get("upload:$uploadId")
            ?: throw UploadNotFoundException("Upload not found")

        var result = true
        var progress = (state.uploadedChunks.toDouble() / totalChunks) * 100

        try {
            if (chunkNumber !in 0..<totalChunks) {
                throw InvalidChunkException("Invalid chunk number")
            }

            val chunkKey = "uploads/temp/${state.videoId}/chunk-$chunkNumber"

            storageClient.upload(
                bucket = "raw",
                key = chunkKey,
                data = data
            )

            val updatedState = state.copy(
                uploadedChunks = state.uploadedChunks + 1,
                status = if (state.uploadedChunks + 1 == totalChunks)
                    UploadStatus.PROCESSING
                else
                    UploadStatus.UPLOADING
            )

            redisTemplate.opsForValue().set(
                "upload:$uploadId",
                updatedState,
                Duration.ofHours(24)
            )

            progress = (updatedState.uploadedChunks.toDouble() / totalChunks) * 100

            if (updatedState.uploadedChunks == totalChunks) {
                assembleAndProcess(updatedState)
            }

        } catch (exception: Exception) {
            result = false
        }

        return ChunkUploadResponse(
            uploadId = uploadId,
            chunkNumber = chunkNumber,
            uploaded = result,
            progress = progress
        )
    }

    private fun assembleAndProcess(state: UploadState) {
        val finalKey = "raw/${state.videoId}/original.mp4"

        val chunks = (0 until state.totalChunks).map { i ->
            "uploads/temp/${state.videoId}/chunk-$i"
        }

        storageClient.assembleChunks(
            sourceBucket = "raw",
            sourceKeys = chunks,
            destBucket = "raw",
            destKey = finalKey
        )

        chunks.forEach { chunkKey ->
            storageClient.delete("raw", chunkKey)
        }

        metadataClient.updateVideo(
            state.videoId,
            UpdateVideoRequest(
                status = "PROCESSED"
            )
        )

        // transcoding queue
        producer.createJob(
            event = TranscodeJobEvent(
                videoId = state.videoId.toString(),
                s3Key = finalKey,
                userId = state.userId,
                fileName = state.fileName
            ))

        redisTemplate.delete("upload:${state.id}")
    }

    fun completeUpload(uploadId: String): CompleteUploadResponse {
        val stateJson = redisTemplate.opsForValue().get("upload:$uploadId")
            ?: throw UploadNotFoundException("Upload not found")
        val state = Json.decodeFromString<UploadState>(stateJson)

        return CompleteUploadResponse(
            videoId = state.videoId,
            status = state.status,
            message = "Upload completed, video is being processed"
        )
    }

    private fun validateVideoFile(fileName: String, fileSize: Long, contentType: String) {
        val allowedExtensions = listOf("mp4", "webm", "mov", "avi", "mkv")
        val extension = fileName.substringAfterLast('.').lowercase()
        if (extension !in allowedExtensions) {
            throw InvalidVideoUploadException("Invalid file type. Allowed: ${allowedExtensions.joinToString()}")
        }

        // max: 10GB
        if (fileSize > 10L * 1024 * 1024 * 1024) {
            throw InvalidVideoUploadException("File too large. Max size: 10GB")
        }

        if (!contentType.startsWith("video/")) {
            throw InvalidVideoUploadException("Invalid content type. Must be video/*")
        }
    }
}