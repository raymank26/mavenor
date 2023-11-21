package com.raymank26.mavenor

import java.io.InputStream

interface Storage {

    fun write(objectPath: String, inputStream: InputStream, contentLength: Long)

    fun read(objectPath: String): InputStream

    fun getBlobInfo(objectPath: String): BlobInfo?
}

data class BlobInfo(
    val etag: String?,
    val size: Long
)