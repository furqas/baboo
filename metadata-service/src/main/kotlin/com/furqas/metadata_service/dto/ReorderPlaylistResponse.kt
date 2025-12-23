package com.furqas.metadata_service.dto

import com.furqas.metadata_service.model.enum.PlaylistVisibility
import java.util.UUID

data class ReorderPlaylistResponse(
    val id: UUID,
    val name: String,
    val description: String,
    val thumbnailUrl: String,
    val visibility: PlaylistVisibility,
    val videos: List<UUID>,
)
