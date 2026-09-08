/*
 * Unit tests for [ITask.ID].
 *
 * - `empty license is valid`: no constraints on license
 * - `special characters in license are valid`: no constraints on characters
 * - `empty props set is valid`: no constraints on props
 * - `IDs with equal fields are equal`: structural equality
 * - `IDs differing in license case are distinct`: case-sensitive
 * - `IDs differing in props are distinct`: props take part in equality
 * - `uid is readable from a task`: task exposes its identifier
 */
package edu.jhu.cobra.framework

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class ITaskTest {
    @Test
    fun `empty license is valid`() {
        val id = ITask.ID("", setOf("p"))
        assertEquals("", id.license)
    }

    @Test
    fun `special characters in license are valid`() {
        val id = ITask.ID("@#!", emptySet())
        assertEquals("@#!", id.license)
    }

    @Test
    fun `empty props set is valid`() {
        val id = ITask.ID("T", emptySet())
        assertTrue(id.props.isEmpty())
    }

    @Test
    fun `IDs with equal fields are equal`() {
        assertEquals(ITask.ID("T", setOf("a", "b")), ITask.ID("T", setOf("b", "a")))
    }

    @Test
    fun `IDs differing in license case are distinct`() {
        assertNotEquals(ITask.ID("Expr", emptySet()), ITask.ID("expr", emptySet()))
    }

    @Test
    fun `IDs differing in props are distinct`() {
        assertNotEquals(ITask.ID("T", setOf("a")), ITask.ID("T", setOf("b")))
    }

    @Test
    fun `uid is readable from a task`() {
        val id = ITask.ID("T", setOf("p"))
        assertEquals(id, TestTask(id).uid)
    }
}
