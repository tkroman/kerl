plugins {
    java
    jacoco
    kotlin("jvm") version "1.5.10"
}

group = "com.tkroman"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.tkroman:encon:1.6.17")
    implementation("io.appulse.epmd.java:server:2.0.2")
    implementation("org.slf4j:slf4j-api:1.7.30")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.awaitility:awaitility-kotlin:4.1.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
        )
        jvmTarget = "1.8"
        languageVersion = "1.5"
        apiVersion = "1.5"
    }
}

jacoco.toolVersion = "0.8.7"

tasks.withType<JacocoReport> {
    executionData(
        fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec")
    )
    reports {
        xml.isEnabled = true
        html.isEnabled = false
        csv.isEnabled = false
        xml.destination = file("${buildDir}/reports/jacoco/report.xml")
    }
}
