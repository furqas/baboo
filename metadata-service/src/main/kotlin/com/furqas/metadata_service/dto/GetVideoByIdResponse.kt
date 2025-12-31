package com.furqas.metadata_service.dto

import com.furqas.metadata_service.model.VideoCategory
import com.furqas.metadata_service.model.enum.ProcessingStatus
import com.furqas.metadata_service.model.enum.VideoLanguage
import com.furqas.metadata_service.model.enum.VideoVisibility
import java.time.LocalDateTime
import java.util.UUID

data class GetVideoByIdResponse(
    val id: UUID,
    val videoId: UUID,
    val title: String,
    val description: String?,
    val accountId: String,
    val videoKey: String?,
    val thumbnailUrl: String?,
    val videoCategory: VideoCategory?,
    val status: ProcessingStatus,
    val visibility: VideoVisibility,
    val defaultLanguage: VideoLanguage?,
    val publishedLocale: String?,
    val onlyForAdults: Boolean,
    val tags: List<String>,
    val duration: Long,
    val resolutions: List<String>,
    val publishedAt: LocalDateTime?,
    val scheduledPublishAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
