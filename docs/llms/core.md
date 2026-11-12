# Core

> Task dispatch, Worker execution, and annotation-based licensing.

## Quick Start

```kotlin
import edu.jhu.cobra.framework.AbcDispatcher
import edu.jhu.cobra.framework.ITask
import edu.jhu.cobra.framework.IWorker

data class ExampleTask(override val uid: ITask.ID) : ITask
val task = ExampleTask(ITask.ID("example", emptySet()))
val dispatcher = AbcDispatcher<ExampleTask, Boolean>()
dispatcher.register(task.uid, IWorker { true })
check(checkNotNull(dispatcher.dispatch(task.uid)).work(task))
```

## API

- **`ITask`** — Unit of work with `uid: ITask.ID`.

- **`ITask.ID(license: String, props: Set<String>)`** — Dispatch key. Structural equality, case-sensitive.

- **`IWorker<T : ITask, R>`** — Processes a Task, returns a result. `fun interface`.

- **`IWorker<T, R>.work(task: T): R`** — Synchronous. Result type `R` determined by caller.

- **`RegisterMode`** — `REPLACE` overwrites an existing registration; `ATTACH` chains after it.

- **`IDispatcher<W : IWorker<*, *>>`** — Routes Tasks to Workers.

- **`IDispatcher<W>.dispatch(forTask: ITask.ID): W?`** — Returns registered Worker or null.

- **`IDispatcher<W>.register(forTask: ITask.ID, toWorker: W, mode: RegisterMode = ATTACH): Unit`** — Binds Worker to Task ID per `mode`.

- **`AbcDispatcher<T : ITask, R>()`** — `IDispatcher<IWorker<T, R>>` implementation. `ATTACH` runs existing then new Worker and returns the new Worker's result.

- **`AbcWorkshop<W : IWorker<*, *>>`** — Groups licensed Workers for registration.

- **`AbcWorkshop<W>.licensedWorkers(): Map<ITask.ID, W>`** — Returns the licensed Workers. Raises `IllegalStateException` when a licensed property is null.

- **`AbcWorkshop<W>.registerTo(dispatcher: IDispatcher<W>): Unit`** — Registers every licensed Worker, one registration per license annotation, with the annotation's `RegisterMode`.

- **`@WorkLicense`** — Meta-annotation for custom license annotation classes.

- **`WorkLicense.getTaskID(annotation: Annotation): ITask.ID`** — Extracts ID from annotation instance; `RegisterMode`-typed properties excluded. Raises `IllegalStateException` when the annotation class has no primary constructor.

- **`WorkLicense.getTaskID(cls: Class<*>, vararg props: String): ITask.ID`** — Constructs ID from class + props.

- **`WorkLicense.isTaskID(taskID: ITask.ID, forLicense: Class<*>): Boolean`** — Checks ID matches license class.

## Configuration

- Select registration behavior with `RegisterMode.REPLACE` or the default `ATTACH`.
- Declare at most one `RegisterMode` property on a custom license annotation; `registerTo` defaults to `ATTACH` when none is declared.
- Import `edu.jhu.cobra.framework.getTaskID` and `edu.jhu.cobra.framework.isTaskID` to call the `WorkLicense` companion extensions.

## Gotchas

- `@WorkLicense` targets `ANNOTATION_CLASS`. Applying it to a property is a compile error.
- `licensedWorkers()` and `registerTo()` use `kotlin-reflect`. The `kotlin-reflect` artifact must be on the classpath.
- `licensedWorkers()` reads `declaredMemberProperties`; inherited properties are not discovered.
- `ITask.ID` is a reusable dispatch key, not a Task-instance identifier; `license` stores `Class.name`, so equal simple class names in different packages remain distinct.
- Default `register` mode is `ATTACH`: registering the same `ITask.ID` twice runs both Workers. Pass `RegisterMode.REPLACE` to overwrite.
- `licensedWorkers()` keeps the last reflected property for a duplicate key; `registerTo()` applies Registration Mode in reflection iteration order, with no source declaration order guarantee.
- `AbcDispatcher` is not thread-safe; synchronize shared use in the caller.
- An annotation with several `RegisterMode` properties violates the input contract; current discovery does not reject it, so never depend on which property it selects.
- `IWorker.work` is synchronous. Callers manage concurrency (coroutines, thread pools).
