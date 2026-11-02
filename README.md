# SmartWidget Android Project (APK Ready)

This repository contains the complete Android Studio project for SmartWidget:
- WidgetConfigActivity.kt (Target date picking, custom background URI selection, Step sensor registration)
- SmartWidgetProvider.kt (AppWidgetProvider with battery level and days countdown)
- WidgetPreferences.kt (Persistent SharedPreferences wrapper)
- All layout XMLs and manifest declarations
- Gradle build scripts configured for Android 15 (API 35)

## How to Build the APK on your machine:
1. Open this unzipped folder in **Android Studio**.
2. Or in your terminal run:
   ```bash
   ./gradlew assembleDebug
   ```
3. The generated APK will be at:
   `app/build/outputs/apk/debug/app-debug.apk`
4. Install directly on your connected Android phone:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

## Build in the Cloud (GitHub Actions):
Push this repository to GitHub. The included `.github/workflows/build-apk.yml` will automatically build and publish `SmartWidget-app-debug.apk` in the **Actions** tab for direct download.
