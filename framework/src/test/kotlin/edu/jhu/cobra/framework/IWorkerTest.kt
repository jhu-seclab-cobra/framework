package edu.jhu.cobra.framework

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class IWorkerTest {
    
    @Test
    fun `test worker processes task with empty properties`() = runBlocking {
        val worker = object : IWorker<TestTask, TestProduct> {
            override suspend fun FlowCollector<TestProduct>.work(task: TestTask) {
                emit(TestProduct("Processed: ${task.uid.license}"))
            }
        }
        
        val taskId = ITask.ID("TestTask", emptySet())
        val task = TestTask(taskId)
        
        val result = mutableListOf<TestProduct>()
        with(worker) {
            flow { work(task) }.collect { result.add(it) }
        }

        assertEquals(1, result.size)
        assertEquals("Processed: TestTask", result[0].value)
    }

    @Test
    fun `test worker processes task with multiple properties`() = runBlocking {
        val worker = object : IWorker<TestTask, TestProduct> {
            override suspend fun FlowCollector<TestProduct>.work(task: TestTask) {
                emit(TestProduct("Processed: ${task.uid.license} with props: ${task.uid.props.joinToString()}"))
            }
        }
        
        val taskId = ITask.ID("TestTask", setOf("prop1", "prop2", "prop3"))
        val task = TestTask(taskId)
        
        val result = mutableListOf<TestProduct>()
        with(worker) {
            flow { work(task) }.collect { result.add(it) }
        }

        assertEquals(1, result.size)
        assertTrue(result[0].value.contains("prop1"))
        assertTrue(result[0].value.contains("prop2"))
        assertTrue(result[0].value.contains("prop3"))
    }

    @Test
    fun `test worker emits multiple products`() = runBlocking {
        val worker = object : IWorker<TestTask, TestProduct> {
            override suspend fun FlowCollector<TestProduct>.work(task: TestTask) {
                emit(TestProduct("First: ${task.uid.license}"))
                emit(TestProduct("Second: ${task.uid.license}"))
            }
        }
        
        val taskId = ITask.ID("TestTask", setOf("prop1"))
        val task = TestTask(taskId)
        
        val result = mutableListOf<TestProduct>()
        with(worker) {
            flow { work(task) }.collect { result.add(it) }
        }

        assertEquals(2, result.size)
        assertEquals("First: TestTask", result[0].value)
        assertEquals("Second: TestTask", result[1].value)
    }

    @Test
    fun `test worker with empty task ID`() = runBlocking {
        val worker = object : IWorker<TestTask, TestProduct> {
            override suspend fun FlowCollector<TestProduct>.work(task: TestTask) {
                emit(TestProduct("Processed: ${task.uid.license}"))
            }
        }
        
        val taskId = ITask.ID("", setOf("prop1"))
        val task = TestTask(taskId)
        
        val result = mutableListOf<TestProduct>()
        with(worker) {
            flow{ work(task) }.collect { result.add(it) }
        }
        assertEquals(1, result.size)
        assertEquals("Processed: ", result[0].value)
    }

    @Test
    fun `test worker with special characters in task ID`() = runBlocking {
        val worker = object : IWorker<TestTask, TestProduct> {
            override suspend fun FlowCollector<TestProduct>.work(task: TestTask) {
                emit(TestProduct("Processed: ${task.uid.license}"))
            }
        }
        
        val taskId = ITask.ID("Test@Task#123", setOf("prop1"))
        val task = TestTask(taskId)
        
        val result = mutableListOf<TestProduct>()
        with(worker) {
            flow { work(task) }.collect { result.add(it) }
        }
        assertEquals(1, result.size)
        assertEquals("Processed: Test@Task#123", result[0].value)
    }

    @Test
    fun `test WorkLicense task ID generation with empty properties`() {
        val taskId = WorkLicense.getTaskID(TestTask::class.java)
        assertEquals("TestTask", taskId.license)
        assertTrue(taskId.props.isEmpty())
    }

    @Test
    fun `test WorkLicense task ID generation with special character props`() {
        val id = WorkLicense.getTaskID(TestTask::class.java, "@!#")
        assertEquals("TestTask", id.license)
        assertEquals(setOf("@!#"), id.props)
    }

    @Test
    fun `test WorkLicense isTaskID with empty license`() {
        val id = ITask.ID("", setOf())
        assertFalse(WorkLicense.isTaskID(id, TestTask::class.java))
    }

    @Test
    fun `test worker has the correct licenses`() {
        assertTrue {
            val task1 = WorkLicense.getTaskID(TestTask::class.java)
            WorkLicense.isTaskID(task1, TestTask::class.java)
        }
    }

    @Test
    fun `test WorkLicense task ID validation with different class`() {
        val taskId = ITask.ID("TestTask", setOf("prop1"))
        assertFalse(WorkLicense.isTaskID(taskId, String::class.java))
    }
} 