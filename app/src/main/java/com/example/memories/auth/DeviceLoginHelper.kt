package com.example.memories.auth

import android.content.Context
import com.example.memories.R
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class DeviceLoginCode(
    val code: String,
    @SerializedName("user_code") val userCode: String,
    @SerializedName("verification_uri") val verificationUri: String,
    @SerializedName("expires_in") val expiresIn: Int,
    val interval: Int
)

data class DeviceLoginStatus(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("error") val error: ErrorInfo?
)

data class ErrorInfo(
    val message: String?,
    val code: Int?
)

class DeviceLoginHelper(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    private fun getAppId(): String {
        return context.getString(R.string.facebook_app_id)
    }
    
    private fun getClientToken(): String {
        return context.getString(R.string.facebook_client_token)
    }
    
    suspend fun requestDeviceCode(): Result<DeviceLoginCode> = withContext(Dispatchers.IO) {
        try {
            val appId = getAppId()
            val clientToken = getClientToken()
            
            val url = "https://graph.facebook.com/v18.0/device/login?" +
                    "access_token=$appId|$clientToken&" +
                    "scope=user_photos,user_videos,public_profile"
            
            val requestBody = FormBody.Builder().build()
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(
                Exception("Empty response from Facebook")
            )
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Facebook API error: ${response.code} - $body")
                )
            }
            
            val deviceCode = gson.fromJson(body, DeviceLoginCode::class.java)
            Result.success(deviceCode)
            
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get device code: ${e.message}", e))
        }
    }
    
    suspend fun checkLoginStatus(code: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val appId = getAppId()
            val clientToken = getClientToken()
            
            val url = "https://graph.facebook.com/v18.0/device/login_status?" +
                    "access_token=$appId|$clientToken&" +
                    "code=$code"
            
            val request = Request.Builder()
                .url(url)
                .post(FormBody.Builder().build())
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(
                Exception("Empty response")
            )
            
            val status = gson.fromJson(body, DeviceLoginStatus::class.java)
            
            when {
                status.accessToken != null -> {
                    // Success! Got the access token
                    Result.success(status.accessToken)
                }
                status.error?.message?.contains("pending", ignoreCase = true) == true -> {
                    // User hasn't authorized yet, return null to continue polling
                    Result.success(null)
                }
                status.error != null -> {
                    // Real error
                    Result.failure(Exception(status.error.message ?: "Unknown error"))
                }
                else -> {
                    Result.success(null) // Continue polling
                }
            }
            
        } catch (e: Exception) {
            Result.failure(Exception("Failed to check login status: ${e.message}", e))
        }
    }
    
    suspend fun pollForAccessToken(
        code: String,
        interval: Int,
        expiresIn: Int,
        onProgress: (Int) -> Unit = {}
    ): Result<String> {
        val endTime = System.currentTimeMillis() + (expiresIn * 1000)
        
        while (System.currentTimeMillis() < endTime) {
            val remainingSeconds = ((endTime - System.currentTimeMillis()) / 1000).toInt()
            onProgress(remainingSeconds)
            
            val result = checkLoginStatus(code)
            
            result.fold(
                onSuccess = { accessToken ->
                    if (accessToken != null) {
                        return Result.success(accessToken)
                    }
                    // null means pending, continue polling
                },
                onFailure = { exception ->
                    return Result.failure(exception)
                }
            )
            
            // Wait for the specified interval before next check
            delay((interval * 1000).toLong())
        }
        
        return Result.failure(Exception("Login timeout - code expired"))
    }
}
