# About

This project provides a Maven proxy for GCP Cloud Storage.

# Run

Use the following command to build an image:

```sh
./gradlew dockerBuildImage # you can optionally pass a version using -Pversion=<version>
docker run mavenor:latest # or docker run mavenor:<version>. Also pass env variables listed below.
```

**Environment variables:**

1. `GOOGLE_SERVICE_ACCOUNT_KEY` – This variable holds the JSON key file of a GCP service account.
2. `GOOGLE_APPLICATION_CREDENTIALS` – This variable specifies the name of a file where the key is written.
3. `GOOGLE_CLOUD_STORAGE_BUCKET_NAME` – This variable specifies the bucket name for Google Cloud Storage.
4. `USERNAME` – This variable is used for the Maven username.
5. `PASSWORD` – This variable is used for the Maven password.