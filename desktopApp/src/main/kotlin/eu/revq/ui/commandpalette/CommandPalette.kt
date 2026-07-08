package eu.revq.ui.commandpalette

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.KeyboardCommandKey
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eu.revq.AppState
import eu.revq.Border
import eu.revq.Olive
import eu.revq.PanelBg
import eu.revq.PanelElevated
import eu.revq.TextMuted
import eu.revq.TextPrimary
import eu.revq.commands.CommandExecutionResult
import eu.revq.commands.CommandExecutor
import eu.revq.commands.CommandId
import eu.revq.keyboard.FocusRegion
import eu.revq.revqTextFieldColors

@Composable
fun CommandPalette(
    state: AppState,
    paletteState: CommandPaletteState,
    onGoToTop: () -> Unit,
) {
    val mode = paletteState.mode
    val results = PaletteResultProvider.results(mode, state, paletteState.query)
    val listState = rememberLazyListState()
    val queryFocusRequester = remember { FocusRequester() }
    val paletteFocusRequester = remember { FocusRequester() }
    val executor = remember(state) { CommandExecutor(state) }
    val quickRunLabels = quickRunShortcutLabels(results)
    val selectedResult = results.getOrNull(paletteState.selectedIndex)

    LaunchedEffect(results.map { it.stableKey to it.enabled }) {
        paletteState.clampSelection(results.size)
        if (results.isNotEmpty() && results.getOrNull(paletteState.selectedIndex)?.enabled == false) {
            paletteState.selectedIndex = results.indexOfFirst { it.enabled }.coerceAtLeast(0)
        }
    }

    LaunchedEffect(paletteState.selectedIndex, results.size) {
        if (results.isNotEmpty()) {
            listState.animateScrollToItem(paletteState.selectedIndex.coerceIn(results.indices))
        }
    }

    LaunchedEffect(mode) {
        if (mode.acceptsTextQuery) {
            queryFocusRequester.requestFocus()
        } else {
            paletteFocusRequester.requestFocus()
        }
    }

    fun execute(result: PaletteResult) {
        if (!result.enabled) return

        when (result) {
            is PaletteResult.CommandResult -> {
                if (result.command.id == CommandId.ShowKeyboardShortcuts) {
                    state.recordCommandExecution(result.command.id)
                    paletteState.query = "keyboard shortcuts"
                    paletteState.selectedIndex = 0
                    return
                }

                val execution = executor.execute(result.command.id)
                if (execution == CommandExecutionResult.Executed) {
                    paletteState.close()
                }
            }

            is PaletteResult.PullRequestResult -> {
                state.recordPaletteTarget("pr:${result.pullRequest.key}")
                state.searchQuery = ""
                state.selectView(result.targetView)
                state.selectedPullRequest = result.pullRequest
                state.keyboardFocusRegion = FocusRegion.PullRequestList
                paletteState.close()
            }

            is PaletteResult.RepositoryResult -> {
                state.recordPaletteTarget("repo:${result.repository}")
                if (state.view == eu.revq.View.Settings) {
                    state.selectView(eu.revq.View.NeedsReview)
                }
                state.searchQuery = result.repository
                state.selectedPullRequest = null
                state.keyboardFocusRegion = FocusRegion.PullRequestList
                paletteState.close()
            }

            is PaletteResult.ShortcutResult -> Unit
            PaletteResult.GoToTopResult -> {
                onGoToTop()
                paletteState.close()
            }
        }
    }

    fun executeSelected() {
        results.getOrNull(paletteState.selectedIndex)?.let(::execute)
    }

    Dialog(onDismissRequest = paletteState::close) {
        Surface(
            modifier = Modifier
                .width(800.dp)
                .heightIn(max = 620.dp)
                .focusRequester(paletteFocusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    handlePaletteKeyEvent(
                        event = event,
                        mode = mode,
                        results = results,
                        paletteState = paletteState,
                        onExecute = ::execute,
                        onExecuteSelected = ::executeSelected,
                    )
                },
            color = PanelBg,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Border),
        ) {
            Column {
                PaletteHeader(mode)

                if (mode.acceptsTextQuery) {
                    OutlinedTextField(
                        value = paletteState.query,
                        onValueChange = {
                            paletteState.query = it
                            paletteState.selectedIndex = 0
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(46.dp)
                            .focusRequester(queryFocusRequester),
                        singleLine = true,
                        placeholder = {
                            Text(mode.placeholder, color = TextMuted)
                        },
                        leadingIcon = {
                            Text(
                                text = mode.entryLabel,
                                color = Olive,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        colors = revqTextFieldColors(),
                    )
                } else {
                    QuickModePrompt(mode)
                }

                Spacer(Modifier.size(10.dp))
                HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)

                if (results.isEmpty()) {
                    PaletteEmptyState(paletteState.query)
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        itemsIndexed(
                            items = results,
                            key = { _, result -> result.stableKey },
                        ) { index, result ->
                            val previousSection = results.getOrNull(index - 1)?.section
                            Column {
                                if (previousSection != result.section) {
                                    PaletteSectionHeader(result.section.label)
                                }
                                PaletteResultRow(
                                    result = result,
                                    quickRunLabel = quickRunLabels[result.stableKey],
                                    selected = index == paletteState.selectedIndex,
                                    onClick = { execute(result) },
                                )
                            }
                        }
                    }
                }

                selectedResult?.let {
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)
                    PaletteExecutionPreview(it)
                }
                HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Border)
                PaletteFooter(mode)
            }
        }
    }
}

@Composable
private fun PaletteHeader(mode: PaletteMode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(PanelElevated)
                .border(1.dp, Border, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = mode.entryLabel,
                color = Olive,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = mode.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = mode.placeholder,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = Icons.Rounded.KeyboardCommandKey,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun QuickModePrompt(mode: PaletteMode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF171A1F))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mode.placeholder,
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PaletteSectionHeader(label: String) {
    Text(
        text = label.uppercase(),
        color = TextMuted,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 5.dp),
    )
}

@Composable
private fun PaletteResultRow(
    result: PaletteResult,
    quickRunLabel: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val icon = paletteResultIcon(result)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) PanelElevated else Color.Transparent)
            .clickable(enabled = result.enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 34.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (selected) Olive else Color.Transparent),
        )
        Spacer(Modifier.size(9.dp))

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (result.enabled) {
                if (selected) Olive else TextMuted
            } else {
                TextMuted.copy(alpha = 0.35f)
            },
            modifier = Modifier.size(19.dp),
        )

        Spacer(Modifier.size(10.dp))

        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                ResultTypePill(result.typeLabel(), selected)
                Text(
                    text = result.title,
                    color = if (result.enabled) TextPrimary else TextMuted.copy(alpha = 0.45f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            result.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                Text(
                    text = subtitle,
                    color = TextMuted.copy(alpha = if (result.enabled) 1f else 0.45f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        quickRunLabel?.let { shortcut ->
            Spacer(Modifier.size(10.dp))
            QuickRunPill(shortcut, selected)
        } ?: result.shortcutLabel?.let { shortcut ->
            Spacer(Modifier.size(10.dp))
            ShortcutPill(shortcut, selected)
        }
    }
}

@Composable
private fun ResultTypePill(
    label: String,
    selected: Boolean,
) {
    Text(
        text = label,
        color = if (selected) Olive else TextMuted,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF15181C))
            .border(1.dp, Border, RoundedCornerShape(999.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp),
    )
}

@Composable
private fun QuickRunPill(
    label: String,
    selected: Boolean,
) {
    Text(
        text = label,
        color = if (selected) Color(0xFF151812) else Olive,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Olive else Color(0xFF2C3323))
            .border(1.dp, Olive.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

@Composable
private fun ShortcutPill(
    label: String,
    selected: Boolean,
) {
    Text(
        text = label,
        color = if (selected) TextPrimary else TextMuted,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF15181C))
            .border(1.dp, Border, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

@Composable
private fun PaletteExecutionPreview(result: PaletteResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF15181C))
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Next",
            color = Olive,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = result.executionPreview(),
            color = if (result.enabled) TextPrimary else TextMuted,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PaletteEmptyState(
    query: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = if (query.isBlank()) "Nothing available here" else "No matches for “$query”",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Try a command, view, PR title, PR number, repository, author, reviewer, or status.",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PaletteFooter(mode: PaletteMode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PaletteFooterHint("↑↓", "Move")
        PaletteFooterHint("Ctrl+n/p", "Move")
        PaletteFooterHint("Enter", "Run")
        PaletteFooterHint("Ctrl+1…9", "Run")
        if (mode.acceptsTextQuery) {
            PaletteFooterHint("Ctrl+u", "Clear")
        }
        Spacer(Modifier.weight(1f))
        PaletteFooterHint("Esc", "Close")
    }
}

@Composable
private fun PaletteFooterHint(
    key: String,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = key,
            color = TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private fun paletteResultIcon(result: PaletteResult): ImageVector = when (result) {
    is PaletteResult.CommandResult -> Icons.Rounded.KeyboardCommandKey
    is PaletteResult.PullRequestResult -> Icons.AutoMirrored.Rounded.OpenInNew
    is PaletteResult.RepositoryResult -> Icons.Rounded.FolderOpen
    is PaletteResult.ShortcutResult -> Icons.Rounded.KeyboardCommandKey
    PaletteResult.GoToTopResult -> Icons.Rounded.KeyboardCommandKey
}

private fun handlePaletteKeyEvent(
    event: KeyEvent,
    mode: PaletteMode,
    results: List<PaletteResult>,
    paletteState: CommandPaletteState,
    onExecute: (PaletteResult) -> Unit,
    onExecuteSelected: () -> Unit,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false

    val primary = event.isCtrlPressed || event.isMetaPressed
    val quickRunIndex = quickRunIndex(event)

    if (event.key == Key.Escape) {
        paletteState.close()
        return true
    }

    when {
        event.key == Key.DirectionDown || (primary && event.key == Key.N) -> {
            paletteState.selectedIndex = nextEnabledIndex(
                results = results,
                currentIndex = paletteState.selectedIndex,
                direction = 1,
            )
            return true
        }

        event.key == Key.DirectionUp || (primary && event.key == Key.P) -> {
            paletteState.selectedIndex = nextEnabledIndex(
                results = results,
                currentIndex = paletteState.selectedIndex,
                direction = -1,
            )
            return true
        }

        event.key == Key.Enter -> {
            onExecuteSelected()
            return true
        }

        quickRunIndex != null -> {
            enabledVisibleResults(results).getOrNull(quickRunIndex)?.let(onExecute)
            return true
        }

        primary && event.key == Key.U && mode.acceptsTextQuery -> {
            paletteState.query = ""
            paletteState.selectedIndex = 0
            return true
        }
    }

    return false
}

internal fun quickRunShortcutLabels(results: List<PaletteResult>): Map<String, String> =
    enabledVisibleResults(results)
        .take(9)
        .mapIndexed { index, result -> result.stableKey to "Ctrl+${index + 1}" }
        .toMap()

private fun enabledVisibleResults(results: List<PaletteResult>): List<PaletteResult> =
    results.filter { it.enabled }

private fun quickRunIndex(event: KeyEvent): Int? {
    val primary = event.isCtrlPressed || event.isMetaPressed
    if (!primary || event.isAltPressed || event.isShiftPressed) return null

    return when (event.key) {
        Key.One -> 0
        Key.Two -> 1
        Key.Three -> 2
        Key.Four -> 3
        Key.Five -> 4
        Key.Six -> 5
        Key.Seven -> 6
        Key.Eight -> 7
        Key.Nine -> 8
        else -> null
    }
}


private fun nextEnabledIndex(
    results: List<PaletteResult>,
    currentIndex: Int,
    direction: Int,
): Int {
    if (results.isEmpty()) return 0
    val enabledIndices = results.indices.filter { results[it].enabled }
    if (enabledIndices.isEmpty()) return currentIndex.coerceIn(results.indices)

    return if (direction > 0) {
        enabledIndices.firstOrNull { it > currentIndex } ?: enabledIndices.last()
    } else {
        enabledIndices.lastOrNull { it < currentIndex } ?: enabledIndices.first()
    }
}
