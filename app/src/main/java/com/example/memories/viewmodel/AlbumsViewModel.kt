package com.example.memories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memories.data.FacebookRepository
import com.example.memories.model.Album
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlbumsViewModel : ViewModel() {
    
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var facebookRepository: FacebookRepository? = null
    
    init {
        // Start with empty state - only load when authenticated
    }
    
    fun setFacebookRepository(repository: FacebookRepository) {
        facebookRepository = repository
        loadAlbums()
    }
    
    private fun loadAlbums() {
        _isLoading.value = true
        _error.value = null
        
        // Only load from Facebook
        viewModelScope.launch {
            facebookRepository?.getAlbums()?.fold(
                onSuccess = { albums ->
                    _albums.value = albums
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load albums"
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun refreshAlbums() {
        loadAlbums()
    }
}
