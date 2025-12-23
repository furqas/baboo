package com.furqas.metadata_service.dto

import com.furqas.metadata_service.model.VideoCategory
import com.furqas.metadata_service.model.enum.VideoLanguage
import com.furqas.metadata_service.model.enum.VideoVisibility
import java.time.LocalDateTime

data class PatchVideoByIdRequest(
    val title: String? = null,
    val description: String? = null,
    val playlistId: String? = null,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val videoCategory: VideoCategory? = null,
    val visibility: VideoVisibility? = null,
    val defaultLanguage: VideoLanguage? = null,
    val onlyForAdults: Boolean? = null,
    val tags: List<String>? = null,
    val duration: Long? = null,
    val resolutions: List<String>? = null,
    val scheduledPublishAt: LocalDateTime? = null,
)
