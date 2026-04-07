package com.mcoder.util.terminal

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Downloads and installs a Node.js toolchain into the workspace.
 */
class ToolchainInstaller(private val toolchainDir: File) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    suspend fun install(onProgress: (String) -> Unit, onPercent: (Float) -> Unit = {}) =
        withContext(Dispatchers.IO) {
        val arch = resolveArch() ?: run {
            onProgress("Unsupported ABI")
            return@withContext
        }
        val baseUrl = "https://nodejs.org/download/release/latest-v20.x/"
        onProgress("Fetching index...")
        val index = fetchText(baseUrl)
        val fileName = findNodeArchive(index, arch) ?: run {
            onProgress("Node archive not found for $arch")
            return@withContext
        }
        val url = baseUrl + fileName
        val tempDir = File(toolchainDir, "tmp")
        tempDir.mkdirs()
        val archive = File(tempDir, fileName)
        onProgress("Downloading $fileName")
        download(url, archive, onPercent)
        onProgress("Extracting...")
        extractTarXz(archive, tempDir)
        val extractedRoot = tempDir.listFiles()?.firstOrNull { it.isDirectory && it.name.startsWith("node-") }
        if (extractedRoot == null) {
            onProgress("Extract failed")
            return@withContext
        }
        val targetBin = File(toolchainDir, "bin")
        val targetLib = File(toolchainDir, "lib")
        val targetInclude = File(toolchainDir, "include")
        val targetShare = File(toolchainDir, "share")
        targetBin.mkdirs()
        targetLib.mkdirs()
        targetInclude.mkdirs()
        targetShare.mkdirs()

        copyDir(File(extractedRoot, "bin"), targetBin)
        copyDir(File(extractedRoot, "lib"), targetLib)
        copyDir(File(extractedRoot, "include"), targetInclude)
        copyDir(File(extractedRoot, "share"), targetShare)

        targetBin.listFiles()?.forEach { it.setExecutable(true) }
        onProgress("Done")
        archive.delete()
        extractedRoot.deleteRecursively()
    }

    suspend fun installBusybox(onProgress: (String) -> Unit, onPercent: (Float) -> Unit = {}) =
        withContext(Dispatchers.IO) {
        val arch = resolveArch() ?: run {
            onProgress("Unsupported ABI")
            return@withContext
        }
        val url = when (arch) {
            "linux-arm64" -> "https://raw.githubusercontent.com/EXALAB/Busybox-static/main/busybox_arm64"
            "linux-armv7l" -> "https://raw.githubusercontent.com/EXALAB/Busybox-static/main/busybox_arm"
            "linux-x64" -> "https://raw.githubusercontent.com/EXALAB/Busybox-static/main/busybox_amd64"
            else -> null
        } ?: run {
            onProgress("BusyBox not available for $arch")
            return@withContext
        }
        val binDir = File(toolchainDir, "bin")
        binDir.mkdirs()
        val busybox = File(binDir, "busybox")
        onProgress("Downloading BusyBox...")
        download(url, busybox, onPercent)
        busybox.setExecutable(true)
        onProgress("Installing BusyBox applets...")
        runBusyboxInstall(busybox, binDir)
        onProgress("BusyBox ready")
    }

    suspend fun installGit(onProgress: (String) -> Unit, onPercent: (Float) -> Unit = {}) =
        withContext(Dispatchers.IO) {
            val arch = resolveTermuxArch() ?: run {
                onProgress("Git not available for this ABI")
                return@withContext
            }
            val mirrors = listOf(
                "https://mirrors.ravidwivedi.in/termux/termux-main/pool/main/g/git/",
                "https://packages.termux.dev/apt/termux-main/pool/main/g/git/",
                "https://mirror.nyist.edu.cn/termux/apt/termux-main/pool/main/g/git/"
            )
            val tempDir = File(toolchainDir, "tmp")
            tempDir.mkdirs()
            var success = false
            for (base in mirrors) {
                if (success) break
                onProgress("Git mirror: $base")
                val index = fetchTextSafe(base)
                if (index == null) {
                    onProgress("Mirror unavailable")
                    continue
                }
                val deb = findGitDeb(index, arch)
                if (deb == null) {
                    onProgress("Git package not found for $arch")
                    continue
                }
                val url = base + deb
                val debFile = File(tempDir, deb)
                onProgress("Downloading git...")
                try {
                    download(url, debFile, onPercent)
                } catch (_: Exception) {
                    onProgress("Download failed, trying next mirror")
                    debFile.delete()
                    continue
                }
                onProgress("Extracting git...")
                try {
                    extractDeb(debFile, toolchainDir)
                    debFile.delete()
                    onProgress("Git ready")
                    success = true
                    break
                } catch (_: Exception) {
                    onProgress("Extract failed, trying next mirror")
                    debFile.delete()
                }
            }
            if (!success) onProgress("Git install failed")
        }

    private fun resolveArch(): String? {
        val abis = Build.SUPPORTED_ABIS
        return when {
            abis.any { it.contains("arm64") } -> "linux-arm64"
            abis.any { it.contains("armeabi-v7a") || it.contains("armv7") } -> "linux-armv7l"
            abis.any { it.contains("x86_64") } -> "linux-x64"
            else -> null
        }
    }

    private fun resolveTermuxArch(): String? {
        val abis = Build.SUPPORTED_ABIS
        return when {
            abis.any { it.contains("arm64") } -> "aarch64"
            abis.any { it.contains("armeabi-v7a") || it.contains("armv7") } -> "arm"
            abis.any { it.contains("x86_64") } -> "x86_64"
            else -> null
        }
    }

    private fun findNodeArchive(index: String, arch: String): String? {
        val pattern = Pattern.compile("node-v[0-9.]+-$arch\\.tar\\.xz")
        val matcher = pattern.matcher(index)
        return if (matcher.find()) matcher.group() else null
    }

    private fun fetchText(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            return response.body?.string().orEmpty()
        }
    }

    private fun fetchTextSafe(url: String): String? {
        return try {
            fetchText(url)
        } catch (_: Exception) {
            null
        }
    }

    private fun download(url: String, dest: File, onPercent: (Float) -> Unit) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            val total = response.body?.contentLength() ?: -1L
            var read = 0L
            dest.outputStream().use { out ->
                val input = response.body?.byteStream() ?: return
                val buffer = ByteArray(8192)
                var n = input.read(buffer)
                while (n >= 0) {
                    out.write(buffer, 0, n)
                    read += n
                    if (total > 0) {
                        onPercent(read.toFloat() / total.toFloat())
                    }
                    n = input.read(buffer)
                }
            }
        }
    }

    private fun findGitDeb(index: String, arch: String): String? {
        val pattern = Pattern.compile("git_[0-9][^\\\"]*_${arch}\\.deb")
        val matcher = pattern.matcher(index)
        var last: String? = null
        while (matcher.find()) {
            last = matcher.group()
        }
        return last
    }

    private fun extractDeb(debFile: File, outputDir: File) {
        org.apache.commons.compress.archivers.ar.ArArchiveInputStream(debFile.inputStream()).use { ar ->
            var entry = ar.nextArEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.startsWith("data.tar")) {
                    val dataBytes = ar.readBytes()
                    val dataFile = File(debFile.parentFile, entry.name)
                    dataFile.writeBytes(dataBytes)
                    extractTarArchive(dataFile, outputDir)
                    dataFile.delete()
                    break
                }
                entry = ar.nextArEntry
            }
        }
    }

    private fun extractTarArchive(archive: File, outputDir: File) {
        val name = archive.name
        val input = when {
            name.endsWith(".xz") -> XZCompressorInputStream(archive.inputStream())
            name.endsWith(".gz") -> org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream(archive.inputStream())
            else -> archive.inputStream()
        }
        input.use { stream ->
            TarArchiveInputStream(stream).use { tar ->
                var entry = tar.nextTarEntry
                while (entry != null) {
                    val outFile = File(outputDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out ->
                            tar.copyTo(out)
                        }
                    }
                    entry = tar.nextTarEntry
                }
            }
        }
    }

    private fun runBusyboxInstall(busybox: File, binDir: File) {
        try {
            ProcessBuilder(listOf(busybox.absolutePath, "--install", "-s", binDir.absolutePath))
                .redirectErrorStream(true)
                .start()
                .waitFor()
        } catch (_: Exception) {
            // Ignore; BusyBox binary is still usable directly.
        }
    }

    private fun extractTarXz(archive: File, outputDir: File) {
        XZCompressorInputStream(archive.inputStream()).use { xz ->
            TarArchiveInputStream(xz).use { tar ->
                var entry = tar.nextTarEntry
                while (entry != null) {
                    val outFile = File(outputDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out ->
                            tar.copyTo(out)
                        }
                    }
                    entry = tar.nextTarEntry
                }
            }
        }
    }

    private fun copyDir(from: File, to: File) {
        if (!from.exists()) return
        from.listFiles()?.forEach { file ->
            val target = File(to, file.name)
            if (file.isDirectory) {
                target.mkdirs()
                copyDir(file, target)
            } else {
                file.copyTo(target, overwrite = true)
            }
        }
    }
}
