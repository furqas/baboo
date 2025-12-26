package com.furqas.storage_service.dto

data class MoveResponse(
    val moved: Boolean,
    val destBucket: String,
    val destKey: String
)

