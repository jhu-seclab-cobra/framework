package edu.jhu.cobra.framework

/**
 * Default implementation of [IDispatcher] with ATTACH/REPLACE registration semantics.
 *
 * Manages a map of task ID to worker. Supports chaining multiple workers for the same
 * task ID via [RegisterMode.ATTACH] (default) — workers execute in registration order.
 *
 * @param W The type of worker managed by this dispatcher.
 */
public open class AbcDispatcher<W : IWorker<*, *>> : IDispatcher<W> {
    private val workers = mutableMapOf<ITask.ID, W>()

    override fun dispatch(forTask: ITask.ID): W? = workers[forTask]

    override fun register(
        forTask: ITask.ID,
        toWorker: W,
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
    @Suppress("UNCHECKED_CAST")
    private fun chain(
        existing: W,
        toWorker: W,
    ): W {
        val chained =
            object : IWorker<ITask, Any?> {
                override fun work(task: ITask): Any? {
                    (existing as IWorker<ITask, Any?>).work(task)
                    return (toWorker as IWorker<ITask, Any?>).work(task)
                }
            }
        return chained as W
    }

    /**
     * Registers all licensed workers from a workshop, respecting each annotation's mode.
     */
    public fun register(workshop: AbcWorkshop<W>) {
        workshop.registerTo(this)
    }
}
