# Explicit local/QA import only. Catalog files and credentials stay outside Git.
param(
    [string]$BaseUrl = 'http://localhost:8080',
    [Parameter(Mandatory)][string]$ProductsPath,
    [Parameter(Mandatory)][string]$ReferencesPath,
    [ValidateRange(0, 1000)][int]$Limit = 0,
    [string]$ReportDirectory = (Join-Path $PSScriptRoot '../../crawler/data/output/import-reports'),
    [switch]$ValidateOnly
)

$ErrorActionPreference = 'Stop'
$origin = [Uri]$BaseUrl
if ($origin.Scheme -notin @('http', 'https') -or -not [string]::IsNullOrEmpty($origin.UserInfo) -or -not $origin.IsLoopback) {
    throw 'Script nay chi goi ung dung localhost de thu du lieu.'
}
$BaseUrl = $BaseUrl.TrimEnd('/')
$referencePath = $ReferencesPath
$productsPath = $ProductsPath
$references = Get-Content -LiteralPath $referencePath -Raw -Encoding UTF8 | ConvertFrom-Json
$lines = @(Get-Content -LiteralPath $productsPath -Encoding UTF8 | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
if ($Limit -gt 0) { $lines = @($lines | Select-Object -First $Limit) }
$records = @($lines | ForEach-Object { $_ | ConvertFrom-Json })
if ($records.Count -eq 0) { throw 'File khong co san pham.' }
if ($records.Count -gt 1000) { throw 'Toi da 1000 san pham moi lan import.' }
if ($ValidateOnly) {
    [pscustomobject]@{ ProductCount = $records.Count; CategoryCount = @($references.categories).Count; BrandCount = @($references.brands).Count }
    return
}

$credential = Get-Credential -Message 'Nhap email va mat khau tai khoan ADMIN cua ung dung local'
if ($null -eq $credential) { throw 'Chua cung cap tai khoan ADMIN.' }
$loginBody = @{ email = $credential.UserName; password = $credential.GetNetworkCredential().Password } | ConvertTo-Json -Compress
try {
    $session = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post `
        -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($loginBody)) -MaximumRedirection 0 -TimeoutSec 30
} finally {
    $loginBody = $null
    $credential = $null
}
$headers = @{ Authorization = "Bearer $($session.accessToken)" }
$session = $null

function Invoke-CatalogApi {
    param([string]$Path, [string]$Method = 'Get', $Payload = $null)
    $request = @{ Uri = "$BaseUrl$Path"; Method = $Method; Headers = $headers; MaximumRedirection = 0; TimeoutSec = 30 }
    if ($null -ne $Payload) {
        $request.ContentType = 'application/json; charset=utf-8'
        $request.Body = [Text.Encoding]::UTF8.GetBytes(($Payload | ConvertTo-Json -Depth 10 -Compress))
    }
    Invoke-RestMethod @request
}

try {
    # This request verifies ADMIN permission before creating reference data.
    $categories = @(Invoke-CatalogApi '/api/admin/categories')
    $brands = @(Invoke-CatalogApi '/api/admin/brands')
    $requiredCategories = @($records | ForEach-Object { $_.category } | Select-Object -Unique)
    $requiredBrands = @($records | ForEach-Object { $_.brand } | Select-Object -Unique)
    foreach ($reference in $references.categories) {
        if ($reference.slug -notin $requiredCategories) { continue }
        $existing = @($categories | Where-Object { $_.slug -eq $reference.slug })
        if ($existing.Count -gt 0) {
            if (-not $existing[0].active) { throw "Danh muc dang tat: $($reference.slug)" }
            continue
        }
        $created = Invoke-CatalogApi '/api/admin/categories' 'Post' @{
            name = $reference.name; description = ''; sortOrder = 0; active = $true
        }
        if ($created.slug -ne $reference.slug) { throw "Slug danh muc khong khop: $($created.slug)" }
        Write-Host "Created category: $($reference.slug)"
    }
    foreach ($reference in $references.brands) {
        if ($reference.name -notin $requiredBrands) { continue }
        $existing = @($brands | Where-Object { $_.name -ieq $reference.name })
        if ($existing.Count -gt 0) {
            if (-not $existing[0].active) { throw "Thuong hieu dang tat: $($reference.name)" }
            continue
        }
        $null = Invoke-CatalogApi '/api/admin/brands' 'Post' @{
            name = $reference.name; description = ''; active = $true
        }
        Write-Host "Created brand: $($reference.name)"
    }

    $boundary = 'UTEGear-' + [Guid]::NewGuid().ToString('N')
    $jsonl = ($lines -join "`n") + "`n"
    $body = "--$boundary`r`nContent-Disposition: form-data; name=`"file`"; filename=`"products.jsonl`"`r`nContent-Type: application/x-ndjson`r`n`r`n$jsonl`r`n--$boundary--`r`n"
    Write-Host "Importing $($records.Count) products; downloading and uploading source images can take several minutes..."
    $report = Invoke-RestMethod -Uri "$BaseUrl/api/admin/products/import" -Method Post -Headers $headers `
        -ContentType "multipart/form-data; boundary=$boundary" -Body ([Text.Encoding]::UTF8.GetBytes($body)) -MaximumRedirection 0 -TimeoutSec 3600
    New-Item -ItemType Directory -Force -Path $ReportDirectory | Out-Null
    $reportPath = Join-Path $ReportDirectory ('import-report-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.json')
    $report | ConvertTo-Json -Depth 15 | Set-Content -LiteralPath $reportPath -Encoding UTF8
    Write-Host "Total: $($report.totalRows); imported: $($report.importedRows); failed: $($report.failedRows)"
    Write-Host "Report: $reportPath"
    if ($report.failedRows -gt 0) {
        $report.rows | Where-Object { $_.status -eq 'FAILED' } | Format-Table -AutoSize
    }
    Write-Host "View products: $BaseUrl/products"
} finally {
    $headers.Clear()
}
