# framework

[![codecov](https://codecov.io/gh/jhu-seclab-cobra/framework/branch/main/graph/badge.svg)](https://codecov.io/gh/jhu-seclab-cobra/framework)
![Kotlin JVM](https://img.shields.io/badge/Kotlin%20JVM-2.4.20%20%7C%20JVM%2021%2B-blue?logo=kotlin)
[![Release](https://img.shields.io/badge/release-v0.1.0-blue.svg)](https://github.com/jhu-seclab-cobra/framework/releases/tag/v0.1.0)
[![last commit](https://img.shields.io/github/last-commit/jhu-seclab-cobra/framework)](https://github.com/jhu-seclab-cobra/framework/commits/main)
[![JitPack](https://jitpack.io/v/jhu-seclab-cobra/framework.svg)](https://jitpack.io/#jhu-seclab-cobra/framework)
![Repo Size](https://img.shields.io/github/repo-size/jhu-seclab-cobra/framework)
[![license](https://img.shields.io/github/license/jhu-seclab-cobra/framework)](./LICENSE)

> Named for the COBRA static analysis platform — the foundational dispatch layer that coordinates analysis workers.

Annotation-driven task dispatching and licensed worker management for Kotlin analysis and interpretation systems.

## Install

Use JDK 21 and a Gradle Kotlin/JVM build with JitPack configured:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.jhu-seclab-cobra:framework:0.1.0")
}
```

## Usage

```kotlin
import edu.jhu.cobra.framework.AbcDispatcher
import edu.jhu.cobra.framework.ITask
import edu.jhu.cobra.framework.IWorker

data class ExampleTask(override val uid: ITask.ID) : ITask
val task = ExampleTask(ITask.ID("example", emptySet()))
val dispatcher = AbcDispatcher<ExampleTask, Boolean>()
dispatcher.register(task.uid, IWorker { true })
check(checkNotNull(dispatcher.dispatch(task.uid)).work(task))
```

## API

[Core API](docs/llms/core.md) lists the exported Task, Worker, Workshop, Dispatcher and annotation signatures.

## Citation

It requests that every project using the library cite the project’s associated paper:

```bibtex
@inproceedings{xu2026cobra,
  title     = {CoBrA: Context-, Branch-sensitive Static Analysis for Detecting Taint-style Vulnerabilities in PHP Web Applications},
  author    = {Xu, Yichao and Kang, Mingqing and Thimmaiah, Neil and Gjomemo, Rigel and Venkatakrishnan, V. N. and Cao, Yinzhi},
  booktitle = {Proceedings of the 48th IEEE/ACM International Conference on Software Engineering (ICSE)},
  year      = {2026},
  address   = {Rio de Janeiro, Brazil}
}
```

## Documentation

- [Concepts and terminology](docs/concept.md) — Task, Worker, Dispatcher, Registration Mode, Workshop and license roles.
- [Design specification](docs/design.md) — class specifications, function specifications, exception types.

## For Agents

Agent-consumable documentation index at `docs/llms.txt` ([llmstxt.org](https://llmstxt.org) format).

## License

[GPL-2.0](LICENSE)
