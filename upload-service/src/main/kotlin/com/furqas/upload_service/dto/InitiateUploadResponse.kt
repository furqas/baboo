package com.furqas.upload_service.dto

import java.util.UUID

data class InitiateUploadResponse(
    val uploadId: UUID,
    val videoId: UUID,
    val chunkSize: Long,
    val totalChunks: Int
)
