package com.furqas.storage_service.dto

data class BatchDeleteResponse(
    val deleted: List<String>,
    val failed: List<String>
)

