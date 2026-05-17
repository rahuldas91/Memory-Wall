package com.example.memories.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.memories.R
import com.example.memories.auth.AuthState
import com.example.memories.model.Album
import com.example.memories.viewmodel.AlbumsViewModel
import com.example.memories.viewmodel.AuthViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AlbumsScreen(
    onAlbumClick: (String) -> Unit,
    onBackPressed: () -> Unit,
    viewModel: AlbumsViewModel = viewModel()
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
                // No albums when not authenticated
            }
        }
    }
    
    val albums by viewModel.albums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
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
                        text = "Your Albums",
                        style = MaterialTheme.typography.displaySmall
                    )
                    if (authState is AuthState.Authenticated) {
                        Text(
                            text = "From your Facebook account",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "Demo albums",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Loading albums...")
                        }
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
                            Button(onClick = { viewModel.refreshAlbums() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                albums.isEmpty() -> {
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
                                contentDescription = "No Albums",
                                modifier = Modifier.size(120.dp)
                            )
                            Text(
                                text = "No albums found",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF2d5fad)
                            )
                        }
                    }
                }
                else -> {
                    TvLazyVerticalGrid(
                        columns = TvGridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(bottom = 48.dp)
                    ) {
                        items(albums) { album ->
                            AlbumCard(
                                album = album,
                                onClick = { onAlbumClick(album.id) }
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
private fun AlbumCard(
    album: Album,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1.33f)
            .onFocusChanged { isFocused = it.isFocused },
        scale = CardDefaults.scale(
            scale = 1f,
            focusedScale = 1.05f
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
            )
        )
    ) {
        Box {
            AsyncImage(
                model = album.coverUrl,
                contentDescription = album.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = album.getCountText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
