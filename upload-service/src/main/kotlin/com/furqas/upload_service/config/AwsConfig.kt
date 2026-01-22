package com.furqas.upload_service.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
@Profile("dev")
class AwsConfig {

    @Bean
    fun s3Client(
        @Value("\${spring.cloud.aws.s3.endpoint}") endpoint: String,
        @Value("\${spring.cloud.aws.region.static}") region: String,
        @Value("\${spring.cloud.aws.credentials.access-key}") accessKey: String,
        @Value("\${spring.cloud.aws.credentials.secret-key}") secretKey: String
    ): S3Client {
        return S3Client.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build()
            )
            .build()
    }
}

