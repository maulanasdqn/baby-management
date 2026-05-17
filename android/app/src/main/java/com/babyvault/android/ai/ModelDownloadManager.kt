package com.babyvault.android.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class DownloadProgress(val bytesDownloaded: Long, val totalBytes: Long) {
    val fraction: Float get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
    val isDone: Boolean get() = bytesDownloaded >= totalBytes && totalBytes > 0
}

/**
 * Downloads the Gemma 3 1B SafeTensors model from HuggingFace.
 * Files: config.json, tokenizer.json, model.safetensors
 */
@Singleton
class ModelDownloadManager @Inject constructor(private val context: Context) {

    companion object {
        private const val HF_BASE =
            "https://huggingface.co/google/gemma-3-1b-it/resolve/main"
        val REQUIRED_FILES = listOf("config.json", "tokenizer.json", "model.safetensors")
    }

    val modelDir: File get() = File(context.filesDir, "gemma3_1b")

    fun isDownloaded(): Boolean =
        REQUIRED_FILES.all { File(modelDir, it).exists() }

    fun download(): Flow<DownloadProgress> = flow {
        modelDir.mkdirs()
        val fileSizes = mapOf(
            "config.json" to 1_024L,
            "tokenizer.json" to 500_000L,
            "model.safetensors" to 2_400_000_000L,
        )
        val total = fileSizes.values.sum()
        var downloaded = 0L

        for (fileName in REQUIRED_FILES) {
            val dest = File(modelDir, fileName)
            if (dest.exists()) {
                downloaded += fileSizes[fileName] ?: 0L
                emit(DownloadProgress(downloaded, total))
                continue
            }
            val url = "$HF_BASE/$fileName"
            withContext(Dispatchers.IO) {
                downloadFile(url, dest) { bytes ->
                    downloaded += bytes
                }
            }
            emit(DownloadProgress(downloaded, total))
        }
    }

    private fun downloadFile(url: String, dest: File, onChunk: (Long) -> Unit) {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 30_000
        conn.readTimeout = 60_000
        conn.connect()
        conn.inputStream.use { input ->
            dest.outputStream().use { output ->
                val buf = ByteArray(8 * 1024)
                var n: Int
                while (input.read(buf).also { n = it } != -1) {
                    output.write(buf, 0, n)
                    onChunk(n.toLong())
                }
            }
        }
        conn.disconnect()
    }
}
