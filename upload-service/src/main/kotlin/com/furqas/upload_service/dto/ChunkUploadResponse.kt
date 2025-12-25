package com.furqas.upload_service.dto

import java.util.UUID

data class ChunkUploadResponse(
    val uploadId: String,
    val chunkNumber: Int,
    val uploaded: Boolean,
    val progress: Double,
)
