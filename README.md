# Wick — Proje README

## Nedir?

Wick, zaman sınırlı sohbet odaları üzerine kurulu bir mobil tanışma uygulamasıdır. Kullanıcılar birbirini keşfeder, eşleşir, davet gönderir ve belirli bir süre içinde sohbet eder. Süre dolunca oturum kilitlenir.

---

## Teknoloji Yığını

| Katman | Teknoloji |
|---|---|
| Backend | Java 17 · Spring Boot 3.5.0 · Maven |
| Veritabanı | PostgreSQL 16 · Flyway (migrations) |
| Android | Kotlin · Jetpack Compose · Hilt · Retrofit · Room |
| Gerçek zamanlı | WebSocket / STOMP |
| Push bildirimleri | Firebase Cloud Messaging (FCM) |
| Auth | JWT (access + refresh token) |

---

## Proje Yapısı

```
WICK/
├── wick-backend/          # Spring Boot API
│   └── src/main/java/com/wick/
│       ├── user/          # Kayıt, giriş, profil, lokasyon, doğrulama
│       ├── match/         # Keşfet, swipe, eşleşme
│       ├── session/       # Sohbet oturumları, mesajlar, WebSocket
│       ├── invitation/    # Davet gönder / kabul et / reddet
│       ├── notification/  # FCM push + inbox bildirimleri
│       ├── token/         # Jeton bakiyesi, harcama, kazanma
│       ├── safety/        # Engelleme, şikayet
│       ├── photo/         # Fotoğraf yükleme, sıralama
│       ├── legal/         # KVKK/Kullanım koşulları onayları
│       ├── payment/       # Satın alma (in-app purchase)
│       ├── config/        # Uygulama konfigürasyonu (AppConfig)
│       ├── location/      # Mesafe hesaplama
│       └── eventlog/      # İç olay kaydı
│
└── wick-android/          # Jetpack Compose uygulaması
    ├── app/               # Ana modül, DI, FCM servisi
    ├── core/
    │   ├── model/         # Domain modelleri (User, Match, …)
    │   ├── network/       # Retrofit API'ları, DTO'lar
    │   ├── data/          # Repository implementasyonları
    │   ├── designsystem/  # WickColors, WickTextStyles, WickShapes, bileşenler
    │   └── billing/       # Google Play Billing
    └── feature/
        ├── auth/          # Giriş, kayıt, şifre sıfırlama
        ├── discover/      # Keşfet ekranı, swipe, filtreler
        ├── matches/       # Eşleşme listesi, davet akışı
        ├── chat/          # Sohbet listesi + sohbet ekranı
        └── profile/       # Profil kurulum, düzenleme, kimlik doğrulama
```

---

## Backend Katman Kuralları

Her domain paketi aynı yapıyı izler:

```
{domain}/
├── domain/          # JPA entity (@Entity)
├── dto/             # Request / Response record'ları
├── repository/      # JpaRepository + @Query metodları
├── service/         # Interface
│   └── impl/        # Implementasyon (@Service)
├── controller/      # @RestController — sadece HTTP, iş mantığı yok
└── mapper/          # Entity ↔ DTO dönüşümleri
```

**Kurallar:**
- İş mantığı **sadece** `service/impl/` içinde
- Controller sadece auth principal alır, service'e delege eder, response döner
- Exception fırlatmak için `ApiException(message, HttpStatus)` kullan — `GlobalExceptionHandler` otomatik yakalar
- Yetersiz token → `InsufficientTokensException` (402 döner)
- Veritabanı migration'ları `src/main/resources/db/migration/V{n}__{açıklama}.sql` formatında, Flyway otomatik çalıştırır

---

## Android Katman Kuralları

```
Screen (Compose UI)
  └── ViewModel (state + action)
        └── Repository (interface)
              └── RepositoryImpl (network + mapping)
                    └── Api (Retrofit interface)
```

- Her ekran bir `UiState` data class + `Action` sealed interface'e sahip
- ViewModel sadece `_uiState: MutableStateFlow<UiState>` günceller
- Network sonuçları `AppResult<T>` (Success / Failure) ile wrap edilir
- Hatalar `UiError` sealed class ile tip güvenli şekilde handle edilir
- Tasarım sistemi: `WickColors`, `WickTextStyles`, `WickShapes` — doğrudan `MaterialTheme` kullanılmaz

---

## Veritabanı Bağlantısı

```
Host:     localhost:5432
DB:       wick
User:     wick
Password: wick
```

---

## Çalıştırma

**Backend:**
```bash
cd wick-backend
./mvnw spring-boot:run -Dmaven.test.skip=true
# Port: 8080
```

**Android derle:**
```bash
cd wick-android
./gradlew :app:compileDebugKotlin
```

> Test kullanıcıları: `ahmet@test.com` / `zeynep@test.com` — şifre: `wick1234`

---

## Önemli Akışlar

| Akış | Detay |
|---|---|
| Kayıt | Register → profil kurulum → keşfet |
| Eşleşme | Like + karşılıklı like → match → davet → oturum → sohbet |
| Sohbet süresi | Oturum açıldıktan itibaren 24 saat (varsayılan), uzatılabilir (token harcar) |
| Super like | 1 jeton harcar, karşı tarafa özel bildirim gider |
| Kimlik doğrulama | Selfie yükle → admin onayı → `verified=true` + rozet |
| Token kazanma | Günlük giriş, kimlik doğrulama bonusu, satın alma |

---

## Dahili API (Admin)

`X-Internal-Key` header'ı gerektirir (`app.internal.secret` değeri):

```
GET  /internal/verify/pending        # Bekleyen kimlik doğrulama listesi
GET  /internal/verify/{userId}/photo # Selfie fotoğrafı
POST /internal/verify/{userId}/approve
POST /internal/verify/{userId}/reject
```

---

## Bilinen Eksikler / TODO

| # | Konu | Durum |
|---|---|---|
| TODO-1 | E-posta doğrulama | SMTP yapılandırması bekleniyor |
| TODO-18 | WebSocket arka plan davranışı | Cihaz testi gerekiyor |
