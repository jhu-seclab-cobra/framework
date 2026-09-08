/*
 * Unit tests for [WorkLicense] task ID derivation.
 *
 * WorkLicense.getTaskID:
 * - `getTaskID from class with empty props`: zero-arg construction
 * - `getTaskID from class with props`: multi-arg construction
 * - `getTaskID from class deduplicates vararg props`: props form a set
 * - `getTaskID from class and collection`: collection overload
 * - `getTaskID from class and collection deduplicates props`: props form a set
 * - `getTaskID from annotation instance extracts parameters`: reflection-based extraction
 * - `getTaskID from annotation excludes RegisterMode properties`: mode is registration metadata, not a discriminant
 * - `getTaskID from annotation equals ID built from class and props`: both constructions agree
 * - `getTaskID distinguishes same-named classes from different packages`: license is the qualified class name
 * - `getTaskID reports annotation without primary constructor by name`: contextual error, no bare NPE
 * - `getTaskID reports missing member property by name`: contextual error naming the parameter
 *
 * WorkLicense.isTaskID:
 * - `isTaskID matches same class`: positive match
 * - `isTaskID rejects different class`: negative match
 * - `isTaskID rejects empty license`: boundary case
 * - `isTaskID rejects simple class name`: license is the qualified name
 */
package edu.jhu.cobra.framework

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class WorkLicenseTest {
    @Test
    fun `getTaskID from class with empty props`() {
        val id = WorkLicense.getTaskID(TestTask::class.java)
        assertEquals(ITask.ID("edu.jhu.cobra.framework.TestTask", emptySet()), id)
    }

    @Test
    fun `getTaskID from class with props`() {
        val id = WorkLicense.getTaskID(TestTask::class.java, "@!#", "b")
        assertEquals(ITask.ID("edu.jhu.cobra.framework.TestTask", setOf("@!#", "b")), id)
    }

    @Test
    fun `getTaskID from class deduplicates vararg props`() {
        val id = WorkLicense.getTaskID(TestTask::class.java, "a", "a")
        assertEquals(setOf("a"), id.props)
    }

    @Test
    fun `getTaskID from class and collection`() {
        val id = WorkLicense.getTaskID(TestTask::class.java, listOf("a", "b"))
        assertEquals(ITask.ID("edu.jhu.cobra.framework.TestTask", setOf("a", "b")), id)
    }

    @Test
    fun `getTaskID from class and collection deduplicates props`() {
        val id = WorkLicense.getTaskID(TestTask::class.java, listOf("a", "a"))
        assertEquals(setOf("a"), id.props)
    }

    @Test
    fun `getTaskID distinguishes same-named classes from different packages`() {
        val utilDateId = WorkLicense.getTaskID(java.util.Date::class.java, "x")
        val sqlDateId = WorkLicense.getTaskID(java.sql.Date::class.java, "x")
        assertNotEquals(utilDateId, sqlDateId)
    }

    /** Annotation literal whose class has no primary constructor. */
    class NoPrimaryCtorLiteral : Annotation {
        constructor()

        fun annotationType(): Class<out Annotation> = NoPrimaryCtorLiteral::class.java
    }

    /** Annotation literal whose constructor parameter has no matching member property. */
    class ParamWithoutPropertyLiteral(
        @Suppress("UNUSED_PARAMETER") name: String,
    ) : Annotation {
        fun annotationType(): Class<out Annotation> = ParamWithoutPropertyLiteral::class.java
    }

    @Test
    fun `getTaskID reports annotation without primary constructor by name`() {
        val failure = assertFailsWith<IllegalStateException> { WorkLicense.getTaskID(NoPrimaryCtorLiteral()) }
        assertTrue("NoPrimaryCtorLiteral" in failure.message.orEmpty())
    }

    @Test
    fun `getTaskID reports missing member property by name`() {
        val failure =
            assertFailsWith<IllegalStateException> { WorkLicense.getTaskID(ParamWithoutPropertyLiteral("x")) }
        assertTrue("ParamWithoutPropertyLiteral" in failure.message.orEmpty())
        assertTrue("name" in failure.message.orEmpty())
    }

    @Test
    fun `getTaskID from annotation instance extracts parameters`() {
        val workshop = TestWorkshop()
        val workers = workshop.licensedWorkers()
        val taskId = workers.keys.first { "worker1" in it.props }
        assertEquals(ITask.ID("edu.jhu.cobra.framework.TestWorkLicense", setOf("worker1")), taskId)
    }

    @Test
    fun `getTaskID from annotation excludes RegisterMode properties`() {
        val workers = TestModedWorkshop().licensedWorkers()
        val taskId = workers.keys.single()
        assertEquals(ITask.ID("edu.jhu.cobra.framework.TestModedLicense", setOf("modedWorker")), taskId)
    }

    @Test
    fun `getTaskID from annotation equals ID built from class and props`() {
        val fromAnnotation = TestModedWorkshop().licensedWorkers().keys.single()
        val fromClass = WorkLicense.getTaskID(TestModedLicense::class.java, "modedWorker")
        assertEquals(fromClass, fromAnnotation)
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

    @Test
    fun `isTaskID rejects simple class name`() {
        val id = ITask.ID("TestTask", emptySet())
        assertFalse(WorkLicense.isTaskID(id, TestTask::class.java))
    }
}
