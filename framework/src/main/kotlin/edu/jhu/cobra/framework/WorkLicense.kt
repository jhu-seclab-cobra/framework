package edu.jhu.cobra.framework

import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Meta-annotation for marking custom annotation classes as task licenses.
 *
 * Apply to annotation classes (not properties). Workshop discovery scans for
 * annotations carrying this meta-annotation to map workers to task IDs.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class WorkLicense {
    public companion object
}

/**
 * Extracts a task ID from a [WorkLicense]-annotated annotation instance.
 *
 * [RegisterMode]-typed properties are registration metadata, not task
 * discriminants; they are excluded so the ID matches one built from the
 * task's own properties alone.
 *
 * @param annotation The annotation instance.
 * @return The generated [ITask.ID].
 * @throws IllegalStateException when the annotation class has no primary constructor
 *   or a constructor parameter has no matching property.
 */
public fun WorkLicense.Companion.getTaskID(annotation: Annotation): ITask.ID {
    val annoCls = annotation.annotationClass
    val primaryCtor =
        annoCls.primaryConstructor
            ?: error("annotation ${annoCls.qualifiedName} has no primary constructor")
    val declaredProps = annoCls.declaredMemberProperties
    val annotatedProps =
        primaryCtor.parameters
            .map { param ->
                declaredProps.firstOrNull { prop -> prop.name == param.name }
                    ?: error("annotation ${annoCls.qualifiedName} has no property named ${param.name}")
            }
    val innerStr =
        annotatedProps
            .filterNot { prop -> prop.returnType.classifier == RegisterMode::class }
            .map { prop -> prop.call(annotation).toString() }
    return WorkLicense.getTaskID(annoCls.java, innerStr)
}

/**
 * Constructs a task ID from a class and property strings.
 */
public fun WorkLicense.Companion.getTaskID(
    cls: Class<*>,
    vararg props: String,
): ITask.ID = ITask.ID(cls.name, props.toSet())

/**
 * Constructs a task ID from a class and a collection of property strings.
 */
public fun WorkLicense.Companion.getTaskID(
    cls: Class<*>,
    props: Collection<String>,
): ITask.ID = ITask.ID(cls.name, props.toSet())

/**
 * Checks if a given task ID matches a specified license class.
 */
public fun WorkLicense.Companion.isTaskID(
    taskID: ITask.ID,
    forLicense: Class<*>,
): Boolean = taskID.license == forLicense.name
