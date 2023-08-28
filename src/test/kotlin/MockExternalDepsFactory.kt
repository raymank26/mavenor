import com.google.cloud.storage.Storage
import com.raymank26.mavenor.ExternalDepsFactory

class MockExternalDepsFactory(
    private val gcpStorage: Storage,
    private val env: Map<String, String>
) : ExternalDepsFactory() {

    override fun gcpStorage(): Storage {
        return gcpStorage
    }

    override fun getEnv(): Map<String, String> {
        return env
    }
}