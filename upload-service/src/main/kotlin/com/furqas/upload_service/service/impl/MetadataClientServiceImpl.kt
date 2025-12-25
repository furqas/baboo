package com.furqas.upload_service.service.impl

import com.furqas.upload_service.client.MetadataClient
import com.furqas.upload_service.dto.CreateVideoRequest
import com.furqas.upload_service.dto.UpdateVideoRequest
import com.furqas.upload_service.service.MetadataClientService
import org.springframework.stereotype.Component

@Component
class MetadataClientServiceImpl(
    private final val client: MetadataClient
): MetadataClientService {
    override fun createVideo(request: CreateVideoRequest) {
        TODO("Not yet implemented")
    }

    override fun updateVideo(request: UpdateVideoRequest, videoId: String) {
        TODO("Not yet implemented")
    }


}