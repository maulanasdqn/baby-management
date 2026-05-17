package com.babyvault.android.data.mapper
import com.babyvault.android.domain.model.Milestone
import com.babyvault.core.MilestoneDto
import java.time.Instant
fun MilestoneDto.toDomain() = Milestone(
    id = id,
    title = title,
    description = description,
    occurredAt = Instant.ofEpochMilli(occurredAtMillis),
    createdAt = Instant.ofEpochMilli(createdAtMillis),
)
