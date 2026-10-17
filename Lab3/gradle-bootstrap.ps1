# Windows first-start launcher: downloads the OFFICIAL Gradle distribution,
# not an unofficial wrapper JAR. Generate a standard Wrapper afterward.
$ErrorActionPreference = 'Stop'
$version = '9.8.1'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$cache = Join-Path $env:USERPROFILE '.gradle\lab3-bootstrap'
$home = Join-Path $cache ("gradle-" + $version)
$gradle = Join-Path $home 'bin\gradle.bat'
if (-not (Test-Path $gradle)) {
    New-Item -ItemType Directory -Force -Path $cache | Out-Null
    $zip = Join-Path $cache ("gradle-" + $version + "-bin.zip")
    $url = "https://services.gradle.org/distributions/gradle-$version-bin.zip"
    Write-Host "Downloading official Gradle $version distribution..."
    Invoke-WebRequest -Uri $url -OutFile $zip
    Expand-Archive -LiteralPath $zip -DestinationPath $cache -Force
    Remove-Item -LiteralPath $zip -Force
}
& $gradle '-p' $root @args
exit $LASTEXITCODE
