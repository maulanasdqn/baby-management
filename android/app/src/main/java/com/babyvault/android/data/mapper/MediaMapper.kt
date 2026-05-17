package com.babyvault.android.data.mapper
import com.babyvault.android.domain.model.MediaItem
import com.babyvault.core.MediaItemDto
import java.time.Instant
fun MediaItemDto.toDomain() = MediaItem(
    id = id,
    title = title,
    encryptedPath = encryptedPath,
    sizeBytes = sizeBytes.toLong(),
    createdAt = Instant.ofEpochMilli(createdAtMillis),
)
