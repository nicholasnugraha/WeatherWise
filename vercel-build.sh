#!/bin/bash
#
# Vercel build script for WeatherWise Flutter Web App.
#
# Why this is layered:
#   1. Flutter refuses to run as root since 3.10 — Vercel builds run as
#      root, so we must switch to a non-root user before invoking flutter.
#   2. Vercel's build sandbox is minimal — `sudo` is NOT installed. We
#      fall back through `runuser` → `setpriv` → `su` to find whatever
#      user-switching tool is available.
#   3. Git refuses to operate on a repo owned by another user (CVE-2022-
#      24765 mitigation). Flutter's bundled .git/ in the SDK tarball is
#      owned by the upstream tarball builder; we whitelist via safe.directory.
#   4. After the build we chown the project dir back to root so Vercel's
#      deploy step can read it (Flutter SDK + pub cache stay owned by
#      the builder user for next-build cache).

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
BUILDER_SCRIPT_PATH="/tmp/weatherwise-builder.sh"

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
mkdir -p "$BUILDER_HOME/.pub-cache"
chown -R "$BUILDER_USER:$BUILDER_USER" \
  "$FLUTTER_DIR" \
  "$BUILDER_HOME/.pub-cache" \
  "$PROJECT_DIR"

# -----------------------------------------------------------------------------
# Step 4: Write the builder script to a temp file
# -----------------------------------------------------------------------------
# Writing to a file avoids messy heredoc quoting when passing to whichever
# user-switching tool we end up using (sudo/runuser/setpriv/su all have
# different stdin / -c semantics).
cat > "$BUILDER_SCRIPT_PATH" <<BUILDER_SCRIPT
#!/bin/bash
set -euo pipefail

export PATH="$FLUTTER_DIR/bin:\$PATH"
export PUB_CACHE="$BUILDER_HOME/.pub-cache"
export HOME="$BUILDER_HOME"

# Trust Flutter SDK's bundled .git directory (CVE-2022-24765).
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

chmod +x "$BUILDER_SCRIPT_PATH"
chown "$BUILDER_USER:$BUILDER_USER" "$BUILDER_SCRIPT_PATH"

# -----------------------------------------------------------------------------
# Step 5: Run the builder script as non-root, using whichever user-switching
# tool the sandbox provides. Tested fallback order: sudo > runuser > setpriv > su
# -----------------------------------------------------------------------------
echo "==> Running build as '$BUILDER_USER'"

run_as_builder() {
  if command -v sudo >/dev/null 2>&1; then
    echo "    (via sudo)"
    sudo -u "$BUILDER_USER" -- "$@"
  elif command -v runuser >/dev/null 2>&1; then
    echo "    (via runuser)"
    runuser -u "$BUILDER_USER" -- "$@"
  elif command -v setpriv >/dev/null 2>&1; then
    echo "    (via setpriv)"
    local uid gid
    uid=$(id -u "$BUILDER_USER")
    gid=$(id -g "$BUILDER_USER")
    setpriv --reuid="$uid" --regid="$gid" --init-groups -- "$@"
  elif command -v su >/dev/null 2>&1; then
    echo "    (via su)"
    su -s /bin/bash "$BUILDER_USER" -c "$(printf '%q ' "$@")"
  else
    echo "ERROR: no user-switching tool available (tried sudo, runuser, setpriv, su)" >&2
    exit 1
  fi
}

run_as_builder bash "$BUILDER_SCRIPT_PATH"
BUILD_STATUS=$?

# -----------------------------------------------------------------------------
# Step 6: Restore project ownership so Vercel can deploy
# -----------------------------------------------------------------------------
echo "==> Restoring project ownership"
chown -R root:root "$PROJECT_DIR" 2>/dev/null || true
# Flutter SDK + pub cache stay owned by builder for next-build cache.
rm -f "$BUILDER_SCRIPT_PATH"

exit $BUILD_STATUS