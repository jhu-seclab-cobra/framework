# COBRA Framework -- Implementation Notes

## APIs

- **[kotlin-reflect]** `KProperty1.isAccessible = true` -- enables reading private/protected workshop properties during license discovery.
- **[kotlin-reflect]** `KClass.declaredMemberProperties` -- returns only properties declared on the concrete class, not inherited ones.
- **[kotlin-reflect]** `KClass.primaryConstructor` -- extracts annotation parameter names; null for Java-only annotations.
- **[kotlin-reflect]** `Annotation.annotationClass.hasAnnotation<WorkLicense>()` -- checks meta-annotation on custom annotation class.

## Libraries

- `org.jetbrains.kotlin:kotlin-reflect` -- runtime reflection for annotation and property discovery in `AbcWorkshop`.

## Developer Instructions

- Custom license annotations: `@WorkLicense` meta-annotation + `@Retention(RUNTIME)` + `@Target(PROPERTY)`.
- License annotation constructor parameters → `props` set of `ITask.ID`.
- License annotation simple class name → `license` field of `ITask.ID`.
- `licensedWorkers()` scans `declaredMemberProperties` only. Inherited workers not discovered.
- Duplicate Task IDs within one workshop: last property in reflection order wins.
- `IDispatcher` implementations: handle concurrent access if shared across threads.
- `ITask.ID` equality: structural, case-sensitive. `"Expr"` ≠ `"expr"`.
- `IWorker` is a `fun interface`: `IWorker<T, R> { task -> result }` via SAM syntax.
- `IWorker.work` is synchronous. Concurrency (coroutines, thread pools) managed by the caller.
- Build: `./gradlew build`. Test: `./gradlew test`.
- Published via JitPack: `com.github.jhu-seclab-cobra:framework:0.1.0`.
