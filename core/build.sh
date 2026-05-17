#!/usr/bin/env bash
set -euo pipefail

ANDROID_REPO="../android"
VAULT_JNILIBS="$ANDROID_REPO/libs/vault/jniLibs"
VAULT_KOTLIN="$ANDROID_REPO/libs/vault/kotlin"

echo "==> Building vault crate..."
cargo ndk \
  -t aarch64-linux-android \
  -t armv7-linux-androideabi \
  -t i686-linux-android \
  -t x86_64-linux-android \
  -o "$VAULT_JNILIBS" \
  build --release -p vault

echo "==> Generating vault UniFFI Kotlin bindings..."
cargo run --bin uniffi-bindgen -- generate \
  --library "target/aarch64-linux-android/release/libvault.so" \
  --language kotlin \
  --out-dir "$VAULT_KOTLIN"

echo "==> Done."
echo "    vault .so    → $VAULT_JNILIBS"
echo "    vault Kotlin → $VAULT_KOTLIN"
