# 🚗 Car Spotter

Track cars you've spotted in real life — a collector's checklist for car enthusiasts.

Browse a catalog of cars grouped by manufacturer (A→Z), search by make or model,
mark cars as **spotted**, and watch your progress grow. Your checklist is saved on
the device between sessions.

- **Backend** — a Python/FastAPI service that serves the car catalog (manufacturers,
  models, photos, key details) over a simple REST API.
- **Android app** — a Kotlin + Jetpack Compose frontend that consumes the API and
  stores your spotted progress locally.

```
car-spotter/
├── backend/                 # FastAPI + SQLite catalog API
│   └── app/
│       ├── main.py          # API endpoints + CORS
│       ├── models.py        # Manufacturer, Car (SQLModel tables)
│       ├── schemas.py       # API response shapes
│       ├── database.py      # SQLite engine/session
│       ├── seed.py          # load data/cars.json -> SQLite (resolves photos)
│       ├── expand_from_nhtsa.py   # optional: grow catalog from NHTSA vPIC
│       └── data/cars.json   # curated catalog (18 makes / ~100 cars)
└── android/                 # Jetpack Compose app (MVVM + Retrofit + Room + Coil)
    └── app/src/main/java/com/carspotter/
        ├── data/            # remote (Retrofit), local (Room), repository
        └── ui/              # catalog, detail, progress screens + navigation
```

## Tech stack

| Layer            | Choice                                  | Why |
|------------------|-----------------------------------------|-----|
| Backend API      | **Python + FastAPI + Uvicorn**          | Tiny, fast, auto Swagger docs at `/docs` |
| Storage          | **SQLite via SQLModel**                 | Zero-config, file-based — nothing to host |
| Car photos       | **Wikipedia REST API** (at seed time)   | Real photos resolved once; no runtime dependency |
| Android UI       | **Kotlin + Jetpack Compose (Material 3)** | Modern, Google-recommended toolkit |
| Architecture     | **MVVM** (ViewModel + StateFlow)        | Clean, testable, reactive |
| Networking       | **Retrofit + Moshi + OkHttp**           | Type-safe API client |
| Images           | **Coil**                                | Async image loading in Compose |
| Local progress   | **Room**                                | "Spotted" state persists on-device, no login |

**Why spotted-state is stored on the device:** a personal checklist needs no
account or login, works offline, and toggles instantly. The backend stays a simple,
read-only catalog.

---

## Data model

**Manufacturer** — `id`, `name` (unique), `country`, `logo_url`

**Car** — `id`, `manufacturer_id`, `model`, `year_start`, `year_end` (null = still
made), `generation`, `body_type`, `image_url`

**Spotted** (Android Room, on-device only) — `carId`, `spottedAt`

### API endpoints

| Method & path            | Returns |
|--------------------------|---------|
| `GET /health`            | status + catalog size |
| `GET /manufacturers`     | makes A→Z, each with a car count |
| `GET /cars`              | cars; filter with `?q=`, `?manufacturer_id=`, `?body_type=` |
| `GET /cars/{id}`         | one car |
| `GET /catalog`           | makes A→Z with their cars nested (the app loads this once) |

---

## Run the backend

Requires Python 3.10+.

```bash
cd car-spotter/backend
python -m venv .venv
source .venv/bin/activate          # Windows: .venv\Scripts\activate
pip install -r requirements.txt

python -m app.seed                 # build + fill carspotter.db (needs internet once
                                   #   to fetch real car photos from Wikipedia)
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

- `--host 0.0.0.0` is what makes the API reachable from your phone.
- Open **http://localhost:8000/docs** in a browser to try the API.
- Re-seed any time with `python -m app.seed --reset`. No internet? Use
  `python -m app.seed --no-images` (labelled placeholders instead of photos).

**Grow the catalog (optional):** pull extra models from the free NHTSA vPIC API:

```bash
python -m app.expand_from_nhtsa --make Kia --make Lexus --limit 25
```

---

## Run the Android app on your own device

You need **Android Studio** (Hedgehog or newer) installed.

1. **Open the project:** in Android Studio choose *Open* → select
   `car-spotter/android`. On first sync, Studio downloads the Gradle/AndroidX
   dependencies and generates the Gradle wrapper jar automatically.

2. **Point the app at your backend.** Edit `BASE_URL` in
   `android/app/build.gradle.kts` (`defaultConfig` block) for your setup:

   | How you test | `BASE_URL` | Extra step |
   |--------------|-----------|------------|
   | **Real phone over USB** *(default)* | `http://localhost:8000/` | enable USB debugging, then run `adb reverse tcp:8000 tcp:8000` |
   | **Phone on same Wi-Fi** | `http://<your-PC-LAN-IP>:8000/` | add that IP to `res/xml/network_security_config.xml` |
   | **Android emulator** | `http://10.0.2.2:8000/` | none |

3. **Build & install.** Press **Run ▶** in Android Studio, or from the terminal:

   ```bash
   cd car-spotter/android
   adb reverse tcp:8000 tcp:8000      # if testing over USB
   ./gradlew installDebug             # builds and installs onto the connected device
   ```

   To produce a shareable APK:

   ```bash
   ./gradlew assembleDebug
   # output: app/build/outputs/apk/debug/app-debug.apk
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

> Cleartext HTTP to `localhost` / `10.0.2.2` / your LAN IP is allowed only for these
> dev hosts via `network_security_config.xml`. Car photos load over HTTPS.

---

## Using the app

- **Catalog** tab — cars grouped by manufacturer (A→Z). Use the search bar to find a
  make or model, the chips to filter **All / Need to find / Spotted**, and the circle
  on each row to mark it spotted (spotted cars are crossed off).
- Tap any car for a **detail** screen with its photo and specs and a spot/unspot button.
- **Progress** tab — a progress bar plus spotted / still-to-find / total counts and the
  list of everything you've spotted. Your progress survives app restarts (stored with Room).
