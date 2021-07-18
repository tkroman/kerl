plugins {
    java
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
