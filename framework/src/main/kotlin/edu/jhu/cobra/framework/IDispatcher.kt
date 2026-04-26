package edu.jhu.cobra.framework

/**
 * Dispatcher for mapping tasks to workers and allocating them dynamically.
 *
 * Manages registration and lookup of workers ([IWorker]) by task identifier ([ITask.ID]).
 *
 * @param Worker The type of worker managed by this dispatcher; must extend [IWorker].
 */
interface IDispatcher<Worker : IWorker<*, *>> {

    /**
     * Returns a worker capable of handling the specified task.
     *
     * @param forTask The [ITask.ID] of the task to dispatch.
     * @return The [Worker] registered for the task, or null if none is available.
     */
    fun dispatch(forTask: ITask.ID): Worker?

    /**
     * Registers a worker for a specific task identifier.
     *
     * @param forTask The [ITask.ID] to associate with the worker.
     * @param toWorker The [Worker] to register.
     */
    fun register(forTask: ITask.ID, toWorker: Worker)
}
