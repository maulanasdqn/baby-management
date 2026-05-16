# Baby Vault — Family Milestone Vault

A privacy-first Android application for securely logging family milestones, tracking baby growth data, and storing media — **entirely offline, entirely encrypted, zero cloud dependency**.

All sensitive data is encrypted at rest using ChaCha20-Poly1305 inside a Rust native library before it ever touches the filesystem. The Android app never sees plaintext on disk.

---

## How It Works

### The Big Picture

```
┌─────────────────────────────────────────────────────┐
│              Android App (Kotlin)                   │
│                                                     │
│  BiometricPrompt → Android Keystore → Master Key   │
│                                                     │
│  Compose UI → ViewModel → UseCase → Repository     │
└─────────────────────┬───────────────────────────────┘
                      │  UniFFI (auto-generated Kotlin bindings)
                      ▼
┌─────────────────────────────────────────────────────┐
│              Rust Core (.so / NDK)                  │
│                                                     │
│  VaultEngine (composition root)                     │
│    ├── SQLite via rusqlite (local DB)               │
│    ├── ChaCha20-Poly1305 (AEAD encryption)          │
│    └── Use-cases (milestone / growth / media)       │
└─────────────────────────────────────────────────────┘
```

### Key Security Flow

1. **First launch** — a 32-byte random master key is generated inside Rust, immediately wrapped by the Android Keystore (AES-256-GCM), and the *wrapped* blob is stored in DataStore. The raw key never touches disk.
2. **Every subsequent launch** — `BiometricPrompt` triggers unwrapping via the Keystore. The raw key is passed to `VaultEngine.unlock()` in Rust, held in a `Mutex<Zeroizing<Vec<u8>>>`, and wiped from memory on drop.
3. **All writes** — media bytes are encrypted by the Rust crypto engine before being written to the filesystem. Milestone/growth text fields are stored in an encrypted SQLite database (row-level).
4. **All reads** — decryption happens inside Rust, plaintext is returned only to the in-memory Compose UI.

---

## Architecture

The monorepo is split into two sub-projects that mirror each other's clean architecture layers.

```
baby-management/
├── core/          Rust NDK library (cdylib)
├── android/       Kotlin + Jetpack Compose app
└── docs/          Planning and specs
```

### `core/` — Rust (Clean Architecture)

Follows the same layer pattern as a backend service, with `presentation/` replaced by an FFI adapter instead of HTTP handlers.

```
core/crates/vault/src/
├── domain/               Pure types + repository/port TRAITS (no I/O)
│   ├── milestone/        entity.rs  repository.rs  errors.rs
│   ├── growth/           entity.rs  repository.rs  errors.rs
│   ├── media/            entity.rs  repository.rs  errors.rs
│   └── vault/            ports.rs (CryptoEngine)   errors.rs
│
├── application/          Business logic — use-cases generic over ports
│   ├── milestone/        create · list · detail · delete
│   ├── growth/           log · list_by_range
│   ├── media/            store_encrypted · read_decrypted · list_metadata
│   └── vault/            init_master_key · unlock
│
├── infrastructure/       Concrete adapters (tech details live here only)
│   ├── repository/       SqliteMilestoneRepository  SqliteGrowthRepository
│   │   └── migrations/   0001_init.sql
│   └── crypto/           ChaCha20Engine (impl CryptoEngine)
│
└── presentation/         FFI adapter — the only layer Kotlin touches
    ├── engine.rs         VaultEngine (#[derive(uniffi::Object)]) — composition root
    ├── dto.rs            #[derive(uniffi::Record)] — Kotlin-safe data classes
    ├── error.rs          #[derive(uniffi::Error)]  — FfiError enum
    └── mappers.rs        domain entity ↔ FFI DTO
```

**Conventions inherited from production Rust backends:**
- Traits named `XxxRepository` / `XxxEngine`; impls prefixed by tech (`SqliteMilestoneRepository`, `ChaCha20Engine`)
- Per-layer error enums with manual `Display` + `Error` impls and `From` cascades — no `thiserror`
- Use-cases are generic structs: `CreateMilestoneUseCase<R: MilestoneRepository>`
- `VaultEngine::new()` is the single composition root, constructed once by Kotlin and held as a `@Singleton`
- Blanket `impl<T: XxxRepository> XxxRepository for &T` so engine methods pass `&self.repo` to use-cases without extra cloning

### `android/` — Kotlin (Clean Architecture)

Single `:app` Gradle module. Layers are enforced by package, not by Gradle module boundaries (mirrors the `atveti-android` pattern).

```
app/src/main/java/com/babyvault/android/
├── core/native/          VaultEngineProvider — wraps the UniFFI VaultEngine @Singleton
│
├── domain/               Pure Kotlin, zero Android/UniFFI imports
│   ├── model/            Milestone  GrowthLog  MediaItem
│   ├── repo/             Repository interfaces (MilestoneRepository, etc.)
│   └── usecase/          Invoke-operator use-cases injected into ViewModels
│
├── data/                 Implementations of domain interfaces
│   ├── engine/           Engine*Repository — delegates to VaultEngine via UniFFI
│   ├── mapper/           UniFFI DTO ↔ domain model (extension functions)
│   └── local/            KeystoreMasterKeyStore — Android Keystore AES-GCM wrap/unwrap
│
├── di/                   Hilt modules
│   ├── EngineModule       @Provides VaultEngine (initializes db path + storage dir)
│   ├── RepositoryModule   @Binds Engine* → domain interfaces
│   └── DataStoreModule    @Provides DataStore<Preferences>
│
└── presentation/
    ├── theme/             Material3 color, typography, theme
    ├── nav/               Routes + AppNavHost (Navigation-Compose)
    └── screens/
        ├── splash/        Checks DataStore for wrapped key → routes to unlock or home
        ├── unlock/        BiometricPrompt → unwrap key → VaultEngine.unlock()
        └── home/          Lists recent milestones + shows engine version
```

**MVVM pattern:** ViewModels expose `StateFlow<State>` via `stateIn(viewModelScope, ...)`. Screens collect with `collectAsState()`. No MVI reducers.

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| UI | Kotlin + Jetpack Compose + Material3 | Declarative, type-safe, native performance |
| DI | Hilt (KSP) | Compile-time verified, Android lifecycle-aware |
| Navigation | Navigation-Compose | Type-safe routes, backstack handled |
| Auth | `androidx.biometric` + Android Keystore | Hardware-backed key storage, no biometric data leaves device |
| Persistence (prefs) | DataStore Preferences | Replaces SharedPreferences, coroutine-native |
| Core logic | Rust (Android NDK, cdylib) | Memory safety, zero-cost crypto, no GC pauses |
| FFI bridge | UniFFI 0.28 (proc-macro) | Auto-generates type-safe Kotlin bindings from Rust |
| Database | SQLite via `rusqlite` (bundled) | Local-first, no network, runs fully inside Rust |
| Encryption | `chacha20poly1305` crate | Fast authenticated encryption, safe on mobile CPUs |
| Key material | `zeroize` crate | Wipes key bytes from memory on drop |

---

## Project Structure

```
baby-management/
├── core/
│   ├── Cargo.toml              Workspace root (resolver = "2")
│   ├── rust-toolchain.toml     Pins stable + Android targets
│   ├── build.sh                One-shot: cargo ndk → copy .so → uniffi-bindgen → vault.kt
│   └── crates/
│       ├── shared/             RepositoryError, logger (shared across crates)
│       └── vault/              The cdylib + uniffi-bindgen binary
│
├── android/
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   ├── gradle/libs.versions.toml
│   ├── libs/vault/
│   │   ├── jniLibs/            ← populated by build.sh (.so files)
│   │   └── kotlin/             ← populated by build.sh (vault.kt bindings)
│   └── app/
│       ├── build.gradle.kts    sourceSets wired to ../libs/vault/
│       └── src/main/
│           ├── AndroidManifest.xml
│           └── java/com/babyvault/android/
│
└── docs/
    └── planning.md             Original architecture spec
```

---

## Getting Started

### Prerequisites

| Tool | Version | Install |
|---|---|---|
| Rust | stable | `rustup install stable` |
| cargo-ndk | 4.x | `cargo install cargo-ndk` |
| Android NDK | r27c (27.2.12479018) | Android Studio → SDK Manager → SDK Tools → NDK |
| Android Studio | Hedgehog+ | For Gradle sync and emulator |

### 1. Set NDK path

```bash
# Linux / macOS
export ANDROID_NDK_HOME=$HOME/Library/Android/sdk/ndk/27.2.12479018

# Windows (PowerShell)
$env:ANDROID_NDK_HOME = "$env:LOCALAPPDATA\Android\Sdk\ndk\android-ndk-r27c"
```

### 2. Add Android targets to Rust

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

### 3. Build the Rust core

```bash
cd core
bash build.sh
```

This compiles `libvault.so` for all four ABIs and runs `uniffi-bindgen` to generate `android/libs/vault/kotlin/com/babyvault/core/vault.kt`.

### 4. Open in Android Studio

Open the `android/` folder in Android Studio. Gradle sync will automatically pick up the `.so` files and the generated Kotlin bindings via the `sourceSets` configuration in `app/build.gradle.kts`.

### 5. Run

Select a device or emulator (API 26+) and hit Run. On first launch the vault initializes automatically; subsequent launches require biometric authentication.

---

## Development Workflow

### Adding a new feature slice

Every new feature follows the same pattern across both repos:

**Rust core (`core/crates/vault/src/`):**
```
domain/<slice>/
  entity.rs       — pure data structs
  repository.rs   — trait XxxRepository + blanket &T impl
  errors.rs       — domain validation errors

application/<slice>/
  error.rs        — XxxError enum with From<RepositoryError>
  use_cases/
    create.rs     — CreateXxxUseCase<R: XxxRepository>
    list.rs       — ...

infrastructure/repository/
  sqlite_xxx.rs   — SqliteXxxRepository (impl XxxRepository)
  migrations/     — add columns to 0001_init.sql or new migration file

presentation/
  dto.rs          — add #[derive(uniffi::Record)] XxxDto
  mappers.rs      — add xxx_to_dto()
  engine.rs       — expose methods on VaultEngine via #[uniffi::export]
```

**Kotlin (`android/app/src/main/java/com/babyvault/android/`):**
```
domain/model/Xxx.kt             — data class
domain/repo/Repositories.kt     — add interface XxxRepository
domain/usecase/XxxUseCases.kt   — invoke-operator use-cases
data/engine/EngineXxxRepository — delegates to VaultEngine
data/mapper/XxxMapper.kt        — XxxDto.toDomain()
di/RepositoryModule.kt          — @Binds EngineXxx → XxxRepository
presentation/screens/xxx/       — XxxScreen + XxxViewModel
```

Then re-run `bash core/build.sh` to regenerate bindings.

### Running Rust tests

```bash
cd core
cargo test
```

Tests in `application/*/use_cases/` use in-memory fake repositories — no Android device or NDK needed.

---

## Roadmap

| Phase | Status | Scope |
|---|---|---|
| Phase 1 — Plumbing | Done | Rust workspace, UniFFI bridge, Android project, build pipeline |
| Phase 2 — Security & DB | Done | ChaCha20 crypto, rusqlite schema, BiometricPrompt key lifecycle |
| Phase 3 — UI & Features | Done | Bottom nav (Timeline / Growth / Media), milestone FAB sheet, growth log form, PhotoPicker encrypted media vault |
| Phase 4 — Self-hosted Sync | Planned | Rust `reqwest` sync engine, chunked upload, Android WorkManager |

---

## Security Notes

- The master key is **never stored in plaintext**. It lives unwrapped only in RAM, inside a `Mutex<Zeroizing<Vec<u8>>>` that wipes on drop.
- The Android Keystore wrapping key is **hardware-backed** on devices with a Trusted Execution Environment (TEE).
- Media files on disk are pure ciphertext — reading the filesystem without the key yields nothing.
- `rusqlite` is compiled with the `bundled` feature — SQLite is statically linked into the `.so`, so no system SQLite (and its version quirks) is used.
- This app requests no internet permission. There is no analytics, no telemetry, no third-party SDK.
