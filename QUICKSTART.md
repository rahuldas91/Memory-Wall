# 🚀 Quick Start Guide - MemoryWall

Get MemoryWall running on your Android TV emulator in 5 minutes!

## Step 1: Open in Android Studio

1. Launch **Android Studio**
2. Click **File > Open**
3. Navigate to and select the `Android` folder
4. Click **OK**
5. Wait for Gradle sync to complete (status bar at bottom)

## Step 2: Create Android TV Emulator

1. Click **Tools > Device Manager** (or the device icon in toolbar)
2. Click **Create Device** button
3. Select **TV** tab on the left
4. Choose **Android TV (1080p)** from the list
5. Click **Next**
6. Select a system image:
   - Recommended: **API 34** or **API 33** (download if needed)
   - Click **Download** if not already available
7. Click **Next**
8. Name it "**Android TV Test**" (optional)
9. Click **Finish**

## Step 3: Run the App

1. From the device dropdown (top toolbar), select your **Android TV Test** emulator
2. Click the green **Run** button ▶️ (or press `Shift + F10`)
3. Wait for emulator to boot (first launch may take 2-3 minutes)
4. App will automatically install and launch

## Step 4: Navigate the App

### Using Emulator Controls

The Android TV emulator has a D-pad control panel on the right side:

- **Arrow buttons**: Navigate
- **Center circle**: Select/OK
- **Back button**: Go back

### Keyboard Shortcuts

- **Arrow keys**: Navigate
- **Enter**: Select/OK
- **Backspace/Esc**: Back
- **Spacebar**: Play/Pause (in slideshow)

## App Flow

```
Home Screen
    ↓ (Select "View Albums")
Albums Grid (8 albums)
    ↓ (Select any album)
Photos Grid (12-30 photos per album)
    ↓ (Select any photo OR click "Start Slideshow")
Slideshow Screen
    - Auto-plays every 5 seconds
    - Use left/right arrows to navigate
    - Press Enter to pause/play
    - Click "Shuffle" to randomize
    - Adjust interval with +/- buttons
```

## 🎯 Try These Features

### 1. **Browse Albums**
- Navigate through 8 different albums with unique cover images
- See photo counts for each album

### 2. **View Photos**
- Open any album to see a grid of photos
- Use D-pad to highlight different photos
- Notice the focus animation (cards scale up)

### 3. **Slideshow Mode**
- Select any photo to start slideshow from that point
- Or click "Start Slideshow" button to begin from first photo
- Watch auto-advance every 5 seconds
- Try these controls:
  - **Pause/Play**: Press Enter
  - **Next Photo**: Press Right Arrow
  - **Previous Photo**: Press Left Arrow
  - **Shuffle**: Click the "Shuffle" button
  - **Speed Up/Down**: Use +/- buttons

### 4. **Focus Effects**
- Notice how cards scale and highlight when focused
- Border appears around focused items
- Smooth animations throughout

## Troubleshooting

### Emulator won't start
- **Solution**: Go to `Tools > AVD Manager`, click the ▼ dropdown next to emulator, select "Cold Boot Now"

### App crashes
- **Solution**: Make sure you're using the **TV** emulator, not a phone/tablet emulator

### Images not loading
- **Solution**: 
  1. Ensure emulator has internet: Open Chrome browser in emulator
  2. Check Android Studio's logcat for network errors

### Gradle sync fails
- **Solution**: 
  ```
  File > Invalidate Caches > Invalidate and Restart
  ```

### Build errors
- **Solution**: 
  ```bash
  1. Build > Clean Project
  2. Build > Rebuild Project
  ```

## Next Steps

Once you've explored the app:

1. **Review the Code**:
   - Check out `MainActivity.kt` - app entry point
   - Look at screen implementations in `ui/screen/`
   - Examine ViewModels for state management logic

2. **Customize**:
   - Modify colors in `ui/theme/Theme.kt`
   - Add more mock albums in `MockDataRepository.kt`
   - Adjust slideshow timing in `SlideshowViewModel.kt`

3. **Prepare for Phase 2**:
   - Facebook integration coming next
   - OAuth authentication
   - Real album data

## Tips for Development

### Hot Reload
- For Compose changes, use **Run > Apply Changes** (Ctrl+F10) for faster updates
- Not all changes support hot reload; sometimes full rebuild is needed

### Debugging
- Use logcat filter: `package:com.example.memories`
- Add breakpoints in code and use Debug mode (Shift+F9)

### Testing on Real Device
- Enable Developer Options on Android TV device
- Enable "ADB Debugging"
- Connect via USB or network ADB
- Device will appear in device dropdown

---

**Enjoy exploring MemoryWall! 📸📺**
