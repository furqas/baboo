package com.furqas.upload_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@EnableFeignClients
@SpringBootApplication
class UploadServiceApplication

fun main(args: Array<String>) {
	runApplication<UploadServiceApplication>(*args)
}
