package com.furqas.video_service.service.impl

import com.furqas.video_service.dto.CreateVideoRequest
import com.furqas.video_service.dto.CreateVideoResponse
import com.furqas.video_service.dto.GetVideoByIdResponse
import com.furqas.video_service.dto.PatchVideoByIdRequest
import com.furqas.video_service.dto.PatchVideoByIdResponse
import com.furqas.video_service.exception.InvalidVideoRequestException
import com.furqas.video_service.model.Video
import com.furqas.video_service.repository.VideoRepository
import com.furqas.video_service.service.VideoService
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import java.util.UUID

@RequiredArgsConstructor
@Service
class VideoServiceImpl(
    private final val videoRepository: VideoRepository
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

            val video = Video(
                videoId = request.videoId,
                title = request.title,
                description = request.description,
                visibility = request.visibility,
                status = request.status,
            )

            videoRepository.save(video)

            return CreateVideoResponse(
                id = java.util.UUID.randomUUID(),
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
                playlistId = video.playlistId?: "",
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

        override fun patchVideoById(
            id: UUID,
            request: PatchVideoByIdRequest
        ): PatchVideoByIdResponse {
            TODO("Not yet implemented")
        }

        override fun deleteVideoById(id: UUID) {
            TODO("Not yet implemented")
        }

    }