package com.example.memories.model

data class Album(
    val id: String,
    val name: String,
    val coverUrl: String,
    val photoCount: Int = 0,
    val videoCount: Int = 0
) {
    val totalCount: Int get() = photoCount + videoCount
    
    fun getCountText(): String {
        // If both counts are specified (non-zero or explicitly set)
        return when {
            videoCount > 0 && photoCount > 0 -> "$photoCount ${if (photoCount == 1) "Photo" else "Photos"} & $videoCount ${if (videoCount == 1) "Video" else "Videos"}"
            videoCount > 0 && photoCount == 0 -> "$videoCount ${if (videoCount == 1) "Video" else "Videos"}"
            totalCount > 0 -> "$totalCount ${if (totalCount == 1) "item" else "items"}"  // Generic count when breakdown not available
            else -> "Empty"
        }
    }
}
