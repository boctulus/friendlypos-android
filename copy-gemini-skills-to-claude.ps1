$source = ".\.agentss\skills"

$targets = @(
    ".\.claude\skills",
    ".\.qwen\skills",
    ".\.opencode\skills",
    ".\.gemini\skills",
    ".\.codex\skills"
)

if (-not (Test-Path $source)) {
    throw "Source folder not found: $source"
}

foreach ($target in $targets) {
    New-Item -ItemType Directory -Path $target -Force | Out-Null

    Get-ChildItem -Path $target -Force | Remove-Item -Recurse -Force

    Copy-Item -Path "$source\*" -Destination $target -Recurse -Force
}