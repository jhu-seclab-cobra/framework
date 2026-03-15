package edu.jhu.cobra.framework

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AbcWorkshopTest {

    // Test workshop implementations
    class EmptyWorkshop : AbcWorkshop<TestWorker>() {
        val worker1 = TestWorker()
        val worker2 = TestWorker()
    }

    class AllLicensedWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense("worker1")
        val worker1 = TestWorker()

        @TestWorkLicense ("worker2")
        val worker2 = TestWorker()

        @TestWorkLicense ("worker3")
        val worker3 = TestWorker()
    }

    class InheritedWorkshop : TestWorkshop() {
        @TestWorkLicense ("worker3")
        val worker3 = TestWorker()
    }

    class PrivateWorkerWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense ("worker1")
        private val privateWorker = TestWorker()

        fun getPrivateWorker() = privateWorker
    }

    class NullWorkerWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense ("worker1")
        val worker1: TestWorker? = null

        @TestWorkLicense ("worker2")
        val worker2: TestWorker? = null
    }

    class DuplicateTaskIDWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense ("worker1")
        val worker1 = TestWorker()

        @TestWorkLicense ("worker1")
        val worker2 = TestWorker()
    }

    @Test
    fun `test licensedWorkers returns only licensed workers`() {
        val workshop = TestWorkshop()
        val licensedWorkers = workshop.licensedWorkers()
        
        assertEquals(2, licensedWorkers.size)
        assertTrue(licensedWorkers.values.contains(workshop.worker1))
        assertTrue(licensedWorkers.values.contains(workshop.worker2))
        assertFalse(licensedWorkers.values.contains(workshop.unlicensedWorker))
    }

    @Test
    fun `test licensedWorkers with no licensed workers`() {
        val workshop = EmptyWorkshop()
        val licensedWorkers = workshop.licensedWorkers()
        assertTrue(licensedWorkers.isEmpty())
    }

    @Test
    fun `test licensedWorkers with all workers licensed`() {
        val workshop = AllLicensedWorkshop()
        val licensedWorkers = workshop.licensedWorkers()
        
        assertEquals(3, licensedWorkers.size)
        assertTrue(licensedWorkers.values.contains(workshop.worker1))
        assertTrue(licensedWorkers.values.contains(workshop.worker2))
        assertTrue(licensedWorkers.values.contains(workshop.worker3))
    }

    @Test
    fun `test licensedWorkers with inherited workers`() {
        val workshop = InheritedWorkshop()
        val licensedWorkers = workshop.licensedWorkers()
        
        assertEquals(1, licensedWorkers.size)
        assertFalse(licensedWorkers.values.contains(workshop.worker1))
        assertFalse(licensedWorkers.values.contains(workshop.worker2))
        assertTrue(licensedWorkers.values.contains(workshop.worker3))
    }

    @Test
    fun `test licensedWorkers with private workers`() {
        val workshop = PrivateWorkerWorkshop()
        val licensedWorkers = workshop.licensedWorkers()
        
        assertEquals(1, licensedWorkers.size)
        assertTrue(licensedWorkers.values.contains(workshop.getPrivateWorker()))
    }

    @Test
    fun `test licensedWorkers with null workers`() {
        val workshop = NullWorkerWorkshop()
        val licensedWorkers = workshop.licensedWorkers()
        
        assertEquals(2, licensedWorkers.size)
    }

    @Test
    fun `test licensedWorkers with duplicate task IDs`() {
        val workshop = DuplicateTaskIDWorkshop()
        val licensedWorkers = workshop.licensedWorkers()
        
        // Should only keep the last registered worker for each task ID
        assertEquals(1, licensedWorkers.size)
        assertTrue(licensedWorkers.values.contains(workshop.worker2))
    }

    @Test
    fun `test dispatcher registration and dispatch`() {
        val dispatcher = TestDispatcher()
        val worker = TestWorker()
        val taskId = ITask.ID("TestTask", setOf("prop1", "prop2"))

        // Test registration
        dispatcher.register(taskId, worker)
        assertEquals(worker, dispatcher.dispatch(taskId))
    }

    @Test
    fun `test dispatcher returns null for unregistered task`() {
        val dispatcher = TestDispatcher()
        val taskId = ITask.ID("NonExistentTask", setOf("prop1"))
        assertNull(dispatcher.dispatch(taskId))
    }

    @Test
    fun `test worker processes task and produces product`() = runBlocking {
        val worker = TestWorker()
        val taskId = ITask.ID("TestTask", setOf("prop1"))
        val task = TestTask(taskId)
        
        val result = mutableListOf<TestProduct>()
        with(worker){
            flow{ work(task) }.collect { result.add(it) }
        }
        assertEquals(1, result.size)
        assertEquals("Processed: TestTask", result[0].value)
    }

    @Test
    fun `test TestWorkLicense task ID generation`() {
        val taskId = TestWorkLicense.getTaskID(TestTask::class.java, "prop1", "prop2")
        assertEquals("TestTask", taskId.license)
        assertEquals(setOf("prop1", "prop2"), taskId.props)
    }

    @Test
    fun `test TestWorkLicense task ID validation`() {
        val taskId = ITask.ID("TestTask", setOf("prop1"))
        assertTrue(TestWorkLicense.isTaskID(taskId, TestTask::class.java))
        assertFalse(TestWorkLicense.isTaskID(taskId, String::class.java))
    }
}