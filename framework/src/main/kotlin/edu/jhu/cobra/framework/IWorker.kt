package edu.jhu.cobra.framework

import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Represents a unit of work that can be performed by a worker.
 *
 * Each task has a unique identifier ([uid]) for dispatching and mapping to workers.
 */
interface ITask {

    /**
     * Unique identifier for a task, consisting of a license and associated properties.
     *
     * @property license The license or type name for the task.
     * @property props The set of properties describing the task.
     */
    data class ID(val license: String, val props: Set<String>)

    /**
     * Unique identifier for the task instance.
     */
    val uid: ID
}

/**
 * Worker capable of performing a task and returning a result.
 *
 * Synchronous. Concurrency managed by the caller, not the worker.
 * Implementable as a SAM lambda via Kotlin's `fun interface`.
 *
 * @param T The type of task this worker can handle; must extend [ITask].
 * @param R The type of result this worker produces.
 */
fun interface IWorker<T : ITask, R> {
    /**
     * Performs the given task and returns a result.
     *
     * @param task The task to perform.
     * @return The result of processing the task.
     */
    fun work(task: T): R
}

/**
 * Meta-annotation for marking custom annotation classes as task licenses.
 *
 * Apply to annotation classes (not properties). Workshop discovery scans for
 * annotations carrying this meta-annotation to map workers to task IDs.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkLicense {
    companion object
}

/**
 * Extracts a task ID from a [WorkLicense]-annotated annotation instance.
 *
 * @param annotation The annotation instance.
 * @return The generated [ITask.ID].
 */
fun WorkLicense.Companion.getTaskID(annotation: Annotation): ITask.ID {
    val annoCls = annotation.annotationClass
    val annotatedProps = annoCls.primaryConstructor!!.parameters.map { it.name }
        .map { tar -> annoCls.declaredMemberProperties.first { it.name == tar } }
    val innerStr = annotatedProps.map { it.call(annotation).toString() }
    return WorkLicense.getTaskID(annotation.annotationClass.java, innerStr)
}

/**
 * Constructs a task ID from a class and property strings.
 */
fun WorkLicense.Companion.getTaskID(cls: Class<*>, vararg props: String) =
    ITask.ID(cls.simpleName, props.toSet())

/**
 * Constructs a task ID from a class and a collection of property strings.
 */
fun WorkLicense.Companion.getTaskID(cls: Class<*>, props: Collection<String>) =
    ITask.ID(cls.simpleName, props.toSet())

/**
 * Checks if a given task ID matches a specified license class.
 */
fun WorkLicense.Companion.isTaskID(taskID: ITask.ID, forLicense: Class<*>) =
    taskID.license == forLicense.simpleName
