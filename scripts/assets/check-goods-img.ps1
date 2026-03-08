param(
    [string]$SqlFile = "database/InsertData.sql",
    [string]$GoodsDir = "backend/storage/goods-img",
    [string]$OutputMissing = "docs/goods-img-missing.txt"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (!(Test-Path $SqlFile)) {
    throw "SQL file not found: $SqlFile"
}

if (!(Test-Path $GoodsDir)) {
    New-Item -ItemType Directory -Force -Path $GoodsDir | Out-Null
}

$content = Get-Content $SqlFile -Raw
$matches = [regex]::Matches($content, "/goods-img/([A-Za-z0-9._-]+)")
$expected = $matches | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique

$existing = @(Get-ChildItem -Path $GoodsDir -File | Select-Object -ExpandProperty Name)
$existingSet = @{}
foreach ($name in $existing) { $existingSet[$name] = $true }

$missing = @()
foreach ($name in $expected) {
    if (-not $existingSet.ContainsKey($name)) {
        $missing += $name
    }
}

"Expected files: $($expected.Count)" | Write-Output
"Existing files: $($existing.Count)" | Write-Output
"Missing files:  $($missing.Count)" | Write-Output

$missing | Set-Content -Path $OutputMissing -Encoding UTF8
"Missing list written to: $OutputMissing" | Write-Output
