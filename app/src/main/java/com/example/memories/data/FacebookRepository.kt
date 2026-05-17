package com.example.memories.data

import android.util.Log
import com.example.memories.model.Album
import com.example.memories.model.MediaType
import com.example.memories.model.Photo
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

// Facebook Graph API Response Models
data class FacebookAlbumsResponse(
    val data: List<FacebookAlbum>?,
    val paging: Paging?
)

data class FacebookAlbum(
    val id: String,
    val name: String,
    @SerializedName("cover_photo") val coverPhoto: CoverPhoto?,
    val count: Int?,
    @SerializedName("photo_count") val photoCount: Int?,
    @SerializedName("video_count") val videoCount: Int?,
    val photos: PhotosSummary?,
    val videos: VideosSummary?
)

data class PhotosSummary(
    val summary: Summary?
)

data class VideosSummary(
    val summary: Summary?
)

data class Summary(
    val total_count: Int?
)

data class CoverPhoto(
    val id: String
)

data class FacebookPhotosResponse(
    val data: List<FacebookPhoto>?,
    val paging: Paging?
)

data class FacebookPhoto(
    val id: String,
    val images: List<PhotoImage>?,
    val picture: String?,  // Thumbnail URL
    @SerializedName("media_type") val mediaType: String?  // Can be "photo" or "video"
)

data class FacebookVideosResponse(
    val data: List<FacebookVideo>?,
    val paging: Paging?
)

data class FacebookVideo(
    val id: String,
    val source: String?,  // Video URL
    val picture: String?,  // Thumbnail URL
    val format: List<VideoFormat>?,  // Different quality formats
    val album: VideoAlbum?  // Album this video belongs to
)

data class VideoAlbum(
    val id: String,
    val name: String?
)

data class VideoFormat(
    val embed_html: String?,
    val filter: String?,
    val height: Int?,
    val width: Int?,
    val picture: String?,
    @SerializedName("url") val url: String?  // This might be the actual playback URL
)

data class PhotoImage(
    val height: Int,
    val width: Int,
    val source: String
)

data class Paging(
    val cursors: Cursors?,
    val next: String?
)

data class Cursors(
    val before: String?,
    val after: String?
)

class FacebookRepository(private val accessToken: String) {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    private val graphApiBaseUrl = "https://graph.facebook.com/v18.0"
    
    companion object {
        private const val TAG = "FacebookRepository"
    }
    
    suspend fun getAlbums(): Result<List<Album>> = withContext(Dispatchers.IO) {
        try {
            val allAlbums = mutableListOf<Album>()
            var nextUrl: String? = "$graphApiBaseUrl/me/albums?" +
                    "fields=id,name,cover_photo,count,type,photo_count,video_count&" +
                    "limit=100&" +
                    "access_token=$accessToken"
            
            while (nextUrl != null) {
                val request = Request.Builder()
                    .url(nextUrl)
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Failed to fetch albums: ${response.code}")
                    )
                }
                
                val body = response.body?.string() ?: return@withContext Result.failure(
                    IOException("Empty response body")
                )
                
                val fbResponse = gson.fromJson(body, FacebookAlbumsResponse::class.java)
                
                val albums = fbResponse.data?.map { fbAlbum ->
                    Log.d(TAG, "Album: ${fbAlbum.name}, ID: ${fbAlbum.id}, count: ${fbAlbum.count}, photoCount: ${fbAlbum.photoCount}, videoCount: ${fbAlbum.videoCount}")
                    // Use accurate photo_count and video_count from Facebook API
                    Album(
                        id = fbAlbum.id,
                        name = fbAlbum.name,
                        coverUrl = getCoverPhotoUrl(fbAlbum.coverPhoto?.id),
                        photoCount = fbAlbum.photoCount ?: 0,
                        videoCount = fbAlbum.videoCount ?: 0
                    )
                } ?: emptyList()
                
                allAlbums.addAll(albums)
                nextUrl = fbResponse.paging?.next
            }
            
            Result.success(allAlbums)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPhotos(albumId: String): Result<List<Photo>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== getPhotos called for album: $albumId ===")
            val allMedia = mutableListOf<Photo>()
            
            Log.d(TAG, "Starting to fetch photos for album $albumId")
            
            // Fetch all photos from the album
            var nextUrl: String? = "$graphApiBaseUrl/$albumId/photos?" +
                    "fields=id,images,picture,media_type&" +
                    "limit=100&" +
                    "access_token=$accessToken"
            
            while (nextUrl != null) {
                val request = Request.Builder()
                    .url(nextUrl)
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to fetch photos: ${response.code}")
                    return@withContext Result.failure(
                        IOException("Failed to fetch photos: ${response.code}")
                    )
                }
                
                val body = response.body?.string() ?: return@withContext Result.failure(
                    IOException("Empty response body")
                )
                
                Log.d(TAG, "Photos response received, length: ${body.length}")
                
                val fbResponse = gson.fromJson(body, FacebookPhotosResponse::class.java)
                
                val photos = fbResponse.data?.mapNotNull { fbPhoto ->
                    // Check if this is a video based on media_type
                    if (fbPhoto.mediaType == "video") {
                        // For videos, we need to fetch the source URL separately
                        Log.d(TAG, "Found video in photos endpoint: ${fbPhoto.id}, fetching source...")
                        try {
                            val videoRequest = Request.Builder()
                                .url("$graphApiBaseUrl/${fbPhoto.id}?fields=source,picture&access_token=$accessToken")
                                .build()
                            
                            val videoResponse = client.newCall(videoRequest).execute()
                            if (videoResponse.isSuccessful) {
                                val videoBody = videoResponse.body?.string()
                                val videoData = videoBody?.let { gson.fromJson(it, FacebookVideo::class.java) }
                                Log.d(TAG, "Video ${fbPhoto.id} source: ${videoData?.source}")
                                
                                videoData?.source?.let { source ->
                                    Photo(
                                        id = fbPhoto.id,
                                        url = source,
                                        albumId = albumId,
                                        mediaType = MediaType.VIDEO,
                                        thumbnailUrl = videoData.picture
                                    )
                                }
                            } else {
                                Log.e(TAG, "Failed to fetch video ${fbPhoto.id}: ${videoResponse.code}")
                                null
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching video ${fbPhoto.id}: ${e.message}", e)
                            null
                        }
                    } else {
                        // Handle regular photos
                        val largestImage = fbPhoto.images?.maxByOrNull { it.width * it.height }
                        largestImage?.let {
                            Log.d(TAG, "Found photo: ${fbPhoto.id}")
                            Photo(
                                id = fbPhoto.id,
                                url = it.source,
                                albumId = albumId,
                                mediaType = MediaType.PHOTO,
                                thumbnailUrl = null
                            )
                        }
                    }
                } ?: emptyList()
                
                Log.d(TAG, "Fetched ${photos.size} media items in this batch")
                allMedia.addAll(photos)
                nextUrl = fbResponse.paging?.next
            }
            
            // NOTE: Facebook Graph API Limitation
            // Videos in albums are NOT accessible via any Graph API endpoint:
            // - /{album-id}/photos only returns photos (not videos)
            // - /{album-id}/videos does not exist (error 100)
            // - /me/videos only returns standalone videos (not album videos)
            // The album metadata correctly reports video_count, but there's no way to fetch them.
            // This is a known limitation of the Facebook Graph API as of v18.0
            Log.d(TAG, "=== Total media fetched: ${allMedia.size} photos (videos not accessible via API) ===")
            
            Result.success(allMedia)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAlbum(albumId: String): Result<Album?> = withContext(Dispatchers.IO) {
        try {
            val url = "$graphApiBaseUrl/$albumId?" +
                    "fields=id,name,cover_photo,count&" +
                    "access_token=$accessToken"
            
            val request = Request.Builder()
                .url(url)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Failed to fetch album: ${response.code}")
                )
            }
            
            val body = response.body?.string() ?: return@withContext Result.failure(
                IOException("Empty response body")
            )
            
            val fbAlbum = gson.fromJson(body, FacebookAlbum::class.java)
            
            val album = Album(
                id = fbAlbum.id,
                name = fbAlbum.name,
                coverUrl = getCoverPhotoUrl(fbAlbum.coverPhoto?.id),
                photoCount = fbAlbum.count ?: 0,
                videoCount = 0  // Will be updated when media is loaded
            )
            
            Result.success(album)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getCoverPhotoUrl(photoId: String?): String {
        return if (photoId != null) {
            "$graphApiBaseUrl/$photoId/picture?access_token=$accessToken"
        } else {
            "https://via.placeholder.com/800x600?text=No+Cover"
        }
    }
}
