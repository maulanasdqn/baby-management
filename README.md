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
└──────────────────────┬──────────────────────────────┘
                       │  UniFFI (auto-generated Kotlin bindings)
           ┌───────────┴────────────┐
           ▼                        ▼
┌──────────────────┐   ┌────────────────────────────────┐
│   Rust: vault    │   │    Rust: inference             │
│                  │   │                                │
│  VaultEngine     │   │  InferenceEngine               │
│  ├── rusqlite    │   │  ├── NNAPI (API 31+)           │
│  ├── ChaCha20    │   │  │   GPT-2 on Hexagon DSP /    │
│  └── Use-cases   │   │  │   Adreno GPU                │
│                  │   │  └── Burn NdArray (fallback)   │
└──────────────────┘   └────────────────────────────────┘
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
| **Baby AI Chat** | On-device GPT-2 assistant; runs fully offline via NNAPI (Hexagon DSP / Adreno GPU) with CPU fallback |
| **Self-hosted Sync** | Optional: periodic WorkManager sync pushing encrypted blobs to a self-hosted server |
| **Biometric Auth** | Hardware-backed unlock; vault is inaccessible without the enrolled biometric |

---

## Architecture

The monorepo contains two sub-projects that mirror each other's clean architecture layers.

```
baby-management/
├── core/          Rust NDK libraries (vault cdylib + inference cdylib)
├── android/       Kotlin + Jetpack Compose app
└── README.md
```

### `core/` — Rust (Clean Architecture)

Follows the same layer pattern as a backend service, with `presentation/` replaced by an FFI adapter instead of HTTP handlers.

```
core/
├── Cargo.toml              Workspace root (resolver = "2")
├── rust-toolchain.toml     Pins stable + Android targets
├── build.sh                One-shot: cargo ndk → copy .so → uniffi-bindgen → *.kt
├── crates/
│   └── nnapi/              Raw Android Neural Networks API FFI + safe wrappers
│       ├── src/sys.rs      Opaque types, op constants, extern "C" declarations
│       └── src/lib.rs      NnapiModel / NnapiCompilation / NnapiExecution, GraphBuilder
└── apps/
    ├── config/             RepositoryError, logger (shared across crates)
    ├── vault/              The main cdylib — encryption, DB, all tracking use-cases
    │   └── src/
    │       ├── domain/               Pure types + repository/port TRAITS (no I/O)
    │       │   ├── milestone/        entity  repository  errors
    │       │   ├── growth/           entity  repository  errors
    │       │   ├── media/            entity  repository  errors
    │       │   ├── feed/             entity  repository  errors
    │       │   ├── sleep/            entity  repository  errors
    │       │   ├── diaper/           entity  repository  errors
    │       │   └── vault/            ports (CryptoEngine)  errors
    │       │
    │       ├── application/          Business logic — use-cases generic over ports
    │       │   ├── milestone/        create · list · detail · delete
    │       │   ├── growth/           log · list_by_range
    │       │   ├── media/            store_encrypted · read_decrypted · list_metadata
    │       │   ├── feed/             log · list_by_range · delete
    │       │   ├── sleep/            log · list_by_range · delete
    │       │   ├── diaper/           log · list_by_range · delete
    │       │   └── vault/            init_master_key · unlock
    │       │
    │       ├── infrastructure/       Concrete adapters
    │       │   ├── repository/       Sqlite*Repository for all slices
    │       │   │   └── migrations/   0001_init.sql · 0002_activities.sql
    │       │   └── crypto/           ChaCha20Engine (impl CryptoEngine)
    │       │
    │       └── presentation/         FFI adapter — the only layer Kotlin touches
    │           ├── engine.rs         VaultEngine (#[derive(uniffi::Object)])
    │           ├── dto.rs            #[derive(uniffi::Record/Enum)]
    │           ├── error.rs          #[derive(uniffi::Error)]
    │           └── mappers.rs        domain entity ↔ FFI DTO
    │
    └── inference/          GPT-2 inference cdylib
        └── src/
            ├── engine.rs       Burn NdArray CPU backend (API < 31 / emulator fallback)
            ├── engine_nnapi.rs Full GPT-2 NNAPI DAG — attention, layer norm, GELU
            ├── ffi.rs          UniFFI exports; tries NNAPI first, falls back to Burn
            ├── loader.rs       SafeTensors weight loader + vocab/config JSON parsing
            ├── model.rs        GPT-2 config struct
            └── tokenizer.rs    BPE tokenizer
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
├── core/native/          VaultEngineProvider · InferenceEngineProvider (@Singleton)
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
│   ├── EngineModule       @Provides VaultEngine + InferenceEngine (singletons)
│   ├── RepositoryModule   @Binds Engine* → domain interfaces
│   └── DataStoreModule    @Provides DataStore<Preferences>
│
└── presentation/
    ├── theme/             Teal pastel color scheme, Material3, Material You (API 31+)
    ├── nav/               Routes + AppNavHost (Navigation-Compose, edge-to-edge)
    │                      Slide+fade transitions (280ms detail · 200ms bottom tabs)
    ├── ui/                BottomNavBar (Home / History / Insights / Chat / Settings)
    └── screens/
        ├── splash/        Vault init check → routes to unlock / profile setup / home
        ├── unlock/        BiometricPrompt → unwrap key → VaultEngine.unlock()
        ├── profile/       Baby name + date-of-birth setup (first launch only)
        ├── home/          Teal gradient header, today's summary strip, quick-log cards
        ├── log/           LogFeedScreen · LogSleepScreen · LogDiaperScreen · FeedTimerScreen
        ├── history/       Filterable activity log with Crossfade loading/empty/content states
        ├── insights/      Weekly stats — sleep avg, feed breakdown, diaper totals
        ├── timeline/      Milestone list
        ├── growth/        Growth log form and chart
        ├── media/         Encrypted photo/video vault
        ├── chat/          Baby AI chat — animated bubbles, TypingDots, NNAPI-powered
        └── settings/      Sync server configuration
```

**MVVM pattern:** ViewModels expose `StateFlow<State>` via `stateIn(viewModelScope, ...)`. All `UiState` data classes are annotated `@Immutable` to prevent spurious Compose recomposition. One-shot navigation events use `Channel<Unit>` + `receiveAsFlow()`.

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| UI | Kotlin + Jetpack Compose + Material3 | Declarative, type-safe, native performance |
| Animations | `AnimatedContent`, `AnimatedVisibility`, `Crossfade`, slide+fade nav | Smooth state transitions without boilerplate |
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
| On-device AI | Android NNAPI (API 31+) via raw Rust FFI | Runs GPT-2 on Hexagon DSP / Adreno GPU; zero model upload |
| AI fallback | Burn NdArray backend | CPU inference on devices below API 31 or emulators |
| Background sync | WorkManager + HiltWorker | 15-min periodic encrypted push to self-hosted server |
| Release size | R8 minify + `shrinkResources` + ABI splits | Strips dead code/resources; arm64-v8a + armeabi-v7a only |

---

## On-Device AI

The Baby AI chat screen runs a GPT-2 small (124M parameter) model entirely on-device via Android's Neural Networks API. No query or response ever leaves the device.

### NNAPI Graph

The static computation graph is compiled once at startup and routed by the Qualcomm driver to whichever accelerator is fastest:

- **Hexagon DSP** — lowest power, best for text workloads
- **Adreno GPU** — higher throughput for large batch sizes
- **CPU fallback** — always available if accelerators are busy

Key implementation details (`core/crates/nnapi/`, `core/apps/inference/`):

| Challenge | Solution |
|---|---|
| No layer-norm op in NNAPI | Composed from 8 primitives: MEAN → SUB → MUL → MEAN → SQRT → DIV → MUL → ADD |
| No GELU op in NNAPI | 9-op tanh approximation: x³ via MUL, tanh, 0.5·x·(1+tanh(...)) |
| GPT-2 Conv1D weights are `[in, out]` | Transposed to `[out, in]` at model load time for FULLY_CONNECTED |
| Attention QK^T | `BATCH_MATMUL` (op 102, API 31+) |
| Pre-API-31 devices / emulators | Auto-detects at startup, falls back to Burn NdArray CPU backend |

### Model Setup

Place a GPT-2 SafeTensors checkpoint and tokenizer in the app's files directory, then configure the path via the Model Setup screen. The model is never bundled in the APK.

---

## Project Structure

```
baby-management/
├── core/
│   ├── Cargo.toml              Workspace root (resolver = "2")
│   ├── rust-toolchain.toml     Pins stable + Android targets
│   ├── build.sh                One-shot: cargo ndk → copy .so → uniffi-bindgen → *.kt
│   ├── crates/
│   │   └── nnapi/              Raw NNAPI FFI + safe GraphBuilder
│   └── apps/
│       ├── config/             RepositoryError, logger
│       ├── vault/              Encryption + DB cdylib
│       └── inference/          GPT-2 inference cdylib (NNAPI + Burn)
│
├── android/
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   ├── gradle/libs.versions.toml
│   ├── libs/
│   │   ├── vault/
│   │   │   ├── jniLibs/        ← populated by build.sh (libvault.so, 4 ABIs)
│   │   │   └── kotlin/         ← populated by build.sh (vault.kt bindings)
│   │   └── inference/
│   │       ├── jniLibs/        ← populated by build.sh (libinference.so, 4 ABIs)
│   │       └── kotlin/         ← populated by build.sh (inference.kt bindings)
│   └── app/
│       ├── build.gradle.kts    sourceSets wired to libs/; R8 minify; ABI splits
│       ├── proguard-rules.pro  Keep rules for JNA, UniFFI, coroutines, Hilt
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

This compiles `libvault.so` and `libinference.so` for all four ABIs and runs `uniffi-bindgen` to generate the Kotlin bindings.

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
| Phase 4 — UI & UX | Done | Baby profile setup, Today summary, History, Insights, teal pastel theme, edge-to-edge |
| Phase 5 — Self-hosted Sync | Done | Rust `ureq` push engine, SQLite sync_state tracking, WorkManager 15-min periodic sync |
| Phase 6 — On-device AI | Done | NNAPI Rust bindings, GPT-2 inference engine, Baby AI chat with animated UI |

---

## Security Notes

- The master key is **never stored in plaintext**. It lives unwrapped only in RAM, inside a `Mutex<Zeroizing<Vec<u8>>>` that wipes on drop.
- The Android Keystore wrapping key is **hardware-backed** on devices with a Trusted Execution Environment (TEE).
- Media files on disk are pure ciphertext — reading the filesystem without the key yields nothing.
- `rusqlite` is compiled with the `bundled` feature — SQLite is statically linked into the `.so`, avoiding system SQLite version quirks.
- This app requests no internet permission by default. There is no analytics, no telemetry, no third-party SDK.
- The self-hosted sync feature pushes only encrypted blobs — the server never receives plaintext data or the encryption key.
- The AI model runs entirely on-device — no prompt, no response, and no conversation history ever leaves the device.
