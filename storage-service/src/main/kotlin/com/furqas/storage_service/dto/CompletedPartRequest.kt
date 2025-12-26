package com.furqas.storage_service.dto

data class CompletedPartRequest(
    val partNumber: Int,
    val etag: String
)

