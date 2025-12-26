package com.furqas.storage_service.handlers

import java.time.LocalDateTime

data class ExceptionHandlerResponse(
    val status: Int,
    val error: String,
    val message: String?,
    val path: String,
    val timestamp: LocalDateTime,
)
