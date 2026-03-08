$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Push-Location $repoRoot
try {
    Write-Host "[tests] backend: mvnw -B test" -ForegroundColor Cyan
    Push-Location "backend"
    try {
        cmd /c mvnw.cmd -B test
    }
    finally {
        Pop-Location
    }
}
finally {
    Pop-Location
}
