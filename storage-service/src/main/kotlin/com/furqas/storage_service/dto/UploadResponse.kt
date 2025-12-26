package com.furqas.storage_service.dto

data class UploadResponse(
    val bucket: String,
    val key: String,
    val url: String?,
    val size: Long?,
    val contentType: String?
)

