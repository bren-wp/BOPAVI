#!/usr/bin/env bash
# Validate simulator images *before* Android-emulator-runner starts. Retry clean
# installs, then prefer the smaller AOSP image if Google's API36 zip is broken.
# Never silently pass without a real bootable system.img.
set -euo pipefail
api="${1:?Android API level required}"
case "$api" in 35|36) ;; *) echo "::error::Unsupported emulator API $api" >&2; exit 2;; esac
sdk_root="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}}"
sdkmanager="$sdk_root/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$sdkmanager" ]; then
  sdkmanager="$(find "$sdk_root/cmdline-tools" -type f -name sdkmanager | sort -V | tail -n 1)"
fi
test -x "$sdkmanager" || { echo "::error::Android SDK Manager unavailable" >&2; exit 2; }

ready() {
  local folder="$1"
  test -s "$folder/source.properties" &&
  test -s "$folder/system.img" &&
  test "$(stat -c '%s' "$folder/system.img")" -ge 104857600
}
announce() {
  local target="$1"
  echo "PASS: API $api $target image verified"
  if [ -n "${GITHUB_OUTPUT:-}" ]; then
    echo "image_target=$target" >> "$GITHUB_OUTPUT"
  fi
}

# API35 keeps the tested Google APIs image; API36 can use stock AOSP if the
# hosted runner temporarily serves an invalid Google APIs archive.
targets=(google_apis)
if [ "$api" = 36 ]; then targets+=(default); fi
for target in "${targets[@]}"; do
  image="$sdk_root/system-images/android-$api/$target/x86_64"
  package="system-images;android-$api;$target;x86_64"
  if ready "$image"; then announce "$target"; exit 0; fi
  for attempt in 1 2; do
    echo "Preparing API $api $target (verified attempt $attempt/2)"
    rm -rf -- "$image"
    if "$sdkmanager" --sdk_root="$sdk_root" --install "$package" && ready "$image"; then
      announce "$target"
      exit 0
    fi
    echo "::warning::Incomplete API $api $target emulator image (attempt $attempt)"
    if [ "$attempt" -eq 1 ]; then sleep 6; fi
  done
done
echo "::error::No verified Android API $api system image available after Google APIs/AOSP fallback"
exit 1
