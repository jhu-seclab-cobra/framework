# COBRA Framework

> Named for the COBRA static analysis platform — the foundational dispatch layer that coordinates analysis workers.

Annotation-driven task dispatching and licensed worker management for Kotlin analysis and interpretation systems.

[![codecov](https://codecov.io/gh/jhu-seclab-cobra/framework/branch/main/graph/badge.svg)](https://codecov.io/gh/jhu-seclab-cobra/framework)
![Kotlin JVM](https://img.shields.io/badge/Kotlin%20JVM-1.8%2B-blue?logo=kotlin)
[![JitPack](https://jitpack.io/v/jhu-seclab-cobra/framework.svg)](https://jitpack.io/#jhu-seclab-cobra/framework)
[![license](https://img.shields.io/github/license/jhu-seclab-cobra/framework)](./LICENSE)

## Install

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
// 1. Define a license annotation
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@WorkLicense
annotation class NodeLicense(val type: String)

// 2. Group workers in a workshop
class ExprWorkshop : AbcWorkshop<IWorker<ExprTask, ExprResult>>() {
    @NodeLicense("BinaryExpr")
    val binary = IWorker<ExprTask, ExprResult> { task -> eval(task.node) }
}

// 3. Discover and dispatch
val workers = ExprWorkshop().licensedWorkers()
dispatcher.register(workers)
val worker = dispatcher.dispatch(task.uid)
```

## API

**`IWorker<T : ITask, R>`** — Performs a task, returns result via `fun work(task: T): R`. Synchronous `fun interface`.

**`IDispatcher<W>`** — `dispatch(forTask: ITask.ID): W?` and `register(forTask: ITask.ID, toWorker: W)`.

**`AbcWorkshop<W>`** — `licensedWorkers(): Map<ITask.ID, W>` discovers `@WorkLicense`-annotated properties via reflection.

**`@WorkLicense`** — Meta-annotation applied to custom annotation classes. Properties of the custom annotation become part of `ITask.ID`.

**`ITask.ID(license: String, props: Set<String>)`** — Task identifier. Constructed via `WorkLicense.getTaskID()`.

## Documentation

- [Concepts and terminology](docs/idea.md) — interpreter pattern, workshop model, licensing mechanism.
- [Design specification](docs/design.md) — class specifications, validation rules, exception types.

## For Agents

Agent-consumable documentation index at `docs/llms.txt` ([llmstxt.org](https://llmstxt.org) format).

## Citation

If you use this repository in your research, please cite our paper:

```bibtex
@inproceedings{xu2026cobra,
  title     = {CoBrA: Context-, Branch-sensitive Static Analysis for Detecting Taint-style Vulnerabilities in PHP Web Applications},
  author    = {Xu, Yichao and Kang, Mingqing and Thimmaiah, Neil and Gjomemo, Rigel and Venkatakrishnan, V. N. and Cao, Yinzhi},
  booktitle = {Proceedings of the 48th IEEE/ACM International Conference on Software Engineering (ICSE)},
  year      = {2026},
  address   = {Rio de Janeiro, Brazil}
}
```

## License

GPL-2.0
