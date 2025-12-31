package com.furqas.upload_service.dto

data class CancelUploadResponse(
    val id: String,
    val uploadedChunks: Int,
    val totalChunks: Int
)
