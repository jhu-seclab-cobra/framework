# COBRA.FRAMEWORK 

[![codecov](https://codecov.io/gh/jhu-seclab-cobra/framework/branch/main/graph/badge.svg)](https://codecov.io/gh/jhu-seclab-cobra/framework) 
![Kotlin JVM](https://img.shields.io/badge/Kotlin%20JVM-1.8%2B-blue?logo=kotlin) 
[![Release](https://img.shields.io/badge/release-v0.1.0-blue.svg)](https://github.com/jhu-seclab-cobra/framework/releases/tag/v0.1.0) 
[![last commit](https://img.shields.io/github/last-commit/jhu-seclab-cobra/framework)](https://github.com/jhu-seclab-cobra/framework/commits/main) 
[![JitPack](https://jitpack.io/v/jhu-seclab-cobra/framework.svg)](https://jitpack.io/#jhu-seclab-cobra/framework) 
![Repo Size](https://img.shields.io/github/repo-size/jhu-seclab-cobra/framework) 
[![license](https://img.shields.io/github/license/jhu-seclab-cobra/framework)](./LICENSE)

A core abstraction layer of the COBRA architecture that provides a template implementation of the Interpreter pattern. This framework serves as a foundational design pattern template that can be extended and specialized for various use cases, with AST processing being one of its primary applications. The framework offers a flexible and extensible architecture for implementing the Interpreter pattern, with built-in support for concurrent processing and a robust licensing system.

---

## Features

- Core abstraction layer for the COBRA architecture
- Template implementation of the Interpreter pattern
- Flexible and extensible design pattern framework
- Type-safe task and result handling
- Thread-safe and concurrent processing
- Comprehensive licensing system
- Extensible handler and dispatcher interfaces
- Built-in test utilities and annotations

---

## Module Overview

- **framework** The core abstraction layer that provides a template implementation of the Interpreter pattern. It defines the fundamental interfaces (`IWorker` for handlers, `IDispatcher` for task distribution, `ITask` for tasks), abstract implementations (`AbcWorkshop` for handler management), and the core architecture for implementing the Interpreter pattern. While AST processing is a primary use case, the framework is designed to be adaptable to various interpretation scenarios.

**Design Notes:**
- The framework provides a template implementation of the Interpreter pattern that can be specialized for different use cases
- The dispatcher (`IDispatcher`) implements the core routing logic of the Interpreter pattern
- Handlers (`IWorker`) provide the template for implementing interpretation logic
- The workshop (`AbcWorkshop`) offers a template for managing the registration and lifecycle of handlers
- The licensing system provides a template for controlling handler authorization
- All core abstractions are designed to be extended and specialized for specific use cases
- The framework serves as a foundation for implementing various interpretation scenarios, with AST processing being one example

---

## Requirements

- Java 8 or higher

---

## Installation

### 1. Add JitPack repository

In your `build.gradle.kts`:
```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

### 2. Add the dependency

```kotlin
dependencies {
    implementation("com.github.jhu-seclab-cobra.framework:framework:VERSION")
}
```

Replace `VERSION` with the latest release version.

---

## Usage

### 1. Basic Handler Implementation

```kotlin
class MyHandler : IWorker<MyTask, MyResult> {
    override suspend fun FlowCollector<MyResult>.work(task: MyTask) {
        // Implement interpretation logic
        emit(MyResult(interpret(task)))
    }
}
```

### 2. Workshop Implementation

```kotlin
class MyWorkshop : AbcWorkshop<MyHandler>() {
    @WorkLicense("handler1")
    val handler1 = MyHandler()
    
    @WorkLicense("handler2")
    val handler2 = MyHandler()
}
```

### 3. Task Processing

```kotlin
val workshop = MyWorkshop()
val taskId = ITask.ID("TaskType", setOf("prop1", "prop2"))
val task = MyTask(taskId)

val result = mutableListOf<MyResult>()
with(workshop.handler1) {
    flow { work(task) }.collect { result.add(it) }
}
```

---

## Testing

Run all tests with:
```shell
./gradlew test
```

---

## License

[GNU General Public License v2.0](./LICENSE)

---

## Contributing

Contributions are welcome! Please open issues or submit pull requests for bug fixes, new features, or improvements.

---

## Acknowledgements

Part of the COBRA platform. For more information, see [COBRA Project](https://github.com/jhu-seclab-cobra).
