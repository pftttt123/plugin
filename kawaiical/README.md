# KawaiiCal 🎀

A cute, pastel calorie-counting app for Android, built with Kotlin +
Jetpack Compose + Material 3 + Room, in a femboy-flag-inspired palette of
soft pink `#FFAEC9`, baby blue `#A9D9F5`, and white (with a deep-charcoal
dark mode that keeps the pink/blue accents).

## Features

- **Home** — animated gradient calorie ring with a rolling number counter,
  macro breakdown as an animated stacked bar, water tracker with a
  fill-up glass + wave animation, streak counter, and per-meal logs
- **Mochi the mascot** — a Canvas-drawn chibi in striped thigh-high socks
  who breathes and blinks on an idle loop, cheers (with a spring hop)
  when you log a meal, and gets sleepy at night
- **Food logging** — searchable local starter database (~80 foods),
  quick-add for custom foods, serving stepper, staggered list entrances
- **History** — weekly line chart that draws itself in, plus day-by-day
  gradient progress pills and summary stat chips
- **Confetti** — a pastel particle burst when you hit your calorie goal
  or a streak milestone (3/7/14/21/30/50/100 days)
- **Settings** — calorie goal slider, water goal, light/dark/auto theme,
  metric/imperial units
- **Persistence** — Room (foods, diary entries, water) + DataStore (prefs)

## Building

Open the `kawaiical/` folder in Android Studio (Ladybug or newer) and run,
or from the command line:

```
./gradlew assembleDebug
```

Min SDK 26, target/compile SDK 35, Kotlin 2.0, AGP 8.7.

## Structure

```
app/src/main/java/com/kawaiical/app/
├── KawaiiCalApplication.kt   # manual DI container
├── MainActivity.kt
├── data/
│   ├── db/                   # Room: entities, DAOs, database, seed data
│   ├── prefs/                # DataStore-backed user preferences
│   └── DiaryRepository.kt
└── ui/
    ├── components/           # CalorieRing, Mascot, Confetti, WaterGlass,
    │                         # RollingNumber, MacroBars, WeeklyLineChart…
    ├── home/  log/  history/  settings/   # MVVM screen + ViewModel pairs
    ├── nav/                  # NavHost with spring-based transitions
    └── theme/                # palette, Nunito type, bubbly shapes
```

The food table's schema mirrors what a nutrition API would return, so a
remote data source can be dropped into `DiaryRepository` later without
touching the UI.
