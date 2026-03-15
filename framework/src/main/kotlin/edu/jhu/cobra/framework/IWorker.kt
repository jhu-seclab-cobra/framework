package edu.jhu.cobra.framework

import kotlinx.coroutines.flow.FlowCollector
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Represents the product produced by an interpreting worker after performing a task.
 * This interface is intended to be implemented by any class that can serve as an output
 * from interpreting.
 */
interface IProduct

/**
 * Represents a task that a worker can perform.
 * Includes a unique identifier used to differentiate tasks and potentially map them to specific workers.
 */
interface ITask {

    /**
     * Nested data class to uniquely identify a task.
     * @property license A string representing a unique license or identifier.
     * @property props A set of string properties associated with the task.
     */
    data class ID(val license: String, val props: Set<String>)

    /**
     * Unique identifier for the task instance.
     */
    val uid: ID
}

/**
 * Generic interface for a worker capable of performing a task and producing a product.
 *
 * @param T Type parameter extending [ITask], representing the type of task the worker can handle.
 * @param R Type parameter extending [IProduct], representing the type of product the worker produces.
 */
interface IWorker<T : ITask, R : IProduct> {
    /**
     * Performs the given task and emits the results through a [FlowCollector].
     *
     * @param task The task to be performed.
     */
    suspend fun FlowCollector<R>.work(task: T)
}

/**
 * Annotation to denote that a certain class, method, or property is licensed for specific tasks.
 * Used to enforce or verify permissions and capabilities dynamically at runtime.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkLicense {
    companion object
}

/**
 * Generates a task ID from a [WorkLicense] annotated instance.
 *
 * @param annotation The annotation from which to derive the task identifier.
 * @return A task ID constructed from the annotation properties.
 */
fun WorkLicense.Companion.getTaskID(annotation: Annotation): ITask.ID {
    val annoCls = annotation.annotationClass
    val annotatedProps = annoCls.primaryConstructor!!.parameters.map { it.name }
        .map { tar -> annoCls.declaredMemberProperties.first { it.name == tar } }
    val innerStr = annotatedProps.map { it.call(annotation).toString() }
    return WorkLicense.getTaskID(annotation.annotationClass.java, innerStr)
}

/**
 * Generates a task ID from a [WorkLicense] annotated class and props.
 *
 * @param cls The class associated with the task.
 * @param props Vararg of properties to include in the task ID.
 * @return A new [ITask.ID] constructed from the provided class and properties.
 */
fun WorkLicense.Companion.getTaskID(cls: Class<*>, vararg props: String) =
    ITask.ID(cls.simpleName, props.toSet())

/**
 * Generates a task ID from a [WorkLicense] annotated class and props.
 *
 * @param cls The class associated with the task.
 * @param props [Collection] of String to include in the task ID.
 * @return A new [ITask.ID] constructed from the provided class and properties.
 */
fun WorkLicense.Companion.getTaskID(cls: Class<*>, props: Collection<String>) =
    ITask.ID(cls.simpleName, props.toSet())

/**
 * Checks if a given task ID matches a specified license class.
 *
 * @param taskID The task ID to check.
 * @param forLicense The license class to compare against.
 * @return True if the task ID corresponds to the given license class, false otherwise.
 */
fun WorkLicense.Companion.isTaskID(taskID: ITask.ID, forLicense: Class<*>) =
    taskID.license == forLicense.simpleName