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
    val springContext = "org.springframework:spring-context:6.2.0"
    val springWebsocket = "org.springframework:spring-websocket:6.2.0"
    val springWebmvc = "org.springframework:spring-webmvc:6.2.0"
    val springWebFlux = "org.springframework:spring-webflux:6.2.0"
    // https://mvnrepository.com/artifact/org.springframework/spring-context
    testImplementation(springContext)
    compileOnly(springContext)
    // https://mvnrepository.com/artifact/org.springframework/spring-websocket
    testImplementation(springWebsocket)
    compileOnly(springWebsocket)
    // https://mvnrepository.com/artifact/org.springframework.data/spring-data-jpa
    implementation("org.springframework.data:spring-data-jpa:3.4.0")
    // https://mvnrepository.com/artifact/org.springframework/spring-webmvc
    testImplementation(springWebmvc)
    compileOnly(springWebmvc)
    // https://mvnrepository.com/artifact/org.springframework/spring-webflux
    testImplementation(springWebFlux)
    compileOnly(springWebFlux)


    // https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-core
    implementation("org.hibernate.orm:hibernate-core:6.6.2.Final")

    implementation("org.springframework.ai:spring-ai-openai:1.0.0-M3") {
        exclude("org.springframework.boot", "*")
        exclude("org.springframework.cloud", "*")
        exclude("org.springframework", "")
    }
    // https://mvnrepository.com/artifact/jakarta.websocket/jakarta.websocket-client-api
    implementation("jakarta.websocket:jakarta.websocket-client-api:2.2.0")
    // https://mvnrepository.com/artifact/jakarta.persistence/jakarta.persistence-api
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    // https://mvnrepository.com/artifact/jakarta.servlet/jakarta.servlet-api
    implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")


    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.16")
    // https://mvnrepository.com/artifact/jakarta.annotation/jakarta.annotation-api
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    // https://mvnrepository.com/artifact/io.quarkus.http/quarkus-http-websocket-core
//    implementation("io.quarkus.http:quarkus-http-websocket-core:5.3.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.h2database:h2:2.3.232")

    // https://mvnrepository.com/artifact/io.projectreactor.netty/reactor-netty
    implementation("io.projectreactor.netty:reactor-netty:1.2.0")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-reactor
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm")
    implementation(kotlin("reflect"))
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
