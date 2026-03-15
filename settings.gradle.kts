plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "framework"
include("framework")

if (file("extern/commons-graph").exists()) {
    includeBuild("extern/commons-graph/extern/commons-value") {
        name = "commons-value"
        dependencySubstitution {
            substitute(module("com.github.jhu-seclab-cobra:commons-value")).using(project(":lib"))
        }
    }
    includeBuild("extern/commons-graph") {
        name = "commons-graph"
        dependencySubstitution {
            substitute(module("com.github.jhu-seclab-cobra.commons-graph:graph")).using(project(":graph"))
        }
    }
}
