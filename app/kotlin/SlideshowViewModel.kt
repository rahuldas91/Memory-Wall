import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*

class SlideshowViewModel : ViewModel() {
    private val _currentIndex = kotlinx.coroutines.flow.MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _isPlaying = kotlinx.coroutines.flow.MutableStateFlow(true)
    val isPlaying = _isPlaying.asStateFlow()

    private var photoList: List<Photo> = java.util.Collections.emptyList()

    fun setPhotos(photos: List<Photo>, startAt: Int) {
        photoList = photos
        _currentIndex.value = startAt
    }

    fun next() {
        if (photoList.isNotEmpty()) {
            _currentIndex.value = (_currentIndex.value + 1) % photoList.size
        }
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
    }
}