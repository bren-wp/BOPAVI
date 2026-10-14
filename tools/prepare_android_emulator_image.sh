#!/usr/bin/env bash
# Fail closed on corrupt Google APIs images: sdkmanager may return zero even
# after "ZipFile unknown archive". Never pass a broken AVD to the emulator.
set -euo pipefail
api="${1:?Android API level required}"
case "$api" in 35|36) ;; *) echo "::error::Unsupported emulator API $api" >&2; exit 2;; esac
sdk_root="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}}"
sdkmanager="$sdk_root/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$sdkmanager" ]; then
  sdkmanager="$(find "$sdk_root/cmdline-tools" -type f -name sdkmanager | sort -V | tail -n 1)"
fi
test -x "$sdkmanager" || { echo "::error::Android SDK Manager unavailable" >&2; exit 2; }
image="$sdk_root/system-images/android-$api/google_apis/x86_64"
package="system-images;android-$api;google_apis;x86_64"
is_ready() {
  test -s "$image/source.properties" &&
  test -s "$image/system.img" &&
  test "$(stat -c '%s' "$image/system.img")" -ge 104857600
}
if is_ready; then
  echo "PASS: preinstalled API $api Google APIs system image verified"
  exit 0
fi
for attempt in 1 2 3; do
  echo "Preparing API $api Google APIs emulator image (attempt $attempt/3)"
  # Only remove the currently selected disposable emulator image.
  rm -rf -- "$image"
  if "$sdkmanager" --sdk_root="$sdk_root" --install "$package"; then
    if is_ready; then
      echo "PASS: API $api system image extracted and verified"
      exit 0
    fi
  fi
  echo "::warning::Corrupt or incomplete Android API $api system image; retrying clean extraction"
  if [ "$attempt" -lt 3 ]; then sleep $((attempt * 6)); fi
done
echo "::error::Android API $api system image unavailable after 3 verified installs"
exit 1
