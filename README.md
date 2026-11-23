# FreeCity — Native Android 3D

Unity-free Android project using Java, Android Gradle Plugin and OpenGL ES 3.0.

## Current prototype
- Full-screen landscape Android game shell
- OpenGL ES 3.0 renderer with perspective camera and depth testing
- Procedurally generated grid city, roads and varied-height buildings
- Third-person player placeholder and starter car placeholder
- On-screen touch movement/look HUD
- No Unity Editor, Unity license, external game assets or network runtime dependency

This is an early playable-engine foundation, not a finished GTA-scale game. Character animation, vehicle physics, AI traffic, missions, audio, detailed models, materials, shadows and optimization are planned follow-up milestones.

## Build in Termux
Install a JDK 17 and Gradle compatible with Android Gradle Plugin 8.6.1. Then:
```sh
pkg update
pkg install openjdk-17 gradle git
git clone -b native-android https://github.com/dahfhkhfbjhvdjhyv-spec/FreeCity-Android-Game.git
cd FreeCity-Android-Game
gradle wrapper --gradle-version 8.7
./gradlew assembleDebug
```
The Android SDK platform 35 and build-tools must be installed and configured in Termux. The GitHub Actions workflow also builds a debug APK on pushes to this branch. Set ANDROID_HOME to the SDK directory. Building native Android projects on Termux can require extra SDK/Gradle setup; this repository has not yet been verified on your phone.

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Graphics
The app requires OpenGL ES 3.0 hardware support. Android devices vary in GPU capability and frame rate. See Android's official NDK/OpenGL ES support notes: https://developer.android.com/ndk/guides/stable_apis
