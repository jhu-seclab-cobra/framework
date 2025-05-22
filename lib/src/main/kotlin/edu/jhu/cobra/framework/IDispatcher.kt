package edu.jhu.cobra.framework

/**
 * Represents a dispatcher responsible for managing and allocating workers based on task identifiers.
 * The dispatcher maps tasks to workers capable of performing those tasks and facilitates dynamic task handling.
 *
 * @param Worker The type parameter representing the worker that extends [IWorker]
 */
interface IDispatcher<Worker : IWorker<*, *>> {

    /**
     * Retrieves a worker capable of handling a specified task, identified by [forTask].
     * This method is used to dynamically allocate tasks to appropriate workers based on their identifiers.
     *
     * @param forTask The [ITask.ID] of the task for which a worker is required.
     * @return The [Worker] capable of executing the specified task, or null if no suitable worker is available.
     */
    fun dispatch(forTask: ITask.ID): Worker?

    /**
     * Registers a worker with the dispatcher, associating it with a specific task identifier.
     * This method allows the dispatcher to map tasks to their respective workers for future task allocations.
     *
     * @param forTask The [ITask.ID] representing the type of tasks the worker will handle.
     * @param toWorker The [Worker] to be registered and associated with the specified task ID.
     */
    fun register(forTask: ITask.ID, toWorker: Worker)
}
