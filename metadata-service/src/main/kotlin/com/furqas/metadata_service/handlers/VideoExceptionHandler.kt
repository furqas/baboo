package com.furqas.metadata_service.handlers

import com.furqas.metadata_service.exception.InvalidVideoRequestException
import com.furqas.metadata_service.exception.VideoNotFoundException
import com.furqas.metadata_service.service.VideoService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackageClasses = [VideoService::class])
class VideoExceptionHandler : GenericExceptionHandler() {

    @ExceptionHandler(
        InvalidVideoRequestException::class)
    fun handleInvalidVideoRequestException(
        ex: InvalidVideoRequestException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionHandlerResponse> {
        return buildErrorResponse(
            status = org.springframework.http.HttpStatus.BAD_REQUEST,
            message = ex.message,
            path =  request.requestURI
        )
    }

    @ExceptionHandler(
        VideoNotFoundException::class
    )
    fun handleVideoNotFoundException(
        ex: VideoNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionHandlerResponse> {
        return buildErrorResponse(
            status = org.springframework.http.HttpStatus.NOT_FOUND,
            message = ex.message,
            path = request.requestURI
        )
    }

}