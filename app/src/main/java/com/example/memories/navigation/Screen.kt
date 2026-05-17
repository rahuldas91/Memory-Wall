package com.example.memories.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Albums : Screen("albums")
    object Photos : Screen("photos/{albumId}") {
        fun createRoute(albumId: String) = "photos/$albumId"
    }
    object Slideshow : Screen("slideshow/{albumId}/{photoIndex}") {
        fun createRoute(albumId: String, photoIndex: Int = 0) = "slideshow/$albumId/$photoIndex"
    }
}
