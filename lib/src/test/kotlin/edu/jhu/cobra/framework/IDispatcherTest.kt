package edu.jhu.cobra.framework

import kotlinx.coroutines.flow.FlowCollector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith
import kotlin.test.BeforeTest

class IDispatcherTest {
    
    private lateinit var dispatcher: TestDispatcher
    private lateinit var worker: TestWorker
    
    @BeforeTest
    fun setup() {
        dispatcher = TestDispatcher()
        worker = TestWorker()
    }
    
    @Test
    fun `test register and dispatch with single worker`() {
        val taskId = ITask.ID("TestTask", setOf("prop1"))
        dispatcher.register(taskId, worker)
        assertEquals(worker, dispatcher.dispatch(taskId))
    }
    
    @Test
    fun `test register and dispatch with multiple workers`() {
        val worker2 = TestWorker()
        val taskId1 = ITask.ID("Task1", setOf("prop1"))
        val taskId2 = ITask.ID("Task2", setOf("prop2"))
        
        dispatcher.register(taskId1, worker)
        dispatcher.register(taskId2, worker2)
        
        assertEquals(worker, dispatcher.dispatch(taskId1))
        assertEquals(worker2, dispatcher.dispatch(taskId2))
    }
    
    @Test
    fun `test dispatch returns null for unregistered task`() {
        val taskId = ITask.ID("NonExistentTask", setOf("prop1"))
        assertNull(dispatcher.dispatch(taskId))
    }
    
    @Test
    fun `test register overwrites existing worker`() {
        val taskId = ITask.ID("TestTask", setOf("prop1"))
        val worker2 = TestWorker()
        
        dispatcher.register(taskId, worker)
        dispatcher.register(taskId, worker2)
        
        assertEquals(worker2, dispatcher.dispatch(taskId))
    }
    
    @Test
    fun `test register with empty task ID`() {
        val taskId = ITask.ID("", setOf("prop1"))
        dispatcher.register(taskId, worker)
        assertEquals(worker, dispatcher.dispatch(taskId))
    }
    
    @Test
    fun `test register with empty properties`() {
        val taskId = ITask.ID("TestTask", emptySet())
        dispatcher.register(taskId, worker)
        assertEquals(worker, dispatcher.dispatch(taskId))
    }
    
    @Test
    fun `test register with special characters in task ID`() {
        val taskId = ITask.ID("Test@Task#123", setOf("prop1"))
        dispatcher.register(taskId, worker)
        assertEquals(worker, dispatcher.dispatch(taskId))
    }
}