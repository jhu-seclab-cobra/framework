package edu.jhu.cobra.framework

/**
 * Represents a unit of work that can be performed by a worker.
 *
 * Each task has a unique identifier ([uid]) for dispatching and mapping to workers.
 */
public interface ITask {
    /**
     * Unique identifier for a task, consisting of a license and associated properties.
     *
     * @property license The license identifier — the fully qualified name of the license class.
     * @property props The set of properties describing the task.
     */
    public data class ID(
        val license: String,
        val props: Set<String>,
    )

    /**
     * Unique identifier for the task instance.
     */
    public val uid: ID
}
