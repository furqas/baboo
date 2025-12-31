package com.furqas.upload_service.dto

data class InitiateUploadRequest(
    val fileName: String,
    val fileSize: Long,
    val contentType: String,
    val userId: String,
    val resolutions : List<String>
)
