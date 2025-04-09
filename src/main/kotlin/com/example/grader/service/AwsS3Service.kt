package com.example.grader.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.example.grader.error.AwsS3Exception
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.*

@Service
class AwsS3Service(
    private val amazonS3: AmazonS3
) {
    @Value("\${spring.cloud.aws.s3.bucketName}")
    private lateinit var bucketName: String

    private val logger = LoggerFactory.getLogger(javaClass)

    fun savePdfToS3(pdf: MultipartFile): String {
        return try {
            val pdfName = pdf.originalFilename ?: throw IllegalArgumentException("File name cannot be null")
            if (!pdfName.endsWith(".pdf", ignoreCase = true)) {
                throw IllegalArgumentException("Uploaded file is not a PDF")
            }
            val metadata = ObjectMetadata().apply {
                contentLength = pdf.size
                contentType = pdf.contentType ?: "application/pdf"
            }

            uploadFileToS3(pdfName, pdf.inputStream, metadata)
            pdfName
        } catch (e: Exception) {
            logger.error("Failed to upload PDF to S3: ${e.message}")
            throw AwsS3Exception("Unable to upload PDF to S3: ${e.message}")
        }
    }

    fun savePngToS3(png: MultipartFile): String {
        return try {
            val pngName = png.originalFilename ?: throw IllegalArgumentException("File name cannot be null")
            if (!pngName.endsWith(".png", ignoreCase = true)) {
                throw IllegalArgumentException("Uploaded file is not a PNG")
            }
            val metadata = ObjectMetadata().apply {
                contentLength = png.size
                contentType = png.contentType ?: "image/png"
            }

            uploadFileToS3(pngName, png.inputStream, metadata)
            pngName
        } catch (e: Exception) {
            logger.error("Failed to upload PNG to S3: ${e.message}")
            throw AwsS3Exception("Unable to upload PNG to S3: ${e.message}")
        }
    }

    private fun uploadFileToS3(fileName: String, inputStream: InputStream, metadata: ObjectMetadata) {
        try {
            val putObjectRequest = PutObjectRequest(bucketName, fileName, inputStream, metadata)
            amazonS3.putObject(putObjectRequest)
            logger.info("Successfully uploaded $fileName to S3")
        } catch (e: Exception) {
            logger.error("Error uploading file to S3", e)
            throw AwsS3Exception("Unable to upload file to S3: ${e.message}")
        }
    }
    fun generatePresignedUrl(objectKey: String): String {
        return try {
            val expiration = Date(System.currentTimeMillis() + 3600 * 1000) // 1 hour validity
            val generatePresignedUrlRequest = GeneratePresignedUrlRequest(bucketName, objectKey)
                .withExpiration(expiration)

            val url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString()
            logger.info("Generated presigned URL for $objectKey: $url")
            url
        } catch (e: Exception) {
            logger.error("Error generating presigned URL for $objectKey", e)
            throw AwsS3Exception("Unable to generate presigned URL: ${e.message}")
        }
    }
}