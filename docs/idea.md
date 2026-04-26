# COBRA Framework -- Concepts & Terminology

## 1. Context

**Problem Statement**
Static analysis and interpretation systems require dispatching heterogeneous node types to specialized handlers. Without a structured dispatch mechanism, handlers become coupled to the traversal engine. A common abstraction decouples task identification from task execution.

**System Role**
The framework library provides task dispatch, worker registration, and annotation-based discovery for all COBRA analysis modules.

**Data Flow**
- **Inputs:** Tasks (typed units of work with identifiers) from analysis modules.
- **Outputs:** Results returned by workers.
- **Connections:** [Analysis module] → [Dispatcher] → [Worker] → [Result]

**Scope Boundaries**
- **Owned:** Task identification, worker licensing, worker dispatch, workshop discovery.
- **Not Owned:** Concrete task/result definitions, AST structures, analysis algorithms, graph construction, traversal strategy (worklist, recursive, etc.).

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
    | work(task): R
    v
  Result (returned directly)
```

**Core Concepts**

- **Name:** Task
- **Definition:** A typed unit of work carrying an identifier that determines which worker handles it.
- **Scope:** Includes the task identifier (license + properties) and task payload. Excludes processing logic.
- **Relationships:** Consumed by Worker. Identified by Task ID.

- **Name:** Task ID
- **Definition:** A composite key consisting of a license string and a set of property strings. Serves as the dispatch key linking tasks to workers.
- **Scope:** Includes license name and property set. Excludes task payload data.
- **Relationships:** Embedded in Task. Used by Dispatcher for lookup. Generated from WorkLicense annotations.

- **Name:** Worker
- **Definition:** A processing unit that receives a task and returns a result. Synchronous, pure function. Each worker handles tasks matching a specific Task ID.
- **Scope:** Includes the `work` function. Excludes task routing, discovery, and traversal.
- **Relationships:** Registered in Dispatcher. Discovered by Workshop. Parameterized by Task and Result types.

- **Name:** Dispatcher
- **Definition:** A registry mapping Task IDs to workers. Resolves which worker handles a given task at runtime.
- **Scope:** Includes registration and lookup. Excludes worker lifecycle and task creation.
- **Relationships:** Populated by Workshop. Queried by analysis modules.

- **Name:** Workshop
- **Definition:** An abstract container grouping related workers. Exposes them via reflective discovery of WorkLicense annotations. Workers within a workshop share resources through their enclosing instance.
- **Scope:** Includes annotation-based discovery and worker grouping. Excludes dispatch logic and traversal.
- **Relationships:** Populates Dispatcher. Contains Workers. Uses WorkLicense for discovery.

- **Name:** WorkLicense
- **Definition:** A meta-annotation applied to custom annotation classes. Marks worker properties for reflective discovery. Maps them to Task IDs derived from the annotation's class name and constructor parameters.
- **Scope:** Includes annotation metadata and Task ID generation. Excludes runtime permission enforcement.
- **Relationships:** Applied to custom annotations. Read by Workshop. Produces Task IDs.

## 3. Contracts & Flow

**Data Contracts**
- **With analysis modules:** Tasks implement `ITask` with `uid: ITask.ID`. Workers implement `IWorker<T, R>`. Modules create tasks, register workers via Dispatcher, and receive results.
- **With traversal engines:** Framework provides dispatch. Traversal strategy (worklist, recursive, etc.) is owned by the analysis module, not the framework. Workers are synchronous — concurrency managed by the caller.

**Internal Processing Flow**
1. **Define** — Custom WorkLicense annotations, Task types, Result types, Worker implementations.
2. **Group** — Licensed workers placed into Workshop subclass, annotated with custom WorkLicense annotation.
3. **Discover** — Workshop reflects over properties to find WorkLicense-annotated workers, maps each to Task ID.
4. **Register** — Worker-to-TaskID mappings registered into Dispatcher.
5. **Dispatch** — Analysis code looks up worker by Task ID through Dispatcher.
6. **Execute** — Worker's `work(task)` called synchronously, returns result.

## 4. Scenarios

- **Typical:** A worklist-driven analyzer creates a Workshop with transfer functions for each AST node type. Each is annotated with a node-specific license. Workshop discovers all licensed workers, registers them in a dispatcher. Worklist driver dispatches by node type during traversal.

- **Boundary:** A Workshop subclass contains workers but none carry a WorkLicense annotation. `licensedWorkers()` returns empty map. No workers register. Dispatch returns null.

- **Interaction:** Two Workshop subclasses (e.g., expression handlers and statement handlers) register workers into the same Dispatcher under non-overlapping Task IDs. The driver dispatches to the correct worker regardless of which workshop provided it. Duplicate Task IDs: later registration overwrites earlier.

See [design.md](design.md) for type specifications and method signatures.
