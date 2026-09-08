/*
 * Unit tests for [IWorker].
 *
 * - `work returns result synchronously`: basic invocation
 * - `work receives task properties`: task payload accessible
 * - `fun interface enables SAM lambda`: lambda construction
 * - `Boolean return type for convergence`: common usage pattern
 * - `Unit return type for side effects`: common usage pattern
 */
package edu.jhu.cobra.framework

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class IWorkerTest {
    @Test
    fun `work returns result synchronously`() {
        val worker =
            IWorker<TestTask, TestResult> { task ->
                TestResult("Processed: ${task.uid.license}")
            }
        val result = worker.work(TestTask(ITask.ID("TestTask", emptySet())))
        assertEquals("Processed: TestTask", result.value)
    }

    @Test
    fun `work receives task properties`() {
        val worker =
            IWorker<TestTask, TestResult> { task ->
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
}
