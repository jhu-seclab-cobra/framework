# COBRA Framework -- Implementation Notes

## APIs

- **[kotlin-reflect]** `import kotlin.reflect.jvm.isAccessible` -- `KProperty1.isAccessible = true` before `get(...)` on private/protected workshop properties.
- **[kotlin-reflect]** `import kotlin.reflect.full.declaredMemberProperties` -- properties declared on the concrete class only; inherited properties excluded.
- **[kotlin-reflect]** `import kotlin.reflect.full.primaryConstructor` -- annotation parameter names; null for Java-only annotations.
- **[kotlin-reflect]** `import kotlin.reflect.full.hasAnnotation` -- `annotation.annotationClass.hasAnnotation<WorkLicense>()` checks the meta-annotation.
- **[kotlin-reflect]** `import kotlin.reflect.full.isSubclassOf` -- `(returnType.classifier as? KClass<*>)?.isSubclassOf(IWorker::class)` filters worker-typed properties; generic arguments are erased.

## Libraries

- `org.jetbrains.kotlin:kotlin-reflect` (version.ref `kotlin`, alias `libs.kotlin.reflect`) -- runtime reflection for annotation and property discovery.
- `com.github.jhu-seclab-cobra.commons-graph:graph` (version.ref `cobra`, alias `libs.cobra.commons.graph`) -- declared with `api` scope; exposed to consumers.

## Developer Instructions

- Custom license annotations: `@WorkLicense` meta-annotation + `@Retention(RUNTIME)` + `@Target(PROPERTY)`.
- License annotation constructor parameters → `props` set of `ITask.ID`; values converted with `toString()`.
- License annotation fully qualified class name (`Class.name`) → `license` field of `ITask.ID`.
- A `RegisterMode`-typed annotation property is excluded from `props` and selects the mode in `AbcWorkshop.registerTo`; absent → `ATTACH`.
- `licensedWorkers()` scans `declaredMemberProperties` only. Inherited workers not discovered.
- Duplicate Task IDs within one workshop: `licensedWorkers()` keeps the last property in reflection order; `registerTo` applies the dispatcher's `RegisterMode`.
- `AbcDispatcher` is not synchronized. Callers sharing a dispatcher across threads manage synchronization.
- `ITask.ID` equality: structural, case-sensitive. `"Expr"` ≠ `"expr"`.
- `IWorker` is a `fun interface`: `IWorker<T, R> { task -> result }` via SAM syntax.
- `IWorker.work` is synchronous. Concurrency (coroutines, thread pools) managed by the caller.
- Build: `./gradlew build`. Test: `./gradlew test`. Gate: `./gradlew detekt ktlintCheck build`.
- Published via JitPack: `com.github.jhu-seclab-cobra:framework:0.1.0`.
