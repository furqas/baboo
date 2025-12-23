package com.furqas.metadata_service.dto

import java.util.UUID

data class AddVideoToPlaylistRequest(
    val videos: List<VideoToAdd>,
)

data class VideoToAdd (
    val videoId: UUID,
    val position: Int? = null
)
