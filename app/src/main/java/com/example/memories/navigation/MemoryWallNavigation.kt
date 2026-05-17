package com.example.memories.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.memories.ui.screen.AlbumsScreen
import com.example.memories.ui.screen.HomeScreen
import com.example.memories.ui.screen.PhotosScreen
import com.example.memories.ui.screen.SlideshowScreen

@Composable
fun MemoryWallNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAlbums = {
                    navController.navigate(Screen.Albums.route)
                }
            )
        }
        
        composable(Screen.Albums.route) {
            AlbumsScreen(
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.Photos.createRoute(albumId))
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.Photos.route,
            arguments = listOf(
                navArgument("albumId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
            PhotosScreen(
                albumId = albumId,
                onPhotoClick = { photoIndex ->
                    navController.navigate(Screen.Slideshow.createRoute(albumId, photoIndex))
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.Slideshow.route,
            arguments = listOf(
                navArgument("albumId") { type = NavType.StringType },
                navArgument("photoIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
            val photoIndex = backStackEntry.arguments?.getInt("photoIndex") ?: 0
            SlideshowScreen(
                albumId = albumId,
                startIndex = photoIndex,
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
