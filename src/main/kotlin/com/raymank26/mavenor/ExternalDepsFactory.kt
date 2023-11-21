package com.raymank26.mavenor

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

open class ExternalDepsFactory {

    open fun gcpStorageClient(): Storage {
        return StorageOptions.getDefaultInstance().getService()
    }

    open fun awsStorageClient(env: Map<String, String>): S3Client {
        return S3Client.builder()
            .region(Region.of(env["AWS_REGION"]))
            .endpointOverride(URI(env["AWS_ENDPOINT_URL"]!!))
            .credentialsProvider {
                AwsBasicCredentials.create(env["AWS_ACCESS_KEY_ID"], env["AWS_SECRET_ACCESS_KEY"])
            }
            .build()
    }

    open fun getEnv(): Map<String, String> {
        return System.getenv()
    }
}