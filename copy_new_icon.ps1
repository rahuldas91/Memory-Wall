# Copy new icon to all Android resource folders
Add-Type -AssemblyName System.Drawing

$sourceIcon = "C:\Users\A2696276\Downloads\mw_icon.png"
$original = [System.Drawing.Bitmap]::new($sourceIcon)

Write-Host "Source icon: $($original.Width)x$($original.Height)"

# Function to create resized icon (will make it square by center-cropping)
function Create-Icon {
    param($sourceBitmap, $targetSize, $outputPath)
    
    # Since source is 320x180 (landscape), crop to 180x180 square from center
    $size = [Math]::Min($sourceBitmap.Width, $sourceBitmap.Height)
    $x = [int](($sourceBitmap.Width - $size) / 2)
    $y = [int](($sourceBitmap.Height - $size) / 2)
    $cropRect = [System.Drawing.Rectangle]::new($x, $y, $size, $size)
    
    # Crop to square
    $cropped = $sourceBitmap.Clone($cropRect, $sourceBitmap.PixelFormat)
    
    # Resize to target size
    $resized = [System.Drawing.Bitmap]::new($targetSize, $targetSize)
    $graphics = [System.Drawing.Graphics]::FromImage($resized)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.DrawImage($cropped, 0, 0, $targetSize, $targetSize)
    $graphics.Dispose()
    
    # Save
    $resized.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $resized.Dispose()
    $cropped.Dispose()
    
    Write-Host "  ✓ Created $outputPath ($targetSize x $targetSize)"
}

# Create icons for all densities
Write-Host "`nCreating launcher icons..."
Create-Icon $original 48 "c:\workspace\Android\app\src\main\res\mipmap-mdpi\ic_launcher.png"
Create-Icon $original 72 "c:\workspace\Android\app\src\main\res\mipmap-hdpi\ic_launcher.png"
Create-Icon $original 96 "c:\workspace\Android\app\src\main\res\mipmap-xhdpi\ic_launcher.png"
Create-Icon $original 144 "c:\workspace\Android\app\src\main\res\mipmap-xxhdpi\ic_launcher.png"
Create-Icon $original 192 "c:\workspace\Android\app\src\main\res\mipmap-xxxhdpi\ic_launcher.png"

# Copy TV banner (it's already 320x180, perfect size)
Write-Host "`nCopying TV banner..."
$original.Save("c:\workspace\Android\app\src\main\res\drawable\tv_banner.png", [System.Drawing.Imaging.ImageFormat]::Png)
Write-Host "  ✓ Created TV banner (320x180)"

$original.Dispose()

Write-Host "`n✅ All icons and banner updated successfully!"
Write-Host "`nIcon sizes:"
Write-Host "  mdpi: 48x48"
Write-Host "  hdpi: 72x72"  
Write-Host "  xhdpi: 96x96"
Write-Host "  xxhdpi: 144x144"
Write-Host "  xxxhdpi: 192x192"
Write-Host "  TV Banner: 320x180"
