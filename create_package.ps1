# Configuración
$sourceDir = "D:\laragon\www\simplerest\app\core"
$packageDir = "D:\laragon\www\simplerest\packages\boctulus\api_client\src"
$sedPath = "C:\Program Files\Git\usr\bin\sed.exe"  # Asegúrate de tener GNU sed instalado

# Archivos a copiar (rutas relativas a $sourceDir)
$filesToCopy = @(
    "libs\ApiClientFallback.php",
    "libs\Url.php",
    "libs\Utils.php",
    "libs\Logger.php",
    "libs\FileCache.php",
    "Exception.php",
    "libs\Strings.php"
)

# Crear estructura de directorios
$directories = @(
    "$packageDir",
    "$packageDir\Helpers",
    "$packageDir\Exceptions",
    "$packageDir\Cache"
)

foreach ($dir in $directories) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
}

# Copiar archivos y ajustar estructura
foreach ($file in $filesToCopy) {
    $sourceFile = Join-Path -Path $sourceDir -ChildPath $file
    $destFile = Switch -Wildcard ($file) {
        "*Strings.php" { Join-Path -Path $packageDir -ChildPath "Helpers\Strings.php" }
        "*Exception.php" { Join-Path -Path $packageDir -ChildPath "Exceptions\AppException.php" }
        "*FileCache.php" { Join-Path -Path $packageDir -ChildPath "Cache\FileCache.php" }
        default { Join-Path -Path $packageDir -ChildPath (Split-Path $file -Leaf) }
    }
    
    Copy-Item -Path $sourceFile -Destination $destFile -Force
}

# Ajustar namespaces usando GNU sed
$filesToProcess = Get-ChildItem -Path $packageDir -Filter *.php -Recurse

foreach ($file in $filesToProcess) {
    # Normalizar rutas para sed
    $filePath = $file.FullName.Replace("\", "/")
    
    # Actualizar namespace principal
    & $sedPath -i "s/namespace\s\+.*;/namespace Boctulus\\ApiClient;/g" $filePath
    
    # Actualizar referencias a clases internas
    & $sedPath -i "s/use\s\+\(.*\)\\Helpers\\Strings;/use Boctulus\\ApiClient\\Helpers\\Strings;/g" $filePath
    & $sedPath -i "s/use\s\+\(.*\)\\Exceptions\\AppException;/use Boctulus\\ApiClient\\Exceptions\\AppException;/g" $filePath
    & $sedPath -i "s/use\s\+\(.*\)\\Cache\\FileCache;/use Boctulus\\ApiClient\\Cache\\FileCache;/g" $filePath
    
    # Ajustar referencias de clases base
    & $sedPath -i "s/extends\s\+\([A-Za-z]\+\)/extends \\Boctulus\\ApiClient\\\1/g" $filePath
}

# Crear composer.json
$composerContent = @{
    "name" = "boctulus/api-client"
    "description" = "Portable API Client Package"
    "type" = "library"
    "autoload" = @{
        "psr-4" = @{
            "Boctulus\\ApiClient\\" = "src/"
        }
    }
    "require" = @{
        "php" = ">=7.4"
    }
} | ConvertTo-Json -Depth 3

$composerContent | Out-File -FilePath "$packageDir\..\composer.json" -Encoding utf8

Write-Host "Package creado exitosamente en: $packageDir"