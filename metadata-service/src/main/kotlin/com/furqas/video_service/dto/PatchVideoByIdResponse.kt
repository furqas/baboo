package com.furqas.video_service.dto

import com.furqas.video_service.model.VideoCategory
import com.furqas.video_service.model.enum.VideoLanguage
import com.furqas.video_service.model.enum.VideoVisibility
import java.time.LocalDateTime
import java.util.UUID

data class PatchVideoByIdResponse(
    val id: UUID,
    val videoId: UUID,
    val title: String,
    val description: String?,
    val accountId: String,
    val playlistId: String,
    val videoUrl: String?,
    val thumbnailUrl: String?,
    val videoCategory: VideoCategory,
    val visibility: VideoVisibility,
    val defaultLanguage: VideoLanguage,
    val publishedLocale: String?,
    val onlyForAdults: Boolean,
    val tags: List<String>,
    val duration: Long,
    val resolutions: List<String>,
    val publishedAt: LocalDateTime?,
    val scheduledPublishAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
