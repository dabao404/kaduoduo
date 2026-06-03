# AGENTS.md

## Build prerequisites

- **No Gradle Wrapper committed.** Run this once before any CLI build:
  ```
  gradle wrapper --gradle-version 8.11.1
  ```
  This creates `gradlew.bat` / `gradlew` so standard `./gradlew` commands work.

- **Requires JDK 17.** CI uses Eclipse Temurin 17.

## Build commands

```
./gradlew -Pandroid.useAndroidX=true -Pandroid.enableJetifier=true clean assembleDebug
```

CI runs exactly this (no `test`, no lint). There is **no `check` or `test` task** that passes — zero tests exist.

## Architecture

Single-module project (`:app`), no flavors. Entry points:

```
app/src/main/java/com/kaduoduo/
├── KaduoduoApplication.kt    # creates Database + CardRepository
├── MainActivity.kt            # single Activity, NavHost with 3 routes
├── data/local/                # Room @Entity, @Dao, @Database + TypeConverters
├── domain/
│   ├── CardRepository.kt      # wraps all 5 DAOs — UI must go through this
│   └── ViewModels.kt          # HomeViewModel, AddCardViewModel, CardDetailViewModel
├── ui/                        # composable screens + formatters
│   └── theme/Theme.kt         # Material3 light color scheme
```

- **Navigation routes:** `"home"`, `"add"`, `"detail/{cardId}"` (cardId is `Long`).
- **Repository pattern:** UI code calls ViewModel → Repository → DAO. Never call DAOs directly from composables.

## Money handling

All money values are stored in Room as `Long` — **fen (分)**, not yuan. Display formatting lives in `ui/Formatters.kt`. Always keep calculations in fen and only format for display.

## KSP & Room

- Room compiler runs via **KSP only** (no KAPT).
- KSP version must align with Kotlin: `2.1.21-2.0.1` (see root `build.gradle.kts`).
- Room schema JSON is exported to `app/schemas/` — this directory is **gitignored**.

## Kotlin & Compose compiler

The Compose compiler is integrated via the Kotlin Compose plugin (`org.jetbrains.kotlin.plugin.compose`), version-locked to the Kotlin version `2.1.21`. The `compose = true` build feature is enabled in `app/build.gradle.kts`.

## Dependencies

No version catalog (`libs.versions.toml`). All dependencies are declared inline in `app/build.gradle.kts`. Compose BOM is `2026.05.00`.

## Code style

Only `.editorconfig` enforces formatting: UTF-8, LF, trimmed trailing whitespace, 4-space indent for Kotlin, 2-space for XML/YAML/Markdown. No detekt, ktlint, or spotless is configured.

## Database preloading

`HomeViewModel.seedDefaultBanks()` inserts 8 Chinese bank records on first launch (guarded by `countBanks() == 0`). Bank IDs are auto-generated; don't hardcode them.
