package com.example.grader.service

import com.example.grader.dto.SubmissionSendMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SubmissionProducer(
    private val rabbitTemplate: RabbitTemplate,
) {

    @Value("\${rabbitmq.exchange.name}")
    private lateinit var exchangeName: String

    @Value("\${rabbitmq.routing.key}")
    private lateinit var routingKey: String

    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendSubmission(message: SubmissionSendMessage) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message)
        logger.info("Sent submission $message to RabbitMQ")
    }
}