package com.raymank26.mavenor

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import software.amazon.awssdk.services.s3.S3Client

open class ExternalDepsFactory {

    open fun gcpStorageClient(): Storage {
        return StorageOptions.getDefaultInstance().getService()
    }

    open fun awsStorageClient(): S3Client {
        return S3Client.create()
    }

    open fun getEnv(): Map<String, String> {
        return System.getenv()
    }
}