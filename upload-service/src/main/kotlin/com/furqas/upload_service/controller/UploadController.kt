package com.furqas.upload_service.controller

import com.furqas.upload_service.dto.ChunkUploadResponse
import com.furqas.upload_service.dto.InitiateUploadRequest
import com.furqas.upload_service.dto.InitiateUploadResponse
import com.furqas.upload_service.service.UploadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping($$"{api.version}/uploads")
class UploadController(
    private val uploadService: UploadService
) {

    @PostMapping("/video/initiate")
    fun initiateUpload(
        @RequestBody request: InitiateUploadRequest,
//        @RequestHeader("Authorization") token: String
    ): ResponseEntity<InitiateUploadResponse> {
//        val userId = authService.getUserIdFromToken(token)

        val response = uploadService.initiateUpload(
            request
        )

        return ResponseEntity.status(102).body(response)
    }

    // 2. Upload de chunk
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

    // 3. Finalizar upload
    @PostMapping("/video/complete")
    fun completeUpload(
        @RequestParam uploadId: String
    ): CompleteUploadResponse {
        return uploadService.completeUpload(uploadId)
    }

    // 4. Cancelar upload
    @DeleteMapping("/video/cancel")
    fun cancelUpload(@RequestParam uploadId: String): CancelResponse {
        return uploadService.cancelUpload(uploadId)
    }

    // 5. Progresso do upload
    @GetMapping("/video/progress")
    fun getProgress(@RequestParam uploadId: String): ProgressResponse {
        return uploadService.getProgress(uploadId)
    }

}