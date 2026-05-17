# Fix Android TV icons and banner
Add-Type -AssemblyName System.Drawing

$sourceIcon = "c:\workspace\Android\app\src\main\res\mipmap-hdpi\ic_launcher.png"

# Create square icon by center-cropping to 1024x1024
$original = New-Object System.Drawing.Bitmap($sourceIcon)

# Calculate crop area (center crop to square using the smaller dimension)
$size = [Math]::Min($original.Width, $original.Height)
$x = ($original.Width - $size) / 2
$y = ($original.Height - $size) / 2

# Function to create resized square icon
function Create-SquareIcon {
    param($sourceBitmap, $targetSize, $outputPath)
    
    # Crop to square first
    $cropRect = New-Object System.Drawing.Rectangle($x, $y, $size, $size)
    $cropped = $sourceBitmap.Clone($cropRect, $sourceBitmap.PixelFormat)
    
    # Resize to target size
    $resized = New-Object System.Drawing.Bitmap($targetSize, $targetSize)
    $graphics = [System.Drawing.Graphics]::FromImage($resized)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.DrawImage($cropped, 0, 0, $targetSize, $targetSize)
    $graphics.Dispose()
    
    # Save
    $resized.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $resized.Dispose()
    $cropped.Dispose()
}

# Create icons for all densities
Create-SquareIcon $original 48 "c:\workspace\Android\app\src\main\res\mipmap-mdpi\ic_launcher.png"
Create-SquareIcon $original 72 "c:\workspace\Android\app\src\main\res\mipmap-hdpi\ic_launcher.png"
Create-SquareIcon $original 96 "c:\workspace\Android\app\src\main\res\mipmap-xhdpi\ic_launcher.png"
Create-SquareIcon $original 144 "c:\workspace\Android\app\src\main\res\mipmap-xxhdpi\ic_launcher.png"
Create-SquareIcon $original 192 "c:\workspace\Android\app\src\main\res\mipmap-xxxhdpi\ic_launcher.png"

Write-Host "✓ Created square icons for all densities"

# Create TV banner (320x180 - landscape for TV)
$banner = New-Object System.Drawing.Bitmap(320, 180)
$graphics = [System.Drawing.Graphics]::FromImage($banner)
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic

# Scale the original to fit 320x180 (will letterbox or crop as needed)
# Calculate aspect ratios
$targetAspect = 320.0 / 180.0
$sourceAspect = $original.Width / $original.Height

if ($sourceAspect -gt $targetAspect) {
    # Source is wider - crop sides
    $newWidth = $original.Height * $targetAspect
    $cropX = [int](($original.Width - $newWidth) / 2)
    $srcRect = New-Object System.Drawing.Rectangle($cropX, 0, [int]$newWidth, $original.Height)
} else {
    # Source is taller - crop top/bottom
    $newHeight = $original.Width / $targetAspect
    $cropY = [int](($original.Height - $newHeight) / 2)
    $srcRect = New-Object System.Drawing.Rectangle(0, $cropY, $original.Width, [int]$newHeight)
}

$destRect = New-Object System.Drawing.Rectangle(0, 0, 320, 180)
$graphics.DrawImage($original, $destRect, $srcRect, [System.Drawing.GraphicsUnit]::Pixel)
$graphics.Dispose()

$banner.Save("c:\workspace\Android\app\src\main\res\drawable\tv_banner.png", [System.Drawing.Imaging.ImageFormat]::Png)
$banner.Dispose()

Write-Host "✓ Created TV banner (320x180)"

$original.Dispose()

Write-Host "`nAll icons fixed! New dimensions:"
Write-Host "  mdpi: 48x48"
Write-Host "  hdpi: 72x72"  
Write-Host "  xhdpi: 96x96"
Write-Host "  xxhdpi: 144x144"
Write-Host "  xxxhdpi: 192x192"
Write-Host "  TV Banner: 320x180"
