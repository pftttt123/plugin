# Car Spotter

An offline Android app for tracking cars you've spotted in real life — a
collector's checklist.

- 269 cars across 43 manufacturers, grouped by manufacturer (A→Z)
- Each car has a photo (bundled into the APK at build time from Wikipedia),
  model name, production years, and body type
- Search by manufacturer or model
- Tap a car to mark it spotted — it gets crossed off and saved between
  sessions (SharedPreferences)
- Filter chips: **All / Spotted / Still to find**, plus an overall progress bar

## Building

The APK is built by the GitHub Actions workflow
`.github/workflows/build-apk.yml`, which first runs
`tools/fetch_images.py` to download and bundle car photos, then runs
`./gradlew assembleDebug`. Each successful build is published as a GitHub
release with `CarSpotter.apk` attached.

To build locally (needs the Android SDK and internet access):

```bash
python3 -m pip install requests pillow
python3 tools/fetch_images.py
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

## Installing

Download `CarSpotter.apk` from the latest release onto your phone, open it,
and allow installation from unknown sources when prompted.
