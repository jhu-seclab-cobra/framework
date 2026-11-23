rootProject.name = "framework"

// No vendored checkouts. Standalone builds resolve commons-graph and
// commons-value from JitPack at the versions in gradle/libs.versions.toml.
// Under the CobraPHP composite, the root substitutes them with its own
// vendored builds.

include("jhu-seclab-cobra-framework")
project(":jhu-seclab-cobra-framework").projectDir = file("framework")
