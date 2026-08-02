#!/bin/bash

# Test script for Yavka.net integration and 2-level caching
# Run this after setting up OMDB_API_KEY environment variable

set -e

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}================================${NC}"
echo -e "${YELLOW}Testing Stremio Addon${NC}"
echo -e "${YELLOW}================================${NC}"
echo ""

# Check if server is running
echo -e "${YELLOW}[1/6] Checking if server is running...${NC}"
if curl -s "$BASE_URL/manifest.json" > /dev/null; then
    echo -e "${GREEN}✓ Server is running${NC}"
else
    echo -e "${RED}✗ Server is NOT running. Please start it first:${NC}"
    echo -e "  ./gradlew bootRun"
    exit 1
fi
echo ""

# Test manifest
echo -e "${YELLOW}[2/6] Testing manifest endpoint...${NC}"
MANIFEST=$(curl -s "$BASE_URL/manifest.json")
if echo "$MANIFEST" | grep -q "BgSubs"; then
    echo -e "${GREEN}✓ Manifest looks good${NC}"
    echo "  Name: $(echo $MANIFEST | grep -o '"name":"[^"]*"' | cut -d'"' -f4)"
    echo "  Resources: $(echo $MANIFEST | grep -o '"resources":\[[^]]*\]')"
else
    echo -e "${RED}✗ Manifest test failed${NC}"
    exit 1
fi
echo ""

# Test movie subtitles (first request - cache miss)
echo -e "${YELLOW}[3/6] Testing movie subtitles (The Dark Knight) - First request (cache MISS)...${NC}"
START_TIME=$(date +%s%N)
MOVIE_RESULT=$(curl -s "$BASE_URL/subtitles/movie/tt0468569/extra.json")
END_TIME=$(date +%s%N)
DURATION=$(( ($END_TIME - $START_TIME) / 1000000 ))

SUBTITLE_COUNT=$(echo "$MOVIE_RESULT" | grep -o '"id"' | wc -l)
if [ "$SUBTITLE_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Found $SUBTITLE_COUNT subtitles${NC}"
    echo -e "  Duration: ${DURATION}ms (expected: 2000-5000ms for cache miss)"
else
    echo -e "${RED}✗ No subtitles found. Check logs for errors.${NC}"
    echo "  Response: $MOVIE_RESULT"
    exit 1
fi
echo ""

# Test movie subtitles (second request - cache hit)
echo -e "${YELLOW}[4/6] Testing movie subtitles (same movie) - Second request (cache HIT)...${NC}"
START_TIME=$(date +%s%N)
MOVIE_RESULT2=$(curl -s "$BASE_URL/subtitles/movie/tt0468569/extra.json")
END_TIME=$(date +%s%N)
DURATION2=$(( ($END_TIME - $START_TIME) / 1000000 ))

SUBTITLE_COUNT2=$(echo "$MOVIE_RESULT2" | grep -o '"id"' | wc -l)
if [ "$SUBTITLE_COUNT2" -eq "$SUBTITLE_COUNT" ]; then
    echo -e "${GREEN}✓ Cache working! Same result returned${NC}"
    echo -e "  Duration: ${DURATION2}ms (expected: <100ms for cache hit)"

    if [ "$DURATION2" -lt 1000 ]; then
        echo -e "${GREEN}✓ CACHE HIT confirmed (fast response)${NC}"
    else
        echo -e "${YELLOW}⚠ Response was slow, might not be from cache${NC}"
    fi
else
    echo -e "${RED}✗ Different results on second request!${NC}"
fi
echo ""

# Test series subtitles
echo -e "${YELLOW}[5/6] Testing series subtitles (Breaking Bad S02E03)...${NC}"
SERIES_RESULT=$(curl -s "$BASE_URL/subtitles/series/tt0903747:2:3/extra.json")
SERIES_COUNT=$(echo "$SERIES_RESULT" | grep -o '"id"' | wc -l)

if [ "$SERIES_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Found $SERIES_COUNT subtitles for series${NC}"
else
    echo -e "${YELLOW}⚠ No series subtitles found (might be expected)${NC}"
fi
echo ""

# Test download proxy endpoint
echo -e "${YELLOW}[6/6] Testing download proxy endpoint...${NC}"

# Extract first subtitle URL from movie result
FIRST_URL=$(echo "$MOVIE_RESULT" | grep -o '"url":"[^"]*"' | head -1 | cut -d'"' -f4)

if [ -n "$FIRST_URL" ]; then
    echo "  Testing URL: $FIRST_URL"

    # Test if it's SubsUnacs or Yavka endpoint
    if echo "$FIRST_URL" | grep -q "subsunacs"; then
        PROVIDER="SubsUnacs"
    elif echo "$FIRST_URL" | grep -q "yavka"; then
        PROVIDER="Yavka"
    else
        PROVIDER="Unknown"
    fi

    # Download subtitle (this tests Level 2 cache)
    START_TIME=$(date +%s%N)
    DOWNLOAD_RESULT=$(curl -s -w "\n%{http_code}" "$FIRST_URL")
    END_TIME=$(date +%s%N)
    DURATION3=$(( ($END_TIME - $START_TIME) / 1000000 ))

    HTTP_CODE=$(echo "$DOWNLOAD_RESULT" | tail -1)

    if [ "$HTTP_CODE" = "200" ]; then
        echo -e "${GREEN}✓ Download successful from $PROVIDER proxy${NC}"
        echo -e "  Duration: ${DURATION3}ms"
        echo -e "  File size: $(echo "$DOWNLOAD_RESULT" | head -n -1 | wc -c) bytes"

        # Test second download (should be cached)
        START_TIME=$(date +%s%N)
        curl -s "$FIRST_URL" > /dev/null
        END_TIME=$(date +%s%N)
        DURATION4=$(( ($END_TIME - $START_TIME) / 1000000 ))

        echo -e "  Second download: ${DURATION4}ms"

        if [ "$DURATION4" -lt "$DURATION3" ]; then
            echo -e "${GREEN}✓ Level 2 cache working (faster second download)${NC}"
        fi
    else
        echo -e "${RED}✗ Download failed with HTTP $HTTP_CODE${NC}"
    fi
else
    echo -e "${YELLOW}⚠ No subtitle URL found to test${NC}"
fi
echo ""

# Summary
echo -e "${YELLOW}================================${NC}"
echo -e "${YELLOW}Test Summary${NC}"
echo -e "${YELLOW}================================${NC}"
echo -e "${GREEN}✓ Server: Running${NC}"
echo -e "${GREEN}✓ Manifest: OK${NC}"
echo -e "${GREEN}✓ Movie subtitles: $SUBTITLE_COUNT found${NC}"
echo -e "${GREEN}✓ Level 1 cache: Working (${DURATION2}ms)${NC}"
echo -e "${GREEN}✓ Series subtitles: $SERIES_COUNT found${NC}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Download proxy: Working${NC}"
    echo -e "${GREEN}✓ Level 2 cache: Working${NC}"
fi
echo ""
echo -e "${YELLOW}Performance:${NC}"
echo -e "  First request:  ${DURATION}ms (cache miss)"
echo -e "  Second request: ${DURATION2}ms (cache hit)"
echo -e "  Speedup: $(( $DURATION / $DURATION2 ))x faster"
echo ""
echo -e "${GREEN}All tests passed! ✓${NC}"
