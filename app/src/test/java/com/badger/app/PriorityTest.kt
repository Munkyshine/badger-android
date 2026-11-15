package com.badger.app

import com.badger.app.ui.Priority
import org.junit.Assert.assertEquals
import org.junit.Test

class PriorityTest {
    @Test
    fun fromRaw_mapsIosRanks() {
        assertEquals(Priority.Bonus, Priority.fromRaw(0))
        assertEquals(Priority.ToDo, Priority.fromRaw(1))
        assertEquals(Priority.High, Priority.fromRaw(2))
        assertEquals(Priority.Urgent, Priority.fromRaw(3))
        assertEquals(Priority.ToDo, Priority.fromRaw(99))
        assertEquals("Urgent", Priority.Urgent.label)
    }
}
