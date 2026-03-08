$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Push-Location $repoRoot
try {
    Write-Host "[tests] frontend: npm run build" -ForegroundColor Cyan
    Push-Location "frontend"
    try {
        npm run build
    }
    finally {
        Pop-Location
    }
}
finally {
    Pop-Location
}
