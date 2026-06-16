#!/usr/bin/env bash
#
# Spots installer — download the latest released APK from GitHub and install it on
# a connected Android device over adb. This script is itself published as a release
# asset, so it can be run straight from the web:
#
#   curl -fsSL https://github.com/L-K-M/Spots/releases/latest/download/install.sh | bash
#
# Or from a checkout:
#
#   ./install.sh
#
# Options:
#   --version <tag>   install a specific release (e.g. v0.1.0) instead of the latest
#   --list            list the available release tags and exit
#   --device <serial> target a specific adb device (see `adb devices`)
#   --apk-only        download (and verify) the APK but don't install it
#   --keep            keep the downloaded APK instead of deleting it afterwards
#   -h, --help        show this help
#
# Requirements: curl, and adb (from the Android platform-tools) unless --apk-only.
# adb is found on PATH or under $ANDROID_HOME/platform-tools.
set -euo pipefail

REPO="L-K-M/Spots"
API="https://api.github.com/repos/${REPO}"
VERSION=""
LIST=0
APK_ONLY=0
KEEP=0
DEVICE=""

log() { printf '==> %s\n' "$*"; }
err() { printf 'error: %s\n' "$*" >&2; }
die() { err "$*"; exit 1; }

usage() { sed -n '2,27p' "$0" | sed 's/^# \{0,1\}//'; }

while [ $# -gt 0 ]; do
  case "$1" in
    --version) VERSION="${2:-}"; shift 2 || die "--version needs a tag" ;;
    --list) LIST=1; shift ;;
    --apk-only) APK_ONLY=1; shift ;;
    --keep) KEEP=1; shift ;;
    --device) DEVICE="${2:-}"; shift 2 || die "--device needs a serial" ;;
    -h|--help) usage; exit 0 ;;
    *) die "unknown option: $1 (try --help)" ;;
  esac
done

command -v curl >/dev/null 2>&1 || die "curl is required."

# --- list mode --------------------------------------------------------------------
if [ "$LIST" -eq 1 ]; then
  log "Available releases of ${REPO}:"
  curl -fsSL "${API}/releases" \
    | grep '"tag_name"' \
    | sed -E 's/.*"tag_name": *"([^"]+)".*/  \1/'
  exit 0
fi

# --- resolve the release JSON -----------------------------------------------------
if [ -n "$VERSION" ]; then
  RELEASE_URL="${API}/releases/tags/${VERSION}"
else
  RELEASE_URL="${API}/releases/latest"
fi

log "Looking up release…"
JSON="$(curl -fsSL "$RELEASE_URL")" || die "could not fetch release info (is the tag correct?)"

TAG="$(printf '%s' "$JSON" | grep -m1 '"tag_name"' | sed -E 's/.*"tag_name": *"([^"]+)".*/\1/')"
[ -n "$TAG" ] || die "could not determine the release tag."

APK_URL="$(printf '%s' "$JSON" \
  | grep -o '"browser_download_url": *"[^"]*\.apk"' \
  | head -n1 \
  | sed -E 's/.*"(https[^"]+\.apk)".*/\1/')"
[ -n "$APK_URL" ] || die "no APK asset found in release ${TAG}."

SUMS_URL="$(printf '%s' "$JSON" \
  | grep -o '"browser_download_url": *"[^"]*SHA256SUMS"' \
  | head -n1 \
  | sed -E 's/.*"(https[^"]+SHA256SUMS)".*/\1/' || true)"

# --- download ---------------------------------------------------------------------
WORKDIR="$(mktemp -d)"
cleanup() { [ "$KEEP" -eq 1 ] || rm -rf "$WORKDIR"; }
trap cleanup EXIT

APK_NAME="$(basename "$APK_URL")"
APK_PATH="${WORKDIR}/${APK_NAME}"

log "Downloading ${APK_NAME} (${TAG})…"
curl -fsSL -o "$APK_PATH" "$APK_URL" || die "download failed."

# --- verify checksum (best effort) ------------------------------------------------
if [ -n "${SUMS_URL:-}" ] && command -v sha256sum >/dev/null 2>&1; then
  log "Verifying checksum…"
  curl -fsSL -o "${WORKDIR}/SHA256SUMS" "$SUMS_URL" || die "could not download SHA256SUMS."
  EXPECTED="$(grep -F "$APK_NAME" "${WORKDIR}/SHA256SUMS" | awk '{print $1}' | head -n1)"
  if [ -n "$EXPECTED" ]; then
    ACTUAL="$(sha256sum "$APK_PATH" | awk '{print $1}')"
    [ "$EXPECTED" = "$ACTUAL" ] || die "checksum mismatch — refusing to install."
    log "Checksum OK."
  else
    err "no checksum entry for ${APK_NAME}; skipping verification."
  fi
fi

if [ "$APK_ONLY" -eq 1 ]; then
  KEEP=1
  log "Downloaded to: ${APK_PATH}"
  exit 0
fi

# --- locate adb -------------------------------------------------------------------
ADB="$(command -v adb || true)"
if [ -z "$ADB" ]; then
  for sdk in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}"; do
    [ -n "$sdk" ] && [ -x "$sdk/platform-tools/adb" ] && ADB="$sdk/platform-tools/adb" && break
  done
fi
[ -n "$ADB" ] || die "adb not found. Install Android platform-tools, or use --apk-only and install the APK manually."

ADB_ARGS=()
[ -n "$DEVICE" ] && ADB_ARGS=(-s "$DEVICE")

if ! "$ADB" "${ADB_ARGS[@]}" get-state >/dev/null 2>&1; then
  die "no device connected. Enable USB debugging and run \`adb devices\` to check."
fi

log "Installing on device..."
"$ADB" "${ADB_ARGS[@]}" install -r "$APK_PATH"

log "Done. Open Spots from your app drawer to get started."
log 'Note: grant "Display over other apps" when prompted.'
