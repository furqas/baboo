package com.furqas.upload_service.client

import com.furqas.upload_service.dto.CreateVideoRequest
import com.furqas.upload_service.dto.UpdateVideoRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@FeignClient(name = "metadata-service", url = $$"${service.metadata-url}")
interface MetadataClient {

    @PostMapping("/videos")
    fun createVideo(
        @RequestBody request: CreateVideoRequest
    ): ResponseEntity<Any>

    @PatchMapping("/videos/{id}")
    fun updateVideo(
        @PathVariable id: UUID,
        @RequestBody request: UpdateVideoRequest
    ): ResponseEntity<Any>

}