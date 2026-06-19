#!/bin/bash
#
# Vercel build script for WeatherWise Flutter Web App.
#
# Installs Flutter SDK into .vercel/cache/flutter (re-used across builds via
# Vercel's cache directive in vercel.json), runs build_runner for Freezed /
# json_serializable generation, and produces a Skwasm-flavored release build.
#
# Exit non-zero on any failure so Vercel surfaces the error.

set -euo pipefail

# -----------------------------------------------------------------------------
# Paths
# -----------------------------------------------------------------------------

# Flutter SDK is cached between builds via vercel.json `cache` directive.
export FLUTTER_HOME="${FLUTTER_HOME:-$PWD/.vercel/cache/flutter}"
export PUB_CACHE="${PUB_CACHE:-$PWD/.vercel/cache/pub-cache}"
export PATH="$FLUTTER_HOME/bin:$PATH"

# Pre-warmed build artifacts that can survive across builds (build_runner output,
# build/web intermediate). Saves a few seconds per build.
ARTIFACT_DIR="$PWD/.vercel/cache/build-artifacts"

# -----------------------------------------------------------------------------
# Flutter SDK
# -----------------------------------------------------------------------------

if [ ! -x "$FLUTTER_HOME/bin/flutter" ]; then
  echo "==> Installing Flutter stable into $FLUTTER_HOME"
  mkdir -p "$(dirname "$FLUTTER_HOME")"
  git clone --depth 1 --branch stable \
      https://github.com/flutter/flutter.git "$FLUTTER_HOME"
fi

echo "==> Flutter version:"
flutter --version

# Disable analytics prompts on Vercel (no TTY).
flutter --disable-analytics 2>/dev/null || true
flutter config --no-cli-animations 2>/dev/null || true

# -----------------------------------------------------------------------------
# Dependencies + codegen
# -----------------------------------------------------------------------------

echo "==> flutter pub get"
flutter pub get

echo "==> dart run build_runner build --delete-conflicting-outputs"
dart run build_runner build --delete-conflicting-outputs

# -----------------------------------------------------------------------------
# Web build (WASM / Skwasm)
# -----------------------------------------------------------------------------

echo "==> flutter build web --wasm --release"
flutter build web --wasm --release

echo "==> Build complete. Output: build/web"