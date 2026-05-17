package com.babyvault.android.domain.model
import java.time.Instant
data class Milestone(
    val id: String,
    val title: String,
    val description: String,
    val occurredAt: Instant,
    val createdAt: Instant,
)
