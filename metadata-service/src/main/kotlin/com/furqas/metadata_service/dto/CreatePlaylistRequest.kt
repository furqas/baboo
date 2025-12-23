package com.furqas.metadata_service.dto

import com.furqas.metadata_service.model.enum.PlaylistVisibility
import java.util.UUID

data class CreatePlaylistRequest(
    val title: String,
    val description: String,
    val visibility: PlaylistVisibility,
    val videos: List<UUID>?,
)
