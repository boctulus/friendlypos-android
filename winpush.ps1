# Ejecutar el script de actualización de versión
.\update_version.ps1

$last_version = .\update_version.ps1 --get_version

# Comprobar si se ha pasado un argumento
if ($args.Count -eq 0) {
    $msg = "Automatic update"
} else {
    $msg = $args -join " "
}

# Obtener la fecha y hora actual en formato ISO
$datetime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

# Crear la línea del changelog
$changelog_line = "[$datetime]`t$last_version`t$msg"

# Agregar la línea al archivo CHANGELOG.txt
Add-Content -Path "CHANGELOG.txt" -Value $changelog_line

# Asegurarnos de que CHANGELOG.txt también se incluya en el commit
git add CHANGELOG.txt

# Ejecutar los comandos de Git
git add *
git commit -m "$msg"
git push