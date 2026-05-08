$epoch = [int][double]::Parse((Get-Date -UFormat %s))
$dst = "$PSScriptRoot\__releases\friendlypos-src-$epoch.zip"
$root = $PSScriptRoot

# Build exclude list from .zipignore
$ignoreRaw = @()
if (Test-Path "$root\.zipignore") {
    $ignoreRaw = Get-Content "$root\.zipignore" | Where-Object {
        $_.Trim() -and -not $_.StartsWith("#")
    }
}

$excludeArgs = @()
foreach ($p in $ignoreRaw) {
    $p = $p.Trim().Replace('/', '\')
    if ($p -match '[/\\]$' -or (Test-Path (Join-Path $root $p) -PathType Container)) {
        $p = $p.TrimEnd('/', '\')
        $excludeArgs += @("-xr!$p", "-xr!$p\*")
    } else {
        $excludeArgs += "-xr!$p"
    }
}
$excludeArgs += @("-xr!__releases", "-xr!__releases\*")

& "7z" a -tzip $dst "$root\*" @excludeArgs -bso0 -bsp0
Write-Host "Created: $dst"
