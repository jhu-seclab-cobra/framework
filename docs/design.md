# COBRA Framework -- Design Specification

## Design Overview

- **Classes**: `ITask`, `ITask.ID`, `IWorker`, `IDispatcher`, `RegisterMode`, `AbcDispatcher`, `AbcWorkshop`, `WorkLicense`
- **Relationships**: `IWorker` consumes `ITask` and returns result; `IDispatcher` maps `ITask.ID` to `IWorker`; `AbcDispatcher` implements `IDispatcher`; `AbcWorkshop` discovers `IWorker` instances via `WorkLicense` and registers them into `IDispatcher`; `WorkLicense` generates `ITask.ID`
- **Abstract**: `IWorker` (implemented by concrete workers), `IDispatcher` (implemented by `AbcDispatcher`), `AbcWorkshop` (subclassed per worker group)
- **Exceptions**: None defined. Null return from `IDispatcher.dispatch` signals missing registration. `IllegalStateException` for reflection contract violations.
- **Dependency roles**: Data holders: `ITask.ID`, `ITask`. Router: `IDispatcher` / `AbcDispatcher`. Discovery: `AbcWorkshop`. Metadata: `WorkLicense`, `RegisterMode`.

## Class / Type Specifications

### `ITask`

**Responsibility:** Represents a unit of work with a unique dispatch identifier.

**State:**
- `uid: ITask.ID` -- Unique identifier for dispatch routing.

---

### `ITask.ID`

**Responsibility:** Composite dispatch key pairing a license name with a property set.

**State:**
- `license: String` -- Primary classification string (fully qualified annotation class name).
- `props: Set<String>` -- Secondary properties refining the classification.

**Methods:** Data class defaults (`equals`, `hashCode`, `toString`, `copy`). Structural equality. Case-sensitive.

---

### `IWorker<T : ITask, R>`

**Responsibility:** Processes a task and returns a result. Synchronous.

**State:** None defined by the interface. Implementations may capture state via closure.

**Methods:**

| Method | Behavior | Input | Output |
|--------|----------|-------|--------|
| `work(task: T): R` | Executes task logic, returns result. | `task: T` | `R` |

`IWorker` is a `fun interface` — implementable as a SAM lambda. The result type `R` is determined by the caller. Common usages: `Boolean` (state changed), `Unit` (side-effect only), domain-specific result types.

---

### `IDispatcher<Worker : IWorker<*, *>>`

**Responsibility:** Registry mapping Task IDs to workers.

**State:** Implementation-defined (typically `Map<ITask.ID, Worker>`).

**Methods:**

| Method | Behavior | Input | Output |
|--------|----------|-------|--------|
| `dispatch(forTask: ITask.ID)` | Returns registered worker or null. | `forTask: ITask.ID` | `Worker?` |
| `register(forTask: ITask.ID, toWorker: Worker, mode: RegisterMode = ATTACH)` | Associates worker with Task ID per `mode`. | `forTask`, `toWorker`, `mode` | Unit |

---

### `RegisterMode`

**Responsibility:** Enum selecting registration behavior for an already-registered Task ID.

**Values:**
- `REPLACE` -- New worker overwrites the existing registration.
- `ATTACH` -- New worker chains after the existing registration (default).

---

### `AbcDispatcher<T : ITask, R>`

**Responsibility:** Default `IDispatcher` implementation: map-backed registry with ATTACH/REPLACE semantics. Implements `IDispatcher<IWorker<T, R>>` -- fixing the worker type to the `IWorker` interface makes ATTACH chaining fully typed. `open` for subclassing.

**State:**
- `workers: MutableMap<ITask.ID, IWorker<T, R>>` (private) -- Registry.

**Methods:**

| Method | Behavior | Input | Output |
|--------|----------|-------|--------|
| `dispatch(forTask: ITask.ID)` | Map lookup; null when unregistered. | `forTask` | `IWorker<T, R>?` |
| `register(forTask, toWorker, mode)` | `REPLACE` overwrites. `ATTACH` on a free ID stores the worker; on an occupied ID stores a chained worker running existing then new, in registration order, returning the new worker's result. Earlier results are discarded. | `forTask`, `toWorker`, `mode` | Unit |

Workshop-driven registration goes through `AbcWorkshop.registerTo(dispatcher)`; the dispatcher offers no workshop-accepting method.

---

### `AbcWorkshop<W : IWorker<*, *>>`

**Responsibility:** Groups related workers. Discovers licensed ones via reflection.

**State:** Subclass-defined worker properties.

**Methods:**

| Method | Behavior | Input | Output |
|--------|----------|-------|--------|
| `licensedWorkers()` | Reflects over `declaredMemberProperties`. Finds worker-typed properties annotated with `@WorkLicense`-marked annotations. Extracts Task IDs. Returns map. | None | `Map<ITask.ID, W>` |
| `registerTo(dispatcher: IDispatcher<W>)` | Registers every licensed worker into the dispatcher, one registration per license annotation, with the annotation's `RegisterMode`. | `dispatcher` | Unit |

Discovery rules:
- Only properties declared on the concrete class (not inherited).
- Only properties whose return type is a subclass of `IWorker`.
- Private/protected properties accessible (reflection sets `isAccessible = true`).
- Duplicate Task IDs in `licensedWorkers()`: last property in iteration order wins. In `registerTo`, duplicates follow the dispatcher's `RegisterMode` semantics instead.
- Properties without a `@WorkLicense`-annotated annotation excluded.
- Null-valued licensed property: `IllegalStateException` naming workshop class and property.

Mode extraction (`registerTo`):
- The mode is read from the annotation's `RegisterMode`-typed property -- the same property `getTaskID` excludes from the ID.
- An annotation declaring no `RegisterMode`-typed property defaults to `ATTACH`.

---

### `WorkLicense`

**Responsibility:** Meta-annotation marking custom annotation classes as license carriers.

**Target:** `AnnotationTarget.ANNOTATION_CLASS` only.

**Retention:** `AnnotationRetention.RUNTIME`.

## Function Specifications

### `WorkLicense.Companion.getTaskID(annotation: Annotation): ITask.ID`

**Responsibility:** Extracts Task ID from a WorkLicense-annotated annotation instance.

**Behavior:** Reads annotation class primary constructor parameters, resolves each to its property, converts values to strings. `RegisterMode`-typed properties are registration metadata and excluded from the ID.

**Output:** `ITask.ID(annotationClass.java.name, paramValues.toSet())`.

**Errors:** `IllegalStateException` if the annotation class has no primary constructor, or a constructor parameter has no matching property.

---

### `WorkLicense.Companion.getTaskID(cls: Class<*>, vararg props: String): ITask.ID`

**Responsibility:** Constructs Task ID from class and property strings.

**Output:** `ITask.ID(cls.name, props.toSet())`.

---

### `WorkLicense.Companion.getTaskID(cls: Class<*>, props: Collection<String>): ITask.ID`

**Responsibility:** Constructs Task ID from class and collection of property strings.

**Output:** `ITask.ID(cls.name, props.toSet())`.

---

### `WorkLicense.Companion.isTaskID(taskID: ITask.ID, forLicense: Class<*>): Boolean`

**Responsibility:** Checks whether Task ID license matches a given class.

**Output:** `taskID.license == forLicense.name`.

## Exception / Error Types

No custom exceptions. Error conditions:
- `IDispatcher.dispatch` returns `null` for unregistered Task IDs.
- `WorkLicense.getTaskID(Annotation)` throws `IllegalStateException` if the annotation class lacks a primary constructor or a constructor parameter has no matching property.
- `AbcWorkshop.licensedWorkers` / `registerTo` throw `IllegalStateException` for a licensed property whose value is null, naming the workshop class and property.
- `AbcWorkshop.registerTo` throws `IllegalStateException` when a `RegisterMode`-typed annotation property holds a non-`RegisterMode` value.

## Validation Rules

- `ITask.ID`: No constraints on `license` or `props` values.
- `IDispatcher.register`: Duplicate registrations follow `RegisterMode` -- `REPLACE` overwrites, `ATTACH` chains.
- `AbcWorkshop.licensedWorkers`: Null-valued licensed property is a wiring bug — `IllegalStateException`, never a silent skip or a null map value.
- `WorkLicense` target restricted to `ANNOTATION_CLASS`.
- `IWorker.work` is synchronous. Concurrency managed by the caller, not the framework.
