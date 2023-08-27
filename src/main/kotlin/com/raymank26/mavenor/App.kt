package com.raymank26.mavenor

import io.javalin.Javalin
import org.slf4j.LoggerFactory
import java.io.FileWriter

private val log = LoggerFactory.getLogger(App::class.java)

object App {

    @JvmStatic
    fun main(args: Array<String>) {
        loadEnvGCloudData()
        val gcpBucketName = readEnv("GOOGLE_CLOUD_STORAGE_BUCKET_NAME")
        val remoteStorage = RemoteStorage(gcpBucketName)

        val app = Javalin.create(/*config*/)
            .before {
                log.info("{} - {}", it.method(), it.path())
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
            .start(7070)
    }
}

fun loadEnvGCloudData() {
    val serviceAccountJson = System.getenv()["GOOGLE_SERVICE_ACCOUNT_KEY"]
    if (serviceAccountJson != null) {
        log.info("Loading key of service account into file")
        val serviceAccountFile = System.getenv()["GOOGLE_APPLICATION_CREDENTIALS"]!!
        FileWriter(serviceAccountFile).use {
            it.write(serviceAccountJson)
        }
        log.info("Completed loading key of service account into file")
    }
}

fun readEnv(name: String): String {
    return System.getenv()[name] ?: error("No env variable found, key = $name")
}