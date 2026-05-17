package com.example.memories.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.itemsIndexed
import androidx.tv.material3.*
import com.example.memories.R
import coil.compose.AsyncImage
import com.example.memories.auth.AuthState
import com.example.memories.model.MediaType
import com.example.memories.model.Photo
import com.example.memories.viewmodel.AuthViewModel
import com.example.memories.viewmodel.PhotosViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PhotosScreen(
    albumId: String,
    onPhotoClick: (Int) -> Unit,
    onBackPressed: () -> Unit,
    viewModel: PhotosViewModel = viewModel()
) {
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel.getInstance(context) }
    val authState by authViewModel.authState.collectAsState()
    
    // Set repository based on auth state
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                authViewModel.getFacebookRepository()?.let { repo ->
                    viewModel.setFacebookRepository(repo)
                }
            }
            else -> {
                // Use mock data (already default)
            }
        }
    }
    
    val photos by viewModel.photos.collectAsState()
    val currentAlbum by viewModel.currentAlbum.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(albumId) {
        viewModel.loadPhotos(albumId)
    }
    
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentAlbum?.name ?: "Album",
                        style = MaterialTheme.typography.displaySmall
                    )
                    // Count photos and videos separately
                    val photoCount = photos.count { it.mediaType == MediaType.PHOTO }
                    val videoCount = photos.count { it.mediaType == MediaType.VIDEO }
                    
                    val countText = when {
                        photoCount > 0 && videoCount > 0 -> "$photoCount ${if (photoCount == 1) "Photo" else "Photos"} & $videoCount ${if (videoCount == 1) "Video" else "Videos"}"
                        photoCount > 0 -> "$photoCount ${if (photoCount == 1) "Photo" else "Photos"}"
                        videoCount > 0 -> "$videoCount ${if (videoCount == 1) "Video" else "Videos"}"
                        else -> "No items"
                    }
                    
                    Text(
                        text = countText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (photos.isNotEmpty()) {
                    Button(
                        onClick = { onPhotoClick(0) },
                        scale = ButtonDefaults.scale(
                            scale = 1f,
                            focusedScale = 1.1f
                        )
                    ) {
                        Text("Start Slideshow")
                    }
                }
            }
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading photos...")
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Error: $error",
                                color = androidx.compose.ui.graphics.Color(0xFF2d5fad)
                            )
                            Button(onClick = { viewModel.loadPhotos(albumId) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                photos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.album_not_found),
                                contentDescription = "No Photos",
                                modifier = Modifier.size(120.dp)
                            )
                            Text(
                                text = "No media in this album",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF2d5fad)
                            )
                        }
                    }
                }
                else -> {
                    TvLazyVerticalGrid(
                        columns = TvGridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 48.dp)
                    ) {
                        itemsIndexed(photos) { index, photo ->
                            PhotoCard(
                                photo = photo,
                                onClick = { onPhotoClick(index) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PhotoCard(
    photo: Photo,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1.5f)
            .onFocusChanged { isFocused = it.isFocused },
        scale = CardDefaults.scale(
            scale = 1f,
            focusedScale = 1.08f
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Show thumbnail for videos, actual image for photos
            val displayUrl = when (photo.mediaType) {
                MediaType.VIDEO -> photo.thumbnailUrl ?: photo.url
                MediaType.PHOTO -> photo.url
            }
            
            AsyncImage(
                model = displayUrl,
                contentDescription = if (photo.mediaType == MediaType.VIDEO) "Video ${photo.id}" else "Photo ${photo.id}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Video icon overlay
            if (photo.mediaType == MediaType.VIDEO) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
