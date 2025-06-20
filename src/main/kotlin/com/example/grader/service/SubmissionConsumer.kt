package com.example.grader.service

import com.example.grader.dto.SubmissionReceiveMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener

import org.springframework.stereotype.Service

@Service
class SubmissionConsumer(
    private val submissionService: SubmissionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = ["\${rabbitmq.queue.result}"])
    fun handleResult(result: SubmissionReceiveMessage) {
        logger.info("✅ Received result from resultQueue: $result")
        finishSubmission(submissionId = result.submissionId, correctTestCases = result.correctTestCases)
    }

    fun finishSubmission(submissionId: Long, correctTestCases: Long) {
        submissionService.updateSubmissionResult(submissionId = submissionId, score = correctTestCases.toFloat() * 4)
        logger.info("✅ Successfully updated submission with ID $submissionId")
    }
}