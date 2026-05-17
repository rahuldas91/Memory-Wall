package com.example.memories.auth

import android.app.Activity
import android.content.Context
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

sealed class AuthState {
    object NotAuthenticated : AuthState()
    object Loading : AuthState()
    data class DeviceLoginPending(
        val userCode: String,
        val verificationUri: String,
        val expiresIn: Int
    ) : AuthState()
    data class Authenticated(val accessToken: String, val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class FacebookAuthManager(private val context: Context) {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    val callbackManager: CallbackManager = CallbackManager.Factory.create()
    
    private val loginManager = LoginManager.getInstance()
    private val deviceLoginHelper = DeviceLoginHelper(context)
    
    init {
        // Check if user is already logged in
        val currentToken = AccessToken.getCurrentAccessToken()
        if (currentToken != null && !currentToken.isExpired) {
            _authState.value = AuthState.Authenticated(
                accessToken = currentToken.token,
                userId = currentToken.userId
            )
        }
        
        // Register callback for fallback browser login
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val token = result.accessToken
                _authState.value = AuthState.Authenticated(
                    accessToken = token.token,
                    userId = token.userId
                )
            }
            
            override fun onCancel() {
                _authState.value = AuthState.NotAuthenticated
            }
            
            override fun onError(error: FacebookException) {
                _authState.value = AuthState.Error(error.message ?: "Login failed")
            }
        })
    }
    
    /**
     * Start device login flow (recommended for TV)
     * Returns DeviceLoginCode on success which contains the code to display to user
     */
    suspend fun startDeviceLogin(): Result<DeviceLoginCode> {
        _authState.value = AuthState.Loading
        
        val result = deviceLoginHelper.requestDeviceCode()
        
        result.fold(
            onSuccess = { deviceCode ->
                _authState.value = AuthState.DeviceLoginPending(
                    userCode = deviceCode.userCode,
                    verificationUri = deviceCode.verificationUri,
                    expiresIn = deviceCode.expiresIn
                )
            },
            onFailure = { exception ->
                _authState.value = AuthState.Error(
                    exception.message ?: "Failed to start device login"
                )
            }
        )
        
        return result
    }
    
    /**
     * Poll for access token after user has authorized the device
     * This should be called after startDeviceLogin() succeeds
     */
    suspend fun pollForAccessToken(
        code: String,
        interval: Int,
        expiresIn: Int,
        onProgress: (Int) -> Unit = {}
    ): Result<String> {
        val result = deviceLoginHelper.pollForAccessToken(code, interval, expiresIn, onProgress)
        
        result.fold(
            onSuccess = { tokenString ->
                // Create and set Facebook AccessToken to persist the login
                val appId = context.getString(com.example.memories.R.string.facebook_app_id)
                val accessToken = AccessToken(
                    tokenString,
                    appId,
                    "device_login_user", // userId - we can fetch real one later
                    listOf("user_photos", "user_videos", "public_profile"),
                    null, // declined permissions
                    null, // expired permissions
                    null, // AccessTokenSource
                    Date(System.currentTimeMillis() + 60L * 24 * 60 * 60 * 1000), // expires in 60 days
                    Date(), // last refresh
                    Date() // data access expiration
                )
                AccessToken.setCurrentAccessToken(accessToken)
                
                _authState.value = AuthState.Authenticated(
                    accessToken = tokenString,
                    userId = "device_login_user"
                )
            },
            onFailure = { exception ->
                _authState.value = AuthState.Error(
                    exception.message ?: "Failed to complete login"
                )
            }
        )
        
        return result
    }
    
    /**
     * Fallback browser-based login (may not work well on TV)
     */
    fun login(activity: Activity) {
        _authState.value = AuthState.Loading
        loginManager.logIn(
            activity,
            listOf("user_photos", "user_videos")
        )
    }
    
    fun logout() {
        loginManager.logOut()
        _authState.value = AuthState.NotAuthenticated
    }
    
    fun isAuthenticated(): Boolean {
        return _authState.value is AuthState.Authenticated
    }
    
    fun getAccessToken(): String? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.accessToken
            else -> null
        }
    }
}
