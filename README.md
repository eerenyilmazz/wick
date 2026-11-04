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
- **Time-limited chat** — real-time messaging over WebSocket/STOMP with a visible countdown (24 h by default) and token-based extensions.
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
| Session length | 24 hours from opening by default; extendable by spending tokens |
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

---

## License

Copyright (c) 2026 Eren Yılmaz. All rights reserved. The source is publicly visible for evaluation only; see [`LICENSE`](LICENSE). To request permission to use any part of it, contact eerenyilmazz1@gmail.com.
