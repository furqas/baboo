package com.furqas.upload_service.dto

import java.time.LocalDateTime

data class UpdateVideoRequest(
    val title: String? = null,
    val description: String? = null,
    val playlistId: String? = null,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val videoCategory: String? = null,
    val visibility: String? = null,
    val defaultLanguage: String? = null,
    val onlyForAdults: Boolean? = null,
    val tags: List<String>? = null,
    val duration: Long? = null,
    val resolutions: List<String>? = null,
    val scheduledPublishAt: LocalDateTime? = null,
    val state: String? = null,
    val status: String? = null,
)
