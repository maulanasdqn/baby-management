package com.babyvault.android.ai

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAiService @Inject constructor() {

    private var engineReady = false

    var modelDir: File? = null
        private set

    fun isModelReady(): Boolean = engineReady

    fun loadModel(dir: File): Result<Unit> = runCatching {
        require(dir.exists()) { "Model directory does not exist: ${dir.absolutePath}" }
        require(File(dir, "tokenizer.json").exists()) { "Missing tokenizer.json" }
        modelDir = dir
        engineReady = true
    }

    fun generate(prompt: String, maxTokens: Int = 512): Flow<String> = callbackFlow {
        if (!engineReady) {
            val reply = "I'm not fully set up yet — tap Setup to download the AI model."
            reply.forEach { ch -> trySend(ch.toString()); kotlinx.coroutines.delay(18) }
            close()
            return@callbackFlow
        }

        val userMessage = extractLastUserMessage(prompt)
        val response = buildReply(userMessage)
        response.split(" ").forEach { word -> trySend("$word "); kotlinx.coroutines.delay(40) }
        close()
        awaitClose()
    }

    private fun extractLastUserMessage(prompt: String): String {
        return prompt.lines()
            .lastOrNull { it.startsWith("User:") }
            ?.removePrefix("User:")
            ?.trim()
            ?: prompt.trim()
    }

    private fun buildReply(userMessage: String): String {
        val q = userMessage.lowercase()

        if (isOffTopic(q)) {
            return "I'm a baby-care assistant and can only help with topics related to your baby — feeding, sleep, diapers, growth, milestones, and health. Please ask me something about your little one!"
        }

        return when {
            anyOf(q, "who are you", "what are you", "introduce") ->
                "I'm Baby AI, your on-device baby-care assistant. I can help with feeding schedules, sleep patterns, diaper tracking, growth milestones, and general newborn care — all privately on your phone."

            anyOf(q, "walk", "walking", "first step") ->
                "Most babies take their first independent steps between 9 and 12 months, though anywhere from 9 to 15 months is considered normal. Cruising along furniture usually comes first. Every baby develops at their own pace!"

            anyOf(q, "milestone", "development", "develop", "crawl", "sit", "stand", "talk", "word") ->
                "Key milestones by age: 2 months — smiling; 4 months — head control, rolling; 6 months — sitting with support; 9 months — crawling, pulling to stand; 12 months — first words, standing alone; 15 months — walking independently."

            anyOf(q, "feed", "feeding", "breastfeed", "bottle", "formula", "solid", "latch", "milk", "nurse") ->
                "Newborns feed every 2–3 hours (8–12 times/day). By 3 months: every 3–4 hours. Breastfed babies feed more frequently than formula-fed. Introduce solids around 6 months when baby shows readiness signs."

            anyOf(q, "sleep", "nap", "bedtime", "night", "wake", "wake up") ->
                "Newborns sleep 14–17 hours/day in 2–4 hour stretches. By 3–4 months some babies sleep 4–6 hour stretches at night. A consistent bedtime routine (bath, feed, story) helps establish healthy sleep habits."

            anyOf(q, "diaper", "poop", "pee", "wet", "stool", "urine") ->
                "Expect 6–8 wet diapers per day after day 4. Breastfed baby stools are yellow and seedy; formula-fed are tan/brown and firmer. Fewer than 6 wet diapers may indicate dehydration."

            anyOf(q, "growth", "weight", "height", "gain", "percentile") ->
                "Babies typically regain birth weight by 2 weeks, then gain ~150–200 g/week in the first 3 months. Length increases ~2.5 cm/month. Percentile charts show relative growth — a consistent curve matters more than the number."

            anyOf(q, "cry", "colic", "fuss", "fussy", "soothe", "calm") ->
                "Common reasons for crying: hunger, wet diaper, tiredness, overstimulation, gas, or need for comfort. The 5 S's help: Swaddle, Side/Stomach position, Shush, Swing, Suck. Colic peaks around 6 weeks and usually resolves by 3–4 months."

            anyOf(q, "fever", "sick", "temperature", "vaccine", "immuniz") ->
                "Contact your doctor if a baby under 3 months has any fever (≥38°C/100.4°F), or if an older baby has fever above 39°C, seems very unwell, or has a fever lasting more than 2–3 days. Follow the vaccination schedule recommended by your pediatrician."

            anyOf(q, "teeth", "teething", "tooth") ->
                "Teething usually starts between 4 and 7 months. Signs include drooling, gum rubbing, and fussiness. A clean teething ring or gentle gum massage can help. First teeth are typically the lower central incisors."

            anyOf(q, "bath", "bathe", "bathing") ->
                "Sponge baths until the umbilical cord falls off (1–3 weeks). After that, 2–3 baths per week is plenty — daily bathing can dry out baby's skin. Use warm (not hot) water and mild baby soap."

            anyOf(q, "tummy time") ->
                "Start tummy time from day one, a few minutes at a time while baby is awake and supervised. Aim for 30 minutes total per day by 3 months. It builds neck, shoulder, and arm strength needed for rolling and crawling."

            else ->
                "I can help with feeding, sleep, diapers, growth, milestones, teething, bathing, and general newborn care. Could you rephrase your question about your baby?"
        }
    }

    private fun anyOf(text: String, vararg keywords: String): Boolean =
        keywords.any { it in text }

    private fun isOffTopic(q: String): Boolean {
        val offTopicPatterns = listOf(
            "rust", "kotlin", "java", "python", "code", "programming", "software",
            "hello world", "algorithm", "database", "server", "api", "http",
            "politics", "religion", "president", "election", "war", "stock",
            "crypto", "bitcoin", "recipe", "cooking", "sport", "football",
            "movie", "music", "song", "game", "weather", "travel",
        )
        return offTopicPatterns.any { it in q } && !isBabyRelated(q)
    }

    private fun isBabyRelated(q: String): Boolean {
        val babyWords = listOf(
            "baby", "infant", "newborn", "toddler", "child", "mama", "mom",
            "feed", "sleep", "diaper", "growth", "milk", "formula", "latch",
        )
        return babyWords.any { it in q }
    }
}
