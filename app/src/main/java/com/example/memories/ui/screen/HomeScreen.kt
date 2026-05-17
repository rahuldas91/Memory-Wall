package com.example.memories.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.example.memories.auth.AuthState
import com.example.memories.viewmodel.AuthViewModel
import com.example.memories.R

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAlbums: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel.getInstance(context) }
    val authState by authViewModel.authState.collectAsState()
    
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Title and buttons
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title section
                Column {
                    Text(
                        text = "MemoryWall",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Your Photo Albums on TV",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
                
                // Buttons section
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    ViewAlbumsButton(
                        onClick = onNavigateToAlbums
                    )
                    
                    when (authState) {
                        is AuthState.Authenticated -> {
                            FacebookDisconnectButton(
                                onClick = {
                                    authViewModel.logout()
                                }
                            )
                        }
                        is AuthState.DeviceLoginPending -> {
                            MenuButton(
                                text = "Cancel Login",
                                onClick = {
                                    authViewModel.logout()
                                }
                            )
                        }
                        else -> {
                            FacebookLoginButton(
                                onClick = {
                                    authViewModel.startDeviceLogin()
                                }
                            )
                        }
                    }
                }
            }
            
            // Right side: Device code and instructions (when pending)
            if (authState is AuthState.DeviceLoginPending) {
                val state = authState as AuthState.DeviceLoginPending
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Facebook Device Login",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Text(
                        text = "Steps to connect:",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(
                        modifier = Modifier.padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "1. Visit facebook.com/device on your phone",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "2. Enter the code shown below",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "3. Approve the login request",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Card(
                        onClick = { /* Non-interactive */ },
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Your Code:",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = state.userCode,
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Text(
                        text = "Waiting for authorization...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (authState is AuthState.Error) {
                val state = authState as AuthState.Error
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Timeout/Error Icon
                    Image(
                        painter = painterResource(id = R.drawable.time_out),
                        contentDescription = "Error Icon",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(bottom = 24.dp)
                    )
                    
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF2d5fad),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF2d5fad)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .width(400.dp)
            .height(80.dp)
            .onFocusChanged { isFocused = it.isFocused },
        scale = ButtonDefaults.scale(
            scale = 1f,
            focusedScale = 1.1f
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FacebookLoginButton(
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(400.dp)
            .height(80.dp)
            .onFocusChanged { isFocused = it.isFocused },
        scale = ButtonDefaults.scale(
            scale = 1f,
            focusedScale = 1.1f
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Facebook Link Icon
            Image(
                painter = painterResource(id = R.drawable.link_fb_account),
                contentDescription = "Facebook Link Icon",
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Button Text
            Text(
                text = "Link Facebook Account",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FacebookDisconnectButton(
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(400.dp)
            .height(80.dp)
            .onFocusChanged { isFocused = it.isFocused },
        scale = ButtonDefaults.scale(
            scale = 1f,
            focusedScale = 1.1f
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Facebook Link Icon (same as connect)
            Image(
                painter = painterResource(id = R.drawable.link_fb_account),
                contentDescription = "Facebook Link Icon",
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Button Text
            Text(
                text = "Disconnect Facebook",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ViewAlbumsButton(
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(400.dp)
            .height(80.dp)
            .onFocusChanged { isFocused = it.isFocused },
        scale = ButtonDefaults.scale(
            scale = 1f,
            focusedScale = 1.1f
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // View Albums Icon
            Image(
                painter = painterResource(id = R.drawable.view_albums),
                contentDescription = "View Albums Icon",
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Button Text
            Text(
                text = "View Albums",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
