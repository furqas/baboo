package com.furqas.upload_service.service

import com.furqas.upload_service.dto.CreateVideoRequest
import com.furqas.upload_service.dto.UpdateVideoRequest

interface MetadataClientService {

    fun createVideo(request: CreateVideoRequest)
    fun updateVideo(request: UpdateVideoRequest, videoId: String)
}