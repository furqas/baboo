package com.furqas.video_service.model

import com.furqas.video_service.model.enum.ProcessingStatus
import com.furqas.video_service.model.enum.VideoLanguage
import com.furqas.video_service.model.enum.VideoVisibility
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "videos")
class Video(

    @Id
    val id: UUID = UUID.randomUUID(),

    val videoId: UUID,

    val title: String,

    val description: String? = null,

    val accountId: String? = null,
    val playlistId: String? = null,

    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val videoCategory: VideoCategory? = null,

    @Enumerated(EnumType.STRING)
    val visibility: VideoVisibility,

    @Enumerated(EnumType.STRING)
    val status: ProcessingStatus,

    @Enumerated(EnumType.STRING)
    val defaultLanguage: VideoLanguage? = null,

    val publishedLocale: String? = null,

    val onlyForAdults: Boolean = false,

    @ElementCollection
    val tags: List<String> = emptyList(),

    val duration: Long? = null,

    @ElementCollection
    val resolutions: List<String> = emptyList(),

    val publishedAt: LocalDateTime? = null,

    val scheduledPublishAt: LocalDateTime? = null,

    @CreationTimestamp
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    val updatedAt: LocalDateTime? = null
)

