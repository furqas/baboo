package com.furqas.video_service.dto

import com.furqas.video_service.model.enum.ProcessingStatus
import com.furqas.video_service.model.enum.VideoVisibility
import java.util.UUID

data class CreateVideoRequest(
    val videoId: UUID,
    val title: String,
    val description: String,
    val visibility: VideoVisibility,
    val status: ProcessingStatus
)
