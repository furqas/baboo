package com.furqas.upload_service.service

import com.furqas.upload_service.dto.CancelUploadResponse
import com.furqas.upload_service.dto.ChunkUploadResponse
import com.furqas.upload_service.dto.InitiateUploadRequest
import com.furqas.upload_service.dto.InitiateUploadResponse

interface UploadService {

    fun initiateUpload(request: InitiateUploadRequest): InitiateUploadResponse
    fun uploadChunk(uploadId: String, chunkNumber: Int, totalChunks: Int, data: ByteArray): ChunkUploadResponse
    fun cancelUpload(uploadId: String): CancelUploadResponse
}
