package com.clipflow.cli

import com.clipflow.cli.commands.ConfigCommand
import com.clipflow.cli.commands.CopyCommand
import com.clipflow.cli.commands.DeleteCommand
import com.clipflow.cli.commands.DevicesCommand
import com.clipflow.cli.commands.EditCommand
import com.clipflow.cli.commands.HistoryCommand
import com.clipflow.cli.commands.PairCommand
import com.clipflow.cli.commands.PasteCommand
import com.clipflow.cli.commands.PickCommand
import com.clipflow.cli.commands.StatusCommand
import com.clipflow.cli.commands.TagsCommand
import com.clipflow.cli.commands.TokenCommand
import com.clipflow.cli.commands.VersionCommand
import com.clipflow.cli.commands.WatchCommand
import com.clipflow.cli.commands.copyToClipFlow
import com.clipflow.cli.commands.readPipedStdinOrFail
import com.clipflow.cli.commands.usageError
import com.clipflow.cli.platform.readAllStdin
import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.obj
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch

/**
 * The name users actually invoke, shown in usage/help output and hints.
 * Design decision D4: the terminal command is `clipflow` on every platform.
 */
internal const val cliCommandName: String = "clipflow"

class ClipFlowCommand(
    /** Injectable so tests of the implicit-copy path never touch real stdin. */
    private val stdinReader: () -> String = ::readAllStdin,
) : CliktCommand(name = cliCommandName) {

    /** Lets a bare invocation with piped stdin behave as `copy`. */
    override val invokeWithoutSubcommand: Boolean get() = true

    override fun aliases(): Map<String, List<String>> =
        mapOf(
            "c" to listOf("copy"),
            "p" to listOf("paste"),
            "h" to listOf("history"),
        )

    override fun help(context: Context): String =
        "ClipFlow CLI - interact with your local ClipFlow application. " +
            "When input is piped and no command is given, behaves as `copy`: " +
            "`git log | $cliCommandName` copies the log."

    override fun helpEpilog(context: Context): String =
        "Command aliases: c = copy, p = paste, h = history.\n\n" +
            "Exit codes: 0 success, 1 error, 2 usage error, 3 ClipFlow not running."

    val json by option("--json", help = "Output in JSON format for machine consumption").flag()

    val autoStart by option(
        help =
            "When ClipFlow is not running: --start launches it without asking, --no-start " +
                "never launches it. Default is to ask on an interactive terminal and to fail " +
                "with exit code 3 otherwise.",
    ).switch(
        "--start" to true,
        "--no-start" to false,
    )

    override fun run() {
        val cliContext = CliContext(json = json, autoStart = autoStart)
        currentContext.obj = cliContext
        if (currentContext.invokedSubcommand != null) return
        // No subcommand: piped stdin means an implicit `copy`; an interactive
        // terminal keeps the pre-invokeWithoutSubcommand usage-error behavior
        if (terminal.terminalInfo.inputInteractive) {
            throw usageError("missing command (pipe input via stdin to copy it without one)")
        }
        copyToClipFlow(readPipedStdinOrFail(stdinReader), jsonOutput = cliContext.json)
    }

    init {
        completionOption()
        subcommands(
            StatusCommand(),
            PasteCommand(),
            HistoryCommand(),
            PickCommand(),
            CopyCommand(),
            EditCommand(),
            DeleteCommand(),
            DevicesCommand(),
            PairCommand(),
            TokenCommand(),
            ConfigCommand(),
            TagsCommand(),
            VersionCommand(),
            WatchCommand(),
        )
    }
}

data class CliContext(
    val json: Boolean = false,
    /** Tri-state launch consent: true = always start, false = never, null = ask. */
    val autoStart: Boolean? = null,
)
