package edu.jhu.cobra.framework

/**
 * Worker capable of performing a task and returning a result.
 *
 * Synchronous. Concurrency managed by the caller, not the worker.
 * Implementable as a SAM lambda via Kotlin's `fun interface`.
 *
 * @param T The type of task this worker can handle; must extend [ITask].
 * @param R The type of result this worker produces.
 */
public fun interface IWorker<T : ITask, R> {
    /**
     * Performs the given task and returns a result.
     *
     * @param task The task to perform.
     * @return The result of processing the task.
     */
    public fun work(task: T): R
}
