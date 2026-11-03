# Implementation Plan - Fix [ksp] unexpected jvm signature V error

The build error `[ksp] java.lang.IllegalStateException: unexpected jvm signature V` is a known compatibility issue between Room and the KSP2 engine (enabled by default in KSP 2.0+). Room 2.6.1 is not fully compatible with KSP2 when processing `suspend` functions that return `Unit`.

## User Review Required

> [!IMPORTANT]
> I am proposing to upgrade the Room persistence library from `2.6.1` to `2.8.4` (or latest stable) which includes better KSP2 support. Alternatively, we can disable KSP2 and fallback to KSP1. I will provide the fix for both but recommend upgrading Room.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/laragon/www/ucua300/UcuaAndroid/gradle/libs.versions.toml)
- Update `room` version to `2.8.4`.
- (Optional) Align `ksp` version with Kotlin `2.2.10` if `2.3.2` is causing other issues.

#### [MODIFY] [gradle.properties](file:///D:/laragon/www/ucua300/UcuaAndroid/gradle.properties)
- Add `ksp.useKSP2=false` to ensure stability by using the KSP1 engine, which is fully compatible with Room 2.6.x/2.7.x.

### Data Access Objects

#### [MODIFY] [ReportDao.kt](file:///D:/laragon/www/ucua300/UcuaAndroid/app/src/main/java/com/ucua/app/core/database/ReportDao.kt)
- Explicitly add `: Unit` return types to `suspend` functions. This sometimes helps KSP resolve the signature correctly.

## Verification Plan

### Automated Tests
- Run `:app:kspDebugKotlin` task to verify that the KSP processing finishes successfully.
- Run a full build: `./gradlew assembleDebug`.

### Manual Verification
- Verify that the app still functions correctly (Room database operations).
