/*
 * Unit tests for [AbcDispatcher], the map-backed [IDispatcher] with ATTACH/REPLACE semantics.
 *
 * Registration:
 * - `register and dispatch single worker`: ATTACH on a free ID stores the worker itself
 * - `register with REPLACE on free ID stores worker`: REPLACE on a free ID stores the worker itself
 * - `register and dispatch multiple workers`: multi-worker registry
 * - `register with REPLACE overwrites existing worker`: explicit last-write-wins
 * - `register with ATTACH chains workers in order`: default mode chains both workers
 * - `register with ATTACH chains three workers in order`: chain of chains preserves order
 * - `register with ATTACH returns the last worker's result`: earlier results discarded
 * - `register with REPLACE after ATTACH discards the chain`: REPLACE drops all chained workers
 * - `register same worker for different tasks`: one worker, multiple IDs
 * - `register with empty task ID`: boundary
 * - `register with empty properties`: boundary
 * - `register with special characters`: boundary
 *
 * Dispatch:
 * - `dispatch returns null for unregistered task`: missing registration
 * - `dispatch with identical task IDs replaces on REPLACE`: structural equality
 * - `dispatch is case sensitive`: case sensitivity
 */
package edu.jhu.cobra.framework

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AbcDispatcherTest {
    private lateinit var dispatcher: AbcDispatcher<TestTask, TestResult>
    private lateinit var worker: TestWorker

    @BeforeTest
    fun setUp() {
        dispatcher = AbcDispatcher()
        worker = TestWorker()
    }

    @Test
    fun `register and dispatch single worker`() {
        val taskId = ITask.ID("TestTask", setOf("prop1"))
        dispatcher.register(taskId, worker)
        assertEquals(worker, dispatcher.dispatch(taskId))
    }

    @Test
    fun `register with REPLACE on free ID stores worker`() {
        val taskId = ITask.ID("TestTask", setOf("prop1"))
        dispatcher.register(taskId, worker, RegisterMode.REPLACE)
        assertEquals(worker, dispatcher.dispatch(taskId))
    }

    @Test
    fun `register and dispatch multiple workers`() {
        val worker2 = TestWorker()
        val id1 = ITask.ID("Task1", setOf("prop1"))
        val id2 = ITask.ID("Task2", setOf("prop2"))
        dispatcher.register(id1, worker)
        dispatcher.register(id2, worker2)
        assertEquals(worker, dispatcher.dispatch(id1))
        assertEquals(worker2, dispatcher.dispatch(id2))
    }

    @Test
    fun `dispatch returns null for unregistered task`() {
        assertNull(dispatcher.dispatch(ITask.ID("NonExistent", setOf("p"))))
    }

    @Test
    fun `register with REPLACE overwrites existing worker`() {
        val id = ITask.ID("TestTask", setOf("prop1"))
        val worker2 = TestWorker()
        dispatcher.register(id, worker)
        dispatcher.register(id, worker2, RegisterMode.REPLACE)
        assertEquals(worker2, dispatcher.dispatch(id))
    }

    @Test
    fun `register with ATTACH chains workers in order`() {
        val calls = mutableListOf<String>()
        val chaining = AbcDispatcher<TestTask, Unit>()
        val id = ITask.ID("TestTask", setOf("prop1"))
        chaining.register(id, { calls.add("first") })
        chaining.register(id, { calls.add("second") })
        chaining.dispatch(id)!!.work(TestTask(id))
        assertEquals(listOf("first", "second"), calls)
    }

    @Test
    fun `register with ATTACH chains three workers in order`() {
        val calls = mutableListOf<String>()
        val chaining = AbcDispatcher<TestTask, Unit>()
        val id = ITask.ID("TestTask", setOf("prop1"))
        chaining.register(id, { calls.add("first") })
        chaining.register(id, { calls.add("second") })
        chaining.register(id, { calls.add("third") })
        chaining.dispatch(id)!!.work(TestTask(id))
        assertEquals(listOf("first", "second", "third"), calls)
    }

    @Test
    fun `register with ATTACH returns the last worker's result`() {
        val chaining = AbcDispatcher<TestTask, String>()
        val id = ITask.ID("TestTask", setOf("prop1"))
        chaining.register(id, { "first" })
        chaining.register(id, { "second" })
        assertEquals("second", chaining.dispatch(id)!!.work(TestTask(id)))
    }

    @Test
    fun `register with REPLACE after ATTACH discards the chain`() {
        val calls = mutableListOf<String>()
        val chaining = AbcDispatcher<TestTask, Unit>()
        val id = ITask.ID("TestTask", setOf("prop1"))
        chaining.register(id, { calls.add("first") })
        chaining.register(id, { calls.add("second") })
        chaining.register(id, { calls.add("only") }, RegisterMode.REPLACE)
        chaining.dispatch(id)!!.work(TestTask(id))
        assertEquals(listOf("only"), calls)
    }

    @Test
    fun `register with empty task ID`() {
        val id = ITask.ID("", setOf("prop1"))
        dispatcher.register(id, worker)
        assertEquals(worker, dispatcher.dispatch(id))
    }

    @Test
    fun `register with empty properties`() {
        val id = ITask.ID("TestTask", emptySet())
        dispatcher.register(id, worker)
        assertEquals(worker, dispatcher.dispatch(id))
    }

    @Test
    fun `register with special characters`() {
        val id = ITask.ID("Test@Task#123", setOf("prop1"))
        dispatcher.register(id, worker)
        assertEquals(worker, dispatcher.dispatch(id))
    }

    @Test
    fun `register same worker for different tasks`() {
        val id1 = ITask.ID("Task1", setOf("p1"))
        val id2 = ITask.ID("Task2", setOf("p2"))
        dispatcher.register(id1, worker)
        dispatcher.register(id2, worker)
        assertEquals(worker, dispatcher.dispatch(id1))
        assertEquals(worker, dispatcher.dispatch(id2))
    }

    @Test
    fun `dispatch with identical task IDs replaces on REPLACE`() {
        val id1 = ITask.ID("TestTask", setOf("prop1"))
        val id2 = ITask.ID("TestTask", setOf("prop1"))
        val worker2 = TestWorker()
        dispatcher.register(id1, worker)
        dispatcher.register(id2, worker2, RegisterMode.REPLACE)
        assertEquals(worker2, dispatcher.dispatch(id1))
    }

    @Test
    fun `dispatch is case sensitive`() {
        val id1 = ITask.ID("TestTask", setOf("prop1"))
        val id2 = ITask.ID("testtask", setOf("prop1"))
        val worker2 = TestWorker()
        dispatcher.register(id1, worker)
        dispatcher.register(id2, worker2)
        assertEquals(worker, dispatcher.dispatch(id1))
        assertEquals(worker2, dispatcher.dispatch(id2))
    }
}
