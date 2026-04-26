package edu.jhu.cobra.framework

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
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
    fun licensedWorkers(): Map<ITask.ID, W> = this::class.declaredMemberProperties.asSequence()
        .filterIsInstance<KProperty1<AbcWorkshop<W>, W>>()
        .map { p -> p to p.annotations.filter { it.annotationClass.hasAnnotation<WorkLicense>() } }
        .filter { (_, licenses) -> licenses.isNotEmpty() }
        .map { (prop, licenses) -> prop.apply { isAccessible = true }.get(this) to licenses }
        .flatMap { (prop, licenses) -> licenses.map { license -> WorkLicense.getTaskID(license) to prop } }.toMap()
}
