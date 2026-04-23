# COBRA Framework -- Design Specification

## Design Overview

- **Classes**: `IProduct`, `ITask`, `ITask.ID`, `IWorker`, `IDispatcher`, `AbcWorkshop`, `WorkLicense`
- **Relationships**: `IWorker` consumes `ITask` and emits `IProduct`; `IDispatcher` maps `ITask.ID` to `IWorker`; `AbcWorkshop` discovers `IWorker` instances via `WorkLicense`; `WorkLicense` generates `ITask.ID`
- **Abstract**: `IWorker` (implemented by concrete workers), `IDispatcher` (implemented by concrete dispatchers), `AbcWorkshop` (subclassed per worker group)
- **Exceptions**: None defined. Null return from `IDispatcher.dispatch` signals missing registration.
- **Dependency roles**: Data holders: `ITask.ID`, `IProduct`, `ITask`. Orchestrator: `AbcWorkshop`. Helpers: `WorkLicense` companion functions. Router: `IDispatcher`.

## Class / Type Specifications

### `IProduct`

**Responsibility:** Marker interface for all worker output types.

**State:** None.

**Methods:** None. Implementations define their own fields.

---

### `ITask`

**Responsibility:** Represents a unit of work with a unique dispatch identifier.

**State:**
- `uid: ITask.ID` -- Unique identifier for dispatch routing.

**Methods:** None beyond the `uid` property.

---

### `ITask.ID`

**Responsibility:** Composite dispatch key pairing a license name with a property set.

**State:**
- `license: String` -- Primary classification string (typically an annotation class simple name).
- `props: Set<String>` -- Secondary properties refining the classification.

**Methods:** Data class defaults (`equals`, `hashCode`, `toString`, `copy`). Equality is structural: two IDs match when both `license` and `props` are equal. Task IDs are case-sensitive.

---

### `IWorker<T : ITask, R : IProduct>`

**Responsibility:** Processes a task and emits products into a coroutine flow.

**State:** None defined by the interface. Implementations may hold state.

**Methods:**

| Method | Behavior | Input | Output | Errors |
|--------|----------|-------|--------|--------|
| `FlowCollector<R>.work(task: T)` | Executes task logic and emits results via `FlowCollector.emit()`. Suspending function. | `task: T` -- the task to process. | Zero or more `R` instances emitted to the collector. | Implementation-defined. |

The `work` function is an extension on `FlowCollector<R>`, enabling direct `emit()` calls within the function body. Callers invoke it inside a `flow { }` builder using `with(worker) { work(task) }`.

---

### `IDispatcher<Worker : IWorker<*, *>>`

**Responsibility:** Registry mapping Task IDs to workers. Resolves which worker handles a given task.

**State:** Implementation-defined (typically a `Map<ITask.ID, Worker>`).

**Methods:**

| Method | Behavior | Input | Output | Errors |
|--------|----------|-------|--------|--------|
| `dispatch(forTask: ITask.ID)` | Looks up the worker registered for the given Task ID. | `forTask: ITask.ID` | `Worker?` -- the registered worker, or null if none exists. | None. |
| `register(forTask: ITask.ID, toWorker: Worker)` | Associates a worker with a Task ID. Overwrites any existing registration for that ID. | `forTask: ITask.ID`, `toWorker: Worker` | Unit. | None. |

---

### `AbcWorkshop<W : IWorker<*, *>>`

**Responsibility:** Abstract base class that groups related workers and discovers licensed ones via reflection.

**State:** Subclass-defined worker properties.

**Methods:**

| Method | Behavior | Input | Output | Errors |
|--------|----------|-------|--------|--------|
| `licensedWorkers()` | Reflects over `declaredMemberProperties` of the concrete subclass. Finds properties whose annotations carry the `@WorkLicense` meta-annotation. Extracts Task IDs from those annotations. Returns a map of Task ID to worker. | None. | `Map<ITask.ID, W>` | None. |

Discovery rules:
- Only properties declared directly on the concrete class are scanned (not inherited).
- Private and protected properties are accessible (reflection sets `isAccessible = true`).
- Duplicate Task IDs: the last property in iteration order wins.
- Properties without a `@WorkLicense`-annotated annotation are excluded.

---

### `WorkLicense`

**Responsibility:** Meta-annotation marking custom annotation classes as license carriers. Companion object provides Task ID generation and validation utilities.

**State:** None.

**Target:** `AnnotationTarget.ANNOTATION_CLASS` only.

**Retention:** `AnnotationRetention.RUNTIME`.

## Function Specifications

### `WorkLicense.Companion.getTaskID(annotation: Annotation): ITask.ID`

**Responsibility:** Extracts a Task ID from a WorkLicense-annotated annotation instance.

**Behavior:** Reads the annotation class's primary constructor parameters by name, retrieves their values from the instance, converts each to a string, and delegates to `getTaskID(Class, Collection<String>)`.

**Input:** `annotation` -- an annotation instance whose class is annotated with `@WorkLicense`.

**Output:** `ITask.ID` with `license` set to the annotation class simple name and `props` set to the stringified constructor parameter values.

**Errors:** `NullPointerException` if the annotation class has no primary constructor.

---

### `WorkLicense.Companion.getTaskID(cls: Class<*>, vararg props: String): ITask.ID`

**Responsibility:** Constructs a Task ID from a class and property strings.

**Input:** `cls` -- the class whose simple name becomes the license. `props` -- zero or more property strings.

**Output:** `ITask.ID(cls.simpleName, props.toSet())`.

---

### `WorkLicense.Companion.getTaskID(cls: Class<*>, props: Collection<String>): ITask.ID`

**Responsibility:** Constructs a Task ID from a class and a collection of property strings.

**Input:** `cls` -- the class whose simple name becomes the license. `props` -- collection of property strings.

**Output:** `ITask.ID(cls.simpleName, props.toSet())`.

---

### `WorkLicense.Companion.isTaskID(taskID: ITask.ID, forLicense: Class<*>): Boolean`

**Responsibility:** Checks whether a Task ID's license matches a given class.

**Input:** `taskID` -- the ID to check. `forLicense` -- the class to compare against.

**Output:** `true` if `taskID.license == forLicense.simpleName`, `false` otherwise.

## Exception / Error Types

No custom exception types are defined. Error conditions:
- `IDispatcher.dispatch` returns `null` for unregistered Task IDs.
- `WorkLicense.getTaskID(Annotation)` throws `NullPointerException` if the annotation class lacks a primary constructor (Kotlin `!!` on `primaryConstructor`).

## Validation Rules

- **ITask.ID:** No constraints on `license` or `props` values. Empty strings and special characters are valid.
- **IDispatcher.register:** No uniqueness enforcement. Duplicate registrations silently overwrite.
- **AbcWorkshop.licensedWorkers:** No null-check on discovered worker values. Null-valued properties annotated with a license are included in the returned map.
- **WorkLicense annotation:** The meta-annotation target is restricted to `ANNOTATION_CLASS`. Applying it to other targets has no effect on discovery.
