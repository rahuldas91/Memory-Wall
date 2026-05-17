package com.example.memories.model

enum class MediaType {
    PHOTO,
    VIDEO
}

data class Photo(
    val id: String,
    val url: String,
    val albumId: String = "",
    val mediaType: MediaType = MediaType.PHOTO,
    val thumbnailUrl: String? = null  // For video thumbnails
)
