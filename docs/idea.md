# COBRA Framework -- Concepts & Terminology

## 1. Context

**Problem Statement** -- Static analysis systems require interpreting heterogeneous AST node types, each demanding specialized processing logic. Without a structured dispatch mechanism, node handlers become tightly coupled to the traversal engine, making the system rigid and difficult to extend. A common abstraction is needed to decouple task identification from task execution.

**System Role** -- The framework library provides the foundational Interpreter pattern template that all COBRA analysis modules build upon for task dispatch and worker management.

**Data Flow**
- **Inputs:** Tasks (typed units of work with identifiers) from analysis modules.
- **Outputs:** Products (typed results) emitted by workers via Kotlin coroutine flows.
- **Connections:** [Analysis module] -> [Dispatcher] -> [Worker] -> [Product collector]

**Scope Boundaries**
- **Owned:** Task identification, worker licensing, worker dispatch, workshop discovery.
- **Not Owned:** Concrete task definitions, concrete product definitions, AST structures, analysis algorithms, graph construction.

## 2. Concepts

**Conceptual Diagram**
```
                     registers
  Workshop -----------------------> Dispatcher
    |  discovers via reflection         |
    |  @WorkLicense annotations         | dispatch(taskID)
    v                                   v
  Worker <--- ITask.ID matching --- Worker
    |
    | suspend FlowCollector.work(task)
    v
  Product (emitted via flow)
```

**Core Concepts**

- **Name:** Task
- **Definition:** A typed unit of work carrying an identifier that determines which worker handles it. Represents a single processing request within the interpreter pipeline.
- **Scope:** Includes the task identifier (license + properties). Excludes the processing logic itself.
- **Relationships:** Consumed by Worker. Identified by Task ID. Created by analysis modules.

---

- **Name:** Task ID
- **Definition:** A composite key consisting of a license string and a set of property strings. Serves as the dispatch key linking tasks to workers.
- **Scope:** Includes license name and property set. Excludes task payload data.
- **Relationships:** Embedded in Task. Used by Dispatcher for lookup. Generated from WorkLicense annotations.

---

- **Name:** Product
- **Definition:** A marker type representing output produced by a worker after processing a task. Workers emit zero or more products per task invocation.
- **Scope:** Includes any typed result. Excludes intermediate computation state.
- **Relationships:** Emitted by Worker. Collected by the caller via Kotlin Flow.

---

- **Name:** Worker
- **Definition:** A processing unit that receives a task and emits products through a coroutine flow collector. Each worker handles tasks matching a specific Task ID.
- **Scope:** Includes the `work` function and its flow-based emission. Excludes task routing and discovery.
- **Relationships:** Registered in Dispatcher. Discovered by Workshop. Parameterized by Task and Product types.

---

- **Name:** Dispatcher
- **Definition:** A registry that maps Task IDs to workers and resolves which worker handles a given task at runtime.
- **Scope:** Includes registration and lookup. Excludes worker lifecycle and task creation.
- **Relationships:** Populated by Workshop. Queried by analysis modules. Contains Workers.

---

- **Name:** Workshop
- **Definition:** An abstract container that groups related workers and exposes them via reflective discovery of WorkLicense annotations. Workers within a workshop share resources through their enclosing instance.
- **Scope:** Includes annotation-based discovery and worker grouping. Excludes dispatch logic.
- **Relationships:** Populates Dispatcher. Contains Workers. Uses WorkLicense for discovery.

---

- **Name:** WorkLicense
- **Definition:** A meta-annotation applied to custom annotation classes. Marks worker properties for reflective discovery and maps them to Task IDs derived from the custom annotation's class name and constructor parameters.
- **Scope:** Includes annotation metadata and Task ID generation. Excludes runtime permission enforcement.
- **Relationships:** Applied to custom annotations. Read by Workshop. Produces Task IDs.

## 3. Contracts & Flow

**Data Contracts**
- **With analysis modules:** Tasks implement `ITask` with a `uid: ITask.ID`. Products implement `IProduct`. Modules create tasks, register workers via Dispatcher, and collect products from flows.
- **With Kotlin Coroutines:** Workers emit products through `FlowCollector<R>`, integrating with `kotlinx.coroutines.flow`.

**Internal Processing Flow**
1. **Define** -- Developer creates custom WorkLicense annotations, Task types, Product types, and Worker implementations.
2. **Group** -- Developer places licensed workers into a Workshop subclass, annotating each with the custom WorkLicense annotation.
3. **Discover** -- Workshop reflects over its own properties to find WorkLicense-annotated workers and maps each to a Task ID.
4. **Register** -- Discovered worker-to-TaskID mappings are registered into a Dispatcher.
5. **Dispatch** -- At runtime, analysis code looks up a worker by Task ID through the Dispatcher.
6. **Execute** -- The resolved worker's `work` function runs within a coroutine flow, emitting products to the collector.

## 4. Scenarios

- **Typical:** An AST interpreter creates a Workshop with workers for each PHP node type (e.g., `ExprWorker`, `StmtWorker`). Each worker is annotated with a node-specific license. The workshop discovers all licensed workers, registers them in a dispatcher, and the interpreter dispatches tasks by node type during traversal.

- **Boundary:** A Workshop subclass contains workers but none carry a WorkLicense annotation. The `licensedWorkers()` call returns an empty map. No workers register in the Dispatcher. Dispatch calls return null.

- **Interaction:** Two Workshop subclasses (e.g., hoisting phase and interpretation phase) each register workers into the same Dispatcher under non-overlapping Task IDs. The interpreter dispatches to the correct worker regardless of which workshop provided it. Duplicate Task IDs across workshops cause the later registration to overwrite the earlier one.

See [design.md](design.md) for type specifications and method signatures.
