/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.8.10"

}

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        url = uri("https://repo1.maven.org/maven2/")
    }

    maven {
        url = uri("https://m2.dv8tion.net/releases")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        url = uri("https://m2.dv8tion.net/releases")
    }

    maven {
        url = uri("https://jitpack.io")
    }

}

dependencies {
    api("org.apache.maven.plugins:maven-compiler-plugin:3.11.0")
    api("com.fasterxml.jackson.core:jackson-core:2.14.2")
    api("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    api("com.fasterxml.jackson.core:jackson-annotations:2.14.2")
    api("org.apache.maven.plugins:maven-failsafe-plugin:3.0.0")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.0") // get access to kotlin coroutines
    api("net.dv8tion:JDA:5.0.0-beta.6") // wrapper for discord api
    api("io.github.cdimascio:dotenv-java:2.3.1") // api to get configs
    api("com.github.origma:Google-PerspectiveAPI-Java-Client:0.0.5")
    api("com.sedmelluq:lavaplayer:1.3.78")
    implementation("org.yaml:snakeyaml:2.0")
    implementation("ch.qos.logback:logback-classic:1.4.6") // implementation to embellish logs
    implementation("org.jsoup:jsoup:1.15.4") // api for web scraping
    testImplementation("org.apache.maven.plugins:maven-surefire-plugin:3.0.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.7.20")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

group = "org.example"
version = "1.0-SNAPSHOT"
description = "Michi"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
