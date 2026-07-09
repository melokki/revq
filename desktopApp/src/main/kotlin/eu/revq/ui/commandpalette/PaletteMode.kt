package eu.revq.ui.commandpalette

/**
 * RevQ has one command-palette surface and one global entry point: Space.
 * RepositoryScope is an invocation context inside that same surface, not a second palette.
 */
sealed interface PaletteMode {
    data object Universal : PaletteMode
    data object RepositoryScope : PaletteMode
}

val PaletteMode.title: String
    get() = when (this) {
        PaletteMode.Universal -> "Command palette"
        PaletteMode.RepositoryScope -> "Repository scope"
    }

val PaletteMode.placeholder: String
    get() = when (this) {
        PaletteMode.Universal -> "Search commands, pull requests, views, repositories, shortcuts..."
        PaletteMode.RepositoryScope -> "Search tracked repositories..."
    }

val PaletteMode.entryLabel: String
    get() = when (this) {
        PaletteMode.Universal -> "Space"
        PaletteMode.RepositoryScope -> "Scope"
    }

val PaletteMode.acceptsTextQuery: Boolean
    get() = true
