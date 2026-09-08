package edu.jhu.cobra.framework

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

/**
 * Workshop for managing and exposing licensed workers.
 *
 * Provides a reflective mechanism to discover and retrieve workers ([IWorker])
 * that are licensed for specific tasks within the same workshop instance.
 * All workers in a workshop may share resources via the enclosing instance.
 *
 * @param W The type of worker managed by this workshop; must extend [IWorker].
 */
public abstract class AbcWorkshop<W : IWorker<*, *>> {
    /**
     * Returns a map of all licensed workers in this workshop, keyed by their task identifiers.
     *
     * Uses reflection to find properties annotated with a [WorkLicense]-marked annotation
     * and maps each to its corresponding [ITask.ID].
     *
     * @return Map from [ITask.ID] to [W].
     * @throws IllegalStateException when a licensed property holds a null value.
     */
    public fun licensedWorkers(): Map<ITask.ID, W> =
        licensedProperties()
            .flatMap { (worker, licenses) -> licenses.map { license -> WorkLicense.getTaskID(license) to worker } }
            .toMap()

    /**
     * Registers all licensed workers from this workshop into the given dispatcher.
     * The [RegisterMode] is read from the annotation's [RegisterMode]-typed property — the same
     * property [WorkLicense.getTaskID] excludes; defaults to [RegisterMode.ATTACH] when the
     * annotation declares none.
     *
     * @throws IllegalStateException when a licensed property holds a null value.
     */
    public fun registerTo(dispatcher: IDispatcher<W>) {
        licensedProperties()
            .flatMap { (worker, licenses) ->
                licenses.map { license -> Triple(WorkLicense.getTaskID(license), worker, extractMode(license)) }
            }.forEach { (taskId, worker, mode) -> dispatcher.register(taskId, worker, mode) }
    }

    // Reflection erases W, so worker-typed properties are matched on the IWorker classifier and
    // cast to the caller-declared W; the value is nullable because the property itself may be.
    private fun licensedProperties(): Sequence<Pair<W, List<Annotation>>> {
        @Suppress("UNCHECKED_CAST")
        return this::class
            .declaredMemberProperties
            .asSequence()
            .filter { p -> (p.returnType.classifier as? KClass<*>)?.isSubclassOf(IWorker::class) == true }
            .map { p -> p as KProperty1<AbcWorkshop<W>, W?> }
            .map { p -> p to p.annotations.filter { annotation -> annotation.annotationClass.hasAnnotation<WorkLicense>() } }
            .filter { (_, licenses) -> licenses.isNotEmpty() }
            .map { (prop, licenses) -> requireWorker(prop) to licenses }
    }

    // A null licensed worker would make dispatch hand callers null for a task the workshop claims to serve.
    private fun requireWorker(prop: KProperty1<AbcWorkshop<W>, W?>): W =
        prop.apply { isAccessible = true }.get(this)
            ?: error("workshop ${this::class.qualifiedName} licensed property ${prop.name} is null")

    private fun extractMode(annotation: Annotation): RegisterMode {
        val annoCls = annotation.annotationClass
        val modeProp =
            annoCls.declaredMemberProperties
                .firstOrNull { it.returnType.classifier == RegisterMode::class }
                ?: return RegisterMode.ATTACH
        return modeProp.call(annotation) as? RegisterMode
            ?: error("annotation ${annoCls.qualifiedName} property ${modeProp.name} holds a non-RegisterMode value")
    }
}
