# QuickPDF

QuickPDF is the first AppMarket utility app: a small, offline-first Android tool to create PDFs from text and images.

## Features in version 1.0
- Create a PDF from typed or pasted text.
- Convert one or multiple selected images into a PDF.
- Share the generated PDF using Android's share sheet.
- No account, ads, analytics, or network permission.
- Documents are processed locally on the device.

## Build
Requirements: JDK 17, Android SDK platform 35 and build-tools 35.0.0, Gradle 8.7.

```bash
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Status
Initial implementation. It must pass CI and be tested on physical Android devices before public release. PDF merge, split, compression, OCR, and internationalization are not included yet.
