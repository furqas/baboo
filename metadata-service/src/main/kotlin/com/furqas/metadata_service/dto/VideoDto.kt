package com.furqas.metadata_service.dto

import com.furqas.metadata_service.model.VideoCategory
import com.furqas.metadata_service.model.enum.ProcessingStatus
import com.furqas.metadata_service.model.enum.VideoLanguage
import com.furqas.metadata_service.model.enum.VideoVisibility

import java.time.LocalDateTime
import java.util.UUID

data class VideoDto(
    val id: UUID = UUID.randomUUID(),
    val videoId: UUID,
    var title: String,
    var description: String? = null,
    val accountId: String? = null,
    var videoKey: String? = null,
    var thumbnailUrl: String? = null,
    var videoCategory: VideoCategory? = null,
    var visibility: VideoVisibility,
    var status: ProcessingStatus,
    var defaultLanguage: VideoLanguage? = null,
    var publishedLocale: String? = null,
    var onlyForAdults: Boolean = false,
    var tags: MutableList<String> = mutableListOf(),
    var duration: Long? = null,
    var resolutions: MutableList<String> = mutableListOf(),
    val publishedAt: LocalDateTime? = null,
    var scheduledPublishAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
