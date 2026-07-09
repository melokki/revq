package eu.revq.commands

enum class CommandExecutionResult {
    Executed,
    Disabled,
    Missing,
}

data class AppCommand(
    val id: CommandId,
    val title: String,
    val description: String? = null,
    val category: CommandCategory,
    val aliases: List<String> = emptyList(),
    val shortcut: Shortcut? = null,
    val isEnabled: (CommandContext) -> Boolean = { true },
    val disabledReason: (CommandContext) -> String? = { null },
)
