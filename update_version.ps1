# PowerShell script to get or update the version in package.json
# Usage:
#   .\update_version.ps1 --get_version    # Returns the current version
#   .\update_version.ps1                  # Increments the revision number (e.g., 1.0.0 to 1.0.1)

# Determine the script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Get the package.json path
$packageJsonPath = Join-Path $scriptDir "package.json"

# Check if package.json exists
if (-not (Test-Path $packageJsonPath)) {
    Write-Error "package.json not found at $packageJsonPath"
    exit 1
}

# Read the package.json content
$content = Get-Content $packageJsonPath -Raw
$packageJson = $content | ConvertFrom-Json

# Check command line arguments to handle the --get_version case
# Use $args[0] to access the first argument passed to the script
if ($args[0] -eq "--get_version") {
    Write-Output $packageJson.version
    exit 0
}
# If no arguments were provided, increment the version
else {
    # Parse the current version
    $currentVersion = $packageJson.version
    if ($currentVersion -match "^(\d+)\.(\d+)\.(\d+)$") {
        $major = [int]$matches[1]
        $minor = [int]$matches[2]
        $patch = [int]$matches[3]
        
        # Increment the patch version (revision)
        $newPatch = $patch + 1
        $newVersion = "$major.$minor.$newPatch"
        
        # Update the version in the object
        $packageJson.version = $newVersion
        
        # Convert back to JSON with proper formatting
        $updatedContent = $packageJson | ConvertTo-Json -Depth 10
        
        # Write the updated content back to package.json
        Set-Content -Path $packageJsonPath -Value $updatedContent
        
        Write-Output "Version updated from $currentVersion to $newVersion"
    } else {
        Write-Error "Current version '$currentVersion' does not match the expected format (x.y.z)"
        exit 1
    }
}