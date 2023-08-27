package com.raymank26.mavenor

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.Channels

@Suppress("JoinDeclarationAndAssignment")
class RemoteStorage(private val bucketName: String) {

    private val gcpStorage: Storage

    init {
        gcpStorage = StorageOptions.getDefaultInstance().getService()
    }

    fun write(objectPath: String, inputStream: InputStream) {
        gcpStorage.writer(BlobInfo.newBuilder(bucketName, objectPath).build()).use {
            Channels.newOutputStream(it).buffered().use { bufferedOs ->
                inputStream.transferTo(bufferedOs)
            }
        }
    }

    fun read(objectPath: String, outputStream: OutputStream) {
        gcpStorage.reader(BlobId.of(bucketName, objectPath)).use { readChannel ->
            outputStream.buffered().use { bufferedOs ->
                Channels.newInputStream(readChannel).transferTo(bufferedOs)
            }
        }
    }
}