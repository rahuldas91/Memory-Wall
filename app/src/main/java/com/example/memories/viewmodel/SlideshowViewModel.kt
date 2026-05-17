package com.example.memories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memories.model.Photo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SlideshowState(
    val photos: List<Photo> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = true,
    val isShuffleMode: Boolean = false,
    val intervalSeconds: Int = 5
)

class SlideshowViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(SlideshowState())
    val state: StateFlow<SlideshowState> = _state.asStateFlow()
    
    private var slideshowJob: Job? = null
    private var shuffledIndices: List<Int> = emptyList()
    
    fun setPhotos(photos: List<Photo>, startIndex: Int = 0) {
        _state.value = _state.value.copy(
            photos = photos,
            currentIndex = startIndex.coerceIn(0, photos.size - 1)
        )
        resetShuffledIndices()
        if (_state.value.isPlaying) {
            startSlideshow()
        }
    }
    
    fun togglePlayPause() {
        val newIsPlaying = !_state.value.isPlaying
        _state.value = _state.value.copy(isPlaying = newIsPlaying)
        
        if (newIsPlaying) {
            startSlideshow()
        } else {
            stopSlideshow()
        }
    }
    
    fun nextPhoto() {
        val photos = _state.value.photos
        if (photos.isEmpty()) return
        
        val nextIndex = if (_state.value.isShuffleMode) {
            val currentPosition = shuffledIndices.indexOf(_state.value.currentIndex)
            val nextPosition = (currentPosition + 1) % shuffledIndices.size
            shuffledIndices[nextPosition]
        } else {
            (_state.value.currentIndex + 1) % photos.size
        }
        
        _state.value = _state.value.copy(currentIndex = nextIndex)
    }
    
    fun previousPhoto() {
        val photos = _state.value.photos
        if (photos.isEmpty()) return
        
        val prevIndex = if (_state.value.isShuffleMode) {
            val currentPosition = shuffledIndices.indexOf(_state.value.currentIndex)
            val prevPosition = if (currentPosition - 1 < 0) shuffledIndices.size - 1 else currentPosition - 1
            shuffledIndices[prevPosition]
        } else {
            if (_state.value.currentIndex - 1 < 0) photos.size - 1 else _state.value.currentIndex - 1
        }
        
        _state.value = _state.value.copy(currentIndex = prevIndex)
    }
    
    fun toggleShuffleMode() {
        val newShuffleMode = !_state.value.isShuffleMode
        _state.value = _state.value.copy(isShuffleMode = newShuffleMode)
        
        if (newShuffleMode) {
            resetShuffledIndices()
        }
    }
    
    fun setInterval(seconds: Int) {
        _state.value = _state.value.copy(intervalSeconds = seconds.coerceIn(1, 60))
        if (_state.value.isPlaying) {
            startSlideshow()
        }
    }
    
    private fun startSlideshow() {
        stopSlideshow()
        slideshowJob = viewModelScope.launch {
            while (true) {
                delay(_state.value.intervalSeconds * 1000L)
                nextPhoto()
            }
        }
    }
    
    private fun stopSlideshow() {
        slideshowJob?.cancel()
        slideshowJob = null
    }
    
    private fun resetShuffledIndices() {
        shuffledIndices = _state.value.photos.indices.shuffled()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopSlideshow()
    }
}
