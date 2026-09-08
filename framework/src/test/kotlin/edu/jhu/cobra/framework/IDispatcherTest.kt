/*
 * Unit tests for the [IDispatcher] contract and [RegisterMode].
 *
 * - `register without mode uses ATTACH`: default parameter of the interface contract
 * - `register with explicit mode passes it through`: mode reaches the implementation
 * - `RegisterMode declares REPLACE and ATTACH`: closed set of modes
 */
package edu.jhu.cobra.framework

import kotlin.test.Test
import kotlin.test.assertEquals

internal class IDispatcherTest {
    /** Records the mode each registration was made with; dispatch is not under test here. */
    class RecordingDispatcher : IDispatcher<TestWorker> {
        val modes = mutableListOf<RegisterMode>()

        override fun dispatch(forTask: ITask.ID): TestWorker? = null

        override fun register(
            forTask: ITask.ID,
            toWorker: TestWorker,
            mode: RegisterMode,
        ) {
            modes.add(mode)
        }
    }

    @Test
    fun `register without mode uses ATTACH`() {
        val dispatcher = RecordingDispatcher()
        dispatcher.register(ITask.ID("T", emptySet()), TestWorker())
        assertEquals(listOf(RegisterMode.ATTACH), dispatcher.modes)
    }

    @Test
    fun `register with explicit mode passes it through`() {
        val dispatcher = RecordingDispatcher()
        dispatcher.register(ITask.ID("T", emptySet()), TestWorker(), RegisterMode.REPLACE)
        assertEquals(listOf(RegisterMode.REPLACE), dispatcher.modes)
    }

    @Test
    fun `RegisterMode declares REPLACE and ATTACH`() {
        assertEquals(setOf(RegisterMode.REPLACE, RegisterMode.ATTACH), RegisterMode.entries.toSet())
    }
}
