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
    compile(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.zeroturnaround:zt-exec:1.10")
    implementation("org.apache.logging.log4j:log4j-core:2.12.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M2")

    testImplementation("junit:junit:4.12")
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "9"
}
