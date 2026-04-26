/**
 * Unit tests for [IWorker] and [WorkLicense].
 *
 * IWorker:
 * - `work returns result synchronously`: basic invocation
 * - `work receives task properties`: task payload accessible
 * - `fun interface enables SAM lambda`: lambda construction
 * - `Boolean return type for convergence`: common usage pattern
 * - `Unit return type for side effects`: common usage pattern
 *
 * ITask.ID:
 * - `empty license is valid`: no constraints on license
 * - `special characters in license are valid`: no constraints on characters
 * - `empty props set is valid`: no constraints on props
 *
 * WorkLicense.getTaskID:
 * - `getTaskID from class with empty props`: zero-arg construction
 * - `getTaskID from class with props`: multi-arg construction
 * - `getTaskID from annotation instance extracts parameters`: reflection-based extraction
 *
 * WorkLicense.isTaskID:
 * - `isTaskID matches same class`: positive match
 * - `isTaskID rejects different class`: negative match
 * - `isTaskID rejects empty license`: boundary case
 */
package edu.jhu.cobra.framework

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

internal class IWorkerTest {

    @Test
    fun `work returns result synchronously`() {
        val worker = IWorker<TestTask, TestResult> { task ->
            TestResult("Processed: ${task.uid.license}")
        }
        val result = worker.work(TestTask(ITask.ID("TestTask", emptySet())))
        assertEquals("Processed: TestTask", result.value)
    }

    @Test
    fun `work receives task properties`() {
        val worker = IWorker<TestTask, TestResult> { task ->
            TestResult(task.uid.props.joinToString())
        }
        val result = worker.work(TestTask(ITask.ID("T", setOf("a", "b", "c"))))
        assertTrue(result.value.contains("a"))
        assertTrue(result.value.contains("b"))
        assertTrue(result.value.contains("c"))
    }

    @Test
    fun `fun interface enables SAM lambda`() {
        val worker: IWorker<TestTask, TestResult> = IWorker { TestResult("SAM") }
        assertEquals("SAM", worker.work(TestTask(ITask.ID("T", emptySet()))).value)
    }

    @Test
    fun `Boolean return type for convergence`() {
        val worker = IWorker<TestTask, Boolean> { true }
        assertTrue(worker.work(TestTask(ITask.ID("T", emptySet()))))
    }

    @Test
    fun `Unit return type for side effects`() {
        var called = false
        val worker = IWorker<TestTask, Unit> { called = true }
        worker.work(TestTask(ITask.ID("T", emptySet())))
        assertTrue(called)
    }

    @Test
    fun `empty license is valid`() {
        val worker = IWorker<TestTask, TestResult> { TestResult(it.uid.license) }
        assertEquals("", worker.work(TestTask(ITask.ID("", setOf("p")))).value)
    }

    @Test
    fun `special characters in license are valid`() {
        val worker = IWorker<TestTask, TestResult> { TestResult(it.uid.license) }
        assertEquals("@#!", worker.work(TestTask(ITask.ID("@#!", emptySet()))).value)
    }

    @Test
    fun `empty props set is valid`() {
        val id = ITask.ID("T", emptySet())
        assertTrue(id.props.isEmpty())
    }

    @Test
    fun `getTaskID from class with empty props`() {
        val id = WorkLicense.getTaskID(TestTask::class.java)
        assertEquals("TestTask", id.license)
        assertTrue(id.props.isEmpty())
    }

    @Test
    fun `getTaskID from class with props`() {
        val id = WorkLicense.getTaskID(TestTask::class.java, "@!#")
        assertEquals("TestTask", id.license)
        assertEquals(setOf("@!#"), id.props)
    }

    @Test
    fun `getTaskID from annotation instance extracts parameters`() {
        val workshop = TestWorkshop()
        val workers = workshop.licensedWorkers()
        val taskId = workers.keys.first { "worker1" in it.props }
        assertEquals("TestWorkLicense", taskId.license)
        assertEquals(setOf("worker1"), taskId.props)
    }

    @Test
    fun `isTaskID matches same class`() {
        val id = WorkLicense.getTaskID(TestTask::class.java)
        assertTrue(WorkLicense.isTaskID(id, TestTask::class.java))
    }

    @Test
    fun `isTaskID rejects different class`() {
        val id = ITask.ID("TestTask", setOf("p"))
        assertFalse(WorkLicense.isTaskID(id, String::class.java))
    }

    @Test
    fun `isTaskID rejects empty license`() {
        val id = ITask.ID("", emptySet())
        assertFalse(WorkLicense.isTaskID(id, TestTask::class.java))
    }
}
