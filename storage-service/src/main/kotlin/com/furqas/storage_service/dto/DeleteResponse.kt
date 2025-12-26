package com.furqas.storage_service.dto

data class DeleteResponse(
    val deleted: Boolean,
    val bucket: String,
    val key: String
)

