package edu.jhu.cobra.framework

/**
 * Default implementation of [IDispatcher] with ATTACH/REPLACE registration semantics.
 *
 * Manages a map of task ID to worker. Supports chaining multiple workers for the same
 * task ID via [RegisterMode.ATTACH] (default) — workers execute in registration order.
 *
 * @param T The type of task handled by this dispatcher's workers.
 * @param R The type of result produced by this dispatcher's workers.
 */
public open class AbcDispatcher<T : ITask, R> : IDispatcher<IWorker<T, R>> {
    private val workers = mutableMapOf<ITask.ID, IWorker<T, R>>()

    override fun dispatch(forTask: ITask.ID): IWorker<T, R>? = workers[forTask]

    override fun register(
        forTask: ITask.ID,
        toWorker: IWorker<T, R>,
        mode: RegisterMode,
    ) {
        if (mode == RegisterMode.REPLACE) {
            workers[forTask] = toWorker
            return
        }
        val existing = workers[forTask]
        workers[forTask] = if (existing == null) toWorker else chain(existing, toWorker)
    }

    // Runs the existing worker, then the new one, returning the new one's result.
    private fun chain(
        existing: IWorker<T, R>,
        toWorker: IWorker<T, R>,
    ): IWorker<T, R> =
        IWorker { task ->
            existing.work(task)
            toWorker.work(task)
        }
}
