package com.furqas.upload_service.config

import com.furqas.upload_service.model.UploadState
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun connectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory()
    }

    @Bean
    fun redisTemplate(
        connectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, UploadState> {

        val template = RedisTemplate<String, UploadState>()
        template.connectionFactory = connectionFactory

        val jsonSerializer = RedisSerializer.json()

        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = jsonSerializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = jsonSerializer

        template.afterPropertiesSet()
        return template
    }



}