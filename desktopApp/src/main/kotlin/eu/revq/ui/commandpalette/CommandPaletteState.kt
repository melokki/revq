package eu.revq.ui.commandpalette

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eu.revq.AppState

class CommandPaletteState {
    var isOpen by mutableStateOf(false)
        private set

    var mode by mutableStateOf<PaletteMode>(PaletteMode.Universal)
        private set

    private var queryValue by mutableStateOf("")
    var query: String
        get() = queryValue
        set(value) {
            if (value != queryValue) clearConfirmation()
            queryValue = value
            updateResults()
        }

    private var selectedIndexValue by mutableStateOf(0)
    var selectedIndex: Int
        get() = selectedIndexValue
        set(value) {
            if (value != selectedIndexValue) clearConfirmation()
            selectedIndexValue = value
        }

    var confirmationMessage by mutableStateOf<String?>(null)
        private set
    private var pendingConfirmationKey: String? = null
    private var catalog: PaletteCatalog? = null

    var results by mutableStateOf<List<PaletteResult>>(emptyList())
        private set

    val selectedResult: PaletteResult?
        get() = results.getOrNull(selectedIndex)

    fun open(
        state: AppState,
        nextMode: PaletteMode = PaletteMode.Universal,
    ) {
        clearConfirmation()
        mode = nextMode
        query = ""
        selectedIndex = 0
        catalog = PaletteResultProvider.catalog(nextMode, state)
        updateResults()
        isOpen = true
    }

    fun close() {
        clearConfirmation()
        isOpen = false
        query = ""
        selectedIndex = 0
        catalog = null
        results = emptyList()
    }

    fun refreshCatalog(state: AppState) {
        catalog = PaletteResultProvider.catalog(mode, state)
        updateResults()
    }

    fun approveExecution(result: PaletteResult): Boolean {
        if (!result.actionable) return false
        val prompt = (result as? PaletteResult.CommandResult)?.confirmationPrompt
        if (prompt == null) {
            clearConfirmation()
            return true
        }
        if (pendingConfirmationKey == result.stableKey) {
            clearConfirmation()
            return true
        }
        pendingConfirmationKey = result.stableKey
        confirmationMessage = prompt
        return false
    }

    private fun clearConfirmation() {
        pendingConfirmationKey = null
        confirmationMessage = null
    }

    private fun updateResults() {
        val selectedKey = selectedResult?.stableKey
        results = catalog?.results(query).orEmpty()
        val preservedIndex = selectedKey?.let { key ->
            results.indexOfFirst { it.stableKey == key }.takeIf { it >= 0 }
        }
        selectedIndex = preservedIndex
            ?: results.indexOfFirst { it.actionable }.takeIf { it >= 0 }
            ?: 0
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
