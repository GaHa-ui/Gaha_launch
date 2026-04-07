package com.mcoder.data.repo

import com.mcoder.domain.FileRepository
import com.mcoder.domain.model.FileEntry
import com.mcoder.util.Constants
import java.io.File
import javax.inject.Inject

/**
 * File repository scoped to the Mcoder workspace directory.
 */
class FileRepositoryImpl @Inject constructor() : FileRepository {

    override suspend fun list(path: String): List<FileEntry> {
        val folder = resolveSafe(path)
        return folder.listFiles()?.map { file ->
            FileEntry(
                path = file.absolutePath,
                name = file.name,
                isDirectory = file.isDirectory,
                sizeBytes = file.length()
            )
        } ?: emptyList()
    }

    override suspend fun readText(path: String): String {
        val file = resolveSafe(path)
        return file.readText()
    }

    override suspend fun writeText(path: String, content: String) {
        val file = resolveSafe(path)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }
        file.writeText(content)
    }

    override suspend fun createFolder(path: String) {
        val folder = resolveSafe(path)
        if (!folder.exists()) {
            folder.mkdirs()
        }
    }

    override suspend fun delete(path: String) {
        val target = resolveSafe(path)
        target.deleteRecursively()
    }

    override suspend fun rename(path: String, newName: String) {
        val target = resolveSafe(path)
        val newFile = File(target.parentFile, newName)
        target.renameTo(newFile)
    }

    private fun resolveSafe(path: String): File {
        val root = File(Constants.WORKSPACE_ROOT).canonicalFile
        val target = File(path).canonicalFile
        require(target.path.startsWith(root.path)) {
            "Access outside workspace is not allowed."
        }
        return target
    }
}
