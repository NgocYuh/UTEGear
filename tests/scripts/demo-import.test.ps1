$ErrorActionPreference = 'Stop'
$scriptPath = Join-Path $PSScriptRoot '../../scripts/demo/import-catalog.ps1'
$tokens = $null; $parseErrors = $null
$null = [Management.Automation.Language.Parser]::ParseFile($scriptPath, [ref]$tokens, [ref]$parseErrors)
if ($parseErrors.Count) { throw 'Import script syntax failed' }

$testDirectory = Join-Path ([IO.Path]::GetTempPath()) ('utegear-import-test-' + [Guid]::NewGuid())
$null = New-Item -ItemType Directory -Path $testDirectory
try {
    $products = Join-Path $testDirectory 'products.jsonl'
    $references = Join-Path $testDirectory 'reference-data.json'
    '{"name":"QA one"}', '{"name":"QA two"}' | Set-Content -LiteralPath $products -Encoding UTF8
    '{"categories":[{"slug":"qa"}],"brands":[{"name":"QA"}]}' | Set-Content -LiteralPath $references -Encoding UTF8
    $result = & $scriptPath -ProductsPath $products -ReferencesPath $references -ValidateOnly
    if ($result.ProductCount -ne 2 -or $result.CategoryCount -ne 1 -or $result.BrandCount -ne 1) { throw 'Validation count mismatch' }
    $limited = & $scriptPath -ProductsPath $products -ReferencesPath $references -Limit 1 -ValidateOnly
    if ($limited.ProductCount -ne 1) { throw 'Limit mismatch' }
    foreach ($url in @('https://example.com', 'http://localhost.example.com', 'http://user:password@localhost:8080')) {
        $rejected = $false
        try { $null = & $scriptPath -BaseUrl $url -ProductsPath $products -ReferencesPath $references -ValidateOnly }
        catch { $rejected = $true }
        if (-not $rejected) { throw 'Nonlocal or credential URL was accepted' }
    }
    Set-Content -LiteralPath $products -Value '' -Encoding UTF8
    $rejected = $false
    try { $null = & $scriptPath -ProductsPath $products -ReferencesPath $references -ValidateOnly }
    catch { $rejected = $true }
    if (-not $rejected) { throw 'Empty catalog was accepted' }
    Write-Output 'PASS: syntax, validation without authentication/network, record limit, 3 unsafe URL rejections, empty catalog.'
} finally {
    # Only explicitly named files created by this test; no recursive cleanup.
    Remove-Item -LiteralPath $products, $references -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $testDirectory -Force
}
