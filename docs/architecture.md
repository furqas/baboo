graph TB
subgraph "Frontend"
WEB[Web App - React/Next.js]
MOBILE[Mobile App - Kotlin]
end

    subgraph "API Gateway"
        GATEWAY[API Gateway / Kong]
    end

    subgraph "Serviços de Autenticação"
        AUTH[Auth Service - Kotlin]
        USER[User Service - Kotlin]
    end

    subgraph "Serviços de Vídeo"
        UPLOAD[Upload Service - Kotlin]
        TRANSCODE[Transcoding Service - Go]
        THUMBNAIL[Thumbnail Service - Go]
        STORAGE[Storage Service - Kotlin]
        STREAM[Streaming Service - Go]
    end

    subgraph "Serviços de Conteúdo"
        METADATA[Metadata Service - Kotlin]
        SEARCH[Search Service - Kotlin/ES]
        RECOMMENDATION[Recommendation Service - Python/Go]
    end

    subgraph "Serviços de Interação"
        COMMENT[Comment Service - Kotlin]
        LIKE[Like/Dislike Service - Kotlin]
        SUBSCRIBE[Subscription Service - Kotlin]
        NOTIFICATION[Notification Service - Kotlin]
    end

    subgraph "Serviços de Analytics"
        ANALYTICS[Analytics Service - Go]
        VIEW[View Counter Service - Go]
        METRICS[Metrics Service - Go]
    end

    subgraph "Infraestrutura AWS"
        S3[S3 - Storage]
        CF[CloudFront - CDN]
        SQS[SQS - Message Queue]
        SNS[SNS - Pub/Sub]
        RDS[(RDS - PostgreSQL)]
        DYNAMO[(DynamoDB)]
        REDIS[(Redis Cache)]
        ES[(ElasticSearch)]
    end

    WEB --> GATEWAY
    MOBILE --> GATEWAY
    
    GATEWAY --> AUTH
    GATEWAY --> USER
    GATEWAY --> UPLOAD
    GATEWAY --> STREAM
    GATEWAY --> METADATA
    GATEWAY --> SEARCH
    GATEWAY --> COMMENT
    GATEWAY --> LIKE
    GATEWAY --> SUBSCRIBE
    GATEWAY --> NOTIFICATION
    
    UPLOAD --> SQS
    SQS --> TRANSCODE
    SQS --> THUMBNAIL
    
    TRANSCODE --> S3
    THUMBNAIL --> S3
    STORAGE --> S3
    STREAM --> CF
    CF --> S3
    
    METADATA --> RDS
    USER --> RDS
    COMMENT --> RDS
    SUBSCRIBE --> RDS
    
    LIKE --> REDIS
    VIEW --> REDIS
    
    SEARCH --> ES
    
    ANALYTICS --> DYNAMO
    METRICS --> DYNAMO
    
    TRANSCODE --> SNS
    SNS --> NOTIFICATION
    
    VIEW --> ANALYTICS
    RECOMMENDATION --> ANALYTICS