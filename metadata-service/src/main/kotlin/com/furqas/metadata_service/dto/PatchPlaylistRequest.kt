package com.furqas.metadata_service.dto

import com.furqas.metadata_service.model.enum.PlaylistVisibility
import java.util.UUID

data class PatchPlaylistRequest(
    val name: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val visibility: PlaylistVisibility? = null,
    val videos: List<UUID>? = null,
)
