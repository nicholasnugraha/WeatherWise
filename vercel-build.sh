#!/bin/bash
#
# Vercel build script for WeatherWise Flutter Web App.
# Kept deliberately simple per the KISS principle.
#
# What it does, in order:
#   1. Download + extract Flutter SDK to /opt/flutter
#   2. chmod a+rX so any UID can read it
#   3. Whitelist the SDK's bundled .git (CVE-2022-24765 mitigation)
#   4. Run flutter commands as UID 1000 (non-root) via setpriv
#      - Flutter refuses to run as UID 0 since 3.10
#      - setpriv is part of util-linux, present on essentially all
#        Linux distributions including Amazon Linux / Vercel's base
#   5. chmod build/ output for Vercel's deploy step to read

set -euo pipefail

FLUTTER_VERSION="3.27.1"
FLUTTER_URL="https://storage.googleapis.com/flutter_infra_release/releases/stable/linux/flutter_linux_${FLUTTER_VERSION}-stable.tar.xz"
FLUTTER_DIR="/opt/flutter"

# -----------------------------------------------------------------------------
# 1. Download Flutter
# -----------------------------------------------------------------------------
if [ ! -x "$FLUTTER_DIR/bin/flutter" ]; then
  echo "==> Downloading Flutter $FLUTTER_VERSION"
  curl -fsSL -o /tmp/flutter.tar.xz "$FLUTTER_URL"
  tar -xJf /tmp/flutter.tar.xz -C /opt/
  rm /tmp/flutter.tar.xz
fi

# -----------------------------------------------------------------------------
# 2. World-readable so non-root UID can read the SDK
# -----------------------------------------------------------------------------
chmod -R a+rX "$FLUTTER_DIR"

# -----------------------------------------------------------------------------
# 3. Git safe.directory (Flutter SDK's bundled .git is owned by upstream)
# -----------------------------------------------------------------------------
git config --global --add safe.directory '*' || true
git config --global --add safe.directory "$FLUTTER_DIR" || true

# -----------------------------------------------------------------------------
# 4. Build as non-root UID via setpriv
# -----------------------------------------------------------------------------
# setpriv drops to UID 1000 without needing an /etc/passwd entry. Flutter's
# root-check reads _processInfo.uid, which is now 1000 — check passes.
run_as_user() {
  setpriv --reuid=1000 --regid=1000 --init-groups -- env \
    PATH="$FLUTTER_DIR/bin:$PATH" \
    PUB_CACHE=/tmp/.pub-cache \
    HOME=/tmp \
    "$@"
}

run_as_user flutter --disable-analytics 2>/dev/null || true
echo "==> Flutter version:"
run_as_user flutter --version
echo "==> flutter pub get"
run_as_user flutter pub get
echo "==> build_runner"
run_as_user dart run build_runner build --delete-conflicting-outputs
echo "==> flutter build web --wasm --release"
run_as_user flutter build web --wasm --release

# -----------------------------------------------------------------------------
# 5. Make build/ readable for Vercel's deploy step
# -----------------------------------------------------------------------------
chmod -R a+rX build/

echo "==> Build complete"