plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.kover)
    `java-library`
    `maven-publish`
}

group = "edu.jhu.cobra"
version = "0.1.0"

val sourceJavaVersion = JavaVersion.toVersion(libs.versions.javaSource.get())
val targetJavaVersion = JavaVersion.toVersion(libs.versions.javaTarget.get())

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    api(libs.cobra.commons.graph)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of(sourceJavaVersion.majorVersion)) }
    compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(targetJavaVersion.toString()) }
}

java {
    sourceCompatibility = sourceJavaVersion
    targetCompatibility = targetJavaVersion
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications { create<MavenPublication>("maven") { from(components["java"]) } }
}

