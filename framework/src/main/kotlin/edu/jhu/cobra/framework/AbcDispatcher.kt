package edu.jhu.cobra.framework

/**
 * Default implementation of [IDispatcher] with ATTACH/REPLACE registration semantics.
 *
 * Manages a map of task ID to worker. Supports chaining multiple workers for the same
 * task ID via [RegisterMode.ATTACH] (default) — workers execute in registration order.
 *
 * @param W The type of worker managed by this dispatcher.
 */
open class AbcDispatcher<W : IWorker<*, *>> : IDispatcher<W> {
    private val workers = mutableMapOf<ITask.ID, W>()

    override fun dispatch(forTask: ITask.ID): W? = workers[forTask]

    @Suppress("UNCHECKED_CAST")
    override fun register(
        forTask: ITask.ID,
        toWorker: W,
        mode: RegisterMode,
    ) {
        when (mode) {
            RegisterMode.REPLACE -> workers[forTask] = toWorker
            RegisterMode.ATTACH -> {
                val existing = workers[forTask]
                if (existing != null) {
                    val chained =
                        object : IWorker<ITask, Any?> {
                            override fun work(task: ITask): Any? {
                                (existing as IWorker<ITask, Any?>).work(task)
                                return (toWorker as IWorker<ITask, Any?>).work(task)
                            }
                        }
                    workers[forTask] = chained as W
                } else {
                    workers[forTask] = toWorker
                }
            }
        }
    }

    /**
     * Registers all licensed workers from a workshop, respecting each annotation's mode.
     */
    fun register(workshop: AbcWorkshop<W>) {
        workshop.registerTo(this)
    }
}
