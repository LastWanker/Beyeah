$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

# Add new quality gates here when new features introduce new test scopes.
$testSteps = @(
    ".\scripts\tests\run-backend-tests.ps1",
    ".\scripts\tests\run-frontend-e2e.ps1",
    ".\scripts\tests\run-frontend-build.ps1"
)

Push-Location $repoRoot
try {
    foreach ($step in $testSteps) {
        Write-Host ""
        Write-Host "==> running $step" -ForegroundColor Yellow
        & $step
    }

    Write-Host ""
    Write-Host "[tests] all passed." -ForegroundColor Green
}
finally {
    Pop-Location
}
