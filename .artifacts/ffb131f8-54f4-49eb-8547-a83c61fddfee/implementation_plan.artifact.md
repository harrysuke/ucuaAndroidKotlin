# Fix Gradle Sync Error: Could not get resource 'builder-8.5.2.jar'

The project is failing to sync because Gradle cannot download the `builder-8.5.2.jar` from the Google Maven repository. This is likely due to a network timeout, a restrictive repository filter, or a transient connectivity issue with the `dl.google.com` host.

## Proposed Changes

### Build Configuration

#### [MODIFY] [settings.gradle.kts](file:///D:/laragon/www/ucua300/UcuaAndroid/settings.gradle.kts)
- Remove the `content` filter from the `google()` repository in `pluginManagement`. This ensures that all required AGP dependencies can be resolved without restriction.
- Ensure `google()` is listed first.

#### [MODIFY] [gradle-wrapper.properties](file:///D:/laragon/www/ucua300/UcuaAndroid/gradle/wrapper/gradle-wrapper.properties)
- Increase `networkTimeout` from `10000` (10s) to `60000` (60s) to allow more time for downloads on slower or unstable connections.

## Verification Plan

### Manual Verification
- Run a Gradle sync in Android Studio.
- If sync still fails, run the following command in the terminal to force a refresh of dependencies:
  ```bash
  ./gradlew build --refresh-dependencies
  ```
