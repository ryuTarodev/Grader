package com.example.grader.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AwsS3Config {

    @Value("\${spring.cloud.credentials.access-key}")
    lateinit var awsAccessKeyId: String

    @Value("\${spring.cloud.credentials.secret-key}")
    lateinit var awsSecretAccessKey: String

    @Value("\${spring.cloud.aws.s3.region}")
    lateinit var region: String

    @Bean
    fun amazonS3(): AmazonS3 {
        val awsCredentials = BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
            .build()
    }
}