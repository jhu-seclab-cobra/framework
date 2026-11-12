# COBRA Framework -- Concepts & Terminology

## 1. Context

**Problem Statement**
Static analysis and interpretation systems dispatch heterogeneous node types to specialized handlers. Without a structured dispatch mechanism, handlers become coupled to the traversal engine. A common abstraction decouples Task identification from Task execution.

**System Role**
The framework library provides Task dispatch, Worker registration, and annotation-based discovery for all COBRA analysis modules.

**Data Flow**
- **Inputs:** Tasks (typed units of work with identifiers) from analysis modules.
- **Outputs:** Results returned by Workers.
- **Connections:** [Analysis module] → [Dispatcher] → [Worker] → [Result]

**Scope Boundaries**
- **Owned:** Task identification, Worker licensing, Worker dispatch, registration modes, Workshop discovery.
- **Not Owned:** Concrete Task/result definitions, AST structures, analysis algorithms, graph construction, traversal strategy (worklist, recursive, etc.), concurrency.

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

- **Name:** [Task](glossary/glossary-dispatch.md#task)
  - **Role:** Carries the input a Worker processes and the key a Dispatcher uses to select it.
  - **Scope:** Includes the dispatch key and payload; excludes execution.
  - **Relationships:** A Worker consumes a Task; the key shape is in design.md.

- **Name:** [`ITask.ID`](design.md#itaskid)
  - **Role:** Lets Task instances with the same dispatch requirements select the same registration.
  - **Scope:** Contains a license string and property set; excludes payload and instance identity.
  - **Relationships:** Connects Task keys, license annotations and Dispatcher registration.

- **Name:** [Worker](glossary/glossary-dispatch.md#worker)
  - **Role:** Runs the Task processing supplied by an analysis module.
  - **Scope:** Execution only; excludes routing, discovery and traversal.
  - **Relationships:** A Workshop supplies Workers and a Dispatcher selects them.

- **Name:** [Dispatcher](glossary/glossary-dispatch.md#dispatcher)
  - **Role:** Decouples analysis code from the Worker that processes a Task.
  - **Scope:** Registration and lookup; excludes Task creation and Worker lifetime.
  - **Relationships:** A Workshop populates the Dispatcher; Registration Mode governs repeated keys.

- **Name:** [Registration Mode](glossary/glossary-dispatch.md#registration-mode)
  - **Role:** Makes repeated-key registration behavior explicit.
  - **Scope:** Replace or attach; excludes order across distinct keys.
  - **Relationships:** Selected per registration or by the license annotation.

- **Name:** [Workshop](glossary/glossary-dispatch.md#workshop)
  - **Role:** Groups Workers that share analysis resources.
  - **Scope:** Worker grouping, discovery and registration; excludes dispatch and traversal.
  - **Relationships:** Supplies Workers to a Dispatcher through license annotations.

- **Name:** [`WorkLicense`](design.md#worklicense)
  - **Role:** Marks the custom annotations used to declare Worker registrations.
  - **Scope:** Annotation metadata, key extraction and optional Registration Mode; excludes runtime permissions.
  - **Relationships:** A Workshop reads the marked annotations; key-extraction details belong to design.md.

## 3. Contracts & Flow

**Data Contracts**
- **With analysis modules:** Modules define Tasks carrying a Task ID, Workers producing results, and license annotations. Modules register Workers through a Workshop or directly into a Dispatcher, look up Workers by Task ID, and receive results. Type signatures: [design.md](design.md).
- **With traversal engines:** Framework provides dispatch. Traversal strategy (worklist, recursive, etc.) is owned by the analysis module, not the framework. Workers are synchronous; concurrency is managed by the caller.

**Internal Processing Flow**
A Workshop supplies registrations to a Dispatcher, the analysis module selects a Worker for a Task key, and the Worker returns the result. The [discovery and registration design](design.md) owns annotation extraction and registration order.

## 4. Scenarios

- **Typical:** A worklist-driven analyzer creates a Workshop with transfer functions for each AST node type. Each is annotated with a node-specific license. The Workshop discovers all licensed Workers and registers them in a Dispatcher. The worklist driver dispatches by node type during traversal.

- **Boundary:** A Workshop subclass contains Workers but none carry a WorkLicense annotation. Discovery yields no Workers. No Workers register. Dispatch reports no Worker for every Task ID.

- **Interaction:** Two Workshop subclasses (e.g., expression handlers and statement handlers) register Workers into the same Dispatcher under non-overlapping Task IDs. The driver dispatches to the correct Worker regardless of which Workshop provided it. Overlapping Task IDs follow the Registration Mode: replace overwrites the earlier Worker; attach runs both in the order register calls are received, with no source-declaration-order promise for discovery.

See [design.md](design.md) for type specifications and method signatures.
