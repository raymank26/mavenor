import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import com.raymank26.mavenor.App
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.s3.S3Client
import java.io.IOException
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class AppTest {
    private val gcpStorage: Storage = mockk()
    private val s3Client: S3Client = mockk()
    private val bucketName = "foo"
    private val basicAuthUsername = "abc"
    private val basicAuthPassword = "355"

    private val env = buildMap {
        put("GOOGLE_CLOUD_STORAGE_BUCKET_NAME", bucketName)
        put("USERNAME", basicAuthUsername)
        put("PASSWORD", basicAuthPassword)
    }
    private val app = App(MockExternalDepsFactory(gcpStorage, s3Client, env))
    private val okHttpClient = OkHttpClient().newBuilder()
        .authenticator { _, response ->
            val credentials = Credentials.basic(basicAuthUsername, basicAuthPassword)
            response.request.newBuilder().header("Authorization", credentials).build()
        }
        .build()
    private val appHost = "http://localhost:8080"

    @BeforeEach
    fun before() {
        clearAllMocks()
        app.start()
    }

    @AfterEach
    fun stop() {
        app.stop()
    }

    @Test
    fun shouldWriteRequestToStorage() {
        // given
        val fileName = "/maven/com/raymank26/0.0.1/file.txt"
        every {
            gcpStorage.writer(eq(BlobInfo.newBuilder(bucketName, fileName).build()))
        } returns InMemoryStorageWriteChannel.inMemoryChannel()

        // when
        val code = okHttpClient.newCall(
            Request.Builder()
                .put("content".toRequestBody())
                .url("$appHost$fileName")
                .build()
        ).execute().use {
            it.code
        }

        // then
        assertEquals(200, code)
    }

    @Test
    fun shouldReadFromStorage() {
        // given
        val fileName = "/maven/com/raymank26/0.0.1/file.txt"
        val byteArray = byteArrayOf(1, 2, 3)
        every {
            gcpStorage.get(eq(BlobId.of(bucketName, fileName)))
        } returns mockk {
            every {
                etag
            } returns "123"
            every {
                size
            } returns byteArray.size.toLong()
        }
        every {
            gcpStorage.reader(eq(BlobId.of(bucketName, fileName)))
        } returns InMemoryStorageReadChannel.inMemoryReadChannel(byteArray)

        // when
        val (code, body) = okHttpClient.newCall(
            Request.Builder()
                .get()
                .url("$appHost$fileName")
                .build()
        ).execute().use {
            it.code to it.body!!.bytes()
        }

        // then
        assertEquals(200, code)
        assertContentEquals(byteArray, body)
    }

    @Test
    fun shouldReceive404IfNotFound() {
        // given
        val fileName = "/maven/com/raymank26/0.0.1/file.txt"
        every {
            gcpStorage.get(any<BlobId>())
        } throws IOException(StorageException(404, "Not found"))

        // when
        val code = okHttpClient.newCall(
            Request.Builder()
                .get()
                .url("$appHost$fileName")
                .build()
        ).execute().use {
            it.code
        }

        // then
        assertEquals(404, code)
    }
}