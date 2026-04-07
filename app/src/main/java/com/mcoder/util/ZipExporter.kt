package com.mcoder.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Zips the workspace into a single archive.
 */
object ZipExporter {
    fun exportWorkspace(outputPath: String = "/storage/emulated/0/Mcoder/export.zip") {
        val root = File(Constants.WORKSPACE_ROOT)
        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            zipFolder(root, root, zos)
        }
    }

    private fun zipFolder(root: File, source: File, zos: ZipOutputStream) {
        source.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                zipFolder(root, file, zos)
            } else {
                val relativePath = root.toURI().relativize(file.toURI()).path
                zos.putNextEntry(ZipEntry(relativePath))
                FileInputStream(file).use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
    }
}
