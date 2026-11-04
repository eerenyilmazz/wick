<div align="center">

# 🔥 Wick

**A mobile dating app built around time-limited chat sessions.**

Discover people, match, send an invitation — and talk before the flame burns out.

![Android](https://img.shields.io/badge/Android-Jetpack_Compose-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Hilt_·_Retrofit_·_Room-7F52FF?logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)

</div>

---

## Overview

Wick replaces endless open-ended chats with **sessions that expire**. Two users who like each other become a match; one of them sends an invitation, and once it is accepted a chat session opens with a countdown. When the timer runs out the session locks — unless someone spends tokens to extend it.

## Screenshots

<p align="center">
  <img src="docs/screenshots/login.png" width="15%" alt="Sign in" />
  <img src="docs/screenshots/discover.png" width="15%" alt="Discover" />
  <img src="docs/screenshots/matches.png" width="15%" alt="Matches" />
  <img src="docs/screenshots/timed-chat.png" width="15%" alt="Timed chat" />
  <img src="docs/screenshots/profile-detail.png" width="15%" alt="Profile detail" />
  <img src="docs/screenshots/token-wallet.png" width="15%" alt="Token wallet" />
</p>

<p align="center"><sub>Sign in · Discover · Matches · Timed chat · Profile detail · Token wallet</sub></p>

## Features

- **Discover & swipe** — distance-aware discovery with age, distance and gender filters, plus super likes.
- **Matches & invitations** — mutual likes create a match; invitations open a chat session.
- **Time-limited chat** — real-time messaging over WebSocket/STOMP with a visible countdown (20 minutes by default, 40 for premium users) and token-based extensions.
- **Token economy** — daily login rewards, verification bonus and in-app purchases via Google Play Billing.
- **Identity verification** — selfie upload, automated face matching and manual admin approval for a verified badge.
- **Safety** — blocking and reporting, legal consent tracking (KVKK / terms of use), rate limits on auth, swipes and invitations.
- **Notifications** — Firebase Cloud Messaging push plus an in-app inbox.

## Tech Stack

| Layer | Technology |
|---|---|
| Android | Kotlin · Jetpack Compose · Hilt · Retrofit · Room · Google Play Billing (minSdk 26, targetSdk 36) |
| Backend | Java 17 · Spring Boot 3.5 · Maven |
| Database | PostgreSQL 16 · Flyway migrations |
| Cache / timers | Redis 7 (keyspace expiry events) |
| Real-time | WebSocket / STOMP |
| Push | Firebase Cloud Messaging |
| Auth | JWT access + refresh tokens |
| Face verification | Python daemon using DeepFace (`wick-backend/face_verify.py`) |

## Project Structure

```
wick/
├── wick-backend/              Spring Boot API
│   └── src/main/java/com/wick/
│       ├── user/              registration, login, profile, location, verification
│       ├── match/             discovery, swipes, matches
│       ├── session/           chat sessions, messages, WebSocket
│       ├── invitation/        send / accept / decline
│       ├── notification/      FCM push + inbox
│       ├── token/             balance, spending, rewards
│       ├── safety/            blocking, reporting
│       ├── photo/             upload, ordering
│       ├── legal/             consent records
│       ├── payment/           in-app purchases
│       ├── config/            application configuration
│       ├── location/          distance calculation
│       └── eventlog/          internal event log
│
├── wick-android/              Jetpack Compose app
│   ├── app/                   entry point, DI, FCM service
│   ├── core/
│   │   ├── model/             domain models
│   │   ├── network/           Retrofit APIs, DTOs
│   │   ├── data/              repository implementations
│   │   ├── designsystem/      WickColors, WickTextStyles, WickShapes, components
│   │   └── billing/           Google Play Billing
│   └── feature/
│       ├── auth/              sign in, sign up, password reset
│       ├── discover/          discovery, swipe, filters
│       ├── matches/           match list, invitation flow
│       ├── chat/              conversation list + chat screen
│       └── profile/           setup, editing, verification
│
└── stitch_wick_login_interface_design 2/   UI design screens
```

## Architecture

### Backend

Every domain package follows the same layout:

```
{domain}/
├── domain/        JPA entities
├── dto/           request / response records
├── repository/    Spring Data repositories
├── service/       interfaces
│   └── impl/      implementations (@Service)
├── controller/    @RestController — HTTP only, no business logic
└── mapper/        entity ↔ DTO mapping
```

- Business logic lives only in `service/impl/`.
- Controllers take the authenticated principal, delegate to a service and return a response.
- Errors are raised as `ApiException(message, HttpStatus)` and handled centrally by `GlobalExceptionHandler`; an insufficient token balance raises `InsufficientTokensException` (HTTP 402).
- Schema changes are Flyway migrations in `src/main/resources/db/migration/V{n}__{description}.sql`.

### Configuration

Spring Boot relaxed binding maps every property in `application.properties` to an environment variable (`app.jwt.secret` → `APP_JWT_SECRET`). The ones you will most likely change:

| Variable | Default | Purpose |
|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | `wick` | Credentials used by `docker-compose.yml` (see `.env.example`) |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | `jdbc:postgresql://localhost:5432/wick`, `wick`, `wick` | Database connection |
| `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT` | `localhost`, `6379` | Redis connection |
| `APP_JWT_SECRET` | placeholder | **Required in production.** Generate with `openssl rand -base64 64` |
| `APP_INTERNAL_SECRET` | placeholder | Value expected in the `X-Internal-Key` header of the internal admin API |
| `APP_CORS_ALLOWED_ORIGINS` | empty | Comma-separated allowed origins |
| `APP_NOTIFICATION_PROVIDER`, `APP_NOTIFICATION_SERVICE_ACCOUNT_KEY_PATH` | `firebase`, `./firebase-service-account.json` | Firebase Cloud Messaging credentials |
| `APP_BILLING_PROVIDER`, `APP_BILLING_GOOGLE_PLAY_SERVICE_ACCOUNT_KEY_PATH` | `mock`, empty | Billing backend (`mock` for local development) |
| `APP_PHOTO_STORAGE_PATH` | `./data/photos` | Where uploaded photos are stored |
| `APP_SYSTEM_MAINTENANCE_MODE`, `APP_SYSTEM_MIN_VERSION` | `false`, `0.1.0` | Maintenance and forced-update switches reported to the app |

Session length, token costs, rate limits and invitation limits are also configurable under the `app.session.*`, `app.token.*`, `app.auth.*`, `app.match.*` and `app.invitation.*` prefixes. The defaults for secrets are placeholders — never deploy with them.

### Redis keyspace notifications

Chat session expiry is driven by Redis key TTLs. The backend subscribes to Redis keyspace expiry events (`SessionExpirationListener`), so Redis **must** run with expired-event notifications enabled:

```bash
redis-server --notify-keyspace-events Ex
```

`docker-compose.yml` already starts Redis this way. For a Redis instance you manage yourself, set it at runtime with `redis-cli config set notify-keyspace-events Ex` or add `notify-keyspace-events Ex` to `redis.conf`. Managed services (for example ElastiCache) usually expose this as a parameter-group setting. If notifications are disabled, a periodic fallback job (`SessionExpirationFallbackJob`, every 60 s by default) still locks expired sessions, but with a delay.

### Face verification daemon (DeepFace)

Selfie verification is processed by a separate Python process, `wick-backend/face_verify.py`. It polls PostgreSQL for users with `PENDING` verification, compares the selfie against their profile photos with DeepFace (Facenet512 model, RetinaFace detector, cosine distance), and calls the backend's `/internal/verify/{userId}/approve` or `/reject` endpoint.

Requirements: Python 3.9+ and the packages `deepface`, `psycopg2-binary` and `requests`. DeepFace downloads its model weights on first use, so allow some time and network access for the first run.

```bash
cd wick-backend
python -m venv .venv && source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install deepface psycopg2-binary requests
export INTERNAL_KEY=<same value as APP_INTERNAL_SECRET>
python face_verify.py
```

The daemon is configured through environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `postgresql://wick:wick@localhost:5432/wick` | PostgreSQL connection string |
| `PHOTO_DIR` | `./data/photos` | Photo storage directory (must match `APP_PHOTO_STORAGE_PATH`) |
| `BACKEND_URL` | `http://localhost:8080` | Backend base URL |
| `INTERNAL_KEY` | placeholder | Must equal the backend's `app.internal.secret` |
| `POLL_INTERVAL` | `60` | Seconds between polls |
| `MATCH_THRESHOLD` | `0.6` | Maximum cosine distance accepted as a match |

Applications still pending after 7 days are rejected automatically by `VerificationTimeoutScheduler`.

### Running backend tests

```bash
cd wick-backend
./mvnw test
```

Unit tests use Mockito. Integration tests extend `AbstractIntegrationTest`, which starts throw-away PostgreSQL 16 and Redis 7 containers through Testcontainers, so **Docker must be running**. CI (`.github/workflows/ci.yml`) runs `./mvnw test -B` with JDK 17 on every pull request. To run a single class:

```bash
./mvnw test -Dtest=JwtServiceTest
```

### Android

```
Screen (Compose)
  └── ViewModel (state + actions)
        └── Repository (interface)
              └── RepositoryImpl (network + mapping)
                    └── Api (Retrofit)
```

- Each screen has a `UiState` data class and an `Action` sealed interface.
- ViewModels expose state through a single `MutableStateFlow<UiState>`.
- Network results are wrapped in `AppResult<T>` (`Success` / `Failure`); errors are typed via the `UiError` sealed class.
- UI uses the Wick design system (`WickColors`, `WickTextStyles`, `WickShapes`) rather than `MaterialTheme` directly.

## Key Flows

| Flow | Details |
|---|---|
| Onboarding | Sign up → profile setup → discover |
| Matching | Like + mutual like → match → invitation → session → chat |
| Session length | 20 minutes (40 for premium users) from opening by default; extendable in 10-minute steps by spending tokens |
| Super like | Costs 1 token and sends the recipient a dedicated notification |
| Verification | Selfie upload → automated face check → admin approval → verified badge |
| Earning tokens | Daily login, verification bonus, purchases |

## Getting Started

### Prerequisites

- JDK 17, Docker + Docker Compose
- Android Studio (latest stable) with Android SDK 36

### Backend

Start PostgreSQL and Redis, then run the API:

```bash
cd wick-backend
docker compose up -d postgres redis
./mvnw spring-boot:run -Dmaven.test.skip=true
# API on http://localhost:8080
```

Or run the whole stack in Docker (requires `APP_JWT_SECRET` in a `.env` file — see `.env.example`):

```bash
cd wick-backend
docker compose up -d --build
```

Default local database: `localhost:5432`, database / user / password `wick`.

### Android

```bash
cd wick-android
./gradlew :app:assembleDebug
```

Or open `wick-android/` in Android Studio and run the `app` configuration.

### Test accounts (local seed data)

| Email | Password |
|---|---|
| `ahmet@test.com` | `wick1234` |
| `zeynep@test.com` | `wick1234` |

## Internal Admin API

Requires the `X-Internal-Key` header (value of `app.internal.secret`):

```
GET  /internal/verify/pending           pending verification requests
GET  /internal/verify/{userId}/photo    verification selfie
POST /internal/verify/{userId}/approve
POST /internal/verify/{userId}/reject
```

## Known Gaps

| # | Topic | Status |
|---|---|---|
| TODO-1 | Email verification | Waiting on SMTP configuration |
| TODO-18 | WebSocket behaviour in background | Needs on-device testing |

See [`BUGS.md`](BUGS.md) for the tracked bug list.
