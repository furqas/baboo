package com.furqas.storage_service.handlers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

abstract class GenericExceptionHandler {

    protected fun buildErrorResponse(
        status: HttpStatus,
        message: String?,
        path: String
    ): ResponseEntity<ExceptionHandlerResponse> {
        val error = ExceptionHandlerResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = message,
            path = path,
            timestamp = LocalDateTime.now()
        )

        return ResponseEntity(error, status)
    }

}