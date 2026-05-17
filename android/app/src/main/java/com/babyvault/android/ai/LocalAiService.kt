package com.babyvault.android.ai

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the native inference engine (or a stub when no model is loaded).
 * Call [isModelReady] before generating; call [loadModel] to initialise.
 */
@Singleton
class LocalAiService @Inject constructor() {

    private var engineReady = false
    private val modelDirPath: String
        get() = modelDir?.absolutePath ?: ""

    var modelDir: File? = null
        private set

    fun isModelReady(): Boolean = engineReady

    /** Point to an already-downloaded model directory and mark the engine ready. */
    fun loadModel(dir: File): Result<Unit> = runCatching {
        require(dir.exists()) { "Model directory does not exist: ${dir.absolutePath}" }
        require(File(dir, "tokenizer.json").exists()) { "Missing tokenizer.json" }
        modelDir = dir
        engineReady = true
    }

    /**
     * Generate a streaming response token-by-token.
     * Falls back to a stub response when no native engine is available.
     */
    fun generate(prompt: String, maxTokens: Int = 512): Flow<String> = callbackFlow {
        if (!engineReady) {
            // Stub: helpful placeholder until the native .so is wired up
            val reply = "I'm not fully loaded yet — please set up the AI model first."
            reply.forEach { ch ->
                trySend(ch.toString())
                kotlinx.coroutines.delay(20)
            }
            close()
            return@callbackFlow
        }

        // Native path — invoke via JNI once the UniFFI binding is in place:
        // InferenceEngine(modelDirPath).generate(prompt, object : StreamCallback { ... }, maxTokens.toLong())
        // For now emit stub tokens so the UI can be tested end-to-end.
        val response = buildBabyReply(prompt)
        response.split(" ").forEach { word ->
            trySend("$word ")
            kotlinx.coroutines.delay(40)
        }
        close()
        awaitClose()
    }

    private fun buildBabyReply(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            "feed" in lower || "feeding" in lower ->
                "Based on typical patterns, newborns feed every 2-3 hours, while older babies may go 3-4 hours between feeds."
            "sleep" in lower ->
                "Newborns sleep 14-17 hours total per day in short stretches. Establishing a bedtime routine helps."
            "diaper" in lower ->
                "Expect 6-8 wet diapers per day after day 4. Fewer than 6 may indicate dehydration."
            "growth" in lower || "weight" in lower ->
                "Babies typically regain birth weight by 2 weeks and gain about 150-200g per week in the first 3 months."
            else ->
                "I'm your baby-care assistant. Ask me about feeding schedules, sleep patterns, diaper changes, or growth milestones!"
        }
    }
}
