package com.clipflow.cli.commands

import com.clipflow.cli.platform.AppLauncher
import com.clipflow.cli.platform.AppReadinessChecker

/**
 * Launches the desktop app and waits for its CLI endpoint to come up.
 * Progress messages are emitted through [echo] so callers can keep them
 * out of stdout (pipes must only receive command output).
 */
class AppAutoStarter(
    private val launcher: AppLauncher,
    private val readinessChecker: AppReadinessChecker,
) {

    suspend fun startAndWait(echo: (message: String) -> Unit): Boolean {
        echo("Starting ClipFlow...")

        val launched = launcher.launch()
        if (!launched) {
            echo("Failed to start ClipFlow. Please start it manually.")
            return false
        }

        echo("Waiting for ClipFlow to become ready...")
        val ready = readinessChecker.waitForAppReady()
        if (!ready) {
            echo("Timed out waiting for ClipFlow to become ready. Please check the application manually.")
            return false
        }

        echo("ClipFlow is now running.")
        return true
    }
}
