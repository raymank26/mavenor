package com.raymank26.mavenor

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

private val log = LoggerFactory.getLogger(BlobCache::class.java)

class BlobCache(
    private val maxSizeBytes: Long,
    private val cacheLoadPercent: Int = 70,
    private val cleanupIntervalSeconds: Long = 30,
) {

    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val currentSizeBytes = AtomicLong(0)
    private val notEnoughSpace: AtomicBoolean = AtomicBoolean(false)
    private val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
        ThreadFactoryBuilder()
            .setDaemon(true)
            .build()
    )
    private val cleanupLock = ReentrantReadWriteLock()

    fun start() {
        scheduledExecutorService.scheduleWithFixedDelay({
            try {
                cleanupLock.write {
                    if (!notEnoughSpace.get() && cacheLoad() < cacheLoadPercent) {
                        return@scheduleWithFixedDelay
                    }
                    val sortedEntries = cache.entries.sortedBy { it.value.cacheHit }
                    var evicted = 0
                    var memoryCleanedBytes = 0L

                    for (entry in sortedEntries) {
                        cache.remove(entry.key)
                        currentSizeBytes.addAndGet(-entry.value.content.size.toLong())
                        memoryCleanedBytes += entry.value.content.size
                        evicted++
                        if (cacheLoad() < cacheLoadPercent) {
                            break
                        }
                    }
                    for (entry in cache.entries) {
                        cache[entry.key] = entry.value.copy(cacheHit = 0)
                    }
                    log.info("Entries evicted = {}, memoryCleanedBytes = {}", evicted, memoryCleanedBytes)
                    notEnoughSpace.set(false)
                }
            } catch (e: Throwable) {
                log.error("Unable to complete scheduled task", e)
            }
        }, cleanupIntervalSeconds, cleanupIntervalSeconds, TimeUnit.SECONDS)
    }

    fun stop() {
        scheduledExecutorService.shutdown()
    }

    fun get(key: String, etag: String, fileSizeBytes: Long, loader: () -> InputStream): InputStream {
        val cachedEntry = cache.compute(key) { _, entry ->
            if (entry != null && entry.etag == etag) {
                return@compute entry.copy(cacheHit = entry.cacheHit + 1)
            }
            if (!canInsert(fileSizeBytes)) {
                notEnoughSpace.set(true)
                return@compute null
            }
            val lock = cleanupLock.readLock()
            if (!lock.tryLock()) {
                return@compute null
            }
            try {
                if (!canInsert(fileSizeBytes)) {
                    notEnoughSpace.set(true)
                    return@compute null
                }
                val os = ByteArrayOutputStream()
                loader.invoke().use {
                    it.transferTo(os)
                }
                currentSizeBytes.addAndGet(fileSizeBytes - (entry?.content?.size?.toLong() ?: 0L))
                return@compute CacheEntry(
                    etag = etag,
                    content = os.toByteArray(),
                    cacheHit = 0,
                )
            } finally {
                lock.unlock()
            }
        }
        return if (cachedEntry != null) {
            ByteArrayInputStream(cachedEntry.content)
        } else loader.invoke()
    }

    fun cacheLoad(): Double {
        return currentSizeBytes.get().toDouble() / maxSizeBytes
    }

    fun getCacheHit(key: String): Long? = cache[key]?.cacheHit

    private fun canInsert(fileSizeBytes: Long) = currentSizeBytes.get() + fileSizeBytes <= maxSizeBytes
}

data class CacheEntry(val etag: String, val content: ByteArray, val cacheHit: Long)