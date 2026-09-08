# COBRA Framework -- Concepts & Terminology

## 1. Context

**Problem Statement**
Static analysis and interpretation systems dispatch heterogeneous node types to specialized handlers. Without a structured dispatch mechanism, handlers become coupled to the traversal engine. A common abstraction decouples task identification from task execution.

**System Role**
The framework library provides task dispatch, worker registration, and annotation-based discovery for all COBRA analysis modules.

**Data Flow**
- **Inputs:** Tasks (typed units of work with identifiers) from analysis modules.
- **Outputs:** Results returned by workers.
- **Connections:** [Analysis module] → [Dispatcher] → [Worker] → [Result]

**Scope Boundaries**
- **Owned:** Task identification, worker licensing, worker dispatch, registration modes, workshop discovery.
- **Not Owned:** Concrete task/result definitions, AST structures, analysis algorithms, graph construction, traversal strategy (worklist, recursive, etc.), concurrency.

## 2. Concepts

**Conceptual Diagram**
```
  Workshop ---- discovers licensed workers ----> (Task ID, Worker, Registration Mode)
      |                                                       |
      | registers                                             v
      v                                                  Dispatcher
  Dispatcher <---- looks up worker by Task ID ---- Analysis module
      |
      | hands the matching Worker
      v
  Worker ---- executes Task ----> Result
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
- **Definition:** A processing unit that receives a task and returns a result synchronously. Each worker handles tasks matching a specific Task ID.
- **Scope:** Includes task execution. Excludes task routing, discovery, and traversal.
- **Relationships:** Registered in Dispatcher. Discovered by Workshop. Parameterized by Task and Result types.

- **Name:** Dispatcher
- **Definition:** A registry mapping Task IDs to workers. Resolves which worker handles a given task at runtime.
- **Scope:** Includes registration and lookup. Excludes worker lifecycle and task creation.
- **Relationships:** Populated by Workshop. Queried by analysis modules. Applies Registration Mode on registration.

- **Name:** Registration Mode
- **Definition:** The rule a Dispatcher applies when a Task ID is registered a second time: replace the existing worker, or attach the new worker so both run in registration order.
- **Scope:** Includes the replace and attach rules. Excludes ordering across distinct Task IDs.
- **Relationships:** Selected per registration. Declared on a WorkLicense annotation when registration is workshop-driven.

- **Name:** Workshop
- **Definition:** An abstract container grouping related workers. Exposes them via reflective discovery of WorkLicense annotations. Workers within a workshop share resources through their enclosing instance.
- **Scope:** Includes annotation-based discovery, worker grouping, and workshop-driven registration. Excludes dispatch logic and traversal.
- **Relationships:** Populates Dispatcher. Contains Workers. Uses WorkLicense for discovery.

- **Name:** WorkLicense
- **Definition:** A meta-annotation applied to custom annotation classes. Marks worker properties for reflective discovery. Maps them to Task IDs derived from the annotation's qualified class name and constructor parameters.
- **Scope:** Includes annotation metadata, Task ID generation, and an optional Registration Mode parameter. Excludes runtime permission enforcement.
- **Relationships:** Applied to custom annotations. Read by Workshop. Produces Task IDs.

## 3. Contracts & Flow

**Data Contracts**
- **With analysis modules:** Modules define tasks carrying a Task ID, workers producing results, and license annotations. Modules register workers through a Workshop or directly into a Dispatcher, look up workers by Task ID, and receive results. Type signatures: [design.md](design.md).
- **With traversal engines:** Framework provides dispatch. Traversal strategy (worklist, recursive, etc.) is owned by the analysis module, not the framework. Workers are synchronous; concurrency is managed by the caller.

**Internal Processing Flow**
1. **Define** — Custom WorkLicense annotations, Task types, Result types, Worker implementations.
2. **Group** — Licensed workers placed into a Workshop subclass, annotated with a custom WorkLicense annotation.
3. **Discover** — Workshop reflects over its declared properties to find WorkLicense-annotated workers, maps each to a Task ID and a Registration Mode.
4. **Register** — Worker-to-Task-ID mappings registered into a Dispatcher under the Registration Mode.
5. **Dispatch** — Analysis code looks up a worker by Task ID through the Dispatcher.
6. **Execute** — The worker executes the task synchronously and returns a result.

## 4. Scenarios

- **Typical:** A worklist-driven analyzer creates a Workshop with transfer functions for each AST node type. Each is annotated with a node-specific license. The Workshop discovers all licensed workers and registers them in a Dispatcher. The worklist driver dispatches by node type during traversal.

- **Boundary:** A Workshop subclass contains workers but none carry a WorkLicense annotation. Discovery yields no workers. No workers register. Dispatch reports no worker for every Task ID.

- **Interaction:** Two Workshop subclasses (e.g., expression handlers and statement handlers) register workers into the same Dispatcher under non-overlapping Task IDs. The driver dispatches to the correct worker regardless of which workshop provided it. Overlapping Task IDs follow the Registration Mode: replace overwrites the earlier worker; attach runs both in registration order.

See [design.md](design.md) for type specifications and method signatures.
