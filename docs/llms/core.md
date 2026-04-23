# Core

> Task dispatching, worker execution, and annotation-based licensing.

## Quick Start

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@WorkLicense
annotation class MyLicense(val name: String)

class MyWorkshop : AbcWorkshop<MyWorker>() {
    @MyLicense("greet")
    val greeter = MyWorker { (name) -> emit(Greeting("Hello $name")) }
}

val workers = MyWorkshop().licensedWorkers()
```

## API

**`ITask`** — Unit of work with a unique `uid: ITask.ID`.

**`ITask.ID(license: String, props: Set<String>)`** — Identifier composed of license class name and property values. Constructed via `WorkLicense.getTaskID()`.

**`IProduct`** — Marker interface for worker output.

**`IWorker<T : ITask, R : IProduct>`** — Performs a task and emits products.

**`suspend fun FlowCollector<R>.work(task: T)`** — Execute task, emit results to the collector.

**`IDispatcher<Worker : IWorker<*, *>>`** — Routes tasks to workers.

**`fun dispatch(forTask: ITask.ID): Worker?`** — Returns registered worker or null.

**`fun register(forTask: ITask.ID, toWorker: Worker)`** — Binds a worker to a task ID.

**`AbcWorkshop<W : IWorker<*, *>>`** — Groups workers; discovers licensed workers via reflection.

**`fun licensedWorkers(): Map<ITask.ID, W>`** — Scans `@WorkLicense`-annotated properties, returns task-to-worker map.

**`@WorkLicense`** — Meta-annotation. Apply to custom annotation classes to mark them as task licenses.

**`WorkLicense.getTaskID(annotation: Annotation): ITask.ID`** — Extracts ID from annotation instance using reflection on constructor parameters.

**`WorkLicense.getTaskID(cls: Class<*>, vararg props: String): ITask.ID`** — Constructs ID from class name and property strings.

**`WorkLicense.isTaskID(taskID: ITask.ID, forLicense: Class<*>): Boolean`** — Checks if task ID matches a license class.

## Configuration

No configuration required. All behavior driven by annotation declarations and registration calls.

## Gotchas

- `@WorkLicense` targets `ANNOTATION_CLASS`, not `PROPERTY`. Applying it directly to a worker property has no effect.
- `licensedWorkers()` uses `kotlin-reflect`. The `kotlin-reflect` artifact must be on the classpath.
- `licensedWorkers()` reads `declaredMemberProperties` — inherited properties from superclasses are not discovered.
- `ITask.ID.license` stores `Class.simpleName`. Two annotation classes with the same simple name in different packages produce identical license strings.
- Worker properties must be accessible via reflection. Private properties require `isAccessible = true` (handled internally by `licensedWorkers()`).
