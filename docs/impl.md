# COBRA Framework -- Implementation Notes

## APIs

- **[kotlin-reflect]** `import kotlin.reflect.jvm.isAccessible` -- `KProperty1.isAccessible = true` before `get(...)` on private/protected workshop properties.
- **[kotlin-reflect]** `import kotlin.reflect.full.declaredMemberProperties` -- properties declared on the concrete class only; inherited properties excluded.
- **[kotlin-reflect]** `import kotlin.reflect.full.primaryConstructor` -- annotation parameter names; null for Java-only annotations.
- **[kotlin-reflect]** `import kotlin.reflect.full.hasAnnotation` -- `annotation.annotationClass.hasAnnotation<WorkLicense>()` checks the meta-annotation.
- **[kotlin-reflect]** `import kotlin.reflect.full.isSubclassOf` -- `(returnType.classifier as? KClass<*>)?.isSubclassOf(IWorker::class)` filters worker-typed properties; generic arguments are erased.

## Libraries

- `org.jetbrains.kotlin:kotlin-reflect` (version.ref `kotlin`, alias `libs.kotlin.reflect`) -- runtime reflection for annotation and property discovery.
- `com.github.jhu-seclab-cobra.commons-graph:graph` (version.ref `cobra`, alias `libs.cobra.commons.graph`) -- `api` scope by design: framework re-exports the graph API so a consumer declares framework alone. Framework sources import nothing from it; the dependency is not unused.

## Developer Instructions

- Custom license annotation setup: `@WorkLicense` + `@Retention(RUNTIME)` + `@Target(PROPERTY)`.
- Discovery, dispatch keys, duplicate registrations, concurrency and Registration Mode follow the [design](design.md).
- Build: `./gradlew build`. Test: `./gradlew test`. Quality commands: `./gradlew detekt ktlintCheck build`.
- Published through JitPack: `com.github.jhu-seclab-cobra:framework:0.1.0`.
