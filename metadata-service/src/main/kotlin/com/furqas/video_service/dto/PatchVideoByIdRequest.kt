package com.furqas.video_service.dto

import com.furqas.video_service.model.VideoCategory
import com.furqas.video_service.model.enum.VideoLanguage
import com.furqas.video_service.model.enum.VideoVisibility
import java.time.LocalDateTime

data class PatchVideoByIdRequest(
    val title: String,
    val description: String?,
    val accountId: String,
    val playlistId: String,
    val videoUrl: String?,
    val thumbnailUrl: String?,
    val videoCategory: VideoCategory,
    val visibility: VideoVisibility,
    val defaultLanguage: VideoLanguage,
    val onlyForAdults: Boolean,
    val tags: List<String>,
    val duration: Long,
    val resolutions: List<String>,
    val scheduledPublishAt: LocalDateTime?,
)
