# Core

> Task dispatch, worker execution, and annotation-based licensing.

## Quick Start

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@WorkLicense
annotation class MyLicense(val name: String)

class MyWorkshop : AbcWorkshop<IWorker<MyTask, Boolean>>() {
    @MyLicense("greet")
    val greeter = IWorker<MyTask, Boolean> { task -> true }
}

val workers = MyWorkshop().licensedWorkers()
```

## API

**`ITask`** — Unit of work with `uid: ITask.ID`.

**`ITask.ID(license: String, props: Set<String>)`** — Dispatch key. Constructed via `WorkLicense.getTaskID()`.

**`IWorker<T : ITask, R>`** — Processes a task, returns a result. `fun interface`.

**`fun work(task: T): R`** — Synchronous. Result type `R` determined by caller.

**`IDispatcher<Worker : IWorker<*, *>>`** — Routes tasks to workers.

**`fun dispatch(forTask: ITask.ID): Worker?`** — Returns registered worker or null.

**`fun register(forTask: ITask.ID, toWorker: Worker)`** — Binds worker to task ID.

**`AbcWorkshop<W : IWorker<*, *>>`** — Groups workers; discovers via reflection.

**`fun licensedWorkers(): Map<ITask.ID, W>`** — Scans `@WorkLicense`-annotated properties.

**`@WorkLicense`** — Meta-annotation for custom license annotation classes.

**`WorkLicense.getTaskID(annotation: Annotation): ITask.ID`** — Extracts ID from annotation instance.

**`WorkLicense.getTaskID(cls: Class<*>, vararg props: String): ITask.ID`** — Constructs ID from class + props.

**`WorkLicense.isTaskID(taskID: ITask.ID, forLicense: Class<*>): Boolean`** — Checks ID matches license class.

## Configuration

No configuration required. Behavior driven by annotations and registration calls.

## Gotchas

- `@WorkLicense` targets `ANNOTATION_CLASS`, not `PROPERTY`. Applying directly to a worker property has no effect.
- `licensedWorkers()` uses `kotlin-reflect`. The `kotlin-reflect` artifact must be on the classpath.
- `licensedWorkers()` reads `declaredMemberProperties` — inherited properties not discovered.
- `ITask.ID.license` stores `Class.simpleName`. Same simple name in different packages → identical license.
- `IWorker.work` is synchronous. Callers manage concurrency (coroutines, thread pools).
