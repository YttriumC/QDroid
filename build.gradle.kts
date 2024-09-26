import java.net.URI

plugins {
    kotlin("jvm") version "2.0.20"
    application
}

group = "ng.i.sav.qdroid"
version = "0.0.1"

repositories {
    mavenCentral()
    maven { url = URI("https://repo.spring.io/milestone") }
    maven { url = URI("https://repo.spring.io/snapshot") }
}

dependencies {
    testImplementation(kotlin("test"))
    val springContext="org.springframework:spring-context:6.1.13"
    val springWebsocket="org.springframework:spring-websocket:6.1.13"
    // https://mvnrepository.com/artifact/org.springframework/spring-context
    testImplementation(springContext)
    compileOnly(springContext)
    // https://mvnrepository.com/artifact/org.springframework/spring-websocket
    testImplementation(springWebsocket)
    compileOnly(springWebsocket)

    implementation("org.springframework.ai:spring-ai-openai:1.0.0-M2") {
        exclude("org.springframework.boot", "*")
        exclude("org.springframework.cloud", "*")
    }
    // https://mvnrepository.com/artifact/jakarta.websocket/jakarta.websocket-client-api
    implementation("jakarta.websocket:jakarta.websocket-client-api:2.1.1")
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.16")
    // https://mvnrepository.com/artifact/jakarta.annotation/jakarta.annotation-api
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
//    // https://mvnrepository.com/artifact/io.quarkus/quarkus-websockets-client
//    implementation("io.quarkus:quarkus-websockets-client:3.5.0")
    // https://mvnrepository.com/artifact/io.quarkus.http/quarkus-http-websocket-core
    implementation("io.quarkus.http:quarkus-http-websocket-core:5.3.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
//    mainClass.set("MainKt")
}
