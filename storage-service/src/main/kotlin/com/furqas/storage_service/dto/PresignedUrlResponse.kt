package com.furqas.storage_service.dto

import java.time.Instant

data class PresignedUrlResponse(
    val url: String,
    val expiresAt: Instant,
    val method: String,
    val requiredHeaders: Map< String, String>?
)
