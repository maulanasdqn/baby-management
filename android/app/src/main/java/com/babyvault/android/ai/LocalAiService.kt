package com.babyvault.android.ai

import android.content.Context
import com.babyvault.inference.NativeInferenceEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAiService @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private var engine: NativeInferenceEngine? = null

    fun modelDir(): File = File(context.filesDir, "inference_model")

    fun isModelDownloaded(): Boolean {
        val dir = modelDir()
        return dir.resolve("model.safetensors").exists() &&
               dir.resolve("vocab.json").exists() &&
               dir.resolve("merges.txt").exists()
    }

    fun isReady(): Boolean = engine?.isReady() == true

    fun initialize(): InferenceStatus {
        if (!isModelDownloaded()) return InferenceStatus.ModelMissing
        return try {
            System.loadLibrary("inference")
            val eng = NativeInferenceEngine(modelDir().absolutePath)
            if (eng.isReady()) {
                engine = eng
                InferenceStatus.Ready
            } else {
                InferenceStatus.Error("Failed to load model weights")
            }
        } catch (e: Exception) {
            InferenceStatus.Error(e.message ?: "unknown error")
        }
    }

    fun generate(userMessage: String): Flow<String> {
        val eng = engine
        if (eng == null || !eng.isReady()) {
            return flow { emit(stubReply(userMessage)) }
        }
        return flow {
            val stream = eng.generateStream(userMessage, 200u)
            while (true) {
                val token = stream.nextToken() ?: break
                emit(token)
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun stubReply(q: String): String {
        val lower = q.lowercase()
        return when {
            anyOf(lower, "feed", "feeding", "formula", "milk", "breastfeed") ->
                "Newborns feed every 2–3 hours. By 3 months: every 3–4 hours. Introduce solids around 6 months."
            anyOf(lower, "sleep", "nap", "night", "bedtime") ->
                "Newborns need 14–17 hours of sleep. A consistent bedtime routine helps."
            anyOf(lower, "diaper", "poop", "pee", "wet") ->
                "After day 4, expect 6–8 wet diapers per day. Fewer may signal dehydration."
            anyOf(lower, "growth", "weight", "height", "milestone", "develop") ->
                "Babies gain ~150–200 g/week in the first 3 months and grow ~2.5 cm/month."
            anyOf(lower, "walk", "crawl", "talk", "first step") ->
                "Most babies take first steps between 9–12 months. Every baby develops at their own pace."
            else ->
                "The on-device model is not loaded yet. Download it from the AI Setup screen."
        }
    }

    private fun anyOf(text: String, vararg keywords: String) = keywords.any { it in text }
}

sealed class InferenceStatus {
    object Ready : InferenceStatus()
    object ModelMissing : InferenceStatus()
    data class Error(val reason: String) : InferenceStatus()
}
