# AriGo - Development Progress Tracker

## Project Info
- **App Name**: AriGo
- **Package**: com.example.arigo
- **Tech Stack**: Kotlin + Jetpack Compose, MVVM + Clean Architecture, Manual DI (no Hilt)
- **Backend**: Firebase Realtime Database + Firebase Auth
- **Figma**: https://www.figma.com/design/PhCkhRkaNfL9pyi3Dqjwvk/AriGo-APP-UI?node-id=0-1&m=dev
- **GitHub**: https://github.com/Gowtham-A-GM/AriGo-Android-APP

---

## Firebase Setup
We use **two Firebase projects**:

### 1. Hardware Team's Firebase (READ ONLY)
- Contains real-time sensor data from the AriGo wearable device
- Path: `airguard_devices/{deviceId}/history/{date}/{time}`
- Keys contain spaces: "Before aqi", "After co_ppm", "User Latitude", etc.
- Also has: `device_status` (online/last_seen) and `alerts` (threshold config)
- We only READ from this — never write

### 2. Our Firebase — "AriGo AirGuardPro"
- Authentication: Email/Password + Google Sign-In enabled
- Realtime Database (locked mode with custom rules)
- Stores: user profiles (`users/{uid}`), device pairings (`user_devices/{uid}`), notifications
- We READ and WRITE to this

### Firebase Security Rules (Our DB)

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "user_devices": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

---

## Architecture Overview

```
com.example.arigo/
├── core/
│   ├── theme/              → Color.kt, Type.kt, Theme.kt, Fonts.kt
│   ├── navigation/         → Screen.kt, AriGoNavHost.kt, AriGoBottomBar.kt
│   └── common/             → Resource.kt, Constants.kt
├── data/
│   ├── local/dao/          → (Room DAOs — future)
│   ├── local/entity/       → (Room entities — future)
│   ├── remote/firebase/    → (Firebase data sources — future)
│   ├── repository/         → AuthRepositoryImpl.kt
│   └── preferences/        → (DataStore — future)
├── domain/
│   ├── model/              → Models.kt (all data classes + DTOs)
│   ├── repository/         → Repositories.kt (interfaces)
│   └── usecase/            → (use cases — future)
├── presentation/
│   ├── splash/             → SplashScreen.kt
│   ├── auth/login/         → LoginScreen.kt, LoginViewModel.kt, LoginState.kt
│   ├── auth/signup/        → SignupScreen.kt, SignupViewModel.kt, SignupState.kt
│   ├── auth/profile_setup/ → (placeholder)
│   ├── home/               → (placeholder)
│   ├── device_detail/      → (placeholder)
│   ├── air_quality/        → (placeholder)
│   ├── filter_health/      → (placeholder)
│   ├── history/            → (placeholder)
│   ├── map/                → (placeholder)
│   ├── notifications/      → (placeholder)
│   ├── profile/            → (placeholder)
│   └── components/         → AuthComponents.kt (shared UI components)
├── di/                     → AppContainer.kt (manual dependency injection)
├── AriGoApplication.kt
└── MainActivity.kt
```

---

## Domain Models (mapped to Firebase)

### SensorReading — from hardware Firebase

| App Field         | Firebase Key         | Description                     |
|-------------------|----------------------|---------------------------------|
| beforeAqi         | `"Before aqi"`       | Raw air AQI reading             |
| coPpm             | `"co_ppm"`           | Carbon monoxide in ppm          |
| dustDensity       | `"dust_density"`     | PM2.5 particle reading (µg/m³)  |
| no2Ppm            | `"no2_ppm"`          | Nitrogen dioxide in ppm         |
| humidity          | `"humidity"`         | Relative humidity %             |
| temperature       | `"temperature"`      | Temperature in °C               |
| afterAqi          | `"After aqi"`        | AQI after filtration            |
| afterCoPpm        | `"After co_ppm"`     | CO after filtration             |
| afterDustDensity  | `"After dust_density"` | PM2.5 after filtration        |
| afterNo2Ppm       | `"After no2_ppm"`    | NO2 after filtration            |
| motorState        | `"motor_state"`      | true = purifier ON              |
| userLatitude      | `"User Latitude"`    | GPS latitude                    |
| userLongitude     | `"User Longitude"`   | GPS longitude                   |
| recordedAt        | `"recorded_at"`      | ISO timestamp                   |

### Device Detail — 6-cell air quality grid

| Cell  | Source         | Notes                              |
|-------|----------------|------------------------------------|
| AQI   | `beforeAqi`    | From sensor                        |
| PM2.5 | `dustDensity`  | `dust_density` mapped as PM2.5     |
| PM10  | —              | N/A placeholder (future sensor)    |
| CO    | `coPpm`        | From sensor                        |
| NO2   | `no2Ppm`       | From sensor                        |
| SO2   | —              | N/A placeholder (future sensor)    |

---

## Key Constants
- AQI threshold for alerts: > 50
- AQI Good: 0-50, Normal: 51-100, Bad: 101-200, Hazardous: 201+
- Filter max hours: 2000 (may change to 300-400 based on hardware testing)
- Battery: placeholder (hardware team to add)

---

## Milestones

### ✅ Milestone 1 — Project Foundation (Completed)
**What was built:**
- Complete Clean Architecture folder structure
- Gradle setup with all dependencies (Firebase, Compose, Navigation, Room, Maps, Coil, DataStore, Coroutines)
- Theme: Color palette (green/white/teal from Figma), Typography scale, Light/Dark theme
- Navigation: All routes defined (`Screen.kt`), NavHost wired with all placeholders, Bottom navigation bar (Home/History/Map/More)
- Domain models: All data classes matching Firebase structure, `SensorReadingDto` with `fromFirebaseMap()` for space-containing keys
- Repository interfaces: Auth, User, Device, SensorData, Filter
- Manual DI container (`AppContainer`) with Firebase instances
- `AriGoApplication` + `MainActivity` with Scaffold and conditional bottom bar
- AndroidManifest with permissions (Internet, Location, Notifications)
- `Constants`, `Resource` sealed class

**Files created:** 25+ Kotlin files, updated Gradle files, manifest, resources

---

### ✅ Milestone 2A — Splash Screen (Completed)
**What was built:**
- Gradient background: light blue (`#A1E3F9`) top → light green (`#80ED99`) bottom
- AriGo logo (`arigo_logo.png`) centered, 150dp
- "AriGo" text in Racing Sans One font, "AirGaurd Pro" in Radio Canada Medium
- Tagline "Clean the air, Choose the care !" at bottom in Radio Canada Regular
- Fade-in animation (1.5s) + auto-navigate after 2.5s
- Firebase Auth check: logged in → Home, not logged in → Login
- Custom fonts bundled locally (not Google Fonts provider)

**Files:** `SplashScreen.kt`, `Fonts.kt`, font resources (`racing_sans_one_regular.ttf`, `radio_canada_regular.ttf`, `radio_canada_medium.ttf`)

---

### ✅ Milestone 2B — Login Screen (Completed)
**What was built:**
- Teal header (`#1B6B93`) with rounded bottom corners, "LOGIN" title
- Username (email) field with validation ("Invalid username")
- Password field with Show/Hide toggle and validation ("Invalid password")
- "Forgot password" link
- "Login" pill button with loading state
- "Login with Google" button with actual Google icon (`ic_google.png`)
- Facebook removed (not using)
- "Don't have an account? Signin" pinned to bottom
- `LoginViewModel` with Firebase Auth integration
- `AuthRepositoryImpl` created and wired in `AppContainer`
- Error handling with Toast messages

**Files:** `LoginScreen.kt`, `LoginViewModel.kt`, `LoginState.kt`, `AuthRepositoryImpl.kt`, updated `AppContainer.kt`

---

### ✅ Milestone 2C — Signup Screen (Completed)
**What was built:**
- Same teal header style, "SIGN IN" title (matching Figma spelling)
- Username, Password, Conform Password fields (Figma spelling preserved)
- Independent Show/Hide toggles for both password fields
- Validation: "Invalid username", "Invalid password", "Password doesn't match"
- "Sign in" pill button with loading state
- "Signin with Google" button (Toast placeholder for now)
- "Already have an account? Login" pinned to bottom
- `SignupViewModel` with Firebase Auth signup
- Shared auth components extracted to `AuthComponents.kt` (`AuthHeader`, `AuthPillButton`, `AuthSocialButton`, `authTextFieldColors`)

**Files:** `SignupScreen.kt`, `SignupViewModel.kt`, `SignupState.kt`, `AuthComponents.kt`

---

### ⬜ Milestone 2D — Profile Setup Screen (Next)
**What to build:**
- Teal header with "PROFILE" title
- Circular avatar placeholder (tappable)
- Form: Profile Name, Phone, DOB + Gender (side by side), Address, City + PIN (side by side), Emergency Phone, Health Issues
- "Continue" button → save to Firebase (`users/{uid}`) → navigate to Home
- `ProfileSetupViewModel` with `UserRepositoryImpl`

---

### ⬜ Milestone 3 — Home Dashboard
- Weather/AQI cards (horizontally scrollable)
- "My Devices" section with device cards
- Live data from hardware Firebase
- Power/Auto controls on device cards

---

### ⬜ Milestone 4 — Device Detail Screen
- AQI status badge (Good/Normal/Bad)
- 6-cell air quality grid
- Purifier controls (Power ON/OFF, Auto mode)

---

### ⬜ Milestone 5 — Air Quality Analytics
- Before/after filtration line charts
- AQI, PM2.5, CO, PM10 charts
- Time granularity tabs

---

### ⬜ Milestone 6 — Filter Health
- Filter image, efficiency bar, replacement button

---

### ⬜ Milestone 7 — History Screen
- Day/Week/Month/Year tabs
- Date navigation, city search
- Historical charts

---

### ⬜ Milestone 8 — Map Screen
- Google Maps integration
- AQI overlay from GPS coordinates

---

### ⬜ Milestone 9 — Profile/More Screen
- Avatar, menu items, edit profile, logout

---

### ⬜ Milestone 10 — Add Device + Notifications
- Device pairing bottom sheet
- FCM push notifications

---

### ⬜ Milestone 11 — Polish & Testing
- Connect both Firebase projects
- Edge cases, offline mode
- App icon, branding, APK

---

## How to Set Up This Project
1. Clone the repo
2. Create a Firebase project and add Android app with package `com.example.arigo`
3. Enable Email/Password and Google authentication
4. Create Realtime Database (locked mode) and set the security rules shown above
5. Download `google-services.json` and place in `app/` folder
6. Add SHA-1 fingerprint for Google Sign-In
7. Open in Android Studio → Sync Gradle → Run

---

## Security Notes
- `google-services.json` is in `.gitignore` — never committed
- No API keys hardcoded in source code
- Google Maps key is a placeholder in `AndroidManifest.xml`
