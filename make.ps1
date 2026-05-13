# Script PowerShell para generar APK y mostrar la ruta de salida

param(
    [string]$buildType = "",
    [string]$flavor = "emulator"
)

# Ruta base del proyecto
$basePath = "app\build\outputs\apk\"

# Si no se pasa un parámetro, mostrar las opciones
if ($buildType -eq "") {
    Write-Host "Uso: .\make.ps1 --debug|--release [-flavor emulator|device|production]"
     Write-Host ""
    Write-Host "Opciones:"
    Write-Host "  --debug    Genera la versión debug (firmada con debug keystore)"
    Write-Host "  --release  Genera la versión release (requiere signing config)"
    Write-Host "  -flavor    Variante: emulator (default), device, production"
    exit
}

# Función para ejecutar el comando de gradle
function EjecutarComandoGradle($comando) {
    Write-Host "Ejecutando comando: $comando"
    Invoke-Expression $comando
}

# Generar el APK según el tipo especificado
if ($buildType -eq "--debug") {
    EjecutarComandoGradle("./gradlew assemble${flavor}Debug")
    $apkPath = "${basePath}${flavor}\debug\"
    Write-Host "APK generado en: $apkPath"
    Write-Host "Archivo: app-${flavor}-debug.apk"
    Write-Host "ADVERTENCIA: --release produce APKs SIN FIRMAR. Usa --debug para instalar."
}
elseif ($buildType -eq "--release") {
    Write-Host "ERROR: No hay signing config para release. Usa --debug en su lugar."
    Write-Host "Si necesitas release, agrega signingConfigs al build.gradle.kts."
    exit 1
}
else {
    Write-Host "Opción inválida. Usa --debug o --release."
    exit
}
