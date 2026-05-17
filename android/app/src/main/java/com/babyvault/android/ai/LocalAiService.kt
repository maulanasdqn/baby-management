package com.babyvault.android.ai
import android.content.Context
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class LocalAiService @Inject constructor(@ApplicationContext private val context: Context) {
    private var model: GenerativeModel? = null
    private var aiCoreAvailable = false
    companion object {
        private const val SYSTEM_PROMPT = """You are Baby AI, a friendly baby-care assistant built into a baby tracking app.
You ONLY answer questions about: feeding, breastfeeding, formula, baby sleep, diapers, growth, developmental milestones, teething, baby health, bathing, tummy time, and newborn care.
If the user asks about anything unrelated to baby care (programming, politics, sports, technology, etc.), politely decline and redirect them to baby-care topics.
Keep answers concise, warm, and evidence-based. Always recommend consulting a pediatrician for medical concerns.
"""
    }
    suspend fun initialize(): AiCoreStatus {
        return try {
            val cfg = generationConfig {
                this.context = this@LocalAiService.context
                temperature = 0.2f
                topK = 16
                maxOutputTokens = 512
            }
            model = GenerativeModel(cfg)
            aiCoreAvailable = true
            AiCoreStatus.Available
        } catch (e: Exception) {
            aiCoreAvailable = false
            AiCoreStatus.Unavailable(e.message ?: "AICore not supported on this device")
        }
    }
    fun isReady(): Boolean = aiCoreAvailable && model != null
    fun generate(userMessage: String): Flow<String> = flow {
        val m = model
        if (!aiCoreAvailable || m == null) {
            stubReply(userMessage).forEach { emit(it) }
            return@flow
        }
        val prompt = buildPrompt(userMessage)
        try {
            m.generateContentStream(prompt).collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        } catch (e: Exception) {
            emit("Sorry, something went wrong. Please try again.")
        }
    }
    private fun buildPrompt(userMessage: String): String =
        "$SYSTEM_PROMPT\nUser: $userMessage\nAssistant:"
    private fun stubReply(userMessage: String): List<String> {
        val q = userMessage.lowercase()
        val reply = when {
            isOffTopic(q) ->
                "I'm a baby-care assistant and can only help with topics related to your baby — feeding, sleep, diapers, growth, and milestones."
            anyOf(q, "who are you", "what are you") ->
                "I'm Baby AI, your on-device baby-care assistant powered by Gemini Nano."
            anyOf(q, "walk", "walking", "first step") ->
                "Most babies take first steps between 9–12 months, though 9–15 months is normal."
            anyOf(q, "feed", "feeding", "breastfeed", "bottle", "formula", "milk") ->
                "Newborns feed every 2–3 hours. By 3 months: every 3–4 hours. Introduce solids around 6 months."
            anyOf(q, "sleep", "nap", "night") ->
                "Newborns sleep 14–17 hours/day. A consistent bedtime routine helps establish healthy sleep habits."
            anyOf(q, "diaper", "poop", "pee") ->
                "Expect 6–8 wet diapers per day after day 4. Fewer may indicate dehydration."
            anyOf(q, "growth", "weight", "milestone", "develop") ->
                "Babies gain ~150–200 g/week in the first 3 months and grow ~2.5 cm/month."
            else ->
                "AICore (Gemini Nano) is not available on this device. I'm running in basic mode — I can help with feeding, sleep, diapers, and growth questions."
        }
        return reply.split(" ").map { "$it " }
    }
    private fun anyOf(text: String, vararg keywords: String) = keywords.any { it in text }
    private fun isOffTopic(q: String): Boolean {
        val offTopicPatterns = listOf(
            "rust", "kotlin", "java", "python", "code", "programming", "software",
            "hello world", "algorithm", "database", "server", "api",
            "politics", "religion", "president", "election", "war",
            "stock", "crypto", "bitcoin", "recipe", "cooking",
            "sport", "football", "movie", "music", "game", "weather",
        )
        val babyWords = listOf(
            "baby", "infant", "newborn", "toddler", "child",
            "feed", "sleep", "diaper", "growth", "milk", "formula",
        )
        return offTopicPatterns.any { it in q } && babyWords.none { it in q }
    }
}
sealed class AiCoreStatus {
    object Available : AiCoreStatus()
    data class Unavailable(val reason: String) : AiCoreStatus()
}
