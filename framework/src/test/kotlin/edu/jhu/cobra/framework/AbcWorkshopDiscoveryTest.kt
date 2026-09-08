/*
 * Unit tests for [AbcWorkshop.licensedWorkers] discovery.
 *
 * Discovery:
 * - `licensedWorkers returns only licensed workers`: excludes unlicensed
 * - `licensedWorkers with no licensed workers`: empty result
 * - `licensedWorkers with all workers licensed`: full result
 * - `licensedWorkers discovers private workers`: isAccessible = true
 * - `licensedWorkers discovers protected workers`: isAccessible = true
 * - `licensedWorkers with null-valued workers fails`: null-valued licensed property is an error
 *   naming the workshop class and property (design doc: null-valued licensed property is a wiring bug)
 * - `licensedWorkers with empty task ID`: empty string valid
 * - `licensedWorkers with special characters in task ID`: no character constraints
 * - `licensedWorkers excludes licensed non-worker properties`: return type must be a worker
 * - `licensedWorkers maps a property with two license annotations to two task IDs`: one entry per annotation
 *
 * Inheritance:
 * - `licensedWorkers only discovers declared properties not inherited`: declaredMemberProperties
 *
 * Duplicate handling:
 * - `licensedWorkers with duplicate task IDs keeps last`: last-write-wins
 *
 * Integration:
 * - `licensedWorkers produces correct task IDs for dispatcher registration`: end-to-end
 */
package edu.jhu.cobra.framework

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AbcWorkshopDiscoveryTest {
    class EmptyWorkshop : AbcWorkshop<TestWorker>() {
        val worker1 = TestWorker()
        val worker2 = TestWorker()
    }

    class AllLicensedWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense("worker1")
        val worker1 = TestWorker()

        @TestWorkLicense("worker2")
        val worker2 = TestWorker()

        @TestWorkLicense("worker3")
        val worker3 = TestWorker()
    }

    class InheritedWorkshop : TestWorkshop() {
        @TestWorkLicense("worker3")
        val worker3 = TestWorker()
    }

    class PrivateWorkerWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense("worker1")
        private val privateWorker = TestWorker()

        fun getPrivateWorker() = privateWorker
    }

    class NullWorkerWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense("worker1")
        val worker1: TestWorker? = null

        @TestWorkLicense("worker2")
        val worker2: TestWorker? = null
    }

    class DuplicateTaskIDWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense("worker1")
        val worker1 = TestWorker()

        @TestWorkLicense("worker1")
        val worker2 = TestWorker()
    }

    open class ProtectedWorkerWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense("worker1")
        protected val worker1 = TestWorker()

        fun accessWorker1() = worker1
    }

    class NonWorkerWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense("worker1")
        val worker1 = TestWorker()

        @TestWorkLicense("notAWorker")
        val label: String = "not a worker"
    }

    class DoubleLicensedWorkshop : AbcWorkshop<TestWorker>() {
        @TestWorkLicense("first")
        @TestModedLicense("second")
        val worker = TestWorker()
    }

    @Test
    fun `licensedWorkers maps a property with two license annotations to two task IDs`() {
        val workshop = DoubleLicensedWorkshop()
        val licensed = workshop.licensedWorkers()
        assertEquals(
            mapOf(
                WorkLicense.getTaskID(TestWorkLicense::class.java, "first") to workshop.worker,
                WorkLicense.getTaskID(TestModedLicense::class.java, "second") to workshop.worker,
            ),
            licensed,
        )
    }

    @Test
    fun `licensedWorkers excludes licensed non-worker properties`() {
        val workshop = NonWorkerWorkshop()
        val licensed = workshop.licensedWorkers()
        assertEquals(1, licensed.size)
        assertTrue(licensed.values.contains(workshop.worker1))
    }

    @Test
    fun `licensedWorkers returns only licensed workers`() {
        val workshop = TestWorkshop()
        val licensed = workshop.licensedWorkers()
        assertEquals(2, licensed.size)
        assertTrue(licensed.values.contains(workshop.worker1))
        assertTrue(licensed.values.contains(workshop.worker2))
        assertFalse(licensed.values.contains(workshop.unlicensedWorker))
    }

    @Test
    fun `licensedWorkers with no licensed workers`() {
        val licensed = EmptyWorkshop().licensedWorkers()
        assertTrue(licensed.isEmpty())
    }

    @Test
    fun `licensedWorkers with all workers licensed`() {
        val workshop = AllLicensedWorkshop()
        val licensed = workshop.licensedWorkers()
        assertEquals(3, licensed.size)
    }

    @Test
    fun `licensedWorkers only discovers declared properties not inherited`() {
        val workshop = InheritedWorkshop()
        val licensed = workshop.licensedWorkers()
        assertEquals(1, licensed.size)
        assertTrue(licensed.values.contains(workshop.worker3))
    }

    @Test
    fun `licensedWorkers discovers private workers`() {
        val workshop = PrivateWorkerWorkshop()
        val licensed = workshop.licensedWorkers()
        assertEquals(1, licensed.size)
        assertTrue(licensed.values.contains(workshop.getPrivateWorker()))
    }

    @Test
    fun `licensedWorkers discovers protected workers`() {
        val workshop = ProtectedWorkerWorkshop()
        val licensed = workshop.licensedWorkers()
        assertEquals(1, licensed.size)
        assertTrue(licensed.values.contains(workshop.accessWorker1()))
    }

    @Test
    fun `licensedWorkers with null-valued workers fails`() {
        val failure =
            assertFailsWith<IllegalStateException> {
                NullWorkerWorkshop().licensedWorkers()
            }
        val message = failure.message.orEmpty()
        assertTrue(message.contains("NullWorkerWorkshop"))
        assertTrue(message.contains("worker1") || message.contains("worker2"))
    }

    @Test
    fun `licensedWorkers with duplicate task IDs keeps last`() {
        val workshop = DuplicateTaskIDWorkshop()
        val licensed = workshop.licensedWorkers()
        assertEquals(1, licensed.size)
    }

    @Test
    fun `licensedWorkers with empty task ID`() {
        val workshop =
            object : AbcWorkshop<TestWorker>() {
                @TestWorkLicense("")
                val worker = TestWorker()
            }
        assertEquals(1, workshop.licensedWorkers().size)
    }

    @Test
    fun `licensedWorkers with special characters in task ID`() {
        val workshop =
            object : AbcWorkshop<TestWorker>() {
                @TestWorkLicense("worker@123#test")
                val worker = TestWorker()
            }
        assertEquals(1, workshop.licensedWorkers().size)
    }

    @Test
    fun `licensedWorkers produces correct task IDs for dispatcher registration`() {
        val workshop = TestWorkshop()
        val dispatcher = AbcDispatcher<TestTask, TestResult>()
        workshop.licensedWorkers().forEach { (id, worker) -> dispatcher.register(id, worker) }
        val id = WorkLicense.getTaskID(TestWorkLicense::class.java, "worker1")
        val dispatched = dispatcher.dispatch(id)
        assertEquals(workshop.worker1, dispatched)
    }
}
