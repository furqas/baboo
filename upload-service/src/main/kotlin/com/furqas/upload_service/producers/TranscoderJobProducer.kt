package com.furqas.upload_service.producers

import com.furqas.upload_service.dto.TranscodeJobEvent
import com.furqas.upload_service.exceptions.TranscoderProducerException
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TranscoderJobProducer(
    private final val template: SqsTemplate,

    @Value($$"${spring.cloud.aws.queues.transcoder-producer}")
    private val queue: String
) {

    fun createJob(
        event: TranscodeJobEvent
    ) {
        try {
            template.send(queue, event)
        } catch (e: Exception) {
            throw TranscoderProducerException(e.message?: "Error while sending message to queue")
        }
    }

}