package com.furqas.metadata_service.service.impl

import com.furqas.metadata_service.dto.CreateVideoRequest
import com.furqas.metadata_service.dto.CreateVideoResponse
import com.furqas.metadata_service.dto.GetVideoByIdResponse
import com.furqas.metadata_service.dto.PatchVideoByIdRequest
import com.furqas.metadata_service.dto.PatchVideoByIdResponse
import com.furqas.metadata_service.exception.InvalidVideoRequestException
import com.furqas.metadata_service.model.Video
import com.furqas.metadata_service.repository.VideoRepository
import com.furqas.metadata_service.service.VideoService
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import org.springframework.boot.data.metrics.DefaultRepositoryTagsProvider
import org.springframework.stereotype.Service
import java.util.UUID

@RequiredArgsConstructor
@Service
class VideoServiceImpl(
    private final val videoRepository: VideoRepository,
    private val repositoryTagsProvider: DefaultRepositoryTagsProvider
): VideoService
    {
        override fun createVideo(request: CreateVideoRequest): CreateVideoResponse {
            if (request.title.isBlank()) {
                throw InvalidVideoRequestException("Title must not be empty")
            }
            if (request.visibility == null) {
                throw InvalidVideoRequestException("Video visibility is required")
            }
            if (request.status == null) {
                throw InvalidVideoRequestException("Video status is required.")
            }

            var video = Video(
                videoId = request.videoId,
                title = request.title,
                description = request.description,
                visibility = request.visibility,
                status = request.status,
            )

            video = videoRepository.save(video)

            return CreateVideoResponse(
                id = video.id,
                videoId = request.videoId,
                title = request.title,
                description = request.description,
                visibility = request.visibility,
                status = request.status
            )
        }

        override fun getVideoById(id: UUID): GetVideoByIdResponse {
            if (id == null) {
                throw InvalidVideoRequestException("Video ID must not be null")
            }

            val video = videoRepository.findById(id)
                .orElseThrow() { InvalidVideoRequestException("Video with ID $id not found") }

            return GetVideoByIdResponse(
                id = video.id,
                videoId = video.videoId,
                title = video.title,
                description = video.description,
                visibility = video.visibility,
                accountId = video.accountId?: "",
                onlyForAdults = video.onlyForAdults,
                videoUrl = video.videoUrl,
                thumbnailUrl = video.thumbnailUrl,
                videoCategory = video.videoCategory,
                defaultLanguage = video.defaultLanguage,
                publishedLocale = video.publishedLocale,
                tags = video.tags,
                duration = video.duration?: 0,
                resolutions = video.resolutions,
                publishedAt = video.publishedAt,
                scheduledPublishAt = video.scheduledPublishAt,
                createdAt = video.createdAt,
                updatedAt = video.updatedAt,
                status = video.status,
            )
        }

        @Transactional
        override fun patchVideoById(
            id: UUID,
            request: PatchVideoByIdRequest
        ): PatchVideoByIdResponse {
            if(id == null) {
                throw InvalidVideoRequestException("Video ID must not be null")
            }

            val video = videoRepository.findById(id)
                .orElseThrow{ InvalidVideoRequestException("Video with ID $id not found") }


            request.title?.let { video.title = it }
            request.description?.let { video.description = it }
            request.duration?.let { video.duration = it }
            request.resolutions?.let { video.updateResolutions(it) }
            request.videoUrl?.let { video.videoUrl = it }
            request.thumbnailUrl?.let { video.thumbnailUrl = it }
            request.videoCategory?.let { video.videoCategory = it }
            request.visibility?.let { video.visibility = it }
            request.defaultLanguage?.let { video.defaultLanguage = it }
            request.onlyForAdults?.let { video.onlyForAdults = it }
            request.tags?.let { video.updateTags(it) }
            request.scheduledPublishAt?.let { video.scheduledPublishAt = it }

            return PatchVideoByIdResponse(
                id = video.id,
                videoId = video.videoId,
                title = video.title,
                description = video.description,
                visibility = video.visibility,
                accountId = video.accountId?: "",
                onlyForAdults = video.onlyForAdults,
                videoUrl = video.videoUrl,
                thumbnailUrl = video.thumbnailUrl,
                videoCategory = video.videoCategory,
                defaultLanguage = video.defaultLanguage,
                publishedLocale = video.publishedLocale,
                tags = video.tags,
                duration = video.duration?: 0,
                resolutions = video.resolutions,
                publishedAt = video.publishedAt,
                scheduledPublishAt = video.scheduledPublishAt,
                createdAt = video.createdAt,
                updatedAt = video.updatedAt,
                status = video.status,
            )
        }

        @Transactional
        override fun deleteVideoById(id: UUID) {
            if(id == null) {
                throw InvalidVideoRequestException("Video ID must not be null")
            }

            val video = videoRepository.findById(id)
                .orElseThrow{ InvalidVideoRequestException("Video with ID $id not found") }


            videoRepository.delete(video)
        }

    }