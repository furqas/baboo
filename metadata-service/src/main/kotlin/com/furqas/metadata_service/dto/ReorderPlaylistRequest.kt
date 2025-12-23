package com.furqas.metadata_service.dto

import java.util.UUID

data class ReorderPlaylistRequest(
    val videosIds: List<UUID>,
)
