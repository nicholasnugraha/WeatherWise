#!/bin/bash
#
# Vercel build script for WeatherWise Flutter Web App.
#
# Installs Flutter SDK from a precompiled tarball (faster than git clone),
# runs build_runner for Freezed / json_serializable generation, and produces
# a Skwasm-flavored release build.
#
# Exit non-zero on any failure so Vercel surfaces the error.
#
# Note on caching: Vercel's vercel.json does NOT support a top-level
# `cache` property for arbitrary paths. The Flutter SDK will be re-downloaded
# on every cold build (~700MB compressed, ~1-2 minutes on Vercel).
# Subsequent builds within the same Vercel deployment pipeline may reuse the
# cached tarball via Vercel's internal artifact cache.

set -euo pipefail

# -----------------------------------------------------------------------------
# Configuration
# -----------------------------------------------------------------------------

# Pin to a known-stable Flutter version with WASM (Skwasm) support.
FLUTTER_VERSION="3.27.1"
FLUTTER_TARBALL="flutter_linux_${FLUTTER_VERSION}-stable.tar.xz"
FLUTTER_URL="https://storage.googleapis.com/flutter_infra_release/releases/stable/linux/${FLUTTER_TARBALL}"
FLUTTER_DIR="$HOME/flutter"

export PATH="$FLUTTER_DIR/bin:$PATH"
# Keep pub cache out of the project tree to avoid bloating deployments.
export PUB_CACHE="${PUB_CACHE:-$HOME/.pub-cache}"

# -----------------------------------------------------------------------------
# Flutter SDK
# -----------------------------------------------------------------------------

if [ ! -x "$FLUTTER_DIR/bin/flutter" ]; then
  echo "==> Downloading Flutter $FLUTTER_VERSION precompiled SDK"
  cd "$HOME"
  curl -fsSL -o "$FLUTTER_TARBALL" "$FLUTTER_URL"
  echo "==> Extracting Flutter SDK"
  tar -xf "$FLUTTER_TARBALL"
  rm -f "$FLUTTER_TARBALL"
fi

echo "==> Flutter version:"
flutter --version

# Disable analytics prompts (no TTY on Vercel).
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