package com.furqas.upload_service.client

import com.furqas.upload_service.dto.CreateVideoRequest
import com.furqas.upload_service.dto.UpdateVideoRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import java.util.UUID

@FeignClient(name = "metadata-service", url = $$"${service.metadata-url}")
interface MetadataClient {

    @RequestMapping(method = [RequestMethod.POST], value = [$$"${metadata.service.version}/videos"])
    fun createVideo(
        @RequestBody request: CreateVideoRequest
    ): ResponseEntity<Any>

    @RequestMapping(method = [RequestMethod.PATCH], value = [$$"${metadata.service.version}/videos/{id}"])
    fun updateVideo(
        @PathVariable id: UUID,
        @RequestBody request: UpdateVideoRequest
    ): ResponseEntity<Any>

}