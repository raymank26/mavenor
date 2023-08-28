package com.raymank26.mavenor

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.Channels

class RemoteStorage(
    private val bucketName: String,
    private val gcpStorage: Storage,
    maxCacheSizeBytes: Long
) {

    private val cache = BlobCache(maxCacheSizeBytes)

    init {
        cache.start()
    }

    fun write(objectPath: String, inputStream: InputStream) {
        gcpStorage.writer(BlobInfo.newBuilder(bucketName, objectPath).build()).use {
            Channels.newOutputStream(it).buffered().use { bufferedOs ->
                inputStream.transferTo(bufferedOs)
            }
        }
    }

    fun read(objectPath: String, outputStream: OutputStream) {
        try {
            val blob = gcpStorage.get(BlobId.of(bucketName, objectPath))
            val inputStream = cache.get(objectPath, blob.etag, blob.size) {
                Channels.newInputStream(gcpStorage.reader(BlobId.of(bucketName, objectPath)))
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

class ObjectNotFound(msg: String, e: Exception) : Exception(msg, e)