package com.example.memories.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.memories.auth.AuthState
import com.example.memories.model.MediaType
import com.example.memories.ui.components.VideoPlayer
import com.example.memories.viewmodel.AuthViewModel
import com.example.memories.viewmodel.SlideshowViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SlideshowScreen(
    albumId: String,
    startIndex: Int,
    onBackPressed: () -> Unit,
    viewModel: SlideshowViewModel = viewModel()
) {
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel.getInstance(context) }
    val authState by authViewModel.authState.collectAsState()
    
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(albumId, authState) {
        try {
            val photos = when (authState) {
                is AuthState.Authenticated -> {
                    val fbRepo = authViewModel.getFacebookRepository()
                    fbRepo?.getPhotos(albumId)?.getOrNull() ?: emptyList()
                }
                else -> emptyList()
            }
            viewModel.setPhotos(photos, startIndex)
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = "Failed to load media: ${e.message}"
            viewModel.setPhotos(emptyList(), startIndex)
        }
    }
    
    var showControls by remember { mutableStateOf(true) }  // Start with controls visible
    var controlsFocused by remember { mutableStateOf(false) }
    
    val focusRequester = remember { FocusRequester() }
    
    // Request focus when the screen starts
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // Auto-hide controls after 5 seconds when shown but not focused
    LaunchedEffect(showControls, controlsFocused) {
        if (showControls && !controlsFocused) {
            kotlinx.coroutines.delay(5000)
            showControls = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp) {
                    when (keyEvent.key) {
                        Key.DirectionCenter -> {
                            // D-pad center: Toggle play/pause and show controls
                            viewModel.togglePlayPause()
                            showControls = true
                            controlsFocused = false
                            true
                        }
                        Key.DirectionUp -> {
                            // UP arrow: Show controls (unfocused)
                            if (!showControls) {
                                showControls = true
                                controlsFocused = false
                            }
                            true
                        }
                        Key.DirectionDown -> {
                            // DOWN arrow: Focus controls if showing, hide if focused
                            if (showControls && !controlsFocused) {
                                controlsFocused = true
                            } else if (controlsFocused) {
                                controlsFocused = false
                                showControls = false
                            }
                            true
                        }
                        Key.DirectionRight -> {
                            // RIGHT: Always go to next photo
                            if (!controlsFocused) {
                                viewModel.nextPhoto()
                                showControls = true
                                controlsFocused = false
                                true
                            } else {
                                false
                            }
                        }
                        Key.DirectionLeft -> {
                            // LEFT: Always go to previous photo
                            if (!controlsFocused) {
                                viewModel.previousPhoto()
                                showControls = true
                                controlsFocused = false
                                true
                            } else {
                                false
                            }
                        }
                        Key.Enter, Key.Spacebar -> {
                            // Pause button: Toggle play/pause and show controls
                            if (!controlsFocused) {
                                viewModel.togglePlayPause()
                                showControls = true
                                controlsFocused = false
                                true
                            } else {
                                false
                            }
                        }
                        Key.Back -> {
                            // Back button: Hide controls if visible, otherwise exit
                            if (showControls) {
                                showControls = false
                                controlsFocused = false
                                true
                            } else {
                                onBackPressed()
                                true
                            }
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        // Current Photo with transition
        if (state.photos.isNotEmpty() && state.currentIndex < state.photos.size) {
            val currentPhoto = state.photos[state.currentIndex]
            
            // Preload next photo
            val nextIndex = if (state.isShuffleMode) {
                (state.currentIndex + 1) % state.photos.size
            } else {
                (state.currentIndex + 1) % state.photos.size
            }
            val nextPhoto = state.photos.getOrNull(nextIndex)
            
            Box(modifier = Modifier.fillMaxSize()) {
                // Display video or photo based on media type
                when (currentPhoto.mediaType) {
                    MediaType.VIDEO -> {
                        VideoPlayer(
                            videoUrl = currentPhoto.url,
                            modifier = Modifier.fillMaxSize(),
                            isPlaying = state.isPlaying,
                            onVideoEnd = {
                                // Move to next media when video ends
                                viewModel.nextPhoto()
                            }
                        )
                    }
                    MediaType.PHOTO -> {
                        AsyncImage(
                            model = currentPhoto.url,
                            contentDescription = "Photo ${state.currentIndex + 1} of ${state.photos.size}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            onError = { 
                                errorMessage = "Failed to load image"
                            }
                        )
                    }
                }
                
                // Preload next image (invisible) - only if it's a photo and not a video
                if (nextPhoto != null && nextPhoto.mediaType == MediaType.PHOTO) {
                    AsyncImage(
                        model = nextPhoto.url,
                        contentDescription = null,
                        modifier = Modifier.size(1.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        } else if (state.photos.isEmpty()) {
            // Show loading or empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading media...", color = Color.White)
            }
        }
        
        // Controls overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(tween(1000)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SlideshowControls(
                currentIndex = state.currentIndex + 1,
                totalPhotos = state.photos.size,
                isPlaying = state.isPlaying,
                isShuffleMode = state.isShuffleMode,
                intervalSeconds = state.intervalSeconds,
                isFocused = controlsFocused,
                onPlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.nextPhoto() },
                onPrevious = { viewModel.previousPhoto() },
                onToggleShuffle = { viewModel.toggleShuffleMode() },
                onIntervalChange = { viewModel.setInterval(it) },
                onBack = onBackPressed
            )
        }
        
        // Error message overlay
        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.Red.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Text(error, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SlideshowControls(
    currentIndex: Int,
    totalPhotos: Int,
    isPlaying: Boolean,
    isShuffleMode: Boolean,
    intervalSeconds: Int,
    isFocused: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onIntervalChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .background(
                if (isFocused) 
                    Color.Black.copy(alpha = 0.85f) 
                else 
                    Color.Black.copy(alpha = 0.5f)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Photo counter
            Text(
                text = "$currentIndex / $totalPhotos",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(
                    text = if (isPlaying) "Pause" else "Play",
                    onClick = onPlayPause,
                    enabled = isFocused
                )
                
                ControlButton(
                    text = "Previous",
                    onClick = onPrevious,
                    enabled = isFocused
                )
                
                ControlButton(
                    text = "Next",
                    onClick = onNext,
                    enabled = isFocused
                )
                
                ControlButton(
                    text = if (isShuffleMode) "Sequential" else "Shuffle",
                    onClick = onToggleShuffle,
                    enabled = isFocused
                )
                
                // Interval selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${intervalSeconds}s",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SmallControlButton(
                            text = "+",
                            onClick = { onIntervalChange(intervalSeconds + 1) },
                            enabled = isFocused
                        )
                        SmallControlButton(
                            text = "-",
                            onClick = { onIntervalChange(intervalSeconds - 1) },
                            enabled = isFocused
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                ControlButton(
                    text = "Exit",
                    onClick = onBack,
                    enabled = isFocused
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ControlButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        enabled = enabled,
        scale = ButtonDefaults.scale(
            scale = 1f,
            focusedScale = 1.1f
        )
    ) {
        Text(text)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SmallControlButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(32.dp),
        contentPadding = PaddingValues(0.dp),
        enabled = enabled,
        scale = ButtonDefaults.scale(
            scale = 1f,
            focusedScale = 1.1f
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}
