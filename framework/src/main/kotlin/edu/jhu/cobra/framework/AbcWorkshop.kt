package edu.jhu.cobra.framework

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
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
abstract class AbcWorkshop<W : IWorker<*, *>> {

    /**
     * Returns a map of all licensed workers in this workshop, keyed by their task identifiers.
     *
     * Uses reflection to find properties annotated with a [WorkLicense]-marked annotation
     * and maps each to its corresponding [ITask.ID].
     *
     * @return Map from [ITask.ID] to [W].
     */
    fun licensedWorkers(): Map<ITask.ID, W> = licensedProperties()
        .flatMap { (worker, licenses) -> licenses.map { license -> WorkLicense.getTaskID(license) to worker } }.toMap()

    /**
     * Registers all licensed workers from this workshop into the given dispatcher.
     * Reads [RegisterMode] from each annotation's `mode` property (defaults to ATTACH if absent).
     */
    fun registerTo(dispatcher: IDispatcher<W>) {
        licensedProperties()
            .flatMap { (worker, licenses) -> licenses.map { license -> Triple(WorkLicense.getTaskID(license), worker, extractMode(license)) } }
            .forEach { (taskId, worker, mode) -> dispatcher.register(taskId, worker, mode) }
    }

    /**
     * Discovers declared properties whose return type is a worker and that carry at least one
     * [WorkLicense]-marked annotation, paired with those annotations.
     *
     * Generic reflection erases [W], so the filter checks the return-type classifier against
     * [IWorker]; the cast to [W] is the caller-declared worker type of this workshop.
     */
    private fun licensedProperties(): Sequence<Pair<W, List<Annotation>>> {
        @Suppress("UNCHECKED_CAST")
        return this::class.declaredMemberProperties.asSequence()
            .filter { p -> (p.returnType.classifier as? KClass<*>)?.isSubclassOf(IWorker::class) == true }
            .map { p -> p as KProperty1<AbcWorkshop<W>, W> }
            .map { p -> p to p.annotations.filter { it.annotationClass.hasAnnotation<WorkLicense>() } }
            .filter { (_, licenses) -> licenses.isNotEmpty() }
            .map { (prop, licenses) -> prop.apply { isAccessible = true }.get(this) to licenses }
    }

    private fun extractMode(annotation: Annotation): RegisterMode {
        annotation.annotationClass.primaryConstructor?.parameters
            ?.firstOrNull { it.name == "mode" }
            ?: return RegisterMode.ATTACH
        val memberProp = annotation.annotationClass.declaredMemberProperties
            .firstOrNull { it.name == "mode" }
            ?: return RegisterMode.ATTACH
        return (memberProp.call(annotation) as? RegisterMode) ?: RegisterMode.ATTACH
    }
}
