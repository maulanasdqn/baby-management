# Baby Vault — Family Milestone Vault

A privacy-first Android application for tracking baby activities, milestones, and growth — **entirely offline, entirely encrypted, zero cloud dependency**.

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
│    └── Use-cases (milestone / growth / media /      │
│                   feed / sleep / diaper)            │
└─────────────────────────────────────────────────────┘
```

### Key Security Flow

1. **First launch** — a 32-byte random master key is generated inside Rust, immediately wrapped by the Android Keystore (AES-256-GCM), and the *wrapped* blob is stored in DataStore. The raw key never touches disk.
2. **Every subsequent launch** — `BiometricPrompt` triggers unwrapping via the Keystore. The raw key is passed to `VaultEngine.unlock()` in Rust, held in a `Mutex<Zeroizing<Vec<u8>>>`, and wiped from memory on drop.
3. **All writes** — media bytes are encrypted by the Rust crypto engine before being written to the filesystem. Text fields are stored in an encrypted SQLite database (row-level).
4. **All reads** — decryption happens inside Rust; plaintext is returned only to the in-memory Compose UI.

---

## Features

| Feature | Description |
|---|---|
| **Baby Profile** | Set baby's name and date of birth on first launch; age label auto-calculated |
| **Feed Tracking** | Log breast / bottle / solid feeds with duration, side (Left/Right/Both), amount, and notes |
| **Feed Timer** | Live stopwatch for timed breast-feeding sessions; saves as a feed log on stop |
| **Sleep Tracking** | Log sleep sessions by hours + minutes |
| **Diaper Tracking** | Log diaper changes (Wet / Dirty / Both) with optional notes |
| **Milestone Vault** | Record developmental milestones with title, description, and date |
| **Growth Log** | Track height, weight, and head circumference over time |
| **Media Vault** | Store photos/videos encrypted at rest via Android PhotoPicker |
| **Today Summary** | Home screen strip showing today's feed count, total sleep, and diaper changes |
| **History** | Filterable activity log (Today / 7 days / 30 days) across all event types |
| **Insights** | Weekly stats — avg sleep, feed breakdown (Breast/Bottle/Solid), diaper totals |
| **Self-hosted Sync** | Optional: periodic WorkManager sync pushing encrypted blobs to a self-hosted server |
| **Biometric Auth** | Hardware-backed unlock; vault is inaccessible without the enrolled biometric |

---

## Architecture

The monorepo contains two sub-projects that mirror each other's clean architecture layers.

```
baby-management/
├── core/          Rust NDK library (cdylib)
├── android/       Kotlin + Jetpack Compose app
└── README.md
```

### `core/` — Rust (Clean Architecture)

Follows the same layer pattern as a backend service, with `presentation/` replaced by an FFI adapter instead of HTTP handlers.

```
core/
├── Cargo.toml              Workspace root (members: apps/config, apps/vault)
├── rust-toolchain.toml     Pins stable + Android targets
├── build.sh                One-shot: cargo ndk → copy .so → uniffi-bindgen → vault.kt
└── apps/
    ├── config/             RepositoryError, logger (shared across crates)
    └── vault/              The cdylib + uniffi-bindgen binary
        └── src/
            ├── domain/               Pure types + repository/port TRAITS (no I/O)
            │   ├── milestone/        entity  repository  errors
            │   ├── growth/           entity  repository  errors
            │   ├── media/            entity  repository  errors
            │   ├── feed/             entity  repository  errors
            │   ├── sleep/            entity  repository  errors
            │   ├── diaper/           entity  repository  errors
            │   └── vault/            ports (CryptoEngine)  errors
            │
            ├── application/          Business logic — use-cases generic over ports
            │   ├── milestone/        create · list · detail · delete
            │   ├── growth/           log · list_by_range
            │   ├── media/            store_encrypted · read_decrypted · list_metadata
            │   ├── feed/             log · list_by_range · delete
            │   ├── sleep/            log · list_by_range · delete
            │   ├── diaper/           log · list_by_range · delete
            │   └── vault/            init_master_key · unlock
            │
            ├── infrastructure/       Concrete adapters (tech details live here only)
            │   ├── repository/       Sqlite*Repository for all slices
            │   │   └── migrations/   0001_init.sql · 0002_activities.sql
            │   └── crypto/           ChaCha20Engine (impl CryptoEngine)
            │
            └── presentation/         FFI adapter — the only layer Kotlin touches
                ├── engine.rs         VaultEngine (#[derive(uniffi::Object)]) — composition root
                ├── dto.rs            #[derive(uniffi::Record/Enum)] — Kotlin-safe types
                ├── error.rs          #[derive(uniffi::Error)]  — FfiError enum
                └── mappers.rs        domain entity ↔ FFI DTO
```

**Key conventions:**
- Traits named `XxxRepository` / `XxxEngine`; impls prefixed by tech (`SqliteFeedRepository`, `ChaCha20Engine`)
- Per-layer error enums with manual `Display` + `Error` impls and `From` cascades — no `thiserror`
- Use-cases are generic structs: `LogFeedUseCase<R: FeedRepository>`
- `VaultEngine::new()` is the single composition root, constructed once by Kotlin and held as a `@Singleton`
- Blanket `impl<T: XxxRepository> XxxRepository for &T` so engine methods pass `&self.repo` without extra cloning

### `android/` — Kotlin (Clean Architecture)

Single `:app` Gradle module. Layers are enforced by package, not by Gradle module boundaries.

```
app/src/main/java/com/babyvault/android/
├── core/native/          VaultEngineProvider — wraps the UniFFI VaultEngine @Singleton
│
├── domain/               Pure Kotlin, zero Android/UniFFI imports
│   ├── model/            Milestone  GrowthLog  MediaItem  FeedLog  SleepLog  DiaperLog
│   ├── repo/             Repository interfaces for all slices
│   └── usecase/          Invoke-operator use-cases injected into ViewModels
│
├── data/                 Implementations of domain interfaces
│   ├── engine/           Engine*Repository — delegates to VaultEngine via UniFFI
│   ├── mapper/           UniFFI DTO ↔ domain model (extension functions)
│   └── local/            KeystoreMasterKeyStore · BabyProfileStore (DataStore)
│
├── di/                   Hilt modules
│   ├── EngineModule       @Provides VaultEngine (initializes db path + storage dir)
│   ├── RepositoryModule   @Binds Engine* → domain interfaces
│   └── DataStoreModule    @Provides DataStore<Preferences>
│
└── presentation/
    ├── theme/             Teal pastel color scheme, Material3, Material You (API 31+)
    ├── nav/               Routes + AppNavHost (Navigation-Compose, edge-to-edge)
    ├── ui/                BottomNavBar (Home / History / Insights / Settings)
    └── screens/
        ├── splash/        Vault init check → routes to unlock / profile setup / home
        ├── unlock/        BiometricPrompt → unwrap key → VaultEngine.unlock()
        ├── profile/       Baby name + date-of-birth setup (first launch only)
        ├── home/          Teal gradient header, today's summary strip, quick-log cards
        ├── log/           LogFeedScreen · LogSleepScreen · LogDiaperScreen · FeedTimerScreen
        ├── history/       Filterable activity log (Today / 7 days / 30 days)
        ├── insights/      Weekly stats — sleep avg, feed breakdown, diaper totals
        ├── timeline/      Milestone list
        ├── growth/        Growth log form and chart
        ├── media/         Encrypted photo/video vault
        └── settings/      Sync server configuration
```

**MVVM pattern:** ViewModels expose `StateFlow<State>` via `stateIn(viewModelScope, ...)`. One-shot navigation events use `Channel<Unit>` + `receiveAsFlow()` consumed via `LaunchedEffect(Unit) { collect { } }` to avoid re-triggering on recomposition.

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| UI | Kotlin + Jetpack Compose + Material3 | Declarative, type-safe, native performance |
| Color system | Material You (`dynamicLightColorScheme` API 31+) with teal pastel fallback | Adapts to system wallpaper on modern devices |
| DI | Hilt (KSP) | Compile-time verified, Android lifecycle-aware |
| Navigation | Navigation-Compose | Type-safe routes, backstack handled |
| Auth | `androidx.biometric` + Android Keystore | Hardware-backed key storage, no biometric data leaves device |
| Persistence (prefs) | DataStore Preferences | Stores wrapped master key + baby profile |
| Core logic | Rust (Android NDK, cdylib) | Memory safety, zero-cost crypto, no GC pauses |
| FFI bridge | UniFFI 0.28 (proc-macro) | Auto-generates type-safe Kotlin bindings from Rust |
| Database | SQLite via `rusqlite` (bundled) | Local-first, no network, runs fully inside Rust |
| Encryption | `chacha20poly1305` crate | Fast authenticated encryption, safe on mobile CPUs |
| Key material | `zeroize` crate | Wipes key bytes from memory on drop |
| Background sync | WorkManager + HiltWorker | 15-min periodic encrypted push to self-hosted server |

---

## Project Structure

```
baby-management/
├── core/
│   ├── Cargo.toml              Workspace root (resolver = "2")
│   ├── rust-toolchain.toml     Pins stable + Android targets
│   ├── build.sh                One-shot: cargo ndk → copy .so → uniffi-bindgen → vault.kt
│   └── apps/
│       ├── config/             RepositoryError, logger (shared across crates)
│       └── vault/              The cdylib + uniffi-bindgen binary
│
├── android/
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   ├── gradle/libs.versions.toml
│   ├── libs/vault/
│   │   ├── jniLibs/            ← populated by build.sh (.so files, 4 ABIs)
│   │   └── kotlin/             ← populated by build.sh (vault.kt bindings)
│   └── app/
│       ├── build.gradle.kts    sourceSets wired to libs/vault/
│       └── src/main/
│           ├── AndroidManifest.xml
│           └── java/com/babyvault/android/
│
└── README.md
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
| JDK | bundled with Android Studio (JBR) | Used via `JAVA_HOME` |

### 1. Set environment variables

```powershell
# Windows (PowerShell)
$env:ANDROID_NDK_HOME = "$env:LOCALAPPDATA\Android\Sdk\ndk\android-ndk-r27c"
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
```

```bash
# macOS / Linux
export ANDROID_NDK_HOME=$HOME/Library/Android/sdk/ndk/27.2.12479018
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

### 4. Build and install the Android app

```powershell
cd android
./gradlew installDebug
```

Or open the `android/` folder in Android Studio, sync Gradle, and hit Run.

### 5. First launch

On first launch the app will:
1. Initialize the encrypted SQLite vault
2. Prompt you to set up a baby profile (name + date of birth)
3. On subsequent launches, biometric authentication is required to unlock the vault

---

## Development Workflow

### Adding a new activity slice

Every new tracked activity (e.g. a "medication" log) follows the same pattern:

**Rust core (`core/apps/vault/src/`):**
```
domain/<slice>/
  entity.rs       — pure data structs (NewXxx, Xxx with Uuid + timestamps)
  repository.rs   — trait XxxRepository + blanket &T impl
  errors.rs       — domain-level error enum

application/<slice>/
  error.rs        — XxxError with From<RepositoryError>
  use_cases/
    log.rs        — LogXxxUseCase<R: XxxRepository>
    list_by_range.rs
    delete.rs

infrastructure/repository/
  sqlite_xxx.rs   — SqliteXxxRepository (impl XxxRepository)
  migrations/     — add table to 0002_activities.sql or a new migration file

presentation/
  dto.rs          — add #[derive(uniffi::Record)] XxxDto
  mappers.rs      — add xxx_to_dto()
  engine.rs       — expose log_xxx / list_xxx_by_range / delete_xxx via #[uniffi::export]
```

**Kotlin (`android/app/src/main/java/com/babyvault/android/`):**
```
domain/model/Xxx.kt              — data class
domain/repo/Repositories.kt      — add interface XxxRepository
domain/usecase/XxxUseCases.kt    — invoke-operator use-cases
data/engine/EngineXxxRepository  — delegates to VaultEngine
data/mapper/XxxMapper.kt         — XxxDto.toDomain()
di/RepositoryModule.kt           — @Binds EngineXxx → XxxRepository
presentation/screens/log/
  LogXxxScreen.kt                — rounded OutlinedTextFields, pastel icon header
  LogXxxViewModel.kt             — Channel<Unit> for one-shot save event
```

Then re-run `bash core/build.sh` to regenerate bindings before building the Android app.

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
| Phase 2 — Security & DB | Done | ChaCha20 crypto, rusqlite schema + migrations, BiometricPrompt key lifecycle |
| Phase 3 — Core Tracking | Done | Feed / Sleep / Diaper logs, Feed timer, Milestone vault, Growth log, Media vault |
| Phase 4 — UI & UX | Done | Baby profile setup, Today summary, History, Insights, teal pastel theme, pastel filled icons, edge-to-edge system bar blending |
| Phase 5 — Self-hosted Sync | Done | Rust `ureq` push engine, SQLite sync_state tracking, WorkManager 15-min periodic sync, Sync Settings screen |

---

## Security Notes

- The master key is **never stored in plaintext**. It lives unwrapped only in RAM, inside a `Mutex<Zeroizing<Vec<u8>>>` that wipes on drop.
- The Android Keystore wrapping key is **hardware-backed** on devices with a Trusted Execution Environment (TEE).
- Media files on disk are pure ciphertext — reading the filesystem without the key yields nothing.
- `rusqlite` is compiled with the `bundled` feature — SQLite is statically linked into the `.so`, avoiding system SQLite version quirks.
- This app requests no internet permission by default. There is no analytics, no telemetry, no third-party SDK.
- The self-hosted sync feature pushes only encrypted blobs — the server never receives plaintext data or the encryption key.
