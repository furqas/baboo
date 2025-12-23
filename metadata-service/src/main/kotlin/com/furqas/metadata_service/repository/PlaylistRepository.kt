package com.furqas.metadata_service.repository

import com.furqas.metadata_service.model.Playlist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PlaylistRepository: JpaRepository<Playlist, UUID> {
}