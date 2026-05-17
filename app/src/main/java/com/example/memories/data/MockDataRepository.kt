package com.example.memories.data

import com.example.memories.model.Album
import com.example.memories.model.Photo

class MockDataRepository {
    
    private val albums = listOf(
        Album(
            id = "1",
            name = "Summer Vacation 2025",
            coverUrl = "https://picsum.photos/seed/album1/800/600",
            photoCount = 15,
            videoCount = 3
        ),
        Album(
            id = "2",
            name = "Family Gatherings",
            coverUrl = "https://picsum.photos/seed/album2/800/600",
            photoCount = 22,
            videoCount = 1
        ),
        Album(
            id = "3",
            name = "Birthday Celebrations",
            coverUrl = "https://picsum.photos/seed/album3/800/600",
            photoCount = 18,
            videoCount = 2
        ),
        Album(
            id = "4",
            name = "Holiday Memories",
            coverUrl = "https://picsum.photos/seed/album4/800/600",
            photoCount = 30,
            videoCount = 5
        ),
        Album(
            id = "5",
            name = "Weekend Adventures",
            coverUrl = "https://picsum.photos/seed/album5/800/600",
            photoCount = 12,
            videoCount = 0
        ),
        Album(
            id = "6",
            name = "Nature Photography",
            coverUrl = "https://picsum.photos/seed/album6/800/600",
            photoCount = 25,
            videoCount = 4
        ),
        Album(
            id = "7",
            name = "City Lights",
            coverUrl = "https://picsum.photos/seed/album7/800/600",
            photoCount = 20,
            videoCount = 2
        ),
        Album(
            id = "8",
            name = "Special Moments",
            coverUrl = "https://picsum.photos/seed/album8/800/600",
            photoCount = 16,
            videoCount = 1
        )
    )
    
    private val photosMap = albums.associate { album ->
        album.id to List(album.photoCount) { index ->
            Photo(
                id = "${album.id}_$index",
                url = "https://picsum.photos/seed/${album.id}photo$index/1920/1080",
                albumId = album.id
            )
        }
    }
    
    fun getAlbums(): List<Album> = albums
    
    fun getPhotos(albumId: String): List<Photo> = photosMap[albumId] ?: emptyList()
    
    fun getAlbum(albumId: String): Album? = albums.find { it.id == albumId }
}
