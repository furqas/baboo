package com.furqas.storage_service.dto

data class ListFilesResponse(
    val bucket: String,
    val prefix: String,
    val files: List<FileInfo>,
    val isTruncated: Boolean,
    val continuationToken: String?,
    val totalFiles: Int
)

