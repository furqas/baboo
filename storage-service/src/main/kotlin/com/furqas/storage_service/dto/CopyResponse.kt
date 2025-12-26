package com.furqas.storage_service.dto

data class CopyResponse(
    val copied: Boolean,
    val destBucket: String,
    val destKey: String
)

