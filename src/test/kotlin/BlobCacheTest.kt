import com.raymank26.mavenor.BlobCache
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BlobCacheTest {

    @Test
    fun shouldNotInsertValueExceed() {
        // given
        val blobCache = BlobCache(maxSizeBytes = 50, cacheLoadPercent = 70)
        blobCache.start()

        // when
        blobCache.get("foo", "123", 70) {
            byteArrayInputStream(70)
        }

        // then
        assertEquals(0.toDouble(), blobCache.cacheLoad())
    }

    @Test
    fun shouldNotCleanupDataIfNotNeeded() {
        // given
        val blobCache = BlobCache(maxSizeBytes = 50, cacheLoadPercent = 70, cleanupIntervalSeconds = 1)
        blobCache.start()

        // when
        blobCache.get("foo", "123", 10) {
            byteArrayInputStream(10)
        }

        // then
        Thread.sleep(3000)
        assertNotEquals(0.toDouble(), blobCache.cacheLoad())
    }

    @Test
    fun shouldCleanupData() {
        // given
        val blobCache = BlobCache(maxSizeBytes = 50, cacheLoadPercent = 70, cleanupIntervalSeconds = 1)
        blobCache.start()

        // when
        blobCache.get("foo", "123", 45) { // it inserts
            byteArrayInputStream(45)
        }
        blobCache.get("bar", "123", 50) { // it doesn't insert
            byteArrayInputStream(50)
        }

        // then
        await().untilAsserted {
            assertEquals(0.toDouble(), blobCache.cacheLoad())
        }
    }

    @Test
    fun shouldIncrementCacheHit() {
        // given
        val blobCache = BlobCache(maxSizeBytes = 50, cacheLoadPercent = 70, cleanupIntervalSeconds = 1)
        blobCache.start()

        // when
        val runnable = {
            blobCache.get("foo", "123", 45) {
                byteArrayInputStream(45)
            }
        }
        runnable.invoke() // inserts
        runnable.invoke() // cache hit
        runnable.invoke() // cache hit

        // then
        assertEquals(2, blobCache.getCacheHit("foo"))
    }

    private fun byteArrayInputStream(size: Int) = ByteArrayInputStream(Array(size) { 0.toByte() }.toByteArray())
}