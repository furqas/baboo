package com.furqas.storage_service.controller

import com.furqas.storage_service.service.StorageService
import com.furqas.storage_service.dto.*
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType

@RequestMapping($$"${api.version}/storages")
@RestController
class StorageController(
    private final val storageService: StorageService
) {
    @GetMapping("/download-url")
    fun generatePresignedDownloadUrl(
        @RequestParam bucket: String,
        @RequestParam key: String,
        @RequestParam(required = false) expiresIn: Int?
    ): PresignedUrlResponse =
        storageService.generatePresignedDownloadUrl(bucket, key, expiresIn)

    @GetMapping("/upload-url")
    fun generatePresignedUploadUrl(
        @RequestParam bucket: String,
        @RequestParam key: String,
        @RequestParam contentType: String,
        @RequestParam(required = false) expiresIn: Int?
    ): PresignedUrlResponse =
        storageService.generatePresignedUploadUrl(bucket, key, contentType, expiresIn)

    @PostMapping("/multipart/initiate")
    fun initiateMultipartUpload(
        @RequestParam bucket: String,
        @RequestParam key: String,
        @RequestParam contentType: String,
        @RequestBody(required = false) metadata: Map<String, String>?
    ): InitiateMultipartResponse =
        storageService.initiateMultipartUpload(bucket, key, contentType, metadata)

    @GetMapping("/multipart/urls")
    fun generateMultipartPresignedUrls(
        @RequestParam bucket: String,
        @RequestParam key: String,
        @RequestParam uploadId: String,
        @RequestParam totalParts: Int,
        @RequestParam(required = false) expiresIn: Int?
    ): MultipartPresignedUrlsResponse =
        storageService.generateMultipartPresignedUrls(bucket, key, uploadId, totalParts, expiresIn)

    @PostMapping("/multipart/complete")
    fun completeMultipartUpload(
        @RequestParam bucket: String,
        @RequestParam key: String,
        @RequestParam uploadId: String,
        @RequestBody parts: List<CompletedPartRequest>
    ): CompleteMultipartResponse =
        storageService.completeMultipartUpload(bucket, key, uploadId, parts)

    @PostMapping("/multipart/abort")
    fun abortMultipartUpload(
        @RequestParam bucket: String,
        @RequestParam key: String,
        @RequestParam uploadId: String
    ): AbortMultipartResponse =
        storageService.abortMultipartUpload(bucket, key, uploadId)

    @PostMapping("/upload", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun uploadFile(
        @RequestParam bucket: String,
        @RequestParam key: String,
        @RequestParam contentType: String,
        @RequestBody data: ByteArray
    ): UploadResponse =
        storageService.uploadFile(bucket, key, data, contentType)

    @GetMapping("/download")
    fun downloadFile(
        @RequestParam bucket: String,
        @RequestParam key: String
    ): ResponseEntity<ByteArray> =
        ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(storageService.downloadFile(bucket, key))

    @DeleteMapping("/file")
    fun deleteFile(
        @RequestParam bucket: String,
        @RequestParam key: String
    ): DeleteResponse =
        storageService.deleteFile(bucket, key)

    @DeleteMapping("/files")
    fun deleteFiles(
        @RequestParam bucket: String,
        @RequestBody keys: List<String>
    ): BatchDeleteResponse =
        storageService.deleteFiles(bucket, keys)

    @PostMapping("/copy")
    fun copyFile(
        @RequestParam sourceBucket: String,
        @RequestParam sourceKey: String,
        @RequestParam destBucket: String,
        @RequestParam destKey: String
    ): CopyResponse =
        storageService.copyFile(sourceBucket, sourceKey, destBucket, destKey)

    @PostMapping("/move")
    fun moveFile(
        @RequestParam sourceBucket: String,
        @RequestParam sourceKey: String,
        @RequestParam destBucket: String,
        @RequestParam destKey: String
    ): MoveResponse =
        storageService.moveFile(sourceBucket, sourceKey, destBucket, destKey)

    @GetMapping("/list")
    fun listFiles(
        @RequestParam bucket: String,
        @RequestParam prefix: String,
        @RequestParam(required = false, defaultValue = "1000") maxKeys: Int,
        @RequestParam(required = false) continuationToken: String?
    ): ListFilesResponse =
        storageService.listFiles(bucket, prefix, maxKeys, continuationToken)

    @GetMapping("/metadata")
    fun getFileMetadata(
        @RequestParam bucket: String,
        @RequestParam key: String
    ): FileMetadataResponse =
        storageService.getFileMetadata(bucket, key)

    @GetMapping("/exists")
    fun fileExists(
        @RequestParam bucket: String,
        @RequestParam key: String
    ): Boolean =
        storageService.fileExists(bucket, key)

    @GetMapping("/public-url")
    fun getPublicUrl(
        @RequestParam bucket: String,
        @RequestParam key: String
    ): String =
        storageService.getPublicUrl(bucket, key)
}