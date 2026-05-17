import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(private val repo: MockDataRepository = MockDataRepository()) : ViewModel() {
    private val _albums = MutableStateFlow(repo.getAlbums())
    val albums = _albums.asStateFlow()

    private val _photos = MutableStateFlow<List<Photo>>(java.util.Collections.emptyList())
    val photos = _photos.asStateFlow()

    fun loadPhotos(albumId: String) {
        _photos.value = repo.getPhotos(albumId)
    }
}