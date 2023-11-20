import com.google.cloud.storage.Storage
import com.raymank26.mavenor.ExternalDepsFactory
import software.amazon.awssdk.services.s3.S3Client

class MockExternalDepsFactory(
    private val gcpStorage: Storage,
    private val s3Client: S3Client,
    private val env: Map<String, String>
) : ExternalDepsFactory() {

    override fun gcpStorageClient(): Storage {
        return gcpStorage
    }

    override fun awsStorageClient(): S3Client {
        return s3Client;
    }

    override fun getEnv(): Map<String, String> {
        return env
    }
}