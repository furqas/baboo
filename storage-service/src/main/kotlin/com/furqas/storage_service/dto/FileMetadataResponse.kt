package com.furqas.storage_service.dto

import java.time.Instant

data class FileMetadataResponse(
    val bucket: String,
    val key: String,
    val size: Long?,
    val contentType: String?,
    val etag: String?,
    val lastModified: Instant?,
    val metadata: Map<String, String>?
)

