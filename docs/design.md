# COBRA Framework -- Design Specification

## Design Overview

- **Classes**: `ITask`, `ITask.ID`, `IWorker`, `IDispatcher`, `RegisterMode`, `AbcDispatcher`, `AbcWorkshop`, `WorkLicense`
- **Relationships**: `IWorker` consumes `ITask` and returns result; `IDispatcher` maps `ITask.ID` to `IWorker`; `AbcDispatcher` implements `IDispatcher`; `AbcWorkshop` discovers `IWorker` instances via `WorkLicense` and registers them into `IDispatcher`; `WorkLicense` generates `ITask.ID`
- **Abstract**: `IWorker` (implemented by concrete Workers), `IDispatcher` (implemented by `AbcDispatcher`), `AbcWorkshop` (subclassed per Worker group)
- **Exceptions**: None defined. Null return from `IDispatcher.dispatch` signals missing registration. `IllegalStateException` for reflection contract violations.
- **Dependency roles**: Data holders: `ITask.ID`, `ITask`. Router: `IDispatcher` / `AbcDispatcher`. Discovery: `AbcWorkshop`. Metadata: `WorkLicense`, `RegisterMode`.
- **Visibility**: All types public (`explicitApi()`), package `edu.jhu.cobra.framework`, module `framework`.
- **Named values**: None introduced.

## Class / Type Specifications

### `ITask`

**Responsibility:** Represents a Task whose dispatch key selects a registered Worker.

**State:**
- `uid: ITask.ID` -- Dispatch key shared by Task instances selecting the same registration.

### `ITask.ID`

**Responsibility:** Composite dispatch key pairing a license name with a property set.

**State:**
- `license: String` -- Primary classification string (fully qualified annotation class name).
- `props: Set<String>` -- Secondary properties refining the classification.

**Methods:** Data class defaults (`equals`, `hashCode`, `toString`, `copy`). Structural equality. Case-sensitive.

**Input:** No constraints on `license` or `props` values; empty strings and empty sets are valid.

### `IWorker<T : ITask, R>`

**Responsibility:** Processes a Task and returns a result. Synchronous; concurrency is managed by the caller, not the framework.

**State:** None defined by the interface. Implementations may capture state via closure.

**Methods:**

| Method | Behavior | Input | Output |
|--------|----------|-------|--------|
| `work(task: T): R` | Executes Task logic, returns result. | `task: T` | `R` |

`IWorker` is a `fun interface` — implementable as a SAM lambda. The result type `R` is determined by the caller. Common usages: `Boolean` (state changed), `Unit` (side-effect only), domain-specific result types.

### `IDispatcher<W : IWorker<*, *>>`

**Responsibility:** Registry mapping Task IDs to Workers.

**State:** None defined by the interface.

**Methods:**

| Method | Behavior | Input | Output |
|--------|----------|-------|--------|
| `dispatch(forTask: ITask.ID)` | Returns registered Worker or null. | `forTask: ITask.ID` | `W?` |
| `register(forTask: ITask.ID, toWorker: W, mode: RegisterMode = ATTACH)` | Associates Worker with Task ID per `mode`. Omitted `mode` is `ATTACH`. | `forTask`, `toWorker`, `mode` | Unit |

### `RegisterMode`

**Responsibility:** Enum selecting registration behavior for an already-registered Task ID.

**Values:**
- `REPLACE` -- New Worker overwrites the existing registration.
- `ATTACH` -- New Worker chains after the existing registration (default).

### `AbcDispatcher<T : ITask, R>`

**Responsibility:** Default `IDispatcher` implementation: map-backed registry with ATTACH/REPLACE semantics. Implements `IDispatcher<IWorker<T, R>>` -- fixing the Worker type to the `IWorker` interface makes ATTACH chaining fully typed. `open` for subclassing.

**State:**
- `workers: MutableMap<ITask.ID, IWorker<T, R>>` (private) -- Registry.

**Methods:**

| Method | Behavior | Input | Output |
|--------|----------|-------|--------|
| `dispatch(forTask: ITask.ID)` | Map lookup; null when unregistered. | `forTask` | `IWorker<T, R>?` |
| `register(forTask, toWorker, mode)` | `REPLACE` overwrites. `ATTACH` on a free ID stores the Worker; on an occupied ID stores a chained Worker running existing then new, in registration order, returning the new Worker's result. Earlier results are discarded. | `forTask`, `toWorker`, `mode` | Unit |

Workshop-driven registration goes through `AbcWorkshop.registerTo(dispatcher)`; the Dispatcher offers no Workshop-accepting method.

### `AbcWorkshop<W : IWorker<*, *>>`

**Responsibility:** Groups related Workers. Discovers licensed ones via reflection.

**State:** Subclass-defined Worker properties.

**Methods:**

| Method | Behavior | Input | Output |
|--------|----------|-------|--------|
| `licensedWorkers()` | Reflects over `declaredMemberProperties`. Finds Worker-typed properties annotated with `@WorkLicense`-marked annotations. Extracts Task IDs. Returns map. | None | `Map<ITask.ID, W>` |
| `registerTo(dispatcher: IDispatcher<W>)` | Registers every licensed Worker into the Dispatcher, one registration per license annotation, with the annotation's `RegisterMode`. | `dispatcher` | Unit |

Discovery rules:
- Only properties declared on the concrete class (not inherited).
- Only properties whose return type is a subclass of `IWorker`.
- Private/protected properties accessible (reflection sets `isAccessible = true`).
- `licensedWorkers()` keeps the last property for a duplicate key in reflection iteration order; `registerTo` sends every registration to the Dispatcher under its Registration Mode. Neither method promises source declaration order.
- Properties without a `@WorkLicense`-annotated annotation excluded.
- Null-valued licensed property: `IllegalStateException` naming Workshop class and property.

Mode extraction (`registerTo`):
- A license annotation declares at most one `RegisterMode`-typed property; it is registration metadata, so `getTaskID` excludes it from the dispatch key.
- Without that property, registration uses `ATTACH`.
- `registerTo` processes properties and license annotations in reflection iteration order; `AbcDispatcher` preserves the order of the `register` calls it receives.

### `WorkLicense`

**Responsibility:** Meta-annotation marking custom annotation classes as license carriers.

**Target:** `AnnotationTarget.ANNOTATION_CLASS` only.

**Retention:** `AnnotationRetention.RUNTIME`.

## Function Specifications

### `WorkLicense.Companion.getTaskID(annotation: Annotation): ITask.ID`

**Responsibility:** Extracts Task ID from a WorkLicense-annotated annotation instance.

**Input:** A license annotation instance satisfying the single-mode-property contract above.

**Behavior:** Reads the annotation class primary constructor parameters, resolves each to its property and converts non-RegisterMode values to strings for the key’s property set.

**Output:** `ITask.ID(annotationClass.java.name, paramValues.toSet())`.

**Errors:** `IllegalStateException` if the annotation class has no primary constructor, or a constructor parameter has no matching property.

### `WorkLicense.Companion.getTaskID(cls: Class<*>, vararg props: String): ITask.ID`

**Responsibility:** Constructs Task ID from class and property strings.

**Input:** `cls` -- license class; `props` -- zero or more property strings, deduplicated into a set.

**Output:** `ITask.ID(cls.name, props.toSet())`.

### `WorkLicense.Companion.isTaskID(taskID: ITask.ID, forLicense: Class<*>): Boolean`

**Responsibility:** Checks whether Task ID license matches a given class.

**Input:** `taskID` -- the ID under test; `forLicense` -- the license class.

**Output:** `taskID.license == forLicense.name`.

## Exception / Error Types

No custom exceptions. Error conditions:
- `IDispatcher.dispatch` returns `null` for unregistered Task IDs.
- `WorkLicense.getTaskID(Annotation)` throws `IllegalStateException` if the annotation class lacks a primary constructor or a constructor parameter has no matching property.
- `AbcWorkshop.licensedWorkers` / `registerTo` throw `IllegalStateException` for a licensed property whose value is null, naming the Workshop class and property.
- `AbcWorkshop.registerTo` throws `IllegalStateException` when a `RegisterMode`-typed annotation property holds a non-`RegisterMode` value.


## Caller Examples

```kotlin
import edu.jhu.cobra.framework.AbcDispatcher
import edu.jhu.cobra.framework.ITask
import edu.jhu.cobra.framework.IWorker

data class DispatchTask(override val uid: ITask.ID) : ITask
val task = DispatchTask(ITask.ID("example", emptySet()))
val dispatcher = AbcDispatcher<DispatchTask, Boolean>()
dispatcher.register(task.uid, IWorker { true })
check(checkNotNull(dispatcher.dispatch(task.uid)).work(task))
```

```kotlin
import edu.jhu.cobra.framework.AbcDispatcher
import edu.jhu.cobra.framework.AbcWorkshop
import edu.jhu.cobra.framework.ITask
import edu.jhu.cobra.framework.IWorker
import edu.jhu.cobra.framework.WorkLicense
import edu.jhu.cobra.framework.getTaskID

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@WorkLicense
annotation class ExampleLicense(val name: String)
data class LicensedTask(override val uid: ITask.ID) : ITask
class ExampleWorkshop : AbcWorkshop<IWorker<LicensedTask, Boolean>>() {
    @ExampleLicense("greet")
    val greeter = IWorker<LicensedTask, Boolean> { true }
}
val task = LicensedTask(WorkLicense.getTaskID(ExampleLicense::class.java, "greet"))
val dispatcher = AbcDispatcher<LicensedTask, Boolean>()
ExampleWorkshop().registerTo(dispatcher)
check(checkNotNull(dispatcher.dispatch(task.uid)).work(task))
```

## Open Gaps

The single-mode-property contract in AbcWorkshop above requires rejecting a license annotation with more than one RegisterMode property. `AbcWorkshop.extractMode` in `framework/src/main/kotlin/edu/jhu/cobra/framework/AbcWorkshop.kt` selects the first reflected property and does not validate that count; `WorkLicense.getTaskID` excludes every mode property from the key. Missing validation is an implementation gap, not permission to depend on first-property selection.
