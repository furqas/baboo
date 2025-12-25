package com.furqas.upload_service.model

import com.furqas.upload_service.model.enums.UploadStatus
import java.time.LocalDateTime
import java.util.UUID

class UploadState(
    val id: UUID,
    val videoId: UUID,
    val userId: String,
    val fileName: String,
    val fileSize: Long,
    val totalChunks: Int,
    val uploadedChunks: Int,
    val status: UploadStatus,
    val createdAt: LocalDateTime
) {

}