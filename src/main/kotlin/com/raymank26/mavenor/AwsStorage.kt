package com.raymank26.mavenor

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
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
            val obj = s3Client.headObject(
                HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectPath)
                    .build()
            )
            return BlobInfo(obj.eTag(), obj.contentLength())
        } catch (e: NoSuchKeyException) {
            null
        }
    }
}