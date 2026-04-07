package com.mcoder.util.setup

/**
 * Setup step state for UI.
 */
data class SetupStep(
    val id: String,
    val label: String,
    val state: StepState = StepState.Pending,
    val progress: Float? = null,
    val message: String = ""
)

enum class StepState {
    Pending,
    Running,
    Done,
    Error
}
