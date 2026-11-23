import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `java-library`
    `maven-publish`
}

group = "edu.jhu.cobra"
version = libs.versions.projectVersion.get()

val minJavaProperty = providers.gradleProperty("minJava").get()
val minJava =
    minJavaProperty.toIntOrNull()?.takeIf { it >= 8 }
        ?: throw GradleException("minJava must be a Java version of 8 or later, got '$minJavaProperty'.")
val targetJavaVersion = JavaVersion.toVersion(minJava)
val runningJavaVersion = JavaVersion.current()
if (runningJavaVersion < targetJavaVersion) {
    throw GradleException(
        "minJava=$minJava requires JDK $minJava or later to run Gradle. " +
            "Gradle is running on JDK ${runningJavaVersion.majorVersion}.",
    )
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Re-exported on purpose: a consumer depends on framework alone and
    // receives the commons-graph API without a second import.
    api(libs.cobra.commons.graph)
    implementation(libs.kotlin.reflect)
    testImplementation(kotlin("test"))
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
        freeCompilerArgs.add("-Xjdk-release=$minJava")
    }
}

java {
    sourceCompatibility = targetJavaVersion
    targetCompatibility = targetJavaVersion
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(minJava)
}

ktlint {
    version.set(libs.versions.ktlintEngine.get())
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

detekt {
    // Resolve the repository's own config even when this module is embedded
    // as a subproject of an enclosing build.
    config.setFrom(files("${projectDir.parentFile}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
}

publishing {
    publications {
        create<MavenPublication>("maven") { from(components["java"]) }
    }
}
