class MockDataRepository {
    fun getAlbums(): List<Album> = List(10) { i ->
        Album("$i", "Album ${i + 1}", "https://picsum.photos/seed/alb$i/400/300")
    }

    fun getPhotos(albumId: String): List<Photo> = List(20) { i ->
        Photo("$i", "https://picsum.photos/seed/img$albumId$i/1920/1080")
    }
}