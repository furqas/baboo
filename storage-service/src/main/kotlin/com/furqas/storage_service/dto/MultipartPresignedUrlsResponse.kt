package com.furqas.storage_service.dto

import java.time.Instant


data class MultipartPresignedUrlsResponse(
    val uploadId: String,
    val urls: List<PresignedPartUrl>,
    val expiresAt: Instant
    ,
)
