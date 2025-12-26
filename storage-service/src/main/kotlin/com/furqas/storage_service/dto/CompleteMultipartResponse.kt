package com.furqas.storage_service.dto

data class CompleteMultipartResponse(
    val bucket: String,
    val key: String,
    val location: String?,
    val etag: String?,
    val size: Long?
)
