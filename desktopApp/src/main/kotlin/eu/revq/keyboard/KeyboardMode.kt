package eu.revq.keyboard

/**
 * RevQ keeps the user-facing modal model intentionally small.
 * Palette and Modal are routing contexts; Normal and Insert are the modes
 * users need to understand during everyday navigation.
 */
enum class KeyboardMode {
    Normal,
    Insert,
    Palette,
    Modal,
}
