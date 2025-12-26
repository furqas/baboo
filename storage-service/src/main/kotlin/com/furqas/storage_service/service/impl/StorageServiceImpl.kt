package com.furqas.storage_service.service.impl

import com.furqas.storage_service.dto.PresignedUrlResponse
import com.furqas.storage_service.dto.InitiateMultipartResponse
import com.furqas.storage_service.dto.MultipartPresignedUrlsResponse
import com.furqas.storage_service.dto.CompleteMultipartResponse
import com.furqas.storage_service.dto.AbortMultipartResponse
import com.furqas.storage_service.dto.UploadResponse
import com.furqas.storage_service.dto.DeleteResponse
import com.furqas.storage_service.dto.BatchDeleteResponse
import com.furqas.storage_service.dto.CopyResponse
import com.furqas.storage_service.dto.MoveResponse
import com.furqas.storage_service.dto.ListFilesResponse
import com.furqas.storage_service.dto.FileMetadataResponse
import com.furqas.storage_service.dto.CompletedPartRequest
import com.furqas.storage_service.dto.PresignedPartUrl
import com.furqas.storage_service.dto.FileInfo
import com.furqas.storage_service.exception.FileTooLargeException
import com.furqas.storage_service.exception.InvalidBucketRequestException
import com.furqas.storage_service.service.StorageService
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.core.sync.RequestBody
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.time.Duration
import java.time.Instant
import kotlin.time.ExperimentalTime

@Service
class StorageServiceImpl(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val cloudFrontDomain: String = System.getenv("CLOUDFRONT_DOMAIN") ?: "example.cloudfront.net",
    private val rawBucket: String = System.getenv("RAW_BUCKET") ?: "raw-bucket",
    private val processedBucket: String = System.getenv("PROCESSED_BUCKET") ?: "processed-bucket",
    private val thumbnailsBucket: String = System.getenv("THUMBNAILS_BUCKET") ?: "thumbnails-bucket"
): StorageService {


    @OptIn(ExperimentalTime::class)
    override fun generatePresignedDownloadUrl(
        bucket: String,
        key: String,
        expiresIn: Int?
    ): PresignedUrlResponse {
        val exp = expiresIn ?: 3600
        validateBucket(bucket)

        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val presignedRequest = s3Presigner.presignGetObject { builder ->
            builder
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofSeconds(exp.toLong()))
        }

        return PresignedUrlResponse(
            url = presignedRequest.url().toString(),
            expiresAt = Instant.now().plusSeconds(exp.toLong()),
            method = "GET",
            requiredHeaders = null
        )
    }

    override fun generatePresignedUploadUrl(
        bucket: String,
        key: String,
        contentType: String,
        expiresIn: Int?
    ): PresignedUrlResponse {
        val exp = expiresIn ?: 3600
        validateBucket(bucket)

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build()

        val presignedRequest = s3Presigner.presignPutObject { builder ->
            builder
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofSeconds(exp.toLong()))
        }

        return PresignedUrlResponse(
            url = presignedRequest.url().toString(),
            expiresAt = Instant.now().plusSeconds(exp.toLong()),
            method = "PUT",
            requiredHeaders = mapOf("Content-Type" to contentType)
        )
    }


    override fun initiateMultipartUpload(
        bucket: String,
        key: String,
        contentType: String,
        metadata: Map<String, String>?
    ): InitiateMultipartResponse {
        val meta = metadata ?: emptyMap()
        validateBucket(bucket)

        val request = CreateMultipartUploadRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .metadata(meta)
            .build()

        val response = s3Client.createMultipartUpload(request)

        return InitiateMultipartResponse(
            uploadId = response.uploadId(),
            bucket = bucket,
            key = key
        )
    }

    override fun generateMultipartPresignedUrls(
        bucket: String,
        key: String,
        uploadId: String,
        totalParts: Int,
        expiresIn: Int?
    ): MultipartPresignedUrlsResponse {
        val exp = expiresIn ?: 7200 // 2 hours for big uploads
        validateBucket(bucket)

        val urls = (1..totalParts).map { partNumber ->
            val uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build()

            val presignedRequest = s3Presigner.presignUploadPart { builder ->
                builder
                    .uploadPartRequest(uploadPartRequest)
                    .signatureDuration(Duration.ofSeconds(exp.toLong()))
            }

            PresignedPartUrl(
                partNumber = partNumber,
                url = presignedRequest.url().toString()
            )
        }

        return MultipartPresignedUrlsResponse(
            uploadId = uploadId,
            urls = urls,
            expiresAt = Instant.now().plusSeconds(exp.toLong())
        )
    }

    override fun completeMultipartUpload(
        bucket: String,
        key: String,
        uploadId: String,
        parts: List<CompletedPartRequest>
    ): CompleteMultipartResponse {
        validateBucket(bucket)

        val completedParts = parts.map { part ->
            CompletedPart.builder()
                .partNumber(part.partNumber)
                .eTag(part.etag)
                .build()
        }

        val completedMultipartUpload = CompletedMultipartUpload.builder()
            .parts(completedParts)
            .build()

        val request = CompleteMultipartUploadRequest.builder()
            .bucket(bucket)
            .key(key)
            .uploadId(uploadId)
            .multipartUpload(completedMultipartUpload)
            .build()

        val response = s3Client.completeMultipartUpload(request)

        val headResponse = s3Client.headObject {
            it.bucket(bucket)
            it.key(key)
        }

        return CompleteMultipartResponse(
            bucket = bucket,
            key = key,
            location = response.location(),
            etag = response.eTag(),
            size = headResponse.contentLength()
        )
    }

    override fun abortMultipartUpload(
        bucket: String,
        key: String,
        uploadId: String
    ): AbortMultipartResponse {
        validateBucket(bucket)

        val request = AbortMultipartUploadRequest.builder()
            .bucket(bucket)
            .key(key)
            .uploadId(uploadId)
            .build()

        s3Client.abortMultipartUpload(request)

        return AbortMultipartResponse(aborted = true)
    }


    override fun uploadFile(
        bucket: String,
        key: String,
        data: ByteArray,
        contentType: String
    ): UploadResponse {
        validateBucket(bucket)

        if (data.size > 5 * 1024 * 1024 ){ // 5mb
            throw FileTooLargeException("File too large for direct upload. Use multipart upload.")
        }

        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .contentLength(data.size.toLong())
            .build()

        s3Client.putObject(request, RequestBody.fromBytes(data))

        return UploadResponse(
            bucket = bucket,
            key = key,
            url = getPublicUrl(bucket, key),
            size = data.size.toLong(),
            contentType = contentType
        )
    }


    override fun downloadFile(bucket: String, key: String): ByteArray {
        validateBucket(bucket)

        val request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        return s3Client.getObject(request).readAllBytes()
    }

    override fun deleteFile(bucket: String, key: String): DeleteResponse {
        validateBucket(bucket)

        val request = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        s3Client.deleteObject(request)

        return DeleteResponse(
            deleted = true,
            bucket = bucket,
            key = key
        )
    }

    override fun deleteFiles(bucket: String, keys: List<String>): BatchDeleteResponse {
        validateBucket(bucket)

        val objectIdentifiers = keys.map { key ->
            ObjectIdentifier.builder().key(key).build()
        }

        val delete = Delete.builder()
            .objects(objectIdentifiers)
            .build()

        val request = DeleteObjectsRequest.builder()
            .bucket(bucket)
            .delete(delete)
            .build()

        val response = s3Client.deleteObjects(request)

        val deleted = response.deleted().map { it.key() }
        val failed = response.errors().map { it.key() }

        return BatchDeleteResponse(
            deleted = deleted,
            failed = failed
        )
    }


    override fun copyFile(
        sourceBucket: String,
        sourceKey: String,
        destBucket: String,
        destKey: String
    ): CopyResponse {
        validateBucket(sourceBucket)
        validateBucket(destBucket)

        val request = CopyObjectRequest.builder()
            .copySource("$sourceBucket/$sourceKey")
            .destinationBucket(destBucket)
            .destinationKey(destKey)
            .build()

        s3Client.copyObject(request)

        return CopyResponse(
            copied = true,
            destBucket = destBucket,
            destKey = destKey
        )
    }

    override fun moveFile(
        sourceBucket: String,
        sourceKey: String,
        destBucket: String,
        destKey: String
    ): MoveResponse {
        copyFile(sourceBucket, sourceKey, destBucket, destKey)
        deleteFile(sourceBucket, sourceKey)
        return MoveResponse(
            moved = true,
            destBucket = destBucket,
            destKey = destKey
        )
    }

    override fun listFiles(
        bucket: String,
        prefix: String,
        maxKeys: Int,
        continuationToken: String?
    ): ListFilesResponse {
        validateBucket(bucket)

        val requestBuilder = ListObjectsV2Request.builder()
            .bucket(bucket)
            .prefix(prefix)
            .maxKeys(maxKeys)

        if (continuationToken != null) {
            requestBuilder.continuationToken(continuationToken)
        }

        val request = requestBuilder.build()

        val response = s3Client.listObjectsV2(request)

        val files = response.contents().map { obj ->
            FileInfo(
                key = obj.key(),
                size = obj.size(),
                lastModified = obj.lastModified(),
                etag = obj.eTag()
            )
        }

        return ListFilesResponse(
            bucket = bucket,
            prefix = prefix,
            files = files,
            isTruncated = response.isTruncated,
            continuationToken = response.nextContinuationToken(),
            totalFiles = files.size
        )
    }

    override fun getFileMetadata(bucket: String, key: String): FileMetadataResponse {
        validateBucket(bucket)

        val request = HeadObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val response = s3Client.headObject(request)

        return FileMetadataResponse(
            bucket = bucket,
            key = key,
            size = response.contentLength(),
            contentType = response.contentType(),
            etag = response.eTag(),
            lastModified = response.lastModified(),
            metadata = response.metadata()
        )
    }

    override fun fileExists(bucket: String, key: String): Boolean {
        validateBucket(bucket)

        return try {
            s3Client.headObject {
                it.bucket(bucket)
                it.key(key)
            }
            true
        } catch (e: Exception) {
            false
        }
    }


    override fun getPublicUrl(bucket: String, key: String): String {
        return "https://$cloudFrontDomain/$key"
    }


    private fun validateBucket(bucket: String) {
        val allowedBuckets = listOf(rawBucket, processedBucket, thumbnailsBucket)
        if (bucket !in allowedBuckets) {
            throw InvalidBucketRequestException("Invalid bucket: $bucket")
        }
    }

}