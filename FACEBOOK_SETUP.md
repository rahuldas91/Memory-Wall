# Facebook Integration Setup Guide

This guide walks you through setting up Facebook authentication and photo access for MemoryWall.

## Phase 2 Overview

Phase 2 adds Facebook integration that allows users to:
- Authenticate with their Facebook account
- View their real Facebook photo albums
- Browse and display photos from their albums
- Use slideshow features with real photos

## Prerequisites

- Android TV emulator or device
- Facebook account
- Developer access to Facebook

---

## Step 1: Create a Facebook App

### 1.1 Go to Facebook Developers

Visit [https://developers.facebook.com/apps/](https://developers.facebook.com/apps/)

### 1.2 Create a New App

1. Click **"Create App"**
2. Select **"Consumer"** as the app type
3. Click **"Next"**

### 1.3 Configure Basic Settings

1. **Display Name**: `MemoryWall` (or your preferred name)
2. **App Contact Email**: Your email address
3. Click **"Create App"**

### 1.4 Get Your App ID and Client Token

1. On the app dashboard, you'll see your **App ID** (top left)
2. Go to **Settings > Basic**
3. Copy the **App ID**
4. Find and copy the **Client Token** (you may need to click "Show" to reveal it)

---

## Step 2: Configure Facebook Products

### 2.1 Add Facebook Login

1. In your Facebook app dashboard, scroll down to **"Add a Product"**
2. Find **"Facebook Login"** and click **"Set Up"**
3. Choose **"Android"** as the platform

### 2.2 Configure Android Settings

1. **Package Name**: `com.example.memories`
2. **Default Activity Class Name**: `com.example.memories.MainActivity`

### 2.3 Add Key Hashes

For development, you need to generate a debug key hash:

#### On Windows (PowerShell):
```powershell
keytool -exportcert -alias androiddebugkey -keystore $env:USERPROFILE\.android\debug.keystore | openssl sha1 -binary | openssl base64
```

Default password is typically: `android`

#### On macOS/Linux:
```bash
keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore | openssl sha1 -binary | openssl base64
```

Paste the generated hash into the **Key Hashes** field.

### 2.4 Enable Single Sign-On

Make sure **"Single Sign On"** is enabled in the Facebook Login settings.

---

## Step 3: Request Permissions

### 3.1 Add User Data Permissions

1. Go to **App Review > Permissions and Features**
2. Request the following permissions:
   - `user_photos` - Access user's photos
   - `public_profile` - Basic profile info

### 3.2 Submit for Review (Optional)

For production, you'll need to submit your app for review. For development and testing, you can add test users.

---

## Step 4: Add Test Users (Development)

### 4.1 Create Test Users

1. Go to **Roles > Test Users**
2. Click **"Add Test Users"**
3. Create one or more test users
4. Copy the credentials

### 4.2 Add Sample Photos

1. Log in to Facebook as your test user
2. Upload some sample photos to create albums
3. These will be accessible through the app

---

## Step 5: Configure the Android App

### 5.1 Update strings.xml

Open `app/src/main/res/values/strings.xml` and replace the placeholders:

```xml
<resources>
    <string name="app_name">MemoryWall</string>
    
    <!-- Replace with YOUR values -->
    <string name="facebook_app_id">YOUR_APP_ID_HERE</string>
    <string name="fb_login_protocol_scheme">fbYOUR_APP_ID_HERE</string>
    <string name="facebook_client_token">YOUR_CLIENT_TOKEN_HERE</string>
</resources>
```

**Example:**
```xml
<string name="facebook_app_id">123456789012345</string>
<string name="fb_login_protocol_scheme">fb123456789012345</string>
<string name="facebook_client_token">abc123def456789</string>
```

### 5.2 Sync and Build

1. In Android Studio, click **"Sync Now"** (if prompted)
2. **Build > Clean Project**
3. **Build > Rebuild Project**

---

## Step 6: Test the Integration

### 6.1 Run the App

1. Start your Android TV emulator
2. Run the app from Android Studio

### 6.2 Link Facebook Account

1. On the Home screen, select **"Link Facebook Account"**
2. Read the dialog about TV login
3. Click **"Continue"**

### 6.3 Important Note About TV Login

**Facebook login on Android TV requires device login flow**, which involves:

1. Getting a code on the TV
2. Visiting `facebook.com/device` on another device (phone/computer)
3. Entering the code
4. Authorizing the app

**Current Implementation:**

The current implementation includes the Facebook SDK and authentication manager, but **device login flow UI is not fully implemented** due to complexity. 

For testing purposes, you have two options:

#### Option A: Use Mock Data (Recommended for Testing)
- Simply click "View Albums" without linking Facebook
- The app will use demo albums automatically
- All features work with mock data

#### Option B: Implement Device Login (Advanced)
If you want real Facebook authentication, you'll need to implement the device login flow. See the [Facebook Device Login documentation](https://developers.facebook.com/docs/facebook-login/for-devices).

---

## Step 7: Using the App

### 7.1 With Mock Data (No Login)

1. Launch app
2. Click **"View Albums"**
3. Browse 8 demo albums with mock photos

### 7.2 With Facebook (When Implemented)

1. Link Facebook account
2. Grant photo permissions
3. View your real albums
4. Browse your actual photos
5. Use slideshow with your photos

---

## Architecture Overview

### Authentication Flow

```
HomeScreen
    ↓
AuthViewModel (manages auth state)
    ↓
FacebookAuthManager (handles Facebook SDK)
    ↓
AuthState (NotAuthenticated | Loading | Authenticated | Error)
```

### Data Flow

```
When Authenticated:
AlbumsViewModel → FacebookRepository → Facebook Graph API → Real Albums

When Not Authenticated:
AlbumsViewModel → MockDataRepository → Demo Albums
```

### Key Components

1. **FacebookAuthManager** (`auth/FacebookAuthManager.kt`)
   - Manages Facebook login/logout
   - Tracks authentication state
   - Provides access tokens

2. **FacebookRepository** (`data/FacebookRepository.kt`)
   - Fetches albums via Graph API
   - Fetches photos via Graph API
   - Handles API responses

3. **AuthViewModel** (`viewmodel/AuthViewModel.kt`)
   - Exposes auth state to UI
   - Creates Facebook repository instances
   - Manages login/logout actions

4. **AlbumsViewModel & PhotosViewModel**
   - Switch between Facebook and Mock repositories
   - Handle loading states
   - Manage error fallbacks

---

## Facebook Graph API

The app uses these endpoints:

### Get Albums
```
GET /v18.0/me/albums?fields=id,name,cover_photo,count&access_token={token}
```

### Get Photos from Album
```
GET /v18.0/{album_id}/photos?fields=id,images&access_token={token}
```

### Get Album Details
```
GET /v18.0/{album_id}?fields=id,name,cover_photo,count&access_token={token}
```

---

## Troubleshooting

### Issue: "Invalid key hash"

**Solution:** Regenerate your key hash and add it to Facebook app settings.

### Issue: "App not configured for Android"

**Solution:** Verify package name is exactly `com.example.memories` in Facebook settings.

### Issue: "Login failed"

**Solutions:**
1. Check App ID and Client Token are correct in `strings.xml`
2. Verify Facebook app is in "Development" mode for testing
3. Add your account as a test user or app admin

### Issue: "No albums showing after login"

**Solution:** 
1. Check logcat for API errors
2. Verify `user_photos` permission is granted
3. Ensure test user has photos/albums on Facebook

### Issue: Build errors after adding Facebook SDK

**Solution:**
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

---

## Permissions Reference

### Required Permissions

| Permission | Purpose | Required For |
|-----------|---------|---------------|
| `public_profile` | Basic user info | Authentication |
| `user_photos` | Access photos | Album & photo access |

### Optional Permissions

| Permission | Purpose | Use Case |
|-----------|---------|-----------|
| `user_videos` | Access videos | Future feature |

---

## Production Checklist

Before releasing to production:

- [ ] Submit app for App Review with `user_photos` permission
- [ ] Switch Facebook app from "Development" to "Live" mode
- [ ] Implement proper error messages for users
- [ ] Add loading indicators for network requests
- [ ] Implement token refresh logic
- [ ] Add offline/cached data support
- [ ] Test with various album sizes
- [ ] Add analytics events
- [ ] Implement rate limiting handling
- [ ] Add privacy policy link

---

## Additional Resources

- [Facebook Login for Android](https://developers.facebook.com/docs/facebook-login/android)
- [Facebook Graph API Reference](https://developers.facebook.com/docs/graph-api)
- [Facebook App Review](https://developers.facebook.com/docs/app-review)
- [Android TV Development](https://developer.android.com/training/tv)

---

## Support

For issues with:
- **Facebook SDK**: [Facebook Developers Community](https://developers.facebook.com/community)
- **Android TV**: [Android Developers](https://developer.android.com/tv)
- **Graph API**: [Facebook Graph API Documentation](https://developers.facebook.com/docs/graph-api)

---

## Summary

**Current Status:**
- ✅ Facebook SDK integrated
- ✅ Authentication manager created
- ✅ Graph API repository implemented
- ✅ ViewModels support both mock and real data
- ✅ UI displays auth state
- ⚠️ Device login flow UI needs completion for full Facebook login

**To Use Now:**
1. Run the app
2. Use "View Albums" to browse mock data
3. All features work with demo data

**To Complete Facebook Integration:**
1. Set up Facebook app (Steps 1-4)
2. Update `strings.xml` with your App ID and tokens
3. Implement device login UI (optional for production)
4. Test with test users

---

**Happy coding! 📸📺**
