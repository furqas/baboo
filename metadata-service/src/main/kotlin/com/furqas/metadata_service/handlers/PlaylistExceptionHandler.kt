package com.furqas.metadata_service.handlers

import com.furqas.metadata_service.exception.InvalidPlaylistRequestException
import com.furqas.metadata_service.exception.PlaylistNotFoundException
import com.furqas.metadata_service.service.PlaylistService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackageClasses = [PlaylistService::class])
class PlaylistExceptionHandler : GenericExceptionHandler() {

    @ExceptionHandler(
        InvalidPlaylistRequestException::class)
    fun handleInvalidPlaylistRequestException(
        ex: InvalidPlaylistRequestException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionHandlerResponse> {
        return buildErrorResponse(
            status = org.springframework.http.HttpStatus.BAD_REQUEST,
            message = ex.message,
            path =  request.requestURI
        )
    }

    @ExceptionHandler(
        PlaylistNotFoundException::class)
    fun handlePlaylistNotFoundException(
        ex: PlaylistNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ExceptionHandlerResponse> {
        return buildErrorResponse(
            status = org.springframework.http.HttpStatus.NOT_FOUND,
            message = ex.message,
            path =  request.requestURI
        )
    }

}

