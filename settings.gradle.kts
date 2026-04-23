plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "framework"

// Include transitive dependencies from submodules when initialized.
// commons-graph depends on commons-value; both exposed via api() to consumers.
if (file("extern/commons-graph/settings.gradle.kts").exists()
    && file("extern/commons-graph/extern/commons-value/settings.gradle.kts").exists()
) {
    includeBuild("extern/commons-graph/extern/commons-value") {
        name = "commons-value"
        dependencySubstitution {
            substitute(module("com.github.jhu-seclab-cobra:commons-value")).using(project(":jhu-seclab-cobra-commons-value"))
        }
    }
    includeBuild("extern/commons-graph") {
        name = "commons-graph"
        dependencySubstitution {
            substitute(module("com.github.jhu-seclab-cobra.commons-graph:graph")).using(project(":jhu-seclab-cobra-commons-graph"))
        }
    }
}

include("jhu-seclab-cobra-framework")
project(":jhu-seclab-cobra-framework").projectDir = file("framework")
