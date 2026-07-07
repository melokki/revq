package eu.revq.keyboard

import eu.revq.commands.CommandId

/**
 * Intent returned by [KeyboardRouter].
 *
 * The command palette has a single global entry point: Space. Movement stays in
 * the keyboard subsystem, while application actions are expressed as
 * [ExecuteCommand].
 */
sealed interface KeyboardAction {
    data object Unhandled : KeyboardAction

    data object MoveNext : KeyboardAction
    data object MovePrevious : KeyboardAction
    data object FocusLeft : KeyboardAction
    data object FocusRight : KeyboardAction
    data object FirstItem : KeyboardAction
    data object LastItem : KeyboardAction
    data object HalfPageDown : KeyboardAction
    data object HalfPageUp : KeyboardAction
    data object Activate : KeyboardAction
    data object Escape : KeyboardAction

    data object ExitInsertMode : KeyboardAction
    data object SaveSettings : KeyboardAction
    data object OpenPalette : KeyboardAction
    data object CloseCommandPalette : KeyboardAction

    data class ExecuteCommand(
        val commandId: CommandId,
    ) : KeyboardAction
}
