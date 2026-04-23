# COBRA Framework -- Implementation Notes

## APIs

- **[kotlin-reflect]** `KProperty1.isAccessible = true` -- enables reading private/protected workshop properties during license discovery.
- **[kotlin-reflect]** `KClass.declaredMemberProperties` -- returns only properties declared on the concrete class, not inherited ones.
- **[kotlin-reflect]** `KClass.primaryConstructor` -- used to extract annotation parameter names; null for Java-only annotations.
- **[kotlin-reflect]** `Annotation.annotationClass.hasAnnotation<WorkLicense>()` -- checks the meta-annotation on a custom annotation class.
- **[kotlinx-coroutines]** `FlowCollector<R>.emit(value)` -- suspending emit inside `IWorker.work`; must be called within a `flow { }` builder.
- **[kotlinx-coroutines]** `flow { with(worker) { work(task) } }.collect { }` -- canonical pattern to invoke a worker and collect products.

## Libraries

- `org.jetbrains.kotlin:kotlin-reflect` -- runtime reflection for annotation and property discovery in `AbcWorkshop`.
- `org.jetbrains.kotlinx:kotlinx-coroutines-core` -- coroutine flow infrastructure for worker product emission.

## Developer Instructions

- Custom license annotations require `@WorkLicense` as a meta-annotation and `@Retention(RUNTIME)`.
- Custom license annotations require `@Target(PROPERTY, FIELD)` to be discoverable on workshop properties.
- License annotation constructor parameters become the `props` set of the generated `ITask.ID`.
- The license annotation's simple class name becomes the `license` field of the generated `ITask.ID`.
- `licensedWorkers()` scans `declaredMemberProperties` only; inherited workers from superclass workshops are not discovered.
- Duplicate Task IDs within a single workshop: the last property in reflection iteration order wins silently.
- `IDispatcher` implementations must handle concurrent access if shared across coroutines.
- `ITask.ID` equality is structural and case-sensitive; `"Expr"` and `"expr"` are distinct licenses.
- Workers are SAM-convertible: `IWorker<T, R> { task -> emit(result) }` via Kotlin SAM syntax.
- Build: `./gradlew build`. Test: `./gradlew test`.
- Published via JitPack: `com.github.jhu-seclab-cobra:framework:0.1.0`.
