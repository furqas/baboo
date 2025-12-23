package com.furqas.metadata_service.model

import com.furqas.metadata_service.model.enum.ProcessingStatus
import com.furqas.metadata_service.model.enum.VideoLanguage
import com.furqas.metadata_service.model.enum.VideoVisibility
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

    var title: String,

    var description: String? = null,

    val accountId: String? = null,

    var videoUrl: String? = null,
    var thumbnailUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    var videoCategory: VideoCategory? = null,

    @Enumerated(EnumType.STRING)
    var visibility: VideoVisibility,

    @Enumerated(EnumType.STRING)
    var status: ProcessingStatus,

    @Enumerated(EnumType.STRING)
    var defaultLanguage: VideoLanguage? = null,

    var publishedLocale: String? = null,

    var onlyForAdults: Boolean = false,

    @ElementCollection
    var tags: MutableList<String> = mutableListOf(),

    var duration: Long? = null,

    @ElementCollection
    var resolutions: MutableList<String> = mutableListOf(),

    val publishedAt: LocalDateTime? = null,

    var scheduledPublishAt: LocalDateTime? = null,

    @CreationTimestamp
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    val updatedAt: LocalDateTime? = null
) {
    fun addTag(tag: String) {
        tags.add(tag)
    }

    fun removeTag(tag: String) {
        tags.remove(tag)
    }

    fun updateResolutions(newResolutions: List<String>) {
        resolutions.clear()
        resolutions.addAll(newResolutions)
    }

    fun updateTags(newTags: List<String>) {
        tags.clear()
        tags.addAll(newTags)
    }
}

