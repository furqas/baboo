package com.furqas.upload_service.service

interface StorageService {

    fun upload(
        bucket: String,
        key: String,
        data: ByteArray,
        contentType: String
    )

    fun assembleChunks(
        sourceBucket: String,
        sourceKeys: List<String>,
        destBucket: String,
        destKey: String
    )

    fun delete(
        bucket: String,
        key: String
    )

    fun deleteMultiple(
        bucket: String,
        keys: List<String>
    )

}