package com.furqas.upload_service.service.impl

import com.furqas.upload_service.client.MetadataClient
import com.furqas.upload_service.dto.CreateVideoRequest
import com.furqas.upload_service.dto.UpdateVideoRequest
import com.furqas.upload_service.service.MetadataClientService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MetadataClientServiceImpl(
    private final val client: MetadataClient,
    private final val log: Logger = LoggerFactory.getLogger(MetadataClientServiceImpl::class.java)
): MetadataClientService {
    override fun createVideo(request: CreateVideoRequest) {
        try {
            val response = client.createVideo(request)

            log.info("Response from metadata service: statusCode={}, body={}", response.statusCode, response.body)

            if(response.statusCode != HttpStatus.CREATED) {
                log.error("Creating video request failed: {}", response.body)
            }
        } catch (e: Exception) {
            log.error("Failed while requesting to create video: {}", e.message)
        }
    }

    override fun updateVideo(request: UpdateVideoRequest, videoId: String) {
        try {
            val response = client.updateVideo(UUID.fromString(videoId), request)

            log.info("Response from metadata service while updating video: statusCode={}, body={}", response.statusCode, response.body)
        } catch (e: Exception) {
            log.error("Failed while requesting to update video: {}", e.message)
        }
    }

}