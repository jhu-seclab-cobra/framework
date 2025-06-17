package edu.jhu.cobra.framework

/**
 * Dispatcher for mapping tasks to workers and allocating them dynamically.
 *
 * Manages registration and lookup of workers ([IWorker]) by task identifier ([ITask.ID]).
 *
 * @param Worker The type of worker managed by this dispatcher; must extend [IWorker]. Non-null.
 * @see [IWorker]
 * @see [ITask.ID]
 */
interface IDispatcher<Worker : IWorker<*, *>> {

    /**
     * Returns a worker capable of handling the specified task.
     *
     * @param forTask The [ITask.ID] of the task to dispatch. Non-null.
     * @return The [Worker] registered for the task, or null if none is available.
     */
    fun dispatch(forTask: ITask.ID): Worker?

    /**
     * Registers a worker for a specific task identifier.
     *
     * @param forTask The [ITask.ID] to associate with the worker. Non-null.
     * @param toWorker The [Worker] to register. Non-null.
     */
    fun register(forTask: ITask.ID, toWorker: Worker)
}
