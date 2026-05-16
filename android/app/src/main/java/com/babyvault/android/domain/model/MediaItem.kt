package com.babyvault.android.domain.model

import java.time.Instant

data class MediaItem(
    val id: String,
    val title: String,
    val encryptedPath: String,
    val sizeBytes: Long,
    val createdAt: Instant,
)
