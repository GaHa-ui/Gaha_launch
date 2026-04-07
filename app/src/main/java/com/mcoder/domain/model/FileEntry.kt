package com.mcoder.domain.model

/**
 * File system entry within the Mcoder workspace.
 */
data class FileEntry(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long
)
