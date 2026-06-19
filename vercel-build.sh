#!/bin/bash
#
# Vercel build script for WeatherWise Flutter Web App.
#
# Why this is more complex than a one-liner:
#   1. Flutter refuses to run as root since 3.10 ("Flutter should not be
#      run as root"). Vercel's build sandbox runs as root, so we create a
#      dedicated non-root user and re-exec under it for the build.
#   2. Git refuses to operate on directories owned by another user
#      (CVE-2022-24765 mitigation). The precompiled Flutter SDK tarball
#      bundles a .git/ owned by the upstream tarball builder, so we
#      whitelist it via `safe.directory '*'`.
#   3. After the build, we chown the project dir back to root so Vercel's
#      deployment step can read it.

set -euo pipefail

# -----------------------------------------------------------------------------
# Configuration
# -----------------------------------------------------------------------------
FLUTTER_VERSION="3.27.1"
FLUTTER_TARBALL="flutter_linux_${FLUTTER_VERSION}-stable.tar.xz"
FLUTTER_URL="https://storage.googleapis.com/flutter_infra_release/releases/stable/linux/${FLUTTER_TARBALL}"
FLUTTER_DIR="/opt/flutter"
BUILDER_USER="vbuild"
BUILDER_HOME="/home/$BUILDER_USER"
PROJECT_DIR="$(pwd)"

# -----------------------------------------------------------------------------
# Step 1: Create non-root builder user
# -----------------------------------------------------------------------------
if ! id "$BUILDER_USER" >/dev/null 2>&1; then
  echo "==> Creating non-root user '$BUILDER_USER'"
  useradd -m -d "$BUILDER_HOME" -s /bin/bash "$BUILDER_USER"
fi

# -----------------------------------------------------------------------------
# Step 2: Download + extract Flutter SDK (as root — fastest path)
# -----------------------------------------------------------------------------
mkdir -p "$(dirname "$FLUTTER_DIR")"
if [ ! -x "$FLUTTER_DIR/bin/flutter" ]; then
  echo "==> Downloading Flutter $FLUTTER_VERSION precompiled SDK"
  curl -fsSL -o "/tmp/$FLUTTER_TARBALL" "$FLUTTER_URL"
  echo "==> Extracting to $FLUTTER_DIR"
  tar -xf "/tmp/$FLUTTER_TARBALL" -C "$(dirname "$FLUTTER_DIR")"
  rm -f "/tmp/$FLUTTER_TARBALL"
fi

# -----------------------------------------------------------------------------
# Step 3: Hand ownership to builder
# -----------------------------------------------------------------------------
# Builder needs write access to:
#   - Flutter SDK (first-run setup downloads Dart SDK + creates caches)
#   - ~/.pub-cache (Dart package cache)
#   - Project dir (.dart_tool/, build/, generated *.freezed.dart/*.g.dart files)
mkdir -p "$BUILDER_HOME/.pub-cache"
chown -R "$BUILDER_USER:$BUILDER_USER" \
  "$FLUTTER_DIR" \
  "$BUILDER_HOME/.pub-cache" \
  "$PROJECT_DIR"

# -----------------------------------------------------------------------------
# Step 4: Run the build as non-root
# -----------------------------------------------------------------------------
echo "==> Running build as '$BUILDER_USER'"
sudo -u "$BUILDER_USER" -E env \
  PATH="$FLUTTER_DIR/bin:$PATH" \
  PUB_CACHE="$BUILDER_HOME/.pub-cache" \
  HOME="$BUILDER_HOME" \
  bash <<BUILDER_SCRIPT
set -euo pipefail

# Trust Flutter SDK's bundled .git (CVE-2022-24765 mitigation).
git config --global --add safe.directory '*'

cd "$PROJECT_DIR"

flutter --disable-analytics 2>/dev/null || true
echo "==> Flutter version:"
flutter --version

echo "==> flutter pub get"
flutter pub get

echo "==> dart run build_runner build --delete-conflicting-outputs"
dart run build_runner build --delete-conflicting-outputs

echo "==> flutter build web --wasm --release"
flutter build web --wasm --release

echo "==> Build complete. Output: build/web"
BUILDER_SCRIPT

BUILD_STATUS=$?

# -----------------------------------------------------------------------------
# Step 5: Restore project ownership so Vercel can deploy
# -----------------------------------------------------------------------------
# Flutter SDK and pub cache stay owned by builder for next build's cache,
# but the project output must be readable by the Vercel deploy process.
echo "==> Restoring project ownership"
chown -R root:root "$PROJECT_DIR" 2>/dev/null || true

exit $BUILD_STATUS