package com.furqas.storage_service.service

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

interface StorageService {
    fun generatePresignedDownloadUrl(bucket: String, key: String, expiresIn: Int? = 3600): PresignedUrlResponse
    fun generatePresignedUploadUrl(bucket: String, key: String, contentType: String, expiresIn: Int? = 3600): PresignedUrlResponse
    fun initiateMultipartUpload(bucket: String, key: String, contentType: String, metadata: Map<String, String>? = emptyMap()): InitiateMultipartResponse
    fun generateMultipartPresignedUrls(bucket: String, key: String, uploadId: String, totalParts: Int, expiresIn: Int? = 7200): MultipartPresignedUrlsResponse
    fun completeMultipartUpload(bucket: String, key: String, uploadId: String, parts: List<CompletedPartRequest>): CompleteMultipartResponse
    fun abortMultipartUpload(bucket: String, key: String, uploadId: String): AbortMultipartResponse
    fun uploadFile(bucket: String, key: String, data: ByteArray, contentType: String): UploadResponse
    fun downloadFile(bucket: String, key: String): ByteArray
    fun deleteFile(bucket: String, key: String): DeleteResponse
    fun deleteFiles(bucket: String, keys: List<String>): BatchDeleteResponse
    fun copyFile(sourceBucket: String, sourceKey: String, destBucket: String, destKey: String): CopyResponse
    fun moveFile(sourceBucket: String, sourceKey: String, destBucket: String, destKey: String): MoveResponse
    fun listFiles(bucket: String, prefix: String, maxKeys: Int = 1000, continuationToken: String? = null): ListFilesResponse
    fun getFileMetadata(bucket: String, key: String): FileMetadataResponse
    fun fileExists(bucket: String, key: String): Boolean
    fun getPublicUrl(bucket: String, key: String): String
}