package com.furqas.upload_service.controller

import com.furqas.upload_service.dto.CancelUploadResponse
import com.furqas.upload_service.dto.ChunkUploadResponse
import com.furqas.upload_service.dto.InitiateUploadRequest
import com.furqas.upload_service.dto.InitiateUploadResponse
import com.furqas.upload_service.service.UploadService
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RequiredArgsConstructor
@RestController
@RequestMapping($$"${api.version}/uploads")
class UploadController(
    private final val uploadService: UploadService
) {

    @PostMapping("/video/initiate")
    fun initiateUpload(
        @RequestBody request: InitiateUploadRequest,
    ): ResponseEntity<InitiateUploadResponse> {

        val response = uploadService.initiateUpload(
            request
        )

        return ResponseEntity.status(201).body(response)
    }

    @PostMapping("/video/chunk")
    fun uploadChunk(
        @RequestParam uploadId: String,
        @RequestParam chunkNumber: Int,
        @RequestParam totalChunks: Int,
        @RequestParam file: MultipartFile
    ): ResponseEntity<ChunkUploadResponse> {
        val response = uploadService.uploadChunk(
            uploadId = uploadId,
            chunkNumber = chunkNumber,
            totalChunks = totalChunks,
            data = file.bytes
        )

        return ResponseEntity.status(200).body(response)
    }

    @DeleteMapping("/video/cancel")
    fun cancelUpload(@RequestParam uploadId: String): CancelUploadResponse {
        return uploadService.cancelUpload(uploadId)
    }

}