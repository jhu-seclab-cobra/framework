package edu.jhu.cobra.framework

/**
 * Registration mode for worker dispatch.
 */
enum class RegisterMode {
    /** Replaces any existing worker for the same task ID. */
    REPLACE,

    /** Chains with existing worker — both execute in registration order. */
    ATTACH,
}

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
     * Default mode is [RegisterMode.ATTACH].
     *
     * @param forTask The [ITask.ID] to associate with the worker.
     * @param toWorker The [Worker] to register.
     * @param mode [RegisterMode.REPLACE] overwrites existing; [RegisterMode.ATTACH] chains with existing.
     */
    fun register(
        forTask: ITask.ID,
        toWorker: Worker,
        mode: RegisterMode = RegisterMode.ATTACH,
    )
}
