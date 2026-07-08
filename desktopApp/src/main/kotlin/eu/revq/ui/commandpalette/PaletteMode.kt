package eu.revq.ui.commandpalette

/**
 * RevQ has one command-palette surface and one global entry point: Space.
 */
sealed interface PaletteMode {
    data object Universal : PaletteMode
}

val PaletteMode.title: String
    get() = "Command palette"

val PaletteMode.placeholder: String
    get() = "Search commands, pull requests, views, repositories, shortcuts..."

val PaletteMode.entryLabel: String
    get() = "Space"

val PaletteMode.acceptsTextQuery: Boolean
    get() = true
