package com.furqas.upload_service.service.impl

import com.furqas.upload_service.service.StorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File

@Component
class S3StorageServiceImpl(
    private val logger: Logger = LoggerFactory.getLogger(S3StorageServiceImpl::class.java),
    private val client: S3Client
): StorageService {

    override fun upload(
        bucket: String,
        key: String,
        data: ByteArray,
        contentType: String
    ) {
        try {
            val putObjectRequest = PutObjectRequest.builder()
                                            .bucket(bucket)
                                            .key(key)
                                            .contentType(contentType)
                                            .build()

            client.putObject(putObjectRequest, RequestBody.fromBytes(data))

        } catch (e: Exception) {
            logger.error("Error uploading to S3: ${e.message}", e)
        }
    }

    override fun assembleChunks(
        sourceBucket: String,
        sourceKeys: List<String>,
        destBucket: String,
        destKey: String
    ) {
        val chunks = mutableListOf<File>()

        try {
            val response: ResponseInputStream<GetObjectResponse> = client.getObject { builder ->
                builder.bucket(sourceBucket)
                builder.key(sourceKeys[0])
            }

            chunks.add(
                File.createTempFile(response.response().metadata()["name"] ?: throw RuntimeException("Chunk does not have the metadata needed"), null)
            )
        } catch (e: Exception) {
            logger.error("Error while getting the chunk from the S3: ${e.message}", e)
            // TODO: throw a exception here
        }

        val listFile = File.createTempFile("chunks", ".txt")

        listFile.bufferedWriter().use { writer ->
            chunks.forEach { chunk ->
                writer.write("file '${chunk.absolutePath}'\n")
            }
        }

        val outputFile = File.createTempFile("assembled", null)

        // ffmpeg to concat the binaries
        val process = ProcessBuilder(
            "ffmpeg",
            "-f", "concat",
            "-safe", "0",
            "-i", listFile.absolutePath,
            "-c", "copy",  // Stream copy - no re-encoding!
            outputFile.absolutePath
        ).redirectErrorStream(true)
            .start()

        process.waitFor()
        chunks.forEach { chunk -> chunk.delete() }
        listFile.delete()

        if (process.exitValue() != 0) {
            throw RuntimeException("FFmpeg failed")
        }    }

    override fun delete(
        bucket: String,
        key: String
    ) {
        try {
            val deleteRequest = DeleteObjectRequest
                .builder()
                .bucket(bucket)
                .key(key)
                .build()

            client.deleteObject(deleteRequest)
        } catch (e: Exception) {
            logger.error("Error deleting from S3: ${e.message}", e)
        }
    }

    override fun deleteMultiple(
        bucket: String,
        keys: List<String>
    ) {
        try {
            for (key in keys) {
                val deleteRequest = DeleteObjectRequest
                    .builder()
                    .bucket(bucket)
                    .key(key)
                    .build()

                client.deleteObject(deleteRequest)
            }
        } catch (e: Exception) {
            logger.error("Error deleting from S3: ${e.message}", e)
        }
    }
}