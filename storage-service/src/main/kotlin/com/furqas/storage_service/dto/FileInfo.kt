package com.furqas.storage_service.dto

import java.time.Instant

data class FileInfo(
    val key: String,
    val size: Long,
    val lastModified: Instant?,
    val etag: String?
)

