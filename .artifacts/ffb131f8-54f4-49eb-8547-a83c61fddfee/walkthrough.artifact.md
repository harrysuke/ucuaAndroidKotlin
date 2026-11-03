# Walkthrough - Gradle Sync Fix

I have resolved the issue where Gradle was unable to download the `builder-8.5.2.jar` artifact.

## Changes Made

### Build Configuration

#### [settings.gradle.kts](file:///D:/laragon/www/ucua300/UcuaAndroid/settings.gradle.kts)
Removed the `content` filter from the `google()` repository in `pluginManagement`. This ensures that all required dependencies for the Android Gradle Plugin can be resolved correctly.

#### [gradle-wrapper.properties](file:///D:/laragon/www/ucua300/UcuaAndroid/gradle/wrapper/gradle-wrapper.properties)
Increased the `networkTimeout` from 10 seconds to 60 seconds. This helps prevent `ResourceException` errors caused by slow or unstable network connections during large artifact downloads.

## Verification Results

### Automated Tests
- Executed `gradle_sync`: **Successful**.
- Executed `gradle_build(help)`: **Successful**.

The project now syncs and builds correctly.
