package com.mcoder.domain

import com.mcoder.domain.model.FileEntry

/**
 * File browser contract for the isolated workspace.
 */
interface FileRepository {
    suspend fun list(path: String): List<FileEntry>
    suspend fun readText(path: String): String
    suspend fun writeText(path: String, content: String)
    suspend fun createFolder(path: String)
    suspend fun delete(path: String)
    suspend fun rename(path: String, newName: String)
}
