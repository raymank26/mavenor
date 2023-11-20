package com.raymank26.mavenor

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectAttributesRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.InputStream

class AwsStorage(
    private val s3Client: S3Client,
    private val bucketName: String
) : Storage {

    override fun write(objectPath: String, inputStream: InputStream, contentLength: Long) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectPath)
                .build(), RequestBody.fromInputStream(inputStream, contentLength)
        )
    }

    override fun read(objectPath: String): InputStream {
        return s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectPath)
                .build()
        )
    }

    override fun getBlobInfo(objectPath: String): BlobInfo? {
        return try {
            val response = s3Client.getObjectAttributes(
                GetObjectAttributesRequest.builder()
                    .build()
            )
            BlobInfo(response.eTag(), response.objectSize())
        } catch (e: NoSuchKeyException) {
            null
        }
    }
}