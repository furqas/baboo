package com.furqas.metadata_service.service.impl

import com.furqas.metadata_service.dto.CreateVideoRequest
import com.furqas.metadata_service.dto.CreateVideoResponse
import com.furqas.metadata_service.dto.GetVideoByIdResponse
import com.furqas.metadata_service.dto.PatchVideoByIdRequest
import com.furqas.metadata_service.dto.PatchVideoByIdResponse
import com.furqas.metadata_service.dto.SearchVideosResponse
import com.furqas.metadata_service.dto.VideoDto
import com.furqas.metadata_service.exception.InvalidVideoRequestException
import com.furqas.metadata_service.model.Video
import com.furqas.metadata_service.model.enum.VideoVisibility
import com.furqas.metadata_service.repository.VideoRepository
import com.furqas.metadata_service.service.VideoService
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.UUID

@RequiredArgsConstructor
@Service
class VideoServiceImpl(
    private final val videoRepository: VideoRepository,
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
            if(request.resolutions.isEmpty()) {
                throw InvalidVideoRequestException("At least one resolution must be specified.")
            }

            var video = Video(
                videoId = request.videoId,
                title = request.title,
                description = request.description,
                visibility = request.visibility,
                status = request.status,
                accountId = request.accountId,
                resolutions = request.resolutions.toMutableList(),
            )

            video = videoRepository.save(video)

            return CreateVideoResponse(
                id = video.id,
                videoId = request.videoId,
                title = request.title,
                description = request.description,
                visibility = request.visibility,
                status = request.status,
                resolutions = request.resolutions,
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
                videoKey = video.videoKey,
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
            request.videoKey?.let { video.videoKey = it }
            request.thumbnailUrl?.let { video.thumbnailUrl = it }
            request.videoCategory?.let { video.videoCategory = it }
            request.visibility?.let { video.visibility = it }
            request.defaultLanguage?.let { video.defaultLanguage = it }
            request.onlyForAdults?.let { video.onlyForAdults = it }
            request.tags?.let { video.updateTags(it) }
            request.scheduledPublishAt?.let { video.scheduledPublishAt = it }
            request.status?.let { video.status = it }

            return PatchVideoByIdResponse(
                id = video.id,
                videoId = video.videoId,
                title = video.title,
                description = video.description,
                visibility = video.visibility,
                accountId = video.accountId?: "",
                onlyForAdults = video.onlyForAdults,
                videoKey = video.videoKey,
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

        override fun searchByAccountId(
            accountId: String,
            start: Int,
            end: Int
        ): SearchVideosResponse {
            if (accountId.isBlank()) {
                throw InvalidVideoRequestException("Account ID must not be empty")
            }

            val videos = videoRepository.findByAccountIdAndVisibility(
                accountId,
                VideoVisibility.PUBLIC,
                PageRequest.of(start, end)
            )

            return SearchVideosResponse(
                videos = videos.toList().map{ toDto(it) }
            )
        }

        override fun searchByCategory(
            categoryName: String,
            start: Int,
            end: Int
        ): SearchVideosResponse {
            if (categoryName.isBlank()) {
                throw InvalidVideoRequestException("Category must not be empty")
            }

            val videos = videoRepository.findByVideoCategory_NameContainsIgnoreCase(
                categoryName,
                PageRequest.of(start, end)
            )

            return SearchVideosResponse(
                videos = videos.toList().map{ toDto(it) }
            )
        }

        override fun searchAll(
            start: Int,
            end: Int
        ): SearchVideosResponse {
            return SearchVideosResponse(
                videoRepository.findAllByOrderByPublishedAtDesc(
                    PageRequest.of(start, end)
                )
                    .toList()
                    .map { toDto(it) }
            )
        }

        private fun toDto(
            video: Video
        ): VideoDto {
            return VideoDto(
                id = video.id,
                videoId = video.videoId,
                title = video.title,
                description = video.description,
                visibility = video.visibility,
                accountId = video.accountId?: "",
                onlyForAdults = video.onlyForAdults,
                videoKey = video.videoKey,
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

    }