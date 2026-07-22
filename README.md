# PinkNote

PinkNote is a Kotlin Android app for menstrual cycle tracking.

## Stack

- Kotlin
- MVVM, Repository Pattern, Clean Architecture
- Hilt
- Coroutines, Flow, StateFlow
- Jetpack Compose, Material 3
- Navigation Component
- Room, DataStore, WorkManager
- Firebase Authentication, Firestore, Storage, Cloud Messaging
- MPAndroidChart
- Lottie

## Open In Android Studio

1. Open this folder: `D:\My Project\RedNote`
2. Let Android Studio sync Gradle.
3. Replace `app/google-services.json` with the real Firebase file.
4. Replace `default_web_client_id` in `app/src/main/res/values/strings.xml`.
5. Run the `app` configuration.

## Build

```powershell
.\gradlew.bat assembleDebug
```

If the wrapper is not generated yet, open the project in Android Studio or run Gradle wrapper from a machine with Android Gradle Plugin access.
