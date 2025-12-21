package com.furqas.video_service.service

import com.furqas.video_service.dto.CreateVideoRequest
import com.furqas.video_service.dto.CreateVideoResponse
import com.furqas.video_service.dto.GetVideoByIdResponse
import com.furqas.video_service.dto.PatchVideoByIdRequest
import com.furqas.video_service.dto.PatchVideoByIdResponse
import java.util.UUID

interface VideoService {

    fun createVideo(request: CreateVideoRequest): CreateVideoResponse
    fun getVideoById(id: UUID): GetVideoByIdResponse
    fun patchVideoById(id: UUID, request: PatchVideoByIdRequest): PatchVideoByIdResponse
    fun deleteVideoById(id: UUID)

}