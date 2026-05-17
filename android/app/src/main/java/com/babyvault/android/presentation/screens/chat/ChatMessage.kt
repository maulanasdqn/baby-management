package com.babyvault.android.presentation.screens.chat
enum class Sender { User, Assistant }
data class ChatMessage(
    val id: Long,
    val sender: Sender,
    val text: String,
    val isStreaming: Boolean = false,
)
