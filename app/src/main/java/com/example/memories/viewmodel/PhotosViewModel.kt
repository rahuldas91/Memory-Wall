package com.example.memories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memories.data.FacebookRepository
import com.example.memories.model.Album
import com.example.memories.model.Photo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhotosViewModel : ViewModel() {
    
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()
    
    private val _currentAlbum = MutableStateFlow<Album?>(null)
    val currentAlbum: StateFlow<Album?> = _currentAlbum.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var facebookRepository: FacebookRepository? = null
    
    fun setFacebookRepository(repository: FacebookRepository) {
        facebookRepository = repository
    }
    
    fun loadPhotos(albumId: String) {
        _isLoading.value = true
        _error.value = null
        
        // Only load from Facebook
        viewModelScope.launch {
            // Load album info
            facebookRepository?.getAlbum(albumId)?.fold(
                onSuccess = { album ->
                    _currentAlbum.value = album
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load album"
                }
            )
            
            // Load photos
            facebookRepository?.getPhotos(albumId)?.fold(
                onSuccess = { photos ->
                    _photos.value = photos
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load photos"
                    _isLoading.value = false
                }
            )
        }
    }
}
