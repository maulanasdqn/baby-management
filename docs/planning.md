```python
content = """# PLAN.md - Family Milestone Vault (Rust NDK + Jetpack Compose)

A privacy-first, local-encryption-first Android application designed to securely log family milestones, track growth data, and store media without relying on commercial cloud providers.

---

## 🏗️ Architecture Overview

```text
+-------------------------------------------------------------+
|               UI LAYER (Kotlin / Jetpack Compose)          |
|  - Timeline UI     - Media Picker       - Biometric Auth   |
+-------------------------------------------------------------+
                              |
                              | [UniFFI Generated Bindings]
                              v
+-------------------------------------------------------------+
|               CORE ENGINE (Rust NDK / .so)                  |
|  - ChaCha20-Poly1305 Crypto  - SQLite Database (Rusqlite)   |
|  - Chunked Sync Engine       - Validation Logic             |
+-------------------------------------------------------------+

```

---

## 📌 Phase 1: Setup & Multi-Language Bridge (The Plumbing)

*Tujuan: Menghubungkan Kotlin dan Rust tanpa error kompilasi.*

* [ ] **Rust Core Workspace Setup**
* [ ] Initialize cargo project as `cdylib`.
* [ ] Tambahkan dependensi: `uniffi`, `chacha20poly1305`, `rusqlite`, `thiserror`.
* [ ] Konfigurasi `cargo-ndk` dan target Android (`aarch64-linux-android`, dll).


* [ ] **UniFFI Integration**
* [ ] Tulis definisi interface dasar (`vault.udl` atau macro-based UniFFI 0.28+).
* [ ] Ekspos fungsi testing sederhana: `fn buat_halo(nama: String) -> String`.


* [ ] **Android Studio Integration**
* [ ] Setup projek Android baru dengan Jetpack Compose.
* [ ] Buat automation script (`build.sh` atau Gradle task) buat auto-copy `.so` dan file Kotlin hasil generate UniFFI ke direktori Android.
* [ ] Test panggil fungsi Rust dari MainActivity Compose.



## 🔒 Phase 2: Security & Local Database (The Core)

*Tujuan: Data masuk langsung di-enkripsi dan disimpan dengan aman di storage lokal.*

* [ ] **Biometric & Key Management (Android Side)**
* [ ] Implementasi Android BiometricPrompt.
* [ ] Setup Android Keystore untuk generate AES key (digunakan untuk mengenkripsi master key milik Rust).


* [ ] **Local Database via Rust (`rusqlite`)**
* [ ] Rancang skema DB lokal di Rust (Tabel: `milestones`, `media_metadata`, `growth_logs`).
* [ ] Buat fungsi CRUD dasar di Rust yang diekspos ke Kotlin.


* [ ] **Crypto Engine Implementation**
* [ ] Implementasi enkripsi/dekripsi *stream-based* pakai ChaCha20-Poly1305 untuk file media besar (foto/video).
* [ ] Buat mekanisme *zero-knowledge storage*: media yang dipilih dari galeri langsung di-oper sebagai *byte array* ke Rust, di-enkripsi di memori, baru simpan file terenkripsinya ke disk.



## 🎨 Phase 3: UI UI & Core Features (The App)

*Tujuan: Aplikasi fungsional secara offline untuk mencatat tumbuh kembang.*

* [ ] **Feature: Secure Timeline UI**
* [ ] Desain feed kronologis momen menggunakan Compose `LazyColumn`.
* [ ] Integrasi dekripsi *on-the-fly* di Rust saat me-render gambar terenkripsi ke UI (pastikan pakai *caching* memori biar nggak *laggy*).


* [ ] **Feature: Growth Tracker Logs**
* [ ] UI input untuk metrik harian/bulanan (Berat badan, Tinggi badan, Catatan imunisasi).
* [ ] Simpan data metrik ke DB lokal via Rust dengan enkripsi tingkat baris (*row-level encryption*).


* [ ] **Feature: Media Vault Picker**
* [ ] Integrasi Android PhotoPicker (menghemat izin akses storage penuh).
* [ ] Mekanisme *background processing* (Coroutines) saat Rust sedang memproses enkripsi file berukuran besar.



## ☁️ Phase 4: Sync & Self-Hosting (The Extension)

*Tujuan: Backup data ke infrastruktur pribadi tanpa bocor ke pihak ketiga.*

* [ ] **Network Layer in Rust (`reqwest` + `tokio`)**
* [ ] Bangun modul sinkronisasi di Rust untuk komunikasi dengan *self-hosted backend* (e.g., REST API/gRPC di Axum).
* [ ] Implementasi *Chunked Upload* di Rust untuk menangani file media yang terputus di tengah jalan (*fault-tolerant*).


* [ ] **Background Sync Worker (Android Side)**
* [ ] Gunakan Android `WorkManager` untuk memicu proses sinkronisasi Rust secara berkala saat HP sedang di-cas dan terhubung Wi-Fi.



---

## 🛠️ Tech Stack Ledger

| Layer | Technology | Purpose |
| --- | --- | --- |
| **Frontend UI** | Kotlin + Jetpack Compose | Modern declarative UI, native performance, smooth lifecycle handling. |
| **Core Logic** | Rust (Android NDK) | Memory safety, zero-cost abstractions, maximum cryptography performance. |
| **Bridge** | UniFFI (Mozilla) | Type-safe automatic binding generator between Rust and Kotlin. |
| **Database** | SQLite via `rusqlite` (Embedded in Rust) | Local-first relational data storage managed completely inside the native layer. |
| **Crypto** | `chacha20poly1305` crate | High-speed authenticated encryption suitable for mobile processors. |

---

## 🚀 Development Command Cheatsheet

### Build Rust Library untuk Android

```bash
# Jalankan di dalam direktori core/ rust
cargo ndk -t aarch64-linux-android -t x86_64-linux-android build --release

```

### Generate UniFFI Bindings Manually (If needed)

```bash
# Contoh menggunakan CLI
uniffi-bindgen generate src/vault.udl --language kotlin --out-dir ../android/app/src/main/java/

```

"""

with open("PLAN.md", "w") as f:
f.write(content)

```
OK, ini file `PLAN.md` dalam format raw yang bisa langsung lu pakai untuk dokumentasi proyek lu.

[file-tag: code-generated-file-0-1778872044613551961]

```markdown
# PLAN.md - Family Milestone Vault (Rust NDK + Jetpack Compose)

A privacy-first, local-encryption-first Android application designed to securely log family milestones, track growth data, and store media without relying on commercial cloud providers.

---

## 🏗️ Architecture Overview

```text
+-------------------------------------------------------------+
|               UI LAYER (Kotlin / Jetpack Compose)          |
|  - Timeline UI     - Media Picker       - Biometric Auth   |
+-------------------------------------------------------------+
                              |
                              | [UniFFI Generated Bindings]
                              v
+-------------------------------------------------------------+
|               CORE ENGINE (Rust NDK / .so)                  |
|  - ChaCha20-Poly1305 Crypto  - SQLite Database (Rusqlite)   |
|  - Chunked Sync Engine       - Validation Logic             |
+-------------------------------------------------------------+

```

---

## 📌 Phase 1: Setup & Multi-Language Bridge (The Plumbing)

*Tujuan: Menghubungkan Kotlin dan Rust tanpa error kompilasi.*

* [ ] **Rust Core Workspace Setup**
* [ ] Initialize cargo project as `cdylib`.
* [ ] Tambahkan dependensi: `uniffi`, `chacha20poly1305`, `rusqlite`, `thiserror`.
* [ ] Konfigurasi `cargo-ndk` dan target Android (`aarch64-linux-android`, dll).


* [ ] **UniFFI Integration**
* [ ] Tulis definisi interface dasar (`vault.udl` atau macro-based UniFFI 0.28+).
* [ ] Ekspos fungsi testing sederhana: `fn buat_halo(nama: String) -> String`.


* [ ] **Android Studio Integration**
* [ ] Setup projek Android baru dengan Jetpack Compose.
* [ ] Buat automation script (`build.sh` atau Gradle task) buat auto-copy `.so` dan file Kotlin hasil generate UniFFI ke direktori Android.
* [ ] Test panggil fungsi Rust dari MainActivity Compose.



## 🔒 Phase 2: Security & Local Database (The Core)

*Tujuan: Data masuk langsung di-enkripsi dan disimpan dengan aman di storage lokal.*

* [ ] **Biometric & Key Management (Android Side)**
* [ ] Implementasi Android BiometricPrompt.
* [ ] Setup Android Keystore untuk generate AES key (digunakan untuk mengenkripsi master key milik Rust).


* [ ] **Local Database via Rust (`rusqlite`)**
* [ ] Rancang skema DB lokal di Rust (Tabel: `milestones`, `media_metadata`, `growth_logs`).
* [ ] Buat fungsi CRUD dasar di Rust yang diekspos ke Kotlin.


* [ ] **Crypto Engine Implementation**
* [ ] Implementasi enkripsi/dekripsi *stream-based* pakai ChaCha20-Poly1305 untuk file media besar (foto/video).
* [ ] Buat mekanisme *zero-knowledge storage*: media yang dipilih dari galeri langsung di-oper sebagai *byte array* ke Rust, di-enkripsi di memori, baru simpan file terenkripsinya ke disk.



## 🎨 Phase 3: UI UI & Core Features (The App)

*Tujuan: Aplikasi fungsional secara offline untuk mencatat tumbuh kembang.*

* [ ] **Feature: Secure Timeline UI**
* [ ] Desain feed kronologis momen menggunakan Compose `LazyColumn`.
* [ ] Integrasi dekripsi *on-the-fly* di Rust saat me-render gambar terenkripsi ke UI (pastikan pakai *caching* memori biar nggak *laggy*).


* [ ] **Feature: Growth Tracker Logs**
* [ ] UI input untuk metrik harian/bulanan (Berat badan, Tinggi badan, Catatan imunisasi).
* [ ] Simpan data metrik ke DB lokal via Rust dengan enkripsi tingkat baris (*row-level encryption*).


* [ ] **Feature: Media Vault Picker**
* [ ] Integrasi Android PhotoPicker (menghemat izin akses storage penuh).
* [ ] Mekanisme *background processing* (Coroutines) saat Rust sedang memproses enkripsi file berukuran besar.



## ☁️ Phase 4: Sync & Self-Hosting (The Extension)

*Tujuan: Backup data ke infrastruktur pribadi tanpa bocor ke pihak ketiga.*

* [ ] **Network Layer in Rust (`reqwest` + `tokio`)**
* [ ] Bangun modul sinkronisasi di Rust untuk komunikasi dengan *self-hosted backend* (e.g., REST API/gRPC di Axum).
* [ ] Implementasi *Chunked Upload* di Rust untuk menangani file media yang terputus di tengah jalan (*fault-tolerant*).


* [ ] **Background Sync Worker (Android Side)**
* [ ] Gunakan Android `WorkManager` untuk memicu proses sinkronisasi Rust secara berkala saat HP sedang di-cas dan terhubung Wi-Fi.



---

## 🛠️ Tech Stack Ledger

| Layer | Technology | Purpose |
| --- | --- | --- |
| **Frontend UI** | Kotlin + Jetpack Compose | Modern declarative UI, native performance, smooth lifecycle handling. |
| **Core Logic** | Rust (Android NDK) | Memory safety, zero-cost abstractions, maximum cryptography performance. |
| **Bridge** | UniFFI (Mozilla) | Type-safe automatic binding generator between Rust and Kotlin. |
| **Database** | SQLite via `rusqlite` (Embedded in Rust) | Local-first relational data storage managed completely inside the native layer. |
| **Crypto** | `chacha20poly1305` crate | High-speed authenticated encryption suitable for mobile processors. |

---

## 🚀 Development Command Cheatsheet

### Build Rust Library untuk Android

```bash
# Jalankan di dalam direktori core/ rust
cargo ndk -t aarch64-linux-android -t x86_64-linux-android build --release

```

### Generate UniFFI Bindings Manually (If needed)

```bash
# Contoh menggunakan CLI
uniffi-bindgen generate src/vault.udl --language kotlin --out-dir ../android/app/src/main/java/

```

```

```
