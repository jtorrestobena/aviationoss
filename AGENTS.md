# 🤖 Agent Guidelines: Aviationstack Flight Tracker

Welcome, Agent! This file outlines the architectural standards, codebase structure, and visual/functional design rules of the **Aviationstack Flight Tracker** project. Please review these detailed guidelines before making any modifications.

---

## 📌 Project Overview
The Aviationstack Flight Tracker is an Android client-side application built in Jetpack Compose, Kotlin, and Gradle Kotlin DSL (.gradle.kts). It integrates the **Aviationstack API** to search flights, cache results utilizing a local Room database, display interactive leaflet map paths, and query subsequent departures sharing the same departure nodes.

---

## 📁 Key File Locations

* **Core Views, App Shell, & MainActivity**:
  `app/src/main/java/com/bytecoders/aviationoss/MainActivity.kt`
  Contains the application's root scaffold, screen container tabs, `FlightRouteMap` (WebView custom Leaflet component), dialog popups, skeleton loaders, and interactive components.

* **Business Logic & State Manager**:
  `app/src/main/java/com/bytecoders/aviationoss/ui/FlightViewModel.kt`
  Manages search queries, free-credit counters, local persistent settings (e.g. customized API keys), selected tracking states, and upcoming flight lookups.

* **Repository Data Layer**:
  `app/src/main/java/com/bytecoders/aviationoss/data/repository/FlightRepository.kt`
  Handles network-to-local synchronization. Maps raw network payloads (`FlightDataDto`) into clean cached entities (`CachedFlightEntity`).

* **API Endpoints Definitions**:
  `app/src/main/java/com/bytecoders/aviationoss/data/remote/AviationstackApiService.kt`
  Retrofit setup for querying the Aviationstack endpoints.

* **Local Android SQLite Database**:
  * Entity definition: `app/src/main/java/com/bytecoders/aviationoss/data/local/CachedFlightEntity.kt` (Currently Version **2**, which supports `liveLatitude` and `liveLongitude`).
  * Database entry: `app/src/main/java/com/bytecoders/aviationoss/data/local/AppDatabase.kt`
  * Dao queries: `app/src/main/java/com/bytecoders/aviationoss/data/local/FlightDao.kt`

---

## 🛠️ Critical Subsystem Designs

### 1. Leaflet Interactive WebView Maps (`FlightRouteMap`)
* **Core Technology**: An Android `WebView` nested inside Jetpack Compose's `AndroidView` interop component.
* **Coordinate Mapping**: Displays both airport-to-airport direct line vectors and the aircraft's current GPS position if active.
* **Mock Coords Lookup**: Airport coordinates are mapped in `getAirportCoords(iata: String?)` in `MainActivity.kt`. If a 3-letter IATA is not pre-defined in the lookup, it uses an algorithmic string-to-coordinate hash fallback to guarantee a visual vector is always generated.
* **HTML Template**: An inline Leaflet.js template utilizing CartoDB's extremely dark basemap tileset to blend into the application's background.

### 2. Daisy-Chaining Departures Query (`UpcomingFlightSummaryCard`)
* **Integration**: When a user highlights a flight, a `LaunchedEffect` triggers `viewModel.fetchUpcomingFlight(departureIata, currentFlightId)`.
* **API Details**: It queries Aviationstack's core flights endpoint pre-filtered by `departureIata`.
* **Sorting Logic**: It filters out the current flight, maps findings into Room entities, and sorts remaining active scheduled records ascendingly (`departureScheduled`) to fetch the **very next** scheduled departure.
* **Interaction**: Clicking the summary card in the details popup re-triggers the dialog popup state directly with the new destination, allowing easy flight bouncing.

### 3. Flight Loading Skeleton Tracker (`FlightSkeletonList`)
* **Styling**: Renders a pulsing linear shimmer brush as background shapes.
* **Expessive Elements**: Displays an active spinning "Radar" telemetry icon utilizing Jetpack Compose's infinite transitions so users receive aesthetic and tactile feedback of ongoing searches.

---

## ⚠️ Important Rules & Constraints

1. **Gradle Build Files**: Never modify or rename `build.gradle.kts` (root or app level) or `settings.gradle.kts` unless adding dependencies verified in `libs.versions.toml`.
2. **Signing Configuration**: Do **NOT** modify `debug.keystore`, `debug.keystore.base64`, or any signing configuration blocks in Gradle. This will brick the build container and prevent streaming device compilation.
3. **Schema Schema Integrity**: If you modify `CachedFlightEntity.kt` fields, remember to increment the SQLite Version number in `AppDatabase.kt` (currently Version **2**).
4. **API Credit Preservation**: The app tracks API calls inside local persistent storage (`remainingFreeRequests`). Respect the credit-checking limit guards inside `FlightViewModel` to prevent API key exhaustions.
5. **Secrets & env configuration**: Instruct users to set credentials via the AI Studio Secrets tab (`AVIATIONSTACK_API_KEY`). Do **NOT** hardcode API tokens inside source repositories.
