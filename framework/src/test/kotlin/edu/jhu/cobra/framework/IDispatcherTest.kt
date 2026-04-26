/**
 * Unit tests for [IDispatcher].
 *
 * Registration:
 * - `register and dispatch single worker`: basic registration
 * - `register and dispatch multiple workers`: multi-worker registry
 * - `register overwrites existing worker`: last-write-wins
 * - `register same worker for different tasks`: one worker, multiple IDs
 * - `register with empty task ID`: boundary
 * - `register with empty properties`: boundary
 * - `register with special characters`: boundary
 *
 * Dispatch:
 * - `dispatch returns null for unregistered task`: missing registration
 * - `dispatch with identical task IDs`: structural equality
 * - `dispatch is case sensitive`: case sensitivity
 */
package edu.jhu.cobra.framework

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.BeforeTest

internal class IDispatcherTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var worker: TestWorker

    @BeforeTest
    fun setUp() {
        dispatcher = TestDispatcher()
        worker = TestWorker()
    }

    @Test
    fun `register and dispatch single worker`() {
        val taskId = ITask.ID("TestTask", setOf("prop1"))
        dispatcher.register(taskId, worker)
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
    fun `register overwrites existing worker`() {
        val id = ITask.ID("TestTask", setOf("prop1"))
        val worker2 = TestWorker()
        dispatcher.register(id, worker)
        dispatcher.register(id, worker2)
        assertEquals(worker2, dispatcher.dispatch(id))
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
    fun `dispatch with identical task IDs`() {
        val id1 = ITask.ID("TestTask", setOf("prop1"))
        val id2 = ITask.ID("TestTask", setOf("prop1"))
        val worker2 = TestWorker()
        dispatcher.register(id1, worker)
        dispatcher.register(id2, worker2)
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
