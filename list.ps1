# Obtener el directorio actual del proyecto Android
$CURRENT_DIR = Get-Location

# Cambiar al directorio donde se encuentra el comando PHP (ajusta esta ruta si es necesario)
Set-Location "D:\laragon\www\simplerest"

# Mostrar ayuda si no hay argumentos
if ($args.Count -eq 0) {
    Write-Host "Usage:"
    Write-Host "  --all        Listar todos los archivos (Java, XML, Gradle, etc.)"
    Write-Host "  --code       Listar todos los archivos Java y Kt"
    Write-Host "  --xml        Listar todos los XML"
    Write-Host "  --views      Listar todas las vistas (XMLs en layout)"
    Write-Host "  --drawables  Listar todos los drawables (XMLs en drawable)"
    
    Set-Location $CURRENT_DIR
    exit
}

# Construir comando según el argumento
switch ($args[0]) {
    "--all" {
        $excludePath = Join-Path $CURRENT_DIR 'app\build\*'
        $command = "php com file list '$CURRENT_DIR' --recursive --pattern='*.kt|*.java|*.xml|*.gradle|*.properties|*.jar' --exclude='$excludePath'"
        break
    }
    "--code" {
        $excludePath = Join-Path $CURRENT_DIR 'app\build\*'
        $command = "php com file list '$CURRENT_DIR' --recursive --pattern='*.kt|*.java' --exclude='$excludePath'"
        break
    }
    "--xml" {
        $Path = Join-Path $CURRENT_DIR 'app\src\main\res'
        $command = "php com file list '$Path' --recursive --pattern='*.xml'"
        break
    }
    "--views" {
        $Path = Join-Path $CURRENT_DIR 'app\src\main\res\layout'
        $command = "php com file list '$Path' --recursive --pattern='*.xml'"
        break
    }
    "--drawables" {
        $Path = Join-Path $CURRENT_DIR 'app\src\main\res\drawable'
        $command = "php com file list '$Path' --recursive --pattern='*.xml'"
        break
    }
    default {
        Write-Host "Opción inválida. Usa --all, --xml o --views o --drawables."
        Write-Host "Usage:"
        Write-Host "  --all        Listar todos los archivos (Java, XML, Gradle, etc.)"
        Write-Host "  --code       Listar todos los archivos Java y Kt"
        Write-Host "  --xml        Listar todos los XML"
        Write-Host "  --views      Listar todas las vistas (XMLs en layout)"
        Write-Host "  --drawables  Listar todos los drawables (XMLs en drawable)"
        
        Set-Location $CURRENT_DIR
        exit
    }
}

# Ejecutar el comando
Invoke-Expression $command

# Regresar al directorio original
Set-Location $CURRENT_DIR