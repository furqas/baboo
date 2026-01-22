package com.furqas.upload_service.service.impl

import com.furqas.upload_service.service.StorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
    private val client: S3Client,

    @Value("\${spring.cloud.aws.s3.raw-bucket-url}")
    private val rawBucket: String,

    @Value("\${spring.cloud.aws.s3.processed-bucket-url}")
    private val processedBucket: String

): StorageService {

    override fun upload(
        bucket: String,
        key: String,
        data: ByteArray,
        contentType: String
    ) {
        try {
            val putObjectRequest = PutObjectRequest.builder()
                                            .bucket(
                                                if(bucket == "raw") rawBucket else if(bucket == "processed") processedBucket else throw IllegalArgumentException("Invalid bucket name")
                                            )
                                            .key(key)
                                            .contentType(contentType)
                                            .build()

            client.putObject(putObjectRequest, RequestBody.fromBytes(data))

        } catch (e: Exception) {
            logger.error("Error uploading to S3: ${e.message}", e)
        }
    }

    override fun assembleChunks(
        sourceKeys: List<String>,
        destBucket: String,
        destKey: String
    ) {
        val chunks = mutableListOf<File>()
        val outputFile = File.createTempFile("assembled-video", ".mp4")

        try {
            logger.info("Starting to download ${sourceKeys.size} chunks from S3")

            sourceKeys.forEachIndexed { index, key ->
                try {
                    val response: ResponseInputStream<GetObjectResponse> = client.getObject { builder ->
                        builder.bucket(rawBucket)
                        builder.key(key)
                    }

                    val chunkFile = File.createTempFile("chunk-$index", ".bin")
                    chunkFile.outputStream().use { output ->
                        response.copyTo(output)
                    }
                    chunks.add(chunkFile)
                    logger.info("Downloaded chunk $index: $key (size: ${chunkFile.length()} bytes)")
                } catch (e: Exception) {
                    logger.error("Error downloading chunk $key: ${e.message}", e)
                    throw e
                }
            }

            logger.info("Starting binary concatenation of ${chunks.size} chunks")

            outputFile.outputStream().use { output ->
                chunks.forEach { chunkFile ->
                    chunkFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }

            logger.info("Binary concatenation completed. Final file size: ${outputFile.length()} bytes")
            logger.info("Uploading assembled video to S3: $destKey")

            val resolvedDestBucket = if(destBucket == "raw") rawBucket
                else if(destBucket == "processed") processedBucket
                else throw IllegalArgumentException("Invalid bucket name")

            val putRequest = PutObjectRequest.builder()
                .bucket(resolvedDestBucket)
                .key(destKey)
                .contentType("video/mp4")
                .build()

            client.putObject(putRequest, RequestBody.fromFile(outputFile))

            logger.info("Successfully uploaded assembled video to S3: $destKey")

        } catch (e: Exception) {
            logger.error("Error assembling chunks: ${e.message}", e)
            throw e
        } finally {
            logger.info("Cleaning up temporary files")
            chunks.forEach { chunk ->
                try {
                    if (chunk.delete()) {
                        logger.debug("Deleted chunk file: ${chunk.absolutePath}")
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to delete chunk file: ${chunk.absolutePath}, ${e.message}")
                }
            }
            try {
                if (outputFile.delete()) {
                    logger.debug("Deleted output file: ${outputFile.absolutePath}")
                }
            } catch (e: Exception) {
                logger.warn("Failed to delete output file: ${outputFile.absolutePath}, ${e.message}")
            }
        }
    }

    override fun delete(
        bucket: String,
        key: String
    ) {
        try {
            val deleteRequest = DeleteObjectRequest
                .builder()
                .bucket(
                    if(bucket == "raw") rawBucket else if(bucket == "processed") processedBucket else throw IllegalArgumentException("Invalid bucket name")
                )
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
                    .bucket(
                        if(bucket == "raw") rawBucket else if(bucket == "processed") processedBucket else throw IllegalArgumentException("Invalid bucket name")
                    )
                    .key(key)
                    .build()

                client.deleteObject(deleteRequest)
            }
        } catch (e: Exception) {
            logger.error("Error deleting from S3: ${e.message}", e)
        }
    }
}