# Walkthrough - Fixed [ksp] unexpected jvm signature V error

The build error `[ksp] java.lang.IllegalStateException: unexpected jvm signature V` was resolved by upgrading the Room persistence library and making minor DAO syntax adjustments to satisfy the KSP2 engine.

## Changes Made

### Room Upgrade
Upgraded Room from `2.6.1` to `2.8.4` in [libs.versions.toml](file:///D:/laragon/www/ucua300/UcuaAndroid/gradle/libs.versions.toml). Newer versions of Room have improved compatibility with the KSP2 engine used in Kotlin 2.0+.

### DAO Refinement
Updated [ReportDao.kt](file:///D:/laragon/www/ucua300/UcuaAndroid/app/src/main/java/com/ucua/app/core/database/ReportDao.kt) to explicitly declare `: Unit` as the return type for `suspend` functions. This helps the KSP processor correctly identify the JVM signature for methods that don't return a value.

### KSP Configuration
Initially attempted to disable KSP2 in `gradle.properties`, but reverted this as KSP1 is no longer supported in the current environment/Kotlin version. The Room upgrade and DAO changes were sufficient to fix the issue under KSP2.

## Verification Results

### Build Verification
- Successfully ran `:app:kspDebugKotlin`.
- The build now completes without the `unexpected jvm signature V` error.

> [!NOTE]
> If you encounter similar issues in other DAOs, adding the explicit `: Unit` return type to `suspend` methods is the recommended first step.
