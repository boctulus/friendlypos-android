param(
    [string]$OutputFile = "",
    [string]$ZipIgnore = ".zipignore"
)

# Si no se proporciona un valor para $OutputFile, usar el nombre del directorio actual
if (-not $OutputFile) {
    $CurrentDir = Split-Path -Path $PWD.Path -Leaf
    $OutputFile = "$CurrentDir.zip"
}

# Función para obtener las exclusiones desde .zipignore
function Get-ZipIgnorePatterns {
    param([string]$ZipIgnoreFile)
    if (Test-Path $ZipIgnoreFile) {
        Get-Content $ZipIgnoreFile | ForEach-Object {
            # Ignorar líneas vacías y comentarios
            if ($_ -notmatch '^\s*(#|$)') {
                $_.Trim()
            }
        }
    } else {
        Write-Error "El archivo .zipignore no fue encontrado en el directorio actual."
        return @()
    }
}

# Función para obtener la versión desde update_version.ps1
function Get-Version {
    $version = & .\update_version.ps1 --get_version
    if ($LASTEXITCODE -ne 0 -or -not $version) {
        Write-Error "No se pudo obtener la versión actual."
        exit 1
    }
    return $version.Trim()
}

# Validar si 7-Zip está disponible
if (-not (Get-Command "7z" -ErrorAction SilentlyContinue)) {
    Write-Error "7-Zip no está instalado o no está en la ruta del sistema."
    exit 1
}

# Obtener los patrones de exclusión
$exclusions = Get-ZipIgnorePatterns $ZipIgnore
if (-not $exclusions) {
    Write-Error "No se encontraron exclusiones en .zipignore."
    exit 1
}

# Crear un archivo temporal con las exclusiones
$exclusionsFile = New-TemporaryFile
$exclusions | Set-Content -Path $exclusionsFile

# Generar el archivo ZIP
Write-Host "Creando el archivo ZIP con las exclusiones..."
& 7z a -tzip $OutputFile * -mx9 -x@"$exclusionsFile"

# Verificar si se creó correctamente el archivo
if (Test-Path $OutputFile) {
    Write-Host "El archivo ZIP se ha creado correctamente: $OutputFile"
} else {
    Write-Error "Hubo un problema al crear el archivo ZIP."
    exit 1
}

# Obtener la versión y preparar el archivo versionado
$version = Get-Version
$releasesDir = Join-Path $PWD "__releases"
if (-not (Test-Path $releasesDir)) {
    Write-Host "Creando la carpeta __releases..."
    New-Item -ItemType Directory -Path $releasesDir | Out-Null
}

# Generar el nombre del archivo versionado
$baseName = [System.IO.Path]::GetFileNameWithoutExtension($OutputFile)
$ext = [System.IO.Path]::GetExtension($OutputFile)
$versionedFile = Join-Path $releasesDir "${baseName}_${version}${ext}"

# Copiar el archivo a __releases
Write-Host "Copiando el archivo ZIP versionado a __releases..."
Copy-Item -Path $OutputFile -Destination $versionedFile -Force

# Confirmar éxito
if (Test-Path $versionedFile) {
    Write-Host "El archivo ZIP versionado se ha copiado correctamente: $versionedFile"
} else {
    Write-Error "Hubo un problema al copiar el archivo ZIP versionado."
}

# Eliminar el archivo temporal
Remove-Item $exclusionsFile -Force
