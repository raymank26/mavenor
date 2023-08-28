package com.raymank26.mavenor

import io.javalin.Javalin
import org.slf4j.LoggerFactory
import java.io.FileWriter

private val log = LoggerFactory.getLogger(App::class.java)


class App(private val externalDepsFactory: ExternalDepsFactory) {

    private lateinit var javalin: Javalin
    private lateinit var remoteStorage: RemoteStorage

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            App(externalDepsFactory = ExternalDepsFactory()).start()
        }
    }

    fun start() {
        val env = externalDepsFactory.getEnv()
        loadEnvGCloudData(env)
        val gcpBucketName = readEnv(env, "GOOGLE_CLOUD_STORAGE_BUCKET_NAME")
        val username = readEnv(env, "USERNAME")
        val password = readEnv(env, "PASSWORD")
        val maxCacheSizeBytes = env["MAX_CACHE_SIZE_BYTES"]?.toLong() ?: (50 * 1024)
        remoteStorage = RemoteStorage(gcpBucketName, externalDepsFactory.gcpStorage(), maxCacheSizeBytes)

        javalin = Javalin.create(/*config*/)
//            .before {
//                log.info(it.headerMap().toString())
//                log.info("{} - {}", it.method(), it.path())
//            }
            .before { ctx ->
                val basicAuthCredentials = ctx.basicAuthCredentials() ?: throw NotAuthorizedException()
                if (basicAuthCredentials.username != username || basicAuthCredentials.password != password) {
                    throw NotAuthorizedException()
                }
            }
            .exception(ObjectNotFound::class.java) { e, ctx ->
                log.trace("Not found", e)
                ctx.status(404)
            }
            .exception(NotAuthorizedException::class.java) { _, ctx ->
                ctx.status(401)
                ctx.header("WWW-Authenticate", "Basic")
            }
            .put("maven/*") { ctx ->
                val bodyInputStream = ctx.bodyInputStream()
                remoteStorage.write(ctx.path(), bodyInputStream)
            }
            .get("maven/*") { ctx ->
                remoteStorage.read(ctx.path(), ctx.outputStream())
                ctx.outputStream().close()
            }
            .get("/healthcheck") {
                it.result("ok")
            }
            .start(8080)
    }

    fun stop() {
        javalin.stop()
        remoteStorage.stop()
    }
}

fun loadEnvGCloudData(env: Map<String, String>) {
    val serviceAccountJson = env["GOOGLE_SERVICE_ACCOUNT_KEY"]
    if (serviceAccountJson != null) {
        log.info("Loading key of service account into file")
        val serviceAccountFile = env["GOOGLE_APPLICATION_CREDENTIALS"]!!
        FileWriter(serviceAccountFile).use {
            it.write(serviceAccountJson)
        }
        log.info("Completed loading key of service account into file")
    }
}

fun readEnv(env: Map<String, String>, name: String): String {
    return env[name] ?: error("No env variable found, key = $name")
}

class NotAuthorizedException : Exception()