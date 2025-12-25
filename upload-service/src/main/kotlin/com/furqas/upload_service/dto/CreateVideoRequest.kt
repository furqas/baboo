package com.furqas.upload_service.dto

import java.util.UUID

data class CreateVideoRequest(
    val videoId: UUID,
    val title: String,
    val description: String,
    val visibility: String,
    val status: String,
    val accountId: String,
)
