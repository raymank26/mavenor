package com.raymank26.mavenor

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions

open class ExternalDepsFactory {

    open fun gcpStorage(): Storage {
        return StorageOptions.getDefaultInstance().getService()
    }

    open fun getEnv(): Map<String, String> {
        return System.getenv()
    }
}