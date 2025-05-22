package edu.jhu.cobra.framework

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible

/**
 * Abstract class representing a workshop where specific types of workers are located.
 * The workers are capable of performing interpreting tasks, and they are dynamically retrieved based on
 * their licensing annotations, ensuring that only appropriately licensed workers handle specific tasks.
 *
 * Please notice that the [W] type parameter is a worker that extends [IWorker], and the [ITask] and [IProduct] types
 * are defined by the worker.
 * All workers located in the same workshop can share resources, which may be useful for some specific worker.
 *
 * @param W The type parameter representing the worker that extends [IWorker]
 */
abstract class AbcWorkshop<W : IWorker<*, *>> {

    /**
     * Retrieves all workers under this workshop that have been licensed to perform specific tasks.
     * Each worker is associated with a unique task identifier, derived from their licensing annotations.
     *
     * This method uses reflection to inspect properties of the workshop class, filtering for
     * properties representing workers and then checking those properties for licensing annotations.
     * Each licensed worker is then mapped to the task identifier specified by their license.
     *
     * @return A map where the keys are task identifiers ([ITask.ID]) and the values are workers ([W]) capable of handling those tasks.
     */
    fun licensedWorkers(): Map<ITask.ID, W> = this::class.declaredMemberProperties.asSequence()
        .filterIsInstance<KProperty1<AbcWorkshop<W>, W>>() // get all workers from the workshop
        .map { p -> p to p.annotations.filter { it.annotationClass.hasAnnotation<WorkLicense>() } }
        .filter { (_, licenses) -> licenses.isNotEmpty() } // filter out workers without licenses
        .map { (prop, licenses) -> prop.apply { isAccessible = true }.get(this) to licenses }
        .flatMap { (prop, licenses) -> licenses.map { license -> WorkLicense.getTaskID(license) to prop } }.toMap()

}
