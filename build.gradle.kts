import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.40"
}

group = "eu.jrie.jetbrains"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.zeroturnaround:zt-exec:1.10")
    implementation( "org.slf4j:slf4j-api:1.7.26")
    implementation( "org.slf4j:slf4j-log4j12:1.7.26")

    implementation("io.reactivex.rxjava2:rxjava:2.2.10")
    implementation("io.reactivex.rxjava2:rxkotlin:2.3.0")

    testImplementation("junit:junit:4.12")
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "9"
}
