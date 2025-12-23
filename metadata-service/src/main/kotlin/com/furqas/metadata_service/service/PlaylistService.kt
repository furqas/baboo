package com.furqas.metadata_service.service

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
import java.util.UUID

interface PlaylistService {

    fun create(createPlaylistRequest: CreatePlaylistRequest): CreatePlaylistResponse
    fun reorder(playlistId: UUID, request: ReorderPlaylistRequest): ReorderPlaylistResponse
    fun delete(playlistId: UUID)
    fun addVideo(playlistId: UUID, request: AddVideoToPlaylistRequest): AddVideoToPlaylistResponse
    fun getById(playlistId: UUID): GetPlaylistByIdResponse
    fun patchPlaylist(playlistId: UUID, request: PatchPlaylistRequest): PatchPlaylistResponse
    fun removeVideo(playlistId: UUID, videoId: UUID): RemoveVideoFromPlaylistResponse

}