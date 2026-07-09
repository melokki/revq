package eu.revq.keyboard

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import eu.revq.View
import eu.revq.commands.CommandCategory
import eu.revq.commands.CommandRegistry
import eu.revq.commands.ShortcutKey
import eu.revq.commands.ShortcutModifier
import eu.revq.commands.ShortcutStroke

/** Snapshot of the state that matters for keyboard routing. */
data class KeyboardContext(
    val mode: KeyboardMode,
    val focusRegion: FocusRegion,
    val view: View,
    val commandPaletteOpen: Boolean,
)

/**
 * Central keyboard router.
 *
 * Space is the only command-palette entry point. Direct global shortcuts remain
 * available in normal mode and are resolved through [CommandRegistry].
 */
class KeyboardRouter {
    fun route(
        event: KeyEvent,
        context: KeyboardContext,
    ): KeyboardAction {
        if (event.type != KeyEventType.KeyDown) return KeyboardAction.Unhandled

        if (context.commandPaletteOpen) {
            return if (event.key == Key.Escape) {
                KeyboardAction.CloseCommandPalette
            } else {
                KeyboardAction.Unhandled
            }
        }

        val primaryModifier = event.isCtrlPressed || event.isMetaPressed

        if (context.mode == KeyboardMode.Insert) {
            return if (event.key == Key.Escape) {
                KeyboardAction.ExitInsertMode
            } else {
                KeyboardAction.Unhandled
            }
        }

        if (!primaryModifier && !event.isAltPressed && event.key == Key.Spacebar) {
            return KeyboardAction.OpenPalette
        }

        if (context.view == View.Settings) {
            if (primaryModifier && event.key == Key.S) return KeyboardAction.SaveSettings

            return when (event.key) {
                Key.DirectionDown, Key.J -> KeyboardAction.MoveNext
                Key.DirectionUp, Key.K -> KeyboardAction.MovePrevious
                Key.DirectionLeft, Key.H -> KeyboardAction.FocusLeft
                Key.DirectionRight, Key.L -> KeyboardAction.FocusRight
                Key.Enter -> KeyboardAction.Activate
                Key.Escape -> KeyboardAction.Escape
                else -> KeyboardAction.Unhandled
            }
        }

        if (event.isAltPressed || (primaryModifier && event.key !in setOf(Key.U, Key.D))) {
            return KeyboardAction.Unhandled
        }

        if (primaryModifier && event.key == Key.D) {
            return KeyboardAction.HalfPageDown
        }

        if (primaryModifier && event.key == Key.U) {
            return KeyboardAction.HalfPageUp
        }

        if (event.key == Key.G && event.isShiftPressed) {
            return KeyboardAction.LastItem
        }

        val navigationAction = when (event.key) {
            Key.MoveHome -> KeyboardAction.FirstItem
            Key.MoveEnd -> KeyboardAction.LastItem
            Key.PageDown -> KeyboardAction.PageDown
            Key.PageUp -> KeyboardAction.PageUp
            Key.DirectionDown, Key.J -> KeyboardAction.MoveNext
            Key.DirectionUp, Key.K -> KeyboardAction.MovePrevious
            Key.DirectionLeft, Key.H -> KeyboardAction.FocusLeft
            Key.DirectionRight, Key.L -> KeyboardAction.FocusRight
            Key.Enter -> KeyboardAction.Activate
            Key.Escape -> KeyboardAction.Escape
            else -> null
        }

        if (navigationAction != null) return navigationAction

        val stroke = event.toShortcutStroke() ?: return KeyboardAction.Unhandled
        val command = CommandRegistry.findBySingleStroke(stroke)
            ?: return KeyboardAction.Unhandled

        if (
            context.focusRegion == FocusRegion.Sidebar &&
            command.category == CommandCategory.Review
        ) {
            return KeyboardAction.Unhandled
        }

        return KeyboardAction.ExecuteCommand(command.id)
    }
}

private fun KeyEvent.toShortcutStroke(): ShortcutStroke? {
    val shortcutKey = when (key) {
        Key.A -> ShortcutKey.A
        Key.B -> ShortcutKey.B
        Key.C -> ShortcutKey.C
        Key.D -> ShortcutKey.D
        Key.E -> ShortcutKey.E
        Key.F -> ShortcutKey.F
        Key.G -> ShortcutKey.G
        Key.H -> ShortcutKey.H
        Key.I -> ShortcutKey.I
        Key.J -> ShortcutKey.J
        Key.K -> ShortcutKey.K
        Key.L -> ShortcutKey.L
        Key.M -> ShortcutKey.M
        Key.N -> ShortcutKey.N
        Key.O -> ShortcutKey.O
        Key.P -> ShortcutKey.P
        Key.Q -> ShortcutKey.Q
        Key.R -> ShortcutKey.R
        Key.S -> ShortcutKey.S
        Key.T -> ShortcutKey.T
        Key.U -> ShortcutKey.U
        Key.V -> ShortcutKey.V
        Key.W -> ShortcutKey.W
        Key.X -> ShortcutKey.X
        Key.Y -> ShortcutKey.Y
        Key.Z -> ShortcutKey.Z
        Key.Spacebar -> ShortcutKey.Space
        Key.One -> ShortcutKey.Digit1
        Key.Two -> ShortcutKey.Digit2
        Key.Three -> ShortcutKey.Digit3
        Key.Four -> ShortcutKey.Digit4
        Key.Five -> ShortcutKey.Digit5
        else -> return null
    }

    val modifiers = buildSet {
        if (isCtrlPressed || isMetaPressed) add(ShortcutModifier.Primary)
        if (isAltPressed) add(ShortcutModifier.Alt)
        if (isShiftPressed) add(ShortcutModifier.Shift)
    }

    return ShortcutStroke(
        key = shortcutKey,
        modifiers = modifiers,
    )
}
