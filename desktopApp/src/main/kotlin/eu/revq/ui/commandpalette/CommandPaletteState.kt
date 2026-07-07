package eu.revq.ui.commandpalette

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CommandPaletteState {
    var isOpen by mutableStateOf(false)
        private set

    var mode by mutableStateOf<PaletteMode>(PaletteMode.Universal)
        private set

    var query by mutableStateOf("")
    var selectedIndex by mutableStateOf(0)

    fun open(nextMode: PaletteMode = PaletteMode.Universal) {
        mode = nextMode
        query = ""
        selectedIndex = 0
        isOpen = true
    }

    fun close() {
        isOpen = false
        query = ""
        selectedIndex = 0
    }

    fun moveSelection(delta: Int, resultCount: Int) {
        if (resultCount <= 0) {
            selectedIndex = 0
            return
        }
        selectedIndex = (selectedIndex + delta).coerceIn(0, resultCount - 1)
    }

    fun clampSelection(resultCount: Int) {
        selectedIndex = when {
            resultCount <= 0 -> 0
            selectedIndex >= resultCount -> resultCount - 1
            selectedIndex < 0 -> 0
            else -> selectedIndex
        }
    }
}
