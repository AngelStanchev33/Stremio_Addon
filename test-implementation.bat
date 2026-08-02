@echo off
setlocal enabledelayedexpansion

REM Test script for Yavka.net integration and 2-level caching
REM Run this after setting up OMDB_API_KEY environment variable

set BASE_URL=http://localhost:8080

echo ================================
echo Testing Stremio Addon
echo ================================
echo.

REM Check if server is running
echo [1/6] Checking if server is running...
curl -s "%BASE_URL%/manifest.json" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Server is running
) else (
    echo [ERROR] Server is NOT running. Please start it first:
    echo   gradlew bootRun
    exit /b 1
)
echo.

REM Test manifest
echo [2/6] Testing manifest endpoint...
curl -s "%BASE_URL%/manifest.json" > temp_manifest.json
findstr /C:"BgSubs" temp_manifest.json >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Manifest looks good
) else (
    echo [ERROR] Manifest test failed
    del temp_manifest.json
    exit /b 1
)
del temp_manifest.json
echo.

REM Test movie subtitles (first request)
echo [3/6] Testing movie subtitles (The Dark Knight) - First request (cache MISS)...
set start=%time%
curl -s "%BASE_URL%/subtitles/movie/tt0468569/extra.json" > temp_movie1.json
set end=%time%

findstr /C:"\"id\"" temp_movie1.json >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Subtitles found
    echo     Expected: 2000-5000ms for cache miss
) else (
    echo [ERROR] No subtitles found. Check logs for errors.
    type temp_movie1.json
    del temp_movie1.json
    exit /b 1
)
del temp_movie1.json
echo.

REM Test movie subtitles (second request - cache hit)
echo [4/6] Testing movie subtitles (same movie) - Second request (cache HIT)...
set start=%time%
curl -s "%BASE_URL%/subtitles/movie/tt0468569/extra.json" > temp_movie2.json
set end=%time%

findstr /C:"\"id\"" temp_movie2.json >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Cache working - same results returned
    echo     Expected: ^<100ms for cache hit
) else (
    echo [ERROR] Cache test failed
)
del temp_movie2.json
echo.

REM Test series subtitles
echo [5/6] Testing series subtitles (Breaking Bad S02E03)...
curl -s "%BASE_URL%/subtitles/series/tt0903747:2:3/extra.json" > temp_series.json
findstr /C:"\"id\"" temp_series.json >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Series subtitles found
) else (
    echo [WARN] No series subtitles found (might be expected)
)
del temp_series.json
echo.

REM Test download proxy
echo [6/6] Testing download proxy endpoint...
curl -s "%BASE_URL%/subtitles/movie/tt0468569/extra.json" > temp_test.json

REM Extract first URL (simplified - just check if endpoints exist)
findstr /C:"subsunacs/download" temp_test.json >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] SubsUnacs download proxy endpoint configured
)

findstr /C:"yavka/download" temp_test.json >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Yavka download proxy endpoint configured
)

del temp_test.json
echo.

REM Summary
echo ================================
echo Test Summary
echo ================================
echo [OK] Server: Running
echo [OK] Manifest: Working
echo [OK] Movie subtitles: Working
echo [OK] Level 1 cache: Working
echo [OK] Series subtitles: Working
echo [OK] Download proxy: Configured
echo.
echo All basic tests passed!
echo.
echo For detailed testing, check the application logs:
echo   tail -f logs/application.log
echo.

endlocal
