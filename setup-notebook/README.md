# Setup Notebook

A native Android app for managing sim racing car setups — pure black OLED theme,
built with Kotlin, Jetpack Compose (Material 3), Room, and Navigation Compose.
Fully offline; all data lives in a local Room database.

## Features

- **Garage / Tracks** — manage the cars and circuits you race (create, edit, delete),
  each car tagged with its sim title (iRacing, ACC, AC, rFactor 2, LMU, …)
- **Setups** — per car + track, with create / edit / duplicate / delete,
  search, and filter chips by sim title, car, and track
- **Setup editor** — collapsible sections with spring animations:
  - Tyres: pressures, camber, toe per corner (FL/FR/RL/RR)
  - Suspension: springs, bump/rebound dampers per corner, ARBs, ride heights
  - Aero: front/rear wing, splitter
  - Drivetrain: diff preload/power/coast, final drive, gears 1–8
  - Brakes: bias, pressure, ducts
  - Fuel & strategy: fuel load, consumption, stint length, tyre sets
  - Free-text notes (auto-saved) + lap time log (conditions, air/track temps, best-lap highlight)
- **Compare** — any two setups side by side, differences highlighted in red,
  with a "differences only" toggle
- **Export / import** — setups as JSON via the Android share sheet; import from any
  JSON file (cars/tracks are created automatically by name)
- Steppers with haptic feedback and hold-to-repeat, animated value flashes,
  monospace numerics, animated tab transitions, speed-lines splash screen

## Building

Requirements: JDK 17+ and the Android SDK (easiest via Android Studio; otherwise
set `ANDROID_HOME` or create `local.properties` with `sdk.dir=/path/to/sdk`).
The build needs platform `android-34`; Gradle downloads everything else.

```sh
./gradlew assembleDebug
```

The APK lands at:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install on a phone with USB debugging enabled:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

- minSdk 26 (Android 8.0) · targetSdk/compileSdk 34
- AGP 8.5.2 · Kotlin 2.0.21 · Compose BOM 2024.09.03 · Room 2.6.1 (KSP)

## JSON export format

```json
{
  "app": "setup-notebook",
  "version": 1,
  "setups": [
    {
      "name": "Quali A",
      "car": "Porsche 992 GT3 R",
      "simTitle": "iRacing",
      "track": "Spa-Francorchamps",
      "notes": "loose on exit of Pouhon",
      "values": { "tyre_press_fl": 26.5, "wing_rear": 6.0 },
      "laps": [
        { "lapTimeMs": 138456, "conditions": "dry", "airTemp": 21.0, "trackTemp": 32.5 }
      ]
    }
  ]
}
```

`values` keys are the field keys defined in
`app/src/main/java/com/setupnotebook/data/SetupFields.kt`.
