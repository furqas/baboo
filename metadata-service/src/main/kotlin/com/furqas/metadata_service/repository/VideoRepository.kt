package com.furqas.metadata_service.repository

import com.furqas.metadata_service.model.Video
import com.furqas.metadata_service.model.enum.VideoVisibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VideoRepository: JpaRepository<Video, UUID> {

    fun findByAccountIdAndVisibility(accountId: String, visibility: VideoVisibility,pageable: Pageable): Page<Video>
    fun findByVideoCategory_NameContainsIgnoreCase(categoryName: String, pageable: Pageable): Page<Video>
    fun findAllByOrderByPublishedAtDesc(pageable: Pageable): Page<Video>
}