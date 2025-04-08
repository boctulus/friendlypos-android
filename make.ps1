# Script PowerShell para generar APK y mostrar la ruta de salida

param(
    [string]$buildType = ""
)

# Ruta base del proyecto
$basePath = "app\build\outputs\apk\"

# Si no se pasa un parámetro, mostrar las opciones
if ($buildType -eq "") {
    Write-Host "Por favor, especifica el tipo de compilación (debug o release)."
     Write-Host ""
    Write-Host "Opciones:"
    Write-Host "  --debug    Para generar la versión debug"
    Write-Host "  --release  Para generar la versión release"
    exit
}

# Función para ejecutar el comando de gradle
function EjecutarComandoGradle($comando) {
    Write-Host "Ejecutando comando: $comando"
    Invoke-Expression $comando
}

# Generar el APK según el tipo especificado
if ($buildType -eq "--debug") {
    EjecutarComandoGradle("./gradlew assembleDebug")
    $apkPath = $basePath + "debug\"
    Write-Host "APK generado en: $apkPath"
}
elseif ($buildType -eq "--release") {
    EjecutarComandoGradle("./gradlew assembleRelease")
    $apkPath = $basePath + "release\"
    Write-Host "APK generado en: $apkPath"
}
else {
    Write-Host "Opción inválida. Usa --debug o --release."
    exit
}
