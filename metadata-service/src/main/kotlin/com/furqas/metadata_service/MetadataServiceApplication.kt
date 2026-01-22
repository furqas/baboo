package com.furqas.metadata_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@EnableFeignClients
@SpringBootApplication
class VideoServiceApplication

fun main(args: Array<String>) {
	runApplication<VideoServiceApplication>(*args)
}
