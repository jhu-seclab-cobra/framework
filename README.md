# COBRA.FRAMEWORK 

[![codecov](https://codecov.io/gh/jhu-seclab-cobra/framework/branch/main/graph/badge.svg)](https://codecov.io/gh/jhu-seclab-cobra/framework) 
![Kotlin JVM](https://img.shields.io/badge/Kotlin%20JVM-1.8%2B-blue?logo=kotlin) 
[![Release](https://img.shields.io/badge/release-v0.1.0-blue.svg)](https://github.com/jhu-seclab-cobra/framework/releases/tag/v0.1.0) 
[![last commit](https://img.shields.io/github/last-commit/jhu-seclab-cobra/framework)](https://github.com/jhu-seclab-cobra/framework/commits/main) 
[![JitPack](https://jitpack.io/v/jhu-seclab-cobra/framework.svg)](https://jitpack.io/#jhu-seclab-cobra/framework) 
![Repo Size](https://img.shields.io/github/repo-size/jhu-seclab-cobra/framework) 
[![license](https://img.shields.io/github/license/jhu-seclab-cobra/framework)](./LICENSE)

A core abstraction layer of the COBRA architecture that provides a template implementation of the Interpreter pattern. This framework serves as a foundational design pattern template that can be extended and specialized for various use cases, with AST processing being one of its primary applications.

## Features

- **Core Abstraction Layer**
  - Template implementation of the Interpreter pattern
  - Extensible handler and dispatcher interfaces
  - Type-safe task and result handling
  - Comprehensive licensing mechanism

- **Task Processing Framework**
  - Thread-safe and concurrent processing
  - Dynamic task distribution system
  - Flexible handler registration
  - Built-in error handling

- **Development Support**
  - Built-in test utilities and annotations
  - Comprehensive documentation
  - Example implementations
  - Integration guides

## Requirements

- JavaUser: Java 8 or higher
- KotlinUser: Kotlin 1.8 or higher

## Installation

### 1. Add JitPack Repository

In your `build.gradle.kts`:
```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

### 2. Add Dependency

```kotlin
dependencies {
    implementation("com.github.jhu-seclab-cobra:framework:0.1.0")
}
```

## Quick Start

### 1. Define AST Node Structure

```kotlin
// Simple AST Node interface
interface AstNode {
    val nodeType: String
}

// Basic node implementations
data class BinaryExpressionNode(val operator: String) : AstNode {
    override val nodeType: String = "BinaryExpression"
}

data class LiteralNode(val value: String) : AstNode {
    override val nodeType: String = "Literal"
}

data class VariableNode(val name: String) : AstNode {
    override val nodeType: String = "Variable"
}
```

### 2. Define Task and Result Types

```kotlin
data class AstTask(
    override val uid: ITask.ID,
    val node: AstNode
) : ITask

data class AstResult(val value: String) : IProduct
```

### 3. Implement Workers and Dispatcher

```kotlin
// Simple workers using SAM pattern
val binaryWorker = IWorker<AstTask, AstResult> { task ->
    emit(AstResult("processed_binary"))
}

val literalWorker = IWorker<AstTask, AstResult> { task ->
    emit(AstResult("processed_literal"))
}

val variableWorker = IWorker<AstTask, AstResult> { task ->
    emit(AstResult("processed_variable"))
}

// Simple dispatcher
class AstDispatcher : IDispatcher<IWorker<AstTask, AstResult>> {
    private val workers = mutableMapOf<ITask.ID, IWorker<AstTask, AstResult>>()
    
    override fun dispatch(forTask: ITask.ID) = workers[forTask]
    override fun register(forTask: ITask.ID, toWorker: IWorker<AstTask, AstResult>) {
        workers[forTask] = toWorker
    }
}
```

### 4. Create Workshop

```kotlin
class AstWorkshop : AbcWorkshop<IWorker<AstTask, AstResult>>() {
    @WorkLicense("BinaryExpression")
    val binaryWorker = binaryWorker
    
    @WorkLicense("Literal")
    val literalWorker = literalWorker
    
    @WorkLicense("Variable")
    val variableWorker = variableWorker
}
```

### 5. Use the Framework

```kotlin
// Create task
val task = AstTask(
    uid = ITask.ID("BinaryExpression", setOf("math")),
    node = BinaryExpressionNode("+")
)

// Setup framework components
val workshop = AstWorkshop()
val dispatcher = AstDispatcher()

// Register workers
workshop.licensedWorkers().forEach { (taskId, worker) ->
    dispatcher.register(taskId, worker)
}

// Process task
val worker = dispatcher.dispatch(task.uid)!!
val result = mutableListOf<AstResult>()
with(worker) {
    flow { work(task) }.collect { result.add(it) }
}

println("Result: ${result.first().value}")
```

## Core Components

### Interfaces

- `IWorker<T : ITask, R : IProduct>`: Task processing interface
- `IDispatcher<W : IWorker<*, *>>`: Task distribution interface
- `ITask`: Task definition interface
- `IProduct`: Result marker interface

### Implementations

- `AbcWorkshop<W : IWorker<*, *>>`: Abstract workshop implementation
- `@WorkLicense`: Handler authorization annotation

## Testing

Run all tests with:
```shell
./gradlew test
```

## License

[GNU General Public License v2.0](./LICENSE)

## Contributing

Contributions are welcome! Please open issues or submit pull requests for:
- Bug fixes
- New features
- Documentation improvements
- Test coverage enhancements

## Acknowledgements

Part of the COBRA platform. For more information, see [COBRA Project](https://github.com/jhu-seclab-cobra).
