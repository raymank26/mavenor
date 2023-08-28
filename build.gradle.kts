plugins {
    kotlin("jvm") version "1.9.0"
    id("application")
}

group = "com.mavenor"
version = "1.0-SNAPSHOT"

application {
    mainClass = "com.raymank26.mavenor.App"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.google.cloud:libraries-bom:26.22.0"))
    implementation("com.google.cloud:google-cloud-storage")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.javalin:javalin:5.6.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

tasks.create("dockerPrepareImage", Copy::class) {
    dependsOn("installDist")
    from("$buildDir/install", "Dockerfile")
    include("*/**")
    into("$buildDir/docker")
}

tasks.create("dockerBuildImage", Exec::class) {
    dependsOn("dockerPrepareImage")
    workingDir("$buildDir/docker")
    commandLine("docker", "build", "-t", "mavenor:$version", "-t", "mavenor:latest", ".")
}