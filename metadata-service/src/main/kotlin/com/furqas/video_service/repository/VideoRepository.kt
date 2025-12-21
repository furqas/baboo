package com.furqas.video_service.repository

import com.furqas.video_service.model.Video
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VideoRepository: JpaRepository<Video, UUID> {
}