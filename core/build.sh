#!/usr/bin/env bash
set -euo pipefail

ANDROID_REPO="../android"
VAULT_JNILIBS="$ANDROID_REPO/libs/vault/jniLibs"
VAULT_KOTLIN="$ANDROID_REPO/libs/vault/kotlin"
INFERENCE_JNILIBS="$ANDROID_REPO/libs/inference/jniLibs"
INFERENCE_KOTLIN="$ANDROID_REPO/libs/inference/kotlin"

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

echo "==> Building inference crate..."
cargo ndk \
  -t aarch64-linux-android \
  -t armv7-linux-androideabi \
  -t i686-linux-android \
  -t x86_64-linux-android \
  -o "$INFERENCE_JNILIBS" \
  build --release -p inference

echo "==> Generating inference UniFFI Kotlin bindings..."
cargo run --bin uniffi-bindgen-inference -- generate \
  --library "target/aarch64-linux-android/release/libinference.so" \
  --language kotlin \
  --out-dir "$INFERENCE_KOTLIN"

echo "==> Done."
echo "    vault .so       → $VAULT_JNILIBS"
echo "    vault Kotlin    → $VAULT_KOTLIN"
echo "    inference .so   → $INFERENCE_JNILIBS"
echo "    inference Kotlin → $INFERENCE_KOTLIN"
