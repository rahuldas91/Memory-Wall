package com.example.memories.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memories.auth.AuthState
import com.example.memories.auth.FacebookAuthManager
import com.example.memories.data.FacebookRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel private constructor(context: Context) : ViewModel() {
    
    private val authManager = FacebookAuthManager(context)
    
    val authState: StateFlow<AuthState> = authManager.authState
    
    companion object {
        @Volatile
        private var instance: AuthViewModel? = null
        
        fun getInstance(context: Context): AuthViewModel {
            return instance ?: synchronized(this) {
                instance ?: AuthViewModel(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Start device login flow (recommended for TV)
     */
    fun startDeviceLogin() {
        viewModelScope.launch {
            val result = authManager.startDeviceLogin()
            
            result.fold(
                onSuccess = { deviceCode ->
                    // Start polling for access token in background
                    viewModelScope.launch {
                        authManager.pollForAccessToken(
                            code = deviceCode.code,
                            interval = deviceCode.interval,
                            expiresIn = deviceCode.expiresIn
                        )
                    }
                },
                onFailure = {
                    // Error is already set in auth state
                }
            )
        }
    }
    
    /**
     * Fallback login (may not work well on TV)
     */
    fun login(activity: Activity) {
        authManager.login(activity)
    }
    
    fun logout() {
        authManager.logout()
    }
    
    fun getFacebookRepository(): FacebookRepository? {
        val accessToken = authManager.getAccessToken()
        return if (accessToken != null) {
            FacebookRepository(accessToken)
        } else {
            null
        }
    }
    
    fun isAuthenticated(): Boolean {
        return authManager.isAuthenticated()
    }
}
