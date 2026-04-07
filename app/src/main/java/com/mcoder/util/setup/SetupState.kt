package com.mcoder.util.setup

import com.mcoder.util.Constants
import java.io.File

/**
 * Persists setup completion state.
 */
object SetupState {
    private val marker = File("${Constants.WORKSPACE_ROOT}/.setup_done")

    fun isDone(): Boolean = marker.exists()

    fun markDone() {
        marker.parentFile?.mkdirs()
        marker.writeText("ok")
    }
}
