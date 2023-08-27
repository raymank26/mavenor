plugins {
    kotlin("jvm") version "1.9.0"
}

group = "com.mavenor"
version = "1.0-SNAPSHOT"

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