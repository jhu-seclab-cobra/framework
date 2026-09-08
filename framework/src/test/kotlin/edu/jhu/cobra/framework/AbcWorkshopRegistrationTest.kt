/*
 * Unit tests for [AbcWorkshop.registerTo].
 *
 * Registration modes:
 * - `registerTo respects REPLACE mode from mode-named property`: re-registration overwrites, not chains
 * - `registerTo respects REPLACE mode from RegisterMode-typed property with another name`: mode
 *   selection by RegisterMode type, consistent with getTaskID exclusion
 * - `registerTo defaults to ATTACH when annotation declares no RegisterMode property`: re-registration chains
 * - `registerTo uses ATTACH from annotation default value`: declared mode property with default ATTACH chains
 *
 * Registration set:
 * - `registerTo registers every licensed worker`: all licensed workers dispatchable
 * - `registerTo skips unlicensed workers`: unlicensed property is not registered
 * - `registerTo registers a property once per license annotation`: one registration per annotation
 * - `registerTo registers duplicate task IDs per dispatcher mode`: ATTACH chains both properties
 *
 * Errors:
 * - `registerTo with null-valued worker fails`: null-valued licensed property is an error naming
 *   the workshop class and property
 */
package edu.jhu.cobra.framework

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AbcWorkshopRegistrationTest {
    class RenamedModeWorkshop : AbcWorkshop<IWorker<TestTask, TestResult>>() {
        @TestRenamedModeLicense("renamed", priority = RegisterMode.REPLACE)
        val worker = TestWorker()
    }

    /** Workshop whose workers append their property name to [calls] on each invocation. */
    class RecordingWorkshop : AbcWorkshop<IWorker<TestTask, Unit>>() {
        val calls = mutableListOf<String>()

        @TestWorkLicense("plain")
        val plain = IWorker<TestTask, Unit> { calls.add("plain") }

        @TestModedLicense("defaulted")
        val defaulted = IWorker<TestTask, Unit> { calls.add("defaulted") }

        @TestWorkLicense("shared")
        val sharedFirst = IWorker<TestTask, Unit> { calls.add("sharedFirst") }

        @TestWorkLicense("shared")
        val sharedSecond = IWorker<TestTask, Unit> { calls.add("sharedSecond") }
    }

    /** Same members as [TestWorkshop], typed for registration into an [AbcDispatcher]. */
    class RegistrableWorkshop : AbcWorkshop<IWorker<TestTask, TestResult>>() {
        @TestWorkLicense("worker1")
        val worker1 = TestWorker()

        @TestWorkLicense("worker2")
        val worker2 = TestWorker()

        val unlicensedWorker = TestWorker()
    }

    class DoubleLicensedWorkshop : AbcWorkshop<IWorker<TestTask, TestResult>>() {
        @TestWorkLicense("first")
        @TestModedLicense("second")
        val worker = TestWorker()
    }

    class NullWorkerWorkshop : AbcWorkshop<IWorker<TestTask, TestResult>>() {
        @TestWorkLicense("worker1")
        val worker1: TestWorker? = null
    }

    private fun taskId(vararg props: String): ITask.ID = WorkLicense.getTaskID(TestWorkLicense::class.java, *props)

    private fun run(
        dispatcher: AbcDispatcher<TestTask, Unit>,
        id: ITask.ID,
    ) = dispatcher.dispatch(id)!!.work(TestTask(id))

    @Test
    fun `registerTo respects REPLACE mode from mode-named property`() {
        val workshop = TestModedWorkshop()
        val dispatcher = AbcDispatcher<TestTask, TestResult>()
        workshop.registerTo(dispatcher)
        workshop.registerTo(dispatcher)
        val id = WorkLicense.getTaskID(TestModedLicense::class.java, "modedWorker")
        assertEquals(workshop.modedWorker, dispatcher.dispatch(id))
    }

    @Test
    fun `registerTo respects REPLACE mode from RegisterMode-typed property with another name`() {
        val workshop = RenamedModeWorkshop()
        val dispatcher = AbcDispatcher<TestTask, TestResult>()
        workshop.registerTo(dispatcher)
        workshop.registerTo(dispatcher)
        val id = WorkLicense.getTaskID(TestRenamedModeLicense::class.java, "renamed")
        assertEquals(workshop.worker, dispatcher.dispatch(id))
    }

    @Test
    fun `registerTo defaults to ATTACH when annotation declares no RegisterMode property`() {
        val workshop = RecordingWorkshop()
        val dispatcher = AbcDispatcher<TestTask, Unit>()
        workshop.registerTo(dispatcher)
        workshop.registerTo(dispatcher)
        run(dispatcher, taskId("plain"))
        assertEquals(listOf("plain", "plain"), workshop.calls)
    }

    @Test
    fun `registerTo uses ATTACH from annotation default value`() {
        val workshop = RecordingWorkshop()
        val dispatcher = AbcDispatcher<TestTask, Unit>()
        workshop.registerTo(dispatcher)
        workshop.registerTo(dispatcher)
        run(dispatcher, WorkLicense.getTaskID(TestModedLicense::class.java, "defaulted"))
        assertEquals(listOf("defaulted", "defaulted"), workshop.calls)
    }

    @Test
    fun `registerTo registers every licensed worker`() {
        val workshop = RegistrableWorkshop()
        val dispatcher = AbcDispatcher<TestTask, TestResult>()
        workshop.registerTo(dispatcher)
        assertEquals(workshop.worker1, dispatcher.dispatch(taskId("worker1")))
        assertEquals(workshop.worker2, dispatcher.dispatch(taskId("worker2")))
    }

    @Test
    fun `registerTo skips unlicensed workers`() {
        val dispatcher = AbcDispatcher<TestTask, TestResult>()
        RegistrableWorkshop().registerTo(dispatcher)
        assertNull(dispatcher.dispatch(taskId("unlicensedWorker")))
    }

    @Test
    fun `registerTo registers a property once per license annotation`() {
        val workshop = DoubleLicensedWorkshop()
        val dispatcher = AbcDispatcher<TestTask, TestResult>()
        workshop.registerTo(dispatcher)
        assertEquals(workshop.worker, dispatcher.dispatch(taskId("first")))
        assertEquals(workshop.worker, dispatcher.dispatch(WorkLicense.getTaskID(TestModedLicense::class.java, "second")))
    }

    @Test
    fun `registerTo registers duplicate task IDs per dispatcher mode`() {
        val workshop = RecordingWorkshop()
        val dispatcher = AbcDispatcher<TestTask, Unit>()
        workshop.registerTo(dispatcher)
        run(dispatcher, taskId("shared"))
        assertEquals(setOf("sharedFirst", "sharedSecond"), workshop.calls.toSet())
        assertEquals(2, workshop.calls.size)
    }

    @Test
    fun `registerTo with null-valued worker fails`() {
        val failure =
            assertFailsWith<IllegalStateException> {
                NullWorkerWorkshop().registerTo(AbcDispatcher<TestTask, TestResult>())
            }
        val message = failure.message.orEmpty()
        assertTrue("NullWorkerWorkshop" in message)
        assertTrue("worker1" in message)
    }
}
