package com.example.grader.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    @Value("\${rabbitmq.queue.name}")
    private lateinit var submissionQueueName: String

    @Value("\${rabbitmq.exchange.name}")
    private lateinit var exchangeName: String

    @Value("\${rabbitmq.routing.key}")
    private lateinit var submissionRoutingKey: String


    @Bean
    fun submissionQueue(): Queue = Queue(submissionQueueName, false)

    @Bean
    fun exchange(): TopicExchange = TopicExchange(exchangeName)

    @Bean
    fun submissionBinding(): Binding {
        return BindingBuilder.bind(submissionQueue())
            .to(exchange())
            .with(submissionRoutingKey)
    }

    @Bean
    fun messageConverter(): Jackson2JsonMessageConverter = Jackson2JsonMessageConverter()

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            messageConverter = messageConverter()
        }
    }
}