package com.furqas.metadata_service.controller

import com.furqas.metadata_service.dto.CreateVideoRequest
import com.furqas.metadata_service.dto.CreateVideoResponse
import com.furqas.metadata_service.dto.GetVideoByIdResponse
import com.furqas.metadata_service.dto.PatchVideoByIdRequest
import com.furqas.metadata_service.dto.PatchVideoByIdResponse
import com.furqas.metadata_service.dto.SearchVideosResponse
import com.furqas.metadata_service.service.VideoService
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RequiredArgsConstructor
@RestController
@RequestMapping($$"${api.version}/videos")
class VideoController(
    private final val videoService: VideoService
    )
{
    @PostMapping
    fun createVideo(
        @RequestBody request: CreateVideoRequest
    ): ResponseEntity<CreateVideoResponse> {
        val response = videoService.createVideo(request)
        return ResponseEntity.status(
            HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    fun getVideoById(
        @PathVariable("id") id: UUID
    ) : ResponseEntity<GetVideoByIdResponse>{
        val response = videoService.getVideoById(id)

        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}")
    fun patchVideoById(
        @PathVariable("id") id: UUID,
        @RequestBody request: PatchVideoByIdRequest
    ): ResponseEntity<PatchVideoByIdResponse> {
        val response = videoService.patchVideoById(id, request)

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteVideoById(
        @PathVariable("id") id: UUID
    ) : ResponseEntity<Void>{
        videoService.deleteVideoById(id)

        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun search(
        @RequestParam(name = "accountId", required = false) accountId: String?,
        @RequestParam(name = "category", required = false) category: String?,
        @RequestParam(name = "start", defaultValue = "0") start: Int,
        @RequestParam(name = "end", defaultValue = "10") end: Int,
    ): ResponseEntity<SearchVideosResponse>{
        val response = if(accountId != null){
            videoService.searchByAccountId(
                accountId,
                start,
                end
            )
        } else if(category != null){
            videoService.searchByCategory(
                category,
                start,
                end
            )
        } else {
            videoService.searchAll(start, end)
        }

        return if(response.videos.isEmpty()) ResponseEntity.noContent().build() else ResponseEntity.ok().body(response)
    }

}