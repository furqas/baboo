package com.furqas.storage_service.handlers

import com.furqas.storage_service.exception.FileTooLargeException
import com.furqas.storage_service.exception.InvalidBucketRequestException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class StorageExceptionHandler: GenericExceptionHandler() {

    @ExceptionHandler(
        FileTooLargeException::class)
    fun handleFileTooLargeException(
        ex: FileTooLargeException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionHandlerResponse> {
        return buildErrorResponse(
            status = org.springframework.http.HttpStatus.BAD_REQUEST,
            message = ex.message,
            path =  request.requestURI
        )
    }

    @ExceptionHandler(
        InvalidBucketRequestException::class)
    fun handleInvalidBucketException(
        ex: InvalidBucketRequestException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionHandlerResponse> {
        return buildErrorResponse(
            status = org.springframework.http.HttpStatus.BAD_REQUEST,
            message = ex.message,
            path =  request.requestURI
        )
    }

}