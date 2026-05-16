#!/usr/bin/env bash
set -euo pipefail

ANDROID_REPO="../android"
JNILIBS_DIR="$ANDROID_REPO/libs/vault/jniLibs"
KOTLIN_DIR="$ANDROID_REPO/libs/vault/kotlin"

echo "==> Building Rust vault crate for Android targets..."
cargo ndk \
  -t aarch64-linux-android \
  -t armv7-linux-androideabi \
  -t i686-linux-android \
  -t x86_64-linux-android \
  -o "$JNILIBS_DIR" \
  build --release -p vault

echo "==> Generating UniFFI Kotlin bindings..."
cargo run --bin uniffi-bindgen -- generate \
  --library "target/aarch64-linux-android/release/libvault.so" \
  --language kotlin \
  --out-dir "$KOTLIN_DIR"

echo "==> Done. .so files in $JNILIBS_DIR, Kotlin bindings in $KOTLIN_DIR"
