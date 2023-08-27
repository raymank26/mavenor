package com.raymank26.mavenor

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions

open class ExternalDepsFactory {

    fun gcpStorage(): Storage {
        return StorageOptions.getDefaultInstance().getService()
    }

    fun getEnv(): Map<String, String> {
        return System.getenv()
    }
}