package com.furqas.upload_service.service.impl

import com.furqas.upload_service.dto.CancelUploadResponse
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
import com.furqas.upload_service.service.MetadataClientService
import com.furqas.upload_service.service.StorageService
import com.furqas.upload_service.service.UploadService
import lombok.RequiredArgsConstructor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@RequiredArgsConstructor
@Service
class UploadServiceImpl(
    private final val storageService: StorageService,
    private final val metadataClient: MetadataClientService,
    private final val producer: TranscoderJobProducer,
    private final val redisTemplate: RedisTemplate<String, UploadState>,
    private final val CHUNK_SIZE: Long = 5 * 1024 * 1024,// 5MB
    private final val log: Logger = LoggerFactory.getLogger(UploadServiceImpl::class.java)
): UploadService {

    override fun initiateUpload(
        request: InitiateUploadRequest
    ): InitiateUploadResponse {

        validateVideoFile(request.fileName, request.fileSize, request.contentType)

        val uploadId = UUID.randomUUID()
        val videoId = UUID.randomUUID()

        val totalChunks = (request.fileSize / CHUNK_SIZE) + if (request.fileSize % CHUNK_SIZE > 0) 1 else 0

        log.info("Initiating upload: uploadId=$uploadId, videoId=$videoId, totalChunks=$totalChunks")

        val uploadState = UploadState(
            id = uploadId,
            videoId = videoId,
            userId = request.userId,
            fileName = request.fileName,
            fileSize = request.fileSize,
            totalChunks = totalChunks.toInt(),
            uploadedChunks = 0,
            status = UploadStatus.INITIATED,
            createdAt = LocalDateTime.now(),
            resolutions = request.resolutions.toString(),
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
                resolutions = request.resolutions
            )
        )

        return InitiateUploadResponse(
            uploadId = uploadId,
            videoId = videoId,
            chunkSize = CHUNK_SIZE,
            totalChunks = totalChunks.toInt(),
            resolutions = request.resolutions,
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
            if (chunkNumber !in 1..totalChunks) {
                throw InvalidChunkException("Invalid chunk number. Expected 1 to $totalChunks, got $chunkNumber")
            }

            val chunkKey = "uploads/temp/${state.videoId}/chunk-$chunkNumber"

            storageService.upload(
                bucket = "raw",
                key = chunkKey,
                data = data,
                contentType = "application/octet-stream",
            )

            val newUploadedChunks = state.uploadedChunks + 1

            val updatedState = state.copy(
                uploadedChunks = newUploadedChunks,
                status = if (newUploadedChunks == state.totalChunks)
                    UploadStatus.PROCESSING
                else
                    UploadStatus.UPLOADING
            )

            log.info("Uploaded chunks: $newUploadedChunks to the of ${state.totalChunks} for uploadId=$uploadId in key=$chunkKey")

            redisTemplate.opsForValue().set(
                "upload:$uploadId",
                updatedState,
                Duration.ofHours(24)
            )

            progress = (updatedState.uploadedChunks.toDouble() / totalChunks) * 100

            if (updatedState.uploadedChunks == totalChunks) {
                assembleAndProcess(updatedState)
            }

        } catch (e: Exception) {
            log.error("Error uploading chunk $chunkNumber for uploadId=$uploadId: ${e.message}", e)
            result = false
        }

        return ChunkUploadResponse(
            uploadId = uploadId,
            chunkNumber = chunkNumber,
            uploaded = result,
            progress = progress
        )
    }

    override fun cancelUpload(uploadId: String): CancelUploadResponse {
        val state = redisTemplate.opsForValue().get("upload:$uploadId")
            ?: throw UploadNotFoundException("Upload not found")

        try {
            val rangeToBeDeleted = 1..state.totalChunks

            val keys = rangeToBeDeleted.map { chunkNumber ->
                "uploads/temp/${state.videoId}/chunk-$chunkNumber"
            }

            storageService.deleteMultiple(
                "raw",
                keys
            )
        } catch (e: Exception) {
            log.error("Error while canceling the upload: ", e)

            // TODO: throw error here
        }

        redisTemplate.delete("upload:$uploadId")


        return CancelUploadResponse(
            id = state.id.toString(),
            uploadedChunks = state.uploadedChunks,
            totalChunks = state.totalChunks
        )
    }

    private fun assembleAndProcess(state: UploadState) {
        val finalKey = "raw/${state.videoId}/original.mp4"

        val chunks = (1..state.totalChunks).map { i ->
            "uploads/temp/${state.videoId}/chunk-$i"
        }

        storageService.assembleChunks(
            sourceKeys = chunks,
            destBucket = "raw",
            destKey = finalKey
        )

        chunks.forEach { chunkKey ->
            storageService.delete("raw", chunkKey)
        }

        val processingTimeInMinutes = Duration.between(state.createdAt, LocalDateTime.now()).toMinutes()

        log.info("Assembled chunks into final video at key=$finalKey for uploadId=${state.id} in $processingTimeInMinutes minutes")

        metadataClient.updateVideo(
            UpdateVideoRequest(
                status = "PROCESSED",
                visibility = "PUBLIC"
            ),
            state.videoId.toString()
        )

        // transcoding queue
        producer.createJob(
            event = TranscodeJobEvent(
                videoId = state.videoId.toString(),
                s3Key = finalKey,
                userId = state.userId,
                fileName = state.fileName,
                resolutions = state.resolutions.split(",") // turning into a list again
            ))

        redisTemplate.delete("upload:${state.id}")
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