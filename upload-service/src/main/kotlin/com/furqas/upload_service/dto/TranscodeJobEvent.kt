package com.furqas.upload_service.dto

data class TranscodeJobEvent(
    val videoId: String,
    val s3Key: String,
    val userId: String,
    val fileName: String,
    val resolutions: List<String>
)
