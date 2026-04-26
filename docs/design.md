# COBRA Framework -- Design Specification

## Design Overview

- **Classes**: `ITask`, `ITask.ID`, `IWorker`, `IDispatcher`, `AbcWorkshop`, `WorkLicense`
- **Relationships**: `IWorker` consumes `ITask` and returns result; `IDispatcher` maps `ITask.ID` to `IWorker`; `AbcWorkshop` discovers `IWorker` instances via `WorkLicense`; `WorkLicense` generates `ITask.ID`
- **Abstract**: `IWorker` (implemented by concrete workers), `IDispatcher` (implemented by concrete dispatchers), `AbcWorkshop` (subclassed per worker group)
- **Exceptions**: None defined. Null return from `IDispatcher.dispatch` signals missing registration.
- **Dependency roles**: Data holders: `ITask.ID`, `ITask`. Router: `IDispatcher`. Discovery: `AbcWorkshop`. Metadata: `WorkLicense`.

## Class / Type Specifications

### `ITask`

**Responsibility:** Represents a unit of work with a unique dispatch identifier.

**State:**
- `uid: ITask.ID` -- Unique identifier for dispatch routing.

---

### `ITask.ID`

**Responsibility:** Composite dispatch key pairing a license name with a property set.

**State:**
- `license: String` -- Primary classification string (annotation class simple name).
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
| `register(forTask: ITask.ID, toWorker: Worker)` | Associates worker with Task ID. Overwrites existing. | `forTask`, `toWorker` | Unit |

---

### `AbcWorkshop<W : IWorker<*, *>>`

**Responsibility:** Groups related workers. Discovers licensed ones via reflection.

**State:** Subclass-defined worker properties.

**Methods:**

| Method | Behavior | Input | Output |
|--------|----------|-------|--------|
| `licensedWorkers()` | Reflects over `declaredMemberProperties`. Finds properties annotated with `@WorkLicense`-marked annotations. Extracts Task IDs. Returns map. | None | `Map<ITask.ID, W>` |

Discovery rules:
- Only properties declared on the concrete class (not inherited).
- Private/protected properties accessible (reflection sets `isAccessible = true`).
- Duplicate Task IDs: last property in iteration order wins.
- Properties without a `@WorkLicense`-annotated annotation excluded.

---

### `WorkLicense`

**Responsibility:** Meta-annotation marking custom annotation classes as license carriers.

**Target:** `AnnotationTarget.ANNOTATION_CLASS` only.

**Retention:** `AnnotationRetention.RUNTIME`.

## Function Specifications

### `WorkLicense.Companion.getTaskID(annotation: Annotation): ITask.ID`

**Responsibility:** Extracts Task ID from a WorkLicense-annotated annotation instance.

**Behavior:** Reads annotation class primary constructor parameters, converts values to strings.

**Output:** `ITask.ID(annotationClass.simpleName, paramValues.toSet())`.

**Errors:** `NullPointerException` if annotation class has no primary constructor.

---

### `WorkLicense.Companion.getTaskID(cls: Class<*>, vararg props: String): ITask.ID`

**Responsibility:** Constructs Task ID from class and property strings.

**Output:** `ITask.ID(cls.simpleName, props.toSet())`.

---

### `WorkLicense.Companion.getTaskID(cls: Class<*>, props: Collection<String>): ITask.ID`

**Responsibility:** Constructs Task ID from class and collection of property strings.

**Output:** `ITask.ID(cls.simpleName, props.toSet())`.

---

### `WorkLicense.Companion.isTaskID(taskID: ITask.ID, forLicense: Class<*>): Boolean`

**Responsibility:** Checks whether Task ID license matches a given class.

**Output:** `taskID.license == forLicense.simpleName`.

## Exception / Error Types

No custom exceptions. Error conditions:
- `IDispatcher.dispatch` returns `null` for unregistered Task IDs.
- `WorkLicense.getTaskID(Annotation)` throws `NullPointerException` if annotation class lacks primary constructor.

## Validation Rules

- `ITask.ID`: No constraints on `license` or `props` values.
- `IDispatcher.register`: Duplicate registrations silently overwrite.
- `AbcWorkshop.licensedWorkers`: Null-valued annotated properties included in map.
- `WorkLicense` target restricted to `ANNOTATION_CLASS`.
- `IWorker.work` is synchronous. Concurrency managed by the caller, not the framework.
