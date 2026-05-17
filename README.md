# MemoryWall - Android TV Photo Album Viewer

A production-ready Android TV application for viewing photo albums on smart TVs with a beautiful, TV-optimized interface.

## Overview

MemoryWall is a Kotlin-based Android TV application built with Jetpack Compose for TV that provides a seamless photo viewing experience optimized for large screens and D-pad navigation. This Phase 1 implementation uses mock data and focuses on delivering a polished user experience with all core features.

## Features

### ✨ Core Features

- **Home Screen**: Welcome screen with intuitive menu navigation
- **Albums Grid**: TV-optimized grid layout displaying 8 sample albums
- **Photos Grid**: Browse photos within each album with smooth navigation
- **Advanced Slideshow**:
  - Auto-play with configurable interval (1-60 seconds)
  - Manual navigation (Previous/Next)
  - Pause/Resume functionality
  - Shuffle mode toggle
  - Smooth transitions with image preloading
  - On-screen controls with auto-hide

### 🎯 TV Optimizations

- D-pad navigation fully supported
- Focus indicators with scaling animations
- Large touch targets for easy selection
- Landscape-only orientation
- Smooth transitions and animations
- Image preloading for seamless slideshow experience

## Architecture

### Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose for TV
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **State Management**: StateFlow
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 35 (Android 15)

### Project Structure

```
com.example.memories/
├── data/
│   └── MockDataRepository.kt         # Mock data provider
├── model/
│   ├── Album.kt                      # Album data model
│   └── Photo.kt                      # Photo data model
├── viewmodel/
│   ├── AlbumsViewModel.kt            # Albums screen state
│   ├── PhotosViewModel.kt            # Photos screen state
│   └── SlideshowViewModel.kt         # Slideshow logic & state
├── ui/
│   ├── screen/
│   │   ├── HomeScreen.kt             # Home navigation screen
│   │   ├── AlbumsScreen.kt           # Albums grid screen
│   │   ├── PhotosScreen.kt           # Photos grid screen
│   │   └── SlideshowScreen.kt        # Full-screen slideshow
│   └── theme/
│       ├── Theme.kt                  # App theme definition
│       └── Type.kt                   # Typography system
├── navigation/
│   ├── Screen.kt                     # Navigation routes
│   └── MemoryWallNavigation.kt       # Navigation graph
└── MainActivity.kt                   # App entry point
```

## Facebook Credentials Setup

> **Security Note:** Real Facebook credentials must **never** be committed to source control. The `strings.xml` file contains only placeholders — follow the steps below to configure your local environment.

### 1. Get your credentials from Facebook Developers

1. Go to [https://developers.facebook.com/apps](https://developers.facebook.com/apps) and open your app.
2. Copy the **App ID** from the app dashboard.
3. Go to **Settings → Advanced** and copy the **Client Token**.

### 2. Add credentials to `local.properties`

Open (or create) `local.properties` in the project root — this file is already listed in `.gitignore` and will never be committed.

```properties
facebook_app_id=YOUR_FACEBOOK_APP_ID
facebook_client_token=YOUR_FACEBOOK_CLIENT_TOKEN
```

### 3. Update `strings.xml` locally (do not commit)

Open `app/src/main/res/values/strings.xml` and replace the placeholders with your actual values **only on your local machine**:

```xml
<string name="facebook_app_id">123456789012345</string>
<string name="fb_login_protocol_scheme">fb123456789012345</string>
<string name="facebook_client_token">your_actual_client_token_here</string>
```

> **Tip:** To prevent accidentally staging this file, you can run:
> ```bash
> git update-index --assume-unchanged app/src/main/res/values/strings.xml
> ```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or later
- Android TV emulator or physical Android TV device

### Installation & Running

1. **Open the project in Android Studio**
   ```
   File > Open > Select the Android folder
   ```

2. **Sync Gradle**
   - Android Studio will automatically prompt to sync
   - Or manually: `File > Sync Project with Gradle Files`

3. **Set up Android TV Emulator**
   - Open AVD Manager: `Tools > Device Manager`
   - Click "Create Device"
   - Select "TV" category
   - Choose any TV device (recommended: "Android TV 1080p")
   - Select a system image with API 21 or higher
   - Click "Finish"

4. **Run the app**
   - Select the TV emulator from the device dropdown
   - Click the "Run" button (green triangle) or press `Shift + F10`

### Building APK

```bash
# Debug build
./gradlew assembleDebug

# Release build (unsigned)
./gradlew assembleRelease
```

APK location: `app/build/outputs/apk/`

## Usage Guide

### Navigation Controls

#### Home Screen
- **D-pad Up/Down**: Navigate between menu options
- **Enter/OK**: Select option

#### Albums Screen
- **D-pad**: Navigate between albums
- **Enter/OK**: Open album
- **Back**: Return to home

#### Photos Screen
- **D-pad**: Navigate between photos
- **Enter/OK**: Start slideshow from selected photo
- **Back**: Return to albums

#### Slideshow Screen
- **Enter/Space**: Play/Pause
- **Right Arrow**: Next photo
- **Left Arrow**: Previous photo
- **D-pad**: Focus controls (when visible)
- **Back**: Exit slideshow

### Slideshow Features

1. **Auto-play**: Photos automatically advance at set interval
2. **Interval Control**: Adjust timing from 1 to 60 seconds using +/- buttons
3. **Shuffle Mode**: Toggle between sequential and random order
4. **Image Preloading**: Next image loads in background for smooth transitions
5. **Auto-hide Controls**: Controls fade after 3 seconds, reappear on any key press

## Mock Data

The app includes 8 sample albums with varying photo counts:

- Summer Vacation 2025 (15 photos)
- Family Gatherings (22 photos)
- Birthday Celebrations (18 photos)
- Holiday Memories (30 photos)
- Weekend Adventures (12 photos)
- Nature Photography (25 photos)
- City Lights (20 photos)
- Special Moments (16 photos)

Images are sourced from Lorem Picsum API with unique seeds for variety.

## Key Implementation Details

### State Management

All ViewModels use `StateFlow` for reactive state management:

```kotlin
private val _albums = MutableStateFlow<List<Album>>(emptyList())
val albums: StateFlow<List<Album>> = _albums.asStateFlow()
```

### Slideshow Auto-play

Implemented using coroutines with cancellable jobs:

```kotlin
private fun startSlideshow() {
    slideshowJob = viewModelScope.launch {
        while (true) {
            delay(state.value.intervalSeconds * 1000L)
            nextPhoto()
        }
    }
}
```

### Focus Animations

TV-optimized focus scaling:

```kotlin
scale = CardDefaults.scale(
    scale = 1f,
    focusedScale = 1.08f
)
```

## Future Enhancements (Phase 2)

- Facebook Graph API integration
- OAuth authentication
- Real album and photo fetching
- Caching and offline support
- Photo metadata display
- Album sorting and filtering
- Favorites/bookmarks

## Dependencies

```toml
androidx-core-ktx = "1.10.1"
androidx-tv-foundation = "1.0.0-alpha07"
androidx-tv-material = "1.0.0-alpha07"
androidx-navigation-compose = "2.7.7"
coil-compose = "2.7.0"
androidx-lifecycle-viewmodel-compose = "2.7.0"
androidx-activity-compose = "1.8.2"
```

## Troubleshooting

### Common Issues

**Issue**: App crashes on launch
- **Solution**: Ensure you're running on Android TV emulator/device, not phone/tablet

**Issue**: Images not loading
- **Solution**: Check internet connection; emulator requires network access

**Issue**: D-pad navigation not working
- **Solution**: Ensure using TV emulator; toggle "Show advanced controls" in emulator

**Issue**: Build errors
- **Solution**: 
  1. Clean project: `Build > Clean Project`
  2. Rebuild: `Build > Rebuild Project`
  3. Invalidate caches: `File > Invalidate Caches > Invalidate and Restart`

## Performance Optimizations

- Lazy loading with `TvLazyVerticalGrid`
- Image caching via Coil
- Efficient state management with StateFlow
- Image preloading in slideshow
- Coroutine-based async operations

## License

This is a sample/starter project. Modify and use as needed.

## Contributing

This is Phase 1 (mock data only). Phase 2 will add Facebook integration.

---

**Built with ❤️ for Android TV**
