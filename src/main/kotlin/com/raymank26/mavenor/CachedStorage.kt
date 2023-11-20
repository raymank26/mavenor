package com.raymank26.mavenor

import com.google.cloud.storage.StorageException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class CachedStorage(
    private val storage: Storage,
    maxCacheSizeBytes: Long
) {

    private val cache = BlobCache(maxCacheSizeBytes)

    init {
        cache.start()
    }

    fun write(objectPath: String, inputStream: InputStream, contentLength: Long) {
        storage.write(objectPath, inputStream, contentLength)
    }

    fun read(objectPath: String, outputStream: OutputStream) {
        try {
            val blob = storage.getBlobInfo(objectPath)
                ?: throw ObjectNotFound("Object not found")
            val inputStream = cache.get(objectPath, blob.etag, blob.size) {
                storage.read(objectPath)
            }
            inputStream.use {
                inputStream.transferTo(outputStream)
            }
        } catch (e: IOException) {
            if (e.cause is StorageException) {
                throw ObjectNotFound("Object not found", e)
            }
        }
    }

    fun stop() {
        cache.stop()
    }
}

class ObjectNotFound(msg: String, e: Exception? = null) : Exception(msg, e)