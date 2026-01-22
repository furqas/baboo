package com.furqas.upload_service.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.furqas.upload_service.model.enums.UploadStatus
import java.time.LocalDateTime
import java.util.UUID

data class UploadState @JsonCreator constructor(
    @JsonProperty("id") val id: UUID,
    @JsonProperty("videoId") val videoId: UUID,
    @JsonProperty("userId") val userId: String,
    @JsonProperty("fileName") val fileName: String,
    @JsonProperty("fileSize") val fileSize: Long,
    @JsonProperty("totalChunks") val totalChunks: Int,
    @JsonProperty("uploadedChunks") val uploadedChunks: Int,
    @JsonProperty("resolutions") val resolutions: String,
    @JsonProperty("status") val status: UploadStatus,
    @JsonProperty("createdAt") val createdAt: LocalDateTime
)