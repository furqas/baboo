# Storage Service - Especificação Completa

## 🎯 Responsabilidades do Storage Service

O Storage Service é a **única porta de entrada para o S3**. Nenhum outro serviço acessa S3 diretamente.

### O que ele FAZ:
✅ Gerar presigned URLs para uploads/downloads
✅ Upload direto de arquivos pequenos (<5MB)
✅ Gerenciar S3 Multipart Upload (arquivos grandes)
✅ Deletar arquivos
✅ Copiar/mover arquivos entre buckets
✅ Listar arquivos
✅ Gerar URLs públicas via CloudFront
✅ Validar permissões de acesso
✅ Gerenciar metadados de arquivos

### O que ele NÃO FAZ:
❌ Não faz proxy de arquivos grandes (usa presigned URLs)
❌ Não processa vídeos (isso é o Transcoding Service)
❌ Não conhece regras de negócio de vídeos (isso é o Metadata Service)
❌ Não faz streaming (isso é o Streaming Service)

---

## 📂 Estrutura de Buckets S3

```
youtube-clone-raw/                    # Vídeos originais (não processados)
├── videos/
│   └── {videoId}/
│       └── original.mp4
└── temp/                             # Uploads temporários
    └── {uploadId}/
        ├── chunk-0
        ├── chunk-1
        └── ...

youtube-clone-processed/              # Vídeos processados
└── videos/
    └── {videoId}/
        ├── master.m3u8              # Playlist principal
        ├── 360p/
        │   ├── playlist.m3u8
        │   ├── segment000.ts
        │   ├── segment001.ts
        │   └── ...
        ├── 720p/
        │   └── ...
        └── 1080p/
            └── ...

youtube-clone-thumbnails/             # Imagens
├── videos/
│   └── {videoId}/
│       ├── auto-0.jpg
│       ├── auto-1.jpg
│       └── custom.jpg
├── playlists/
│   └── {playlistId}/
│       └── thumbnail.jpg
├── avatars/
│   └── {userId}/
│       └── avatar.jpg
└── banners/
    └── {userId}/
        └── banner.jpg
```

---

## 🔌 API Completa do Storage Service

### 1. Presigned URLs (Principal - Arquivos Grandes)

#### 1.1 Gerar URL para Download
```kotlin
POST /api/v1/storage/presigned-url/download

Request:
{
  "bucket": "raw",
  "key": "videos/abc-123/original.mp4",
  "expiresIn": 3600  // segundos (opcional, default: 3600)
}

Response:
{
  "url": "https://youtube-clone-raw.s3.amazonaws.com/videos/abc-123/original.mp4?X-Amz-Algorithm=...",
  "expiresAt": "2024-01-15T13:00:00Z",
  "method": "GET"
}
```

#### 1.2 Gerar URL para Upload Simples
```kotlin
POST /api/v1/storage/presigned-url/upload

Request:
{
  "bucket": "thumbnails",
  "key": "videos/abc-123/custom.jpg",
  "contentType": "image/jpeg",
  "expiresIn": 3600
}

Response:
{
  "url": "https://youtube-clone-thumbnails.s3.amazonaws.com/...",
  "expiresAt": "2024-01-15T13:00:00Z",
  "method": "PUT",
  "requiredHeaders": {
    "Content-Type": "image/jpeg"
  }
}
```

---

### 2. Multipart Upload (Arquivos Muito Grandes)

#### 2.1 Iniciar Multipart Upload
```kotlin
POST /api/v1/storage/multipart/initiate

Request:
{
  "bucket": "raw",
  "key": "videos/abc-123/original.mp4",
  "contentType": "video/mp4",
  "metadata": {
    "userId": "user-456",
    "uploadedFrom": "web"
  }
}

Response:
{
  "uploadId": "s3-multipart-xyz789",
  "bucket": "raw",
  "key": "videos/abc-123/original.mp4"
}
```

#### 2.2 Gerar URLs para Todas as Partes
```kotlin
POST /api/v1/storage/multipart/presigned-urls

Request:
{
  "bucket": "raw",
  "key": "videos/abc-123/original.mp4",
  "uploadId": "s3-multipart-xyz789",
  "totalParts": 400
}

Response:
{
  "urls": [
    {
      "partNumber": 1,
      "url": "https://youtube-clone-raw.s3.amazonaws.com/...?partNumber=1&uploadId=..."
    },
    {
      "partNumber": 2,
      "url": "https://youtube-clone-raw.s3.amazonaws.com/...?partNumber=2&uploadId=..."
    },
    // ... 400 URLs
  ]
}
```

#### 2.3 Completar Multipart Upload
```kotlin
POST /api/v1/storage/multipart/complete

Request:
{
  "bucket": "raw",
  "key": "videos/abc-123/original.mp4",
  "uploadId": "s3-multipart-xyz789",
  "parts": [
    { "partNumber": 1, "etag": "etag-1" },
    { "partNumber": 2, "etag": "etag-2" },
    // ... todas as partes
  ]
}

Response:
{
  "bucket": "raw",
  "key": "videos/abc-123/original.mp4",
  "location": "https://youtube-clone-raw.s3.amazonaws.com/videos/abc-123/original.mp4",
  "etag": "final-etag",
  "size": 2147483648
}
```

#### 2.4 Cancelar Multipart Upload
```kotlin
DELETE /api/v1/storage/multipart/abort

Request:
{
  "bucket": "raw",
  "key": "videos/abc-123/original.mp4",
  "uploadId": "s3-multipart-xyz789"
}

Response:
{
  "aborted": true
}
```

---

### 3. Upload Direto (Arquivos Pequenos <5MB)

#### 3.1 Upload de Arquivo
```kotlin
POST /api/v1/storage/upload

Request (multipart/form-data):
- bucket: "thumbnails"
- key: "videos/abc-123/custom.jpg"
- file: (binary)

Response:
{
  "bucket": "thumbnails",
  "key": "videos/abc-123/custom.jpg",
  "url": "https://d1234abcd.cloudfront.net/videos/abc-123/custom.jpg",
  "size": 245678,
  "contentType": "image/jpeg"
}
```

#### 3.2 Upload Múltiplos Arquivos
```kotlin
POST /api/v1/storage/upload/batch

Request (multipart/form-data):
- bucket: "thumbnails"
- files[]: (múltiplos arquivos)
- keys[]: ["thumb-1.jpg", "thumb-2.jpg"]

Response:
{
  "uploaded": [
    {
      "key": "videos/abc-123/thumb-1.jpg",
      "url": "https://...",
      "size": 123456
    },
    {
      "key": "videos/abc-123/thumb-2.jpg",
      "url": "https://...",
      "size": 234567
    }
  ],
  "failed": []
}
```

---

### 4. Download

#### 4.1 Download Direto (apenas arquivos pequenos)
```kotlin
GET /api/v1/storage/download

Query params:
- bucket: "thumbnails"
- key: "videos/abc-123/custom.jpg"

Response:
- Binary data
- Headers:
  - Content-Type: image/jpeg
  - Content-Length: 245678
  - Content-Disposition: inline; filename="custom.jpg"
```

---

### 5. Operações de Arquivo

#### 5.1 Deletar Arquivo
```kotlin
DELETE /api/v1/storage/delete

Request:
{
  "bucket": "thumbnails",
  "key": "videos/abc-123/old-thumb.jpg"
}

Response:
{
  "deleted": true,
  "bucket": "thumbnails",
  "key": "videos/abc-123/old-thumb.jpg"
}
```

#### 5.2 Deletar Múltiplos Arquivos
```kotlin
POST /api/v1/storage/delete/batch

Request:
{
  "bucket": "processed",
  "keys": [
    "videos/abc-123/360p/segment000.ts",
    "videos/abc-123/360p/segment001.ts",
    "videos/abc-123/360p/segment002.ts"
  ]
}

Response:
{
  "deleted": [
    "videos/abc-123/360p/segment000.ts",
    "videos/abc-123/360p/segment001.ts",
    "videos/abc-123/360p/segment002.ts"
  ],
  "failed": []
}
```

#### 5.3 Copiar Arquivo
```kotlin
POST /api/v1/storage/copy

Request:
{
  "sourceBucket": "raw",
  "sourceKey": "videos/abc-123/original.mp4",
  "destBucket": "backup",
  "destKey": "videos/abc-123/original.mp4"
}

Response:
{
  "copied": true,
  "destBucket": "backup",
  "destKey": "videos/abc-123/original.mp4"
}
```

#### 5.4 Mover Arquivo
```kotlin
POST /api/v1/storage/move

Request:
{
  "sourceBucket": "temp",
  "sourceKey": "uploads/xyz/video.mp4",
  "destBucket": "raw",
  "destKey": "videos/abc-123/original.mp4"
}

Response:
{
  "moved": true,
  "destBucket": "raw",
  "destKey": "videos/abc-123/original.mp4"
}
```

---

### 6. Listagem e Metadados

#### 6.1 Listar Arquivos
```kotlin
GET /api/v1/storage/list

Query params:
- bucket: "processed"
- prefix: "videos/abc-123/"
- maxKeys: 1000 (opcional)
- continuationToken: "..." (para paginação)

Response:
{
  "bucket": "processed",
  "prefix": "videos/abc-123/",
  "files": [
    {
      "key": "videos/abc-123/master.m3u8",
      "size": 1234,
      "lastModified": "2024-01-15T10:00:00Z",
      "etag": "abc123..."
    },
    {
      "key": "videos/abc-123/360p/playlist.m3u8",
      "size": 567,
      "lastModified": "2024-01-15T10:00:00Z",
      "etag": "def456..."
    }
  ],
  "isTruncated": false,
  "continuationToken": null,
  "totalFiles": 2
}
```

#### 6.2 Obter Metadados de Arquivo
```kotlin
GET /api/v1/storage/metadata

Query params:
- bucket: "raw"
- key: "videos/abc-123/original.mp4"

Response:
{
  "bucket": "raw",
  "key": "videos/abc-123/original.mp4",
  "size": 2147483648,
  "contentType": "video/mp4",
  "etag": "abc123...",
  "lastModified": "2024-01-15T10:00:00Z",
  "metadata": {
    "userId": "user-456",
    "uploadedFrom": "web"
  }
}
```

#### 6.3 Verificar se Arquivo Existe
```kotlin
HEAD /api/v1/storage/exists

Query params:
- bucket: "raw"
- key: "videos/abc-123/original.mp4"

Response:
- Status 200: Arquivo existe
- Status 404: Arquivo não existe
```

---

### 7. URLs Públicas

#### 7.1 Gerar URL Pública (via CloudFront)
```kotlin
GET /api/v1/storage/public-url

Query params:
- bucket: "processed"
- key: "videos/abc-123/master.m3u8"

Response:
{
  "url": "https://d1234abcd.cloudfront.net/videos/abc-123/master.m3u8",
  "cdn": true
}
```

---

## 💻 Implementação Completa em Kotlin

### Configuração

```kotlin
// build.gradle.kts
dependencies {
    implementation("software.amazon.awssdk:s3:2.20.0")
    implementation("software.amazon.awssdk:s3-transfer-manager:2.20.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

// application.yml
aws:
  region: us-east-1
  s3:
    buckets:
      raw: youtube-clone-raw
      processed: youtube-clone-processed
      thumbnails: youtube-clone-thumbnails
  cloudfront:
    domain: d1234abcd.cloudfront.net
```

### Service Layer

```kotlin
@Service
class StorageService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    @Value("\${aws.s3.buckets.raw}") private val rawBucket: String,
    @Value("\${aws.s3.buckets.processed}") private val processedBucket: String,
    @Value("\${aws.s3.buckets.thumbnails}") private val thumbnailsBucket: String,
    @Value("\${aws.cloudfront.domain}") private val cloudFrontDomain: String
) {
    
    // ==================== PRESIGNED URLS ====================
    
    fun generatePresignedDownloadUrl(
        bucket: String,
        key: String,
        expiresIn: Int = 3600
    ): PresignedUrlResponse {
        
        validateBucket(bucket)
        
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
        
        val presignedRequest = s3Presigner.presignGetObject { builder ->
            builder
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofSeconds(expiresIn.toLong()))
        }
        
        return PresignedUrlResponse(
            url = presignedRequest.url().toString(),
            expiresAt = Instant.now().plusSeconds(expiresIn.toLong()),
            method = "GET"
        )
    }
    
    fun generatePresignedUploadUrl(
        bucket: String,
        key: String,
        contentType: String,
        expiresIn: Int = 3600
    ): PresignedUrlResponse {
        
        validateBucket(bucket)
        
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build()
        
        val presignedRequest = s3Presigner.presignPutObject { builder ->
            builder
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofSeconds(expiresIn.toLong()))
        }
        
        return PresignedUrlResponse(
            url = presignedRequest.url().toString(),
            expiresAt = Instant.now().plusSeconds(expiresIn.toLong()),
            method = "PUT",
            requiredHeaders = mapOf("Content-Type" to contentType)
        )
    }
    
    // ==================== MULTIPART UPLOAD ====================
    
    fun initiateMultipartUpload(
        bucket: String,
        key: String,
        contentType: String,
        metadata: Map<String, String> = emptyMap()
    ): InitiateMultipartResponse {
        
        validateBucket(bucket)
        
        val request = CreateMultipartUploadRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .metadata(metadata)
            .build()
        
        val response = s3Client.createMultipartUpload(request)
        
        return InitiateMultipartResponse(
            uploadId = response.uploadId(),
            bucket = bucket,
            key = key
        )
    }
    
    fun generateMultipartPresignedUrls(
        bucket: String,
        key: String,
        uploadId: String,
        totalParts: Int,
        expiresIn: Int = 7200 // 2 horas para uploads grandes
    ): MultipartPresignedUrlsResponse {
        
        validateBucket(bucket)
        
        val urls = (1..totalParts).map { partNumber ->
            val uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build()
            
            val presignedRequest = s3Presigner.presignUploadPart { builder ->
                builder
                    .uploadPartRequest(uploadPartRequest)
                    .signatureDuration(Duration.ofSeconds(expiresIn.toLong()))
            }
            
            PresignedPartUrl(
                partNumber = partNumber,
                url = presignedRequest.url().toString()
            )
        }
        
        return MultipartPresignedUrlsResponse(
            uploadId = uploadId,
            urls = urls,
            expiresAt = Instant.now().plusSeconds(expiresIn.toLong())
        )
    }
    
    fun completeMultipartUpload(
        bucket: String,
        key: String,
        uploadId: String,
        parts: List<CompletedPartRequest>
    ): CompleteMultipartResponse {
        
        validateBucket(bucket)
        
        val completedParts = parts.map { part ->
            CompletedPart.builder()
                .partNumber(part.partNumber)
                .eTag(part.etag)
                .build()
        }
        
        val completedMultipartUpload = CompletedMultipartUpload.builder()
            .parts(completedParts)
            .build()
        
        val request = CompleteMultipartUploadRequest.builder()
            .bucket(bucket)
            .key(key)
            .uploadId(uploadId)
            .multipartUpload(completedMultipartUpload)
            .build()
        
        val response = s3Client.completeMultipartUpload(request)
        
        // Obtém tamanho do arquivo
        val headResponse = s3Client.headObject {
            it.bucket(bucket)
            it.key(key)
        }
        
        return CompleteMultipartResponse(
            bucket = bucket,
            key = key,
            location = response.location(),
            etag = response.eTag(),
            size = headResponse.contentLength()
        )
    }
    
    fun abortMultipartUpload(
        bucket: String,
        key: String,
        uploadId: String
    ): AbortMultipartResponse {
        
        validateBucket(bucket)
        
        val request = AbortMultipartUploadRequest.builder()
            .bucket(bucket)
            .key(key)
            .uploadId(uploadId)
            .build()
        
        s3Client.abortMultipartUpload(request)
        
        return AbortMultipartResponse(aborted = true)
    }
    
    // ==================== DIRECT UPLOAD ====================
    
    fun uploadFile(
        bucket: String,
        key: String,
        data: ByteArray,
        contentType: String
    ): UploadResponse {
        
        validateBucket(bucket)
        
        // Limita tamanho (apenas para arquivos pequenos)
        if (data.size > 5 * 1024 * 1024) {
            throw IllegalArgumentException("File too large for direct upload. Use multipart upload.")
        }
        
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .contentLength(data.size.toLong())
            .build()
        
        s3Client.putObject(request, RequestBody.fromBytes(data))
        
        return UploadResponse(
            bucket = bucket,
            key = key,
            url = getPublicUrl(bucket, key),
            size = data.size.toLong(),
            contentType = contentType
        )
    }
    
    // ==================== DOWNLOAD ====================
    
    fun downloadFile(bucket: String, key: String): ByteArray {
        validateBucket(bucket)
        
        val request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
        
        return s3Client.getObject(request).readAllBytes()
    }
    
    // ==================== DELETE ====================
    
    fun deleteFile(bucket: String, key: String): DeleteResponse {
        validateBucket(bucket)
        
        val request = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
        
        s3Client.deleteObject(request)
        
        return DeleteResponse(
            deleted = true,
            bucket = bucket,
            key = key
        )
    }
    
    fun deleteFiles(bucket: String, keys: List<String>): BatchDeleteResponse {
        validateBucket(bucket)
        
        val objectIdentifiers = keys.map { key ->
            ObjectIdentifier.builder().key(key).build()
        }
        
        val delete = Delete.builder()
            .objects(objectIdentifiers)
            .build()
        
        val request = DeleteObjectsRequest.builder()
            .bucket(bucket)
            .delete(delete)
            .build()
        
        val response = s3Client.deleteObjects(request)
        
        val deleted = response.deleted().map { it.key() }
        val failed = response.errors().map { it.key() }
        
        return BatchDeleteResponse(
            deleted = deleted,
            failed = failed
        )
    }
    
    // ==================== COPY & MOVE ====================
    
    fun copyFile(
        sourceBucket: String,
        sourceKey: String,
        destBucket: String,
        destKey: String
    ): CopyResponse {
        
        validateBucket(sourceBucket)
        validateBucket(destBucket)
        
        val copySource = "$sourceBucket/$sourceKey"
        
        val request = CopyObjectRequest.builder()
            .sourceBucket(sourceBucket)
            .sourceKey(sourceKey)
            .destinationBucket(destBucket)
            .destinationKey(destKey)
            .build()
        
        s3Client.copyObject(request)
        
        return CopyResponse(
            copied = true,
            destBucket = destBucket,
            destKey = destKey
        )
    }
    
    fun moveFile(
        sourceBucket: String,
        sourceKey: String,
        destBucket: String,
        destKey: String
    ): MoveResponse {
        
        // Copy
        copyFile(sourceBucket, sourceKey, destBucket, destKey)
        
        // Delete source
        deleteFile(sourceBucket, sourceKey)
        
        return MoveResponse(
            moved = true,
            destBucket = destBucket,
            destKey = destKey
        )
    }
    
    // ==================== LIST & METADATA ====================
    
    fun listFiles(
        bucket: String,
        prefix: String,
        maxKeys: Int = 1000,
        continuationToken: String? = null
    ): ListFilesResponse {
        
        validateBucket(bucket)
        
        val request = ListObjectsV2Request.builder()
            .bucket(bucket)
            .prefix(prefix)
            .maxKeys(maxKeys)
            .apply {
                if (continuationToken != null) {
                    continuationToken(continuationToken)
                }
            }
            .build()
        
        val response = s3Client.listObjectsV2(request)
        
        val files = response.contents().map { obj ->
            FileInfo(
                key = obj.key(),
                size = obj.size(),
                lastModified = obj.lastModified(),
                etag = obj.eTag()
            )
        }
        
        return ListFilesResponse(
            bucket = bucket,
            prefix = prefix,
            files = files,
            isTruncated = response.isTruncated,
            continuationToken = response.nextContinuationToken(),
            totalFiles = files.size
        )
    }
    
    fun getFileMetadata(bucket: String, key: String): FileMetadataResponse {
        validateBucket(bucket)
        
        val request = HeadObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
        
        val response = s3Client.headObject(request)
        
        return FileMetadataResponse(
            bucket = bucket,
            key = key,
            size = response.contentLength(),
            contentType = response.contentType(),
            etag = response.eTag(),
            lastModified = response.lastModified(),
            metadata = response.metadata()
        )
    }
    
    fun fileExists(bucket: String, key: String): Boolean {
        validateBucket(bucket)
        
        return try {
            s3Client.headObject {
                it.bucket(bucket)
                it.key(key)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ==================== PUBLIC URLS ====================
    
    fun getPublicUrl(bucket: String, key: String): String {
        // Retorna URL do CloudFront (CDN)
        return "https://$cloudFrontDomain/$key"
    }
    
    // ==================== HELPERS ====================
    
    private fun validateBucket(bucket: String) {
        val allowedBuckets = listOf(rawBucket, processedBucket, thumbnailsBucket)
        if (bucket !in allowedBuckets) {
            throw IllegalArgumentException("Invalid bucket: $bucket")
        }
    }
}
```

### Controller Layer

```kotlin
@RestController
@RequestMapping("/api/v1/storage")
class StorageController(
    private val storageService: StorageService
) {
    
    // ==================== PRESIGNED URLS ====================
    
    @PostMapping("/presigned-url/download")
    fun generateDownloadUrl(
        @RequestBody @Valid request: PresignedDownloadRequest
    ): ResponseEntity<PresignedUrlResponse> {
        val response = storageService.generatePresignedDownloadUrl(
            bucket = request.bucket,
            key = request.key,
            expiresIn = request.expiresIn ?: 3600
        )
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/presigned-url/upload")
    fun generateUploadUrl(
        @RequestBody @Valid request: PresignedUploadRequest
    ): ResponseEntity<PresignedUrlResponse> {
        val response = storageService.generatePresignedUploadUrl(
            bucket = request.bucket,
            key = request.key,
            contentType = request.contentType,
            expiresIn = request.expiresIn ?: 3600
        )
        return ResponseEntity.ok(response)
    }
    
    // ==================== MULTIPART ====================
    
    @PostMapping("/multipart/initiate")
    fun initiateMultipart(
        @RequestBody @Valid request: InitiateMultipartRequest
    ): ResponseEntity<InitiateMultipartResponse> {
        val response = storageService.initiateMultipartUpload(
            bucket = request.bucket,
            key = request.key,
            contentType = request.contentType,
            metadata = request.metadata ?: emptyMap()
        )
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/multipart/presigned-urls")
    fun generateMultipartUrls(
        @RequestBody @Valid request: GenerateMultipartUrlsRequest
    ): ResponseEntity<MultipartPresignedUrlsResponse> {
        val response = storageService.generateMultipartPresignedUrls(
            bucket = request.bucket,
            key = request.key,
            uploadId = request.uploadId,
            totalParts = request.totalParts
        )
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/multipart/complete")
    fun completeMultipart(
        @RequestBody @Valid request: CompleteMultipartRequest
    ): ResponseEntity<CompleteMultipartResponse> {
        val response = storageService.completeMultipartUpload(
            bucket = request.bucket,
            key = request.key,
            uploadId = request.uploadId,
            parts = request.parts
        )
        return ResponseEntity.ok(response)
    }
    
    @DeleteMapping("/multipart/abort")
    fun abortMultipart(
        @RequestBody @Valid request: AbortMultipartRequest
    ): ResponseEntity<AbortMultipartResponse> {
        val response = storageService.abortMultipartUpload(
            bucket = request.bucket,
            key = request.key,
            uploadId = request.uploadId
        )
        return ResponseEntity.ok(response)
    }
    
    // ==================== DIRECT UPLOAD ====================
    
    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam bucket: String,
        @RequestParam key: String,
        @RequestParam file: MultipartFile
    ): ResponseEntity<UploadResponse> {
        val response = storageService.uploadFile(
            bucket = bucket,
            key = key,
            data = file.bytes,
            contentType = file.contentType ?: "application/octet-stream"
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    // ==================== DELETE ====================
    
    @DeleteMapping("/delete")
    fun deleteFile(
        @RequestParam bucket: String,
        @RequestParam key: String
    ): ResponseEntity<DeleteResponse> {
        val response = storageService.deleteFile(bucket, key)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/delete/batch")
    fun deleteFiles(
        @RequestBody @Valid request: BatchDeleteRequest
    ): ResponseEntity<BatchDeleteResponse> {
        val response = storageService.deleteFiles(
            bucket = request.bucket,
            keys = request.keys
        )
        return ResponseEntity.ok(response)
    }
    
    // ==================== COPY & MOVE ====================
    
    @PostMapping("/copy")
    fun copyFile(
        @RequestBody @Valid request: CopyRequest
    ): ResponseEntity<CopyResponse> {
        val response = storageService.copyFile(
            sourceBucket = request.sourceBucket,
            sourceKey = request.sourceKey,
            destBucket = request.destBucket,
            destKey = request.destKey
        )
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/move")
    fun moveFile(
        @RequestBody @Valid request: MoveRequest
    ): ResponseEntity<MoveResponse> {
        val response = storageService.moveFile(
            sourceBucket = request.sourceBucket,
            sourceKey = request.sourceKey,
            destBucket = request.destBucket,
            destKey = request.destKey
        )
        return ResponseEntity.ok(response)
    }
    
    // ==================== LIST & METADATA ====================
    
    @GetMapping("/list")
    fun listFiles(
        @RequestParam bucket: String,
        @RequestParam prefix: String,
        @RequestParam(required = false) maxKeys: Int?,
        @RequestParam(required = false) continuationToken: String?
    ): ResponseEntity<ListFilesResponse> {
        val response = storageService.listFiles(
            bucket = bucket,
            prefix = prefix,
            maxKeys = maxKeys ?: 1000,
            continuationToken = continuationToken
        )
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/metadata")
    fun getMetadata(
        @RequestParam bucket: String,
        @RequestParam key: String
    ): ResponseEntity<FileMetadataResponse> {
        val response = storageService.getFileMetadata(bucket, key)
        return ResponseEntity.ok(response)
    }
    
    @HeadMapping("/exists")
    fun fileExists(
        @RequestParam bucket: String,
        @RequestParam key: String
    ): ResponseEntity<Void> {
        val exists = storageService.fileExists(bucket, key)
        return if (exists) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    // ==================== PUBLIC URL ====================
    
    @GetMapping("/public-url")
    fun getPublicUrl(
        @RequestParam bucket: String,
        @RequestParam key: String
    ): ResponseEntity<PublicUrlResponse> {
        val url = storageService.getPublicUrl(bucket, key)
        return ResponseEntity.ok(PublicUrlResponse(url, cdn = true))
    }
}
```

### DTOs

```kotlin
// Presigned URLs
data class PresignedDownloadRequest(
    val bucket: String,
    val key: String,
    val expiresIn: Int? = null
)

data class PresignedUploadRequest(
    val bucket: String,
    val key: String,
    val contentType: String,
    val expiresIn: Int? = null
)

data class PresignedUrlResponse(
    val url: String,
    val expiresAt: Instant,
    val method: String,
    val requiredHeaders: Map<String, String>? = null
)

// Multipart
data class InitiateMultipartRequest(
    val bucket: String,
    val key: String,
    val contentType: String,
    val metadata: Map<String, String>? = null
)

data class InitiateMultipartResponse(
    val uploadId: String,
    val bucket: String,
    val key: String
)

data class GenerateMultipartUrlsRequest(
    val bucket: String,
    val key: String,
    val uploadId: String,
    val totalParts: Int
)

data class PresignedPartUrl(
    val partNumber: Int,
    val url: String
)

data class MultipartPresignedUrlsResponse(
    val uploadId: String,
    val urls: List<PresignedPartUrl>,
    val expiresAt: Instant
)

data class CompletedPartRequest(
    val partNumber: Int,
    val etag: String
)

data class CompleteMultipartRequest(
    val bucket: String,
    val key: String,
    val uploadId: String,
    val parts: List<CompletedPartRequest>
)

data class CompleteMultipartResponse(
    val bucket: String,
    val key: String,
    val location: String,
    val etag: String,
    val size: Long
)

data class AbortMultipartRequest(
    val bucket: String,
    val key: String,
    val uploadId: String
)

data class AbortMultipartResponse(
    val aborted: Boolean
)

// Upload
data class UploadResponse(
    val bucket: String,
    val key: String,
    val url: String,
    val size: Long,
    val contentType: String
)

// Delete
data class DeleteResponse(
    val deleted: Boolean,
    val bucket: String,
    val key: String
)

data class BatchDeleteRequest(
    val bucket: String,
    val keys: List<String>
)

data class BatchDeleteResponse(
    val deleted: List<String>,
    val failed: List<String>
)

// Copy & Move
data class CopyRequest(
    val sourceBucket: String,
    val sourceKey: String,
    val destBucket: String,
    val destKey: String
)

data class CopyResponse(
    val copied: Boolean,
    val destBucket: String,
    val destKey: String
)

data class MoveRequest(
    val sourceBucket: String,
    val sourceKey: String,
    val destBucket: String,
    val destKey: String
)

data class MoveResponse(
    val moved: Boolean,
    val destBucket: String,
    val destKey: String
)

// List & Metadata
data class FileInfo(
    val key: String,
    val size: Long,
    val lastModified: Instant,
    val etag: String
)

data class ListFilesResponse(
    val bucket: String,
    val prefix: String,
    val files: List<FileInfo>,
    val isTruncated: Boolean,
    val continuationToken: String?,
    val totalFiles: Int
)

data class FileMetadataResponse(
    val bucket: String,
    val key: String,
    val size: Long,
    val contentType: String,
    val etag: String,
    val lastModified: Instant,
    val metadata: Map<String, String>
)

// Public URL
data class PublicUrlResponse(
    val url: String,
    val cdn: Boolean
)
```

---

## 🔐 Segurança e Validações

### Validações Importantes

```kotlin
@Component
class StorageValidator {
    
    fun validateBucket(bucket: String) {
        val allowedBuckets = listOf("raw", "processed", "thumbnails")
        if (bucket !in allowedBuckets) {
            throw IllegalArgumentException("Invalid bucket")
        }
    }
    
    fun validateKey(key: String) {
        // Previne path traversal
        if (key.contains("..")) {
            throw IllegalArgumentException("Invalid key: path traversal detected")
        }
        
        // Valida caracteres
        if (!key.matches(Regex("^[a-zA-Z0-9/_.-]+$"))) {
            throw IllegalArgumentException("Invalid key: contains invalid characters")
        }
    }
    
    fun validateFileSize(size: Long, maxSize: Long) {
        if (size > maxSize) {
            throw IllegalArgumentException("File too large: $size bytes (max: $maxSize)")
        }
    }
    
    fun validateContentType(contentType: String, allowedTypes: List<String>) {
        if (!allowedTypes.any { contentType.startsWith(it) }) {
            throw IllegalArgumentException("Invalid content type: $contentType")
        }
    }
}
```

---

## 📊 Resumo

### O Storage Service é responsável por:

✅ **Gerar presigned URLs** (principal funcionalidade)
✅ **Gerenciar multipart uploads** para arquivos grandes
✅ **Upload direto** de arquivos pequenos
✅ **Operações CRUD** em arquivos (delete, copy, move)
✅ **Listagem e metadados**
✅ **URLs públicas via CDN**
✅ **Validações e segurança**

### O Storage Service NÃO faz:

❌ Proxy de dados grandes (usa presigned URLs)
❌ Processamento de vídeos
❌ Streaming
❌ Regras de negócio

**É a camada de abstração perfeita sobre S3!** 🎯