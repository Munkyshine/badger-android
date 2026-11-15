package com.badger.app.ui

import androidx.compose.ui.graphics.Color

enum class Priority(val raw: Int, val label: String, val stripe: Color) {
    Bonus(0, "Bonus", Color(0xFFAF52DE)),
    ToDo(1, "To Do", Color(0xFF007AFF)),
    High(2, "High Priority", Color(0xFFFF9F0A)),
    Urgent(3, "Urgent", Color(0xFFFF3B30));

    companion object {
        fun fromRaw(raw: Int) = entries.find { it.raw == raw } ?: ToDo
    }
}

object BadgerColors {
    val lightBg = Color(0xFFF2F2F7)
    val darkBg = Color(0xFF0A0A0A)
    val darkCard = Color(0xFF1C1C1E)
    val lightTitle = Color(0xFF111114)
    val lightSubtitle = Color(0xFF3C3C43)
    val darkTitle = Color(0xFFF5F5F7)
    val darkSubtitle = Color(0xFFAEAEB2)
    val forestLight = Color(0xFF15804A)
    val forestDark = Color(0xFF2BD67F)
}
