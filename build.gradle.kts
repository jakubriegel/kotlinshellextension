import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50-eap-54"
    maven
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
    id("org.jetbrains.dokka") version "0.9.17"
}

group = "eu.jrie.jetbrains"
version = "0.2"

repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
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

val integration by tasks.creating(Test::class) {
    description = "Runs the integration tests"
    group = JavaBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets["integration"].output.classesDirs
    classpath = sourceSets["integration"].runtimeClasspath
    mustRunAfter(tasks.test)
    useJUnitPlatform()
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    classifier = "javadoc"
    description = "Assembles Kotlin docs with Dokka"
    from(tasks.dokka)
}

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<Test> {
        useJUnitPlatform()
    }

    check { dependsOn(integration) }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
        noStdlibLink = true
    }
}

artifacts {
    archives(sourcesJar)
    archives(dokkaJar)
    archives(tasks.jar)
}

val bintrayPublication = "kse"

publishing {
    publications {
        create<MavenPublication>(bintrayPublication) {
            from(components["kotlin"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            setArtifacts(listOf(sourcesJar, dokkaJar, tasks.jar.get()))
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    setPublications(bintrayPublication)
    publish = true
    pkg (delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "KotlinShell"
        name = "kotlin-shellextension"
        userOrg = "jakubriegel"
        websiteUrl = ""
        githubRepo = "jakubriegel/kotlinshellextension"
        vcsUrl = "https://github.com/jakubriegel/kotlinshellextension.git"
        description = "Library for performing shell-like programing in Kotlin. Includes process management and piping."
        setLabels("kotlin", "shell", "pipeline", "process-management")
        setLicenses("apache2")
        desc = description
    })
}
