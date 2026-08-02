#!/bin/bash

echo "================================"
echo "Basic Tests (No API Key Needed)"
echo "================================"
echo ""

# Test 1: Manifest
echo "[1/4] Testing manifest..."
MANIFEST=$(curl -s http://localhost:8080/manifest.json)
if echo "$MANIFEST" | grep -q "BgSubs"; then
    echo "✓ Manifest OK"
else
    echo "✗ Manifest failed"
    exit 1
fi

# Test 2: Check SubsUnacs endpoint exists
echo "[2/4] Testing SubsUnacs download endpoint..."
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/subsunacs/download?link=test")
if [ "$RESPONSE" != "404" ]; then
    echo "✓ SubsUnacs endpoint exists"
else
    echo "✗ SubsUnacs endpoint not found"
fi

# Test 3: Check Yavka endpoint exists
echo "[3/4] Testing Yavka download endpoint..."
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/yavka/download?link=test")
if [ "$RESPONSE" != "404" ]; then
    echo "✓ Yavka endpoint exists"
else
    echo "✗ Yavka endpoint not found"
fi

# Test 4: Check subtitle endpoint exists
echo "[4/4] Testing subtitle endpoint..."
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/subtitles/movie/tt0468569/extra.json")
if [ "$RESPONSE" != "404" ]; then
    echo "✓ Subtitle endpoint exists"
    echo ""
    echo "Note: Actual subtitle fetching requires OMDB_API_KEY"
else
    echo "✗ Subtitle endpoint not found"
fi

echo ""
echo "================================"
echo "Basic tests complete!"
echo ""
echo "To test with actual data:"
echo "1. Get OMDB API key from https://www.omdbapi.com/apikey.aspx"
echo "2. Set: export OMDB_API_KEY='your_key'"
echo "3. Run: ./test-implementation.sh"
echo "================================"
