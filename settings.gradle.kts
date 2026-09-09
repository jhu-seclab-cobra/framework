plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "framework"

// No COBRA sibling dependencies: the framework depends only on the Kotlin
// standard library and kotlin-reflect.

include("jhu-seclab-cobra-framework")
project(":jhu-seclab-cobra-framework").projectDir = file("framework")
