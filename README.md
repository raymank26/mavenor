# About

This project provides a Maven proxy for GCP Cloud Storage.

# Run

Use the following command to build an image:

```sh
./gradlew dockerBuildImage # you can optionally pass a version using -Pversion=<version>
docker run mavenor:latest # or docker run mavenor:<version>. Also pass env variables listed below.
```

After the launch, the app will listen on `8080` port.

**Environment variables:**

1. `GOOGLE_SERVICE_ACCOUNT_KEY` – This variable holds the JSON key file of a GCP service account.
2. `GOOGLE_APPLICATION_CREDENTIALS` – This variable specifies the name of a file where the key is written.
3. `GOOGLE_CLOUD_STORAGE_BUCKET_NAME` – This variable specifies the bucket name for Google Cloud Storage.
4. `USERNAME` – This variable is used for the Maven username.
5. `PASSWORD` – This variable is used for the Maven password.

# Maven configuration

```
publishing {
    repositories {
        maven {
            url "http://localhost:8080/maven" // replace with real url
            allowInsecureProtocol true // <- only for HTTP testing
        }
    }
}

// and for consumption
repositories {
    mavenCentral()
    maven {
        url = uri("http://localhost:8080/maven") // replace with real url
        setAllowInsecureProtocol(true) // <- only for HTTP testing
        credentials {
            username = "<username>"
            password = "<password>"
        }
    }
}
```