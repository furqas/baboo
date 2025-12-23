package com.furqas.metadata_service.service.impl

import com.furqas.metadata_service.dto.AddVideoToPlaylistRequest
import com.furqas.metadata_service.dto.AddVideoToPlaylistResponse
import com.furqas.metadata_service.dto.CreatePlaylistRequest
import com.furqas.metadata_service.dto.CreatePlaylistResponse
import com.furqas.metadata_service.dto.GetPlaylistByIdResponse
import com.furqas.metadata_service.dto.PatchPlaylistRequest
import com.furqas.metadata_service.dto.PatchPlaylistResponse
import com.furqas.metadata_service.dto.RemoveVideoFromPlaylistResponse
import com.furqas.metadata_service.dto.ReorderPlaylistRequest
import com.furqas.metadata_service.dto.ReorderPlaylistResponse
import com.furqas.metadata_service.exception.InvalidPlaylistRequestException
import com.furqas.metadata_service.exception.PlaylistNotFoundException
import com.furqas.metadata_service.model.Playlist
import com.furqas.metadata_service.repository.PlaylistRepository
import com.furqas.metadata_service.service.PlaylistService
import com.furqas.metadata_service.service.VideoService
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import java.util.UUID

@RequiredArgsConstructor
@Service
class PlaylistServiceImpl(
    private final val playlistRepository: PlaylistRepository,
    private final val videoService: VideoService,
): PlaylistService {

    override fun create(createPlaylistRequest: CreatePlaylistRequest): CreatePlaylistResponse {
        if (createPlaylistRequest.title.isBlank()) {
            throw InvalidPlaylistRequestException("Title must not be empty")
        }

        // If the playlist it's been created with videos, the thumbnail will be the first video of the playlist
        val thumbnailUrl = if (!createPlaylistRequest.videos.isNullOrEmpty()) {
            val firstVideoId = createPlaylistRequest.videos[0]
            val video = videoService.getVideoById(firstVideoId)
            video.thumbnailUrl?: ""
        } else {
            ""
        }

        val playlist = Playlist(
            id = UUID.randomUUID(),
            name = createPlaylistRequest.title,
            description = createPlaylistRequest.description,
            thumbnailUrl = thumbnailUrl,
            visibility = createPlaylistRequest.visibility,
            videos = createPlaylistRequest.videos?.toMutableList() ?: mutableListOf()
        )

        val saved = playlistRepository.save(playlist)

        return CreatePlaylistResponse(
            id = saved.id,
            name = saved.name,
            description = saved.description,
            thumbnailUrl = saved.thumbnailUrl,
            visibility = saved.visibility,
            videos = saved.videos
        )
    }

    override fun reorder(
        playlistId: UUID,
        request: ReorderPlaylistRequest
    ): ReorderPlaylistResponse {
        if (playlistId == null) {
            throw InvalidPlaylistRequestException("Playlist ID must not be null")
        }

        val playlist = playlistRepository.findById(playlistId)
            .orElseThrow { PlaylistNotFoundException("Playlist wth id: $playlistId not found") }

        if (request.videosIds.isEmpty()) {
            throw InvalidPlaylistRequestException("videosIds must not be null")
        }

        val newOrder = request.videosIds

        val size = minOf(playlist.videos.size, newOrder.size)
        for (i in 0 until size) {
            if (playlist.videos[i] != newOrder[i]) {
                playlist.videos[i] = newOrder[i]
            }
        }

        if (newOrder.size > playlist.videos.size) {
            playlist.videos.addAll(newOrder.subList(playlist.videos.size, newOrder.size))
        } else if (newOrder.size < playlist.videos.size) {
            for (i in playlist.videos.size - 1 downTo newOrder.size) {
                playlist.videos.removeAt(i)
            }
        }

        playlistRepository.save(playlist)

        return ReorderPlaylistResponse(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            thumbnailUrl = playlist.thumbnailUrl,
            visibility = playlist.visibility,
            videos = playlist.videos
        )
    }

    override fun delete(playlistId: UUID) {
        if (playlistId == null) {
            throw InvalidPlaylistRequestException("Playlist ID must not be null")
        }

        val playlist = playlistRepository.findById(playlistId)
            .orElseThrow { PlaylistNotFoundException("Playlist wth id: $playlistId not found") }


        playlistRepository.delete(playlist)
    }

    override fun addVideo(
        playlistId: UUID,
        request: AddVideoToPlaylistRequest
    ): AddVideoToPlaylistResponse {
        if (playlistId == null) {
            throw InvalidPlaylistRequestException("Playlist ID must not be null")
        }

        val playlist = playlistRepository.findById(playlistId)
            .orElseThrow { PlaylistNotFoundException("Playlist wth id: $playlistId not found") }

        val size = if(playlist.videos.size > request.videos.size) playlist.videos.size else request.videos.size

        for (i in 0 until size) {
            val videoToAdd = request.videos.getOrNull(i)
            if (videoToAdd != null) {
                val position = videoToAdd.position ?: playlist.videos.size
                if (position >= 0 && position <= playlist.videos.size) {
                    playlist.videos.add(position, videoToAdd.videoId)
                } else {
                    throw InvalidPlaylistRequestException("Invalid position ${videoToAdd.position} for video ${videoToAdd.videoId}")
                }
            }
        }

        playlistRepository.save(playlist)


        return AddVideoToPlaylistResponse(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            thumbnailUrl = playlist.thumbnailUrl,
            visibility = playlist.visibility,
            videos = playlist.videos
        )
    }


    override fun getById(playlistId: UUID): GetPlaylistByIdResponse {
        if (playlistId == null) {
            throw InvalidPlaylistRequestException("Playlist ID must not be null")
        }

        val playlist = playlistRepository.findById(playlistId)
            .orElseThrow { PlaylistNotFoundException("Playlist wth id: $playlistId not found") }


        return GetPlaylistByIdResponse(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            thumbnailUrl = playlist.thumbnailUrl,
            visibility = playlist.visibility,
            videos = playlist.videos
        )
    }

    override fun patchPlaylist(
        playlistId: UUID,
        request: PatchPlaylistRequest
    ): PatchPlaylistResponse {
        if (playlistId == null) {
            throw InvalidPlaylistRequestException("Playlist ID must not be null")
        }

        val playlist = playlistRepository.findById(playlistId)
            .orElseThrow { PlaylistNotFoundException("Playlist wth id: $playlistId not found") }

        request.videos?.let { playlist.videos = it.toMutableList() }
        request.name?.let { playlist.name = it }
        request.description?.let { playlist.description = it }
        request.visibility?.let { playlist.visibility = it }
        request.thumbnailUrl?.let { playlist.thumbnailUrl = it }

        playlistRepository.save(playlist)

        return PatchPlaylistResponse(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            thumbnailUrl = playlist.thumbnailUrl,
            visibility = playlist.visibility,
            videos = playlist.videos
        )
    }

    override fun removeVideo(
        playlistId: UUID,
        videoId: UUID
    ): RemoveVideoFromPlaylistResponse {
        if (playlistId == null) {
            throw InvalidPlaylistRequestException("Playlist ID must not be null")
        }

        val playlist = playlistRepository.findById(playlistId)
            .orElseThrow { PlaylistNotFoundException("Playlist wth id: $playlistId not found") }

        val removed = playlist.videos.remove(videoId)

        if(!removed) {
            throw InvalidPlaylistRequestException("Video with id: $videoId not found in playlist with id: $playlistId")
        }

        playlistRepository.save(playlist)

        return RemoveVideoFromPlaylistResponse(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            thumbnailUrl = playlist.thumbnailUrl,
            visibility = playlist.visibility,
            videos = playlist.videos
        )
    }
}