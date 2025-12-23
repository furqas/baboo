package com.furqas.metadata_service.controller

import com.furqas.metadata_service.dto.AddVideoToPlaylistRequest
import com.furqas.metadata_service.dto.AddVideoToPlaylistResponse
import com.furqas.metadata_service.dto.CreatePlaylistRequest
import com.furqas.metadata_service.dto.CreatePlaylistResponse
import com.furqas.metadata_service.dto.GetPlaylistByIdResponse
import com.furqas.metadata_service.dto.PatchPlaylistRequest
import com.furqas.metadata_service.dto.PatchPlaylistResponse
import com.furqas.metadata_service.dto.ReorderPlaylistRequest
import com.furqas.metadata_service.dto.ReorderPlaylistResponse
import com.furqas.metadata_service.service.PlaylistService
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RequiredArgsConstructor
@RestController
@RequestMapping($$"${api.version}/playlists")
class PlaylistController(
    private final val service: PlaylistService
) {

    @PostMapping
    fun createPlaylist(
        @RequestBody request: CreatePlaylistRequest
    ): ResponseEntity<CreatePlaylistResponse> {
        val response = service.create(request)
        return ResponseEntity.status(
            201).body(response)
    }

    @GetMapping("/{id}")
    fun getPlaylistById(
        @PathVariable id: UUID
    ): ResponseEntity<GetPlaylistByIdResponse> {
        val response = service.getById(id)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}")
    fun patchPlaylist(
        @PathVariable id: UUID,
        @RequestBody request: PatchPlaylistRequest
    ): ResponseEntity<PatchPlaylistResponse> {
        val response = service.patchPlaylist(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deletePlaylistById(
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/videos")
    fun addVideoToPlaylist(
        @PathVariable id: UUID,
        @RequestBody request: AddVideoToPlaylistRequest
    ): ResponseEntity<AddVideoToPlaylistResponse> {
        val response = service.addVideo(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}/videos/{videoId}")
    fun removeVideoFromPlaylist(
        @PathVariable id: UUID,
        @PathVariable videoId: UUID
    ): ResponseEntity<Void> {
        service.removeVideo(id, videoId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}/reorder")
    fun reorderPlaylist(
        @PathVariable id: UUID,
        @RequestBody request: ReorderPlaylistRequest
    ): ResponseEntity<ReorderPlaylistResponse> {
        val response = service.reorder(id, request)

        return ResponseEntity.ok(response)
    }
}