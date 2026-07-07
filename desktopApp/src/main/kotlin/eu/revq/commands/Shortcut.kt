package eu.revq.commands

enum class ShortcutModifier {
    Primary,
    Control,
    Meta,
    Alt,
    Shift,
}

enum class ShortcutKey(val display: String) {
    A("a"),
    B("b"),
    C("c"),
    D("d"),
    E("e"),
    F("f"),
    G("g"),
    H("h"),
    I("i"),
    J("j"),
    K("k"),
    L("l"),
    M("m"),
    N("n"),
    O("o"),
    P("p"),
    Q("q"),
    R("r"),
    S("s"),
    T("t"),
    U("u"),
    V("v"),
    W("w"),
    X("x"),
    Y("y"),
    Z("z"),
    Space("Space"),
    Digit1("1"),
    Digit2("2"),
    Digit3("3"),
    Digit4("4"),
    Digit5("5"),
}

data class ShortcutStroke(
    val key: ShortcutKey,
    val modifiers: Set<ShortcutModifier> = emptySet(),
)

data class Shortcut(
    val strokes: List<ShortcutStroke>,
) {
    init {
        require(strokes.isNotEmpty()) { "A shortcut needs at least one stroke" }
    }

    val displayLabel: String
        get() = strokes.joinToString(" ") { stroke ->
            buildString {
                if (ShortcutModifier.Primary in stroke.modifiers) append("Ctrl/⌘+")
                if (ShortcutModifier.Control in stroke.modifiers) append("Ctrl+")
                if (ShortcutModifier.Meta in stroke.modifiers) append("⌘+")
                if (ShortcutModifier.Alt in stroke.modifiers) append("Alt+")
                if (ShortcutModifier.Shift in stroke.modifiers) append("Shift+")
                append(stroke.key.display)
            }
        }

    companion object {
        fun single(
            key: ShortcutKey,
            vararg modifiers: ShortcutModifier,
        ): Shortcut = Shortcut(
            strokes = listOf(
                ShortcutStroke(
                    key = key,
                    modifiers = modifiers.toSet(),
                ),
            ),
        )

        fun sequence(vararg keys: ShortcutKey): Shortcut = Shortcut(
            strokes = keys.map(::ShortcutStroke),
        )
    }
}
