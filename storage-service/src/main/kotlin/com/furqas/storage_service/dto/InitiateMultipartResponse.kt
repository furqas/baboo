package com.furqas.storage_service.dto

data class InitiateMultipartResponse(
    val uploadId : String,
    val bucket : String,
    val key : String
)
