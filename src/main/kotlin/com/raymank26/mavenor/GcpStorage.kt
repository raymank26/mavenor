package com.raymank26.mavenor

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import java.io.InputStream
import java.nio.channels.Channels

class GcpStorage(
    private val bucketName: String,
    private val gcpStorage: Storage,
) : com.raymank26.mavenor.Storage {

    override fun write(objectPath: String, inputStream: InputStream, contentLength: Long) {
        gcpStorage.writer(BlobInfo.newBuilder(bucketName, objectPath).build()).use {
            Channels.newOutputStream(it).buffered().use { bufferedOs ->
                inputStream.transferTo(bufferedOs)
            }
        }
    }

    override fun read(objectPath: String): InputStream {
        return Channels.newInputStream(gcpStorage.reader(BlobId.of(bucketName, objectPath)))
    }

    override fun getBlobInfo(objectPath: String): com.raymank26.mavenor.BlobInfo? {
        return gcpStorage.get(BlobId.of(bucketName, objectPath))?.let { blob ->
            BlobInfo(blob.etag, blob.size)
        }
    }
}