import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
    maven
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

group = "eu.jrie.jetbrains"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    api("org.zeroturnaround:zt-exec:1.11")
    api("org.slf4j:slf4j-api:1.7.26")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M2")
    api("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.11")

    testImplementation(kotlin("reflect"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.0")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.apache.logging.log4j:log4j-core:2.12.0")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.12.0")
}

sourceSets {
    create("integration") {
        withConvention(KotlinSourceSet::class) {
            kotlin.srcDir("src/integration/kotlin")
            resources.srcDir("src/integration/resources")
            compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
            runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
        }
    }
}

task<Test>("integration") {
    description = "Runs the integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integration"].output.classesDirs
    classpath = sourceSets["integration"].runtimeClasspath
    mustRunAfter(tasks["test"])
    useJUnitPlatform()
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}


tasks.shadowJar {
    archiveName = "kotlin-shell-extension.jar"
    minimize {
        exclude(dependency("org.scala-lang:.*:.*"))
    }
}
