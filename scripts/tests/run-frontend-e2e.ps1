$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Push-Location $repoRoot
try {
    Write-Host "[tests] frontend: npm run test:e2e -- --project=chromium" -ForegroundColor Cyan
    Push-Location "frontend"
    try {
        npm run test:e2e -- --project=chromium
    }
    finally {
        Pop-Location
    }
}
finally {
    Pop-Location
}
