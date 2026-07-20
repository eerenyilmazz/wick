# Bug & Geliştirme Notları

## Yapılacaklar

### [TODO-1] Email doğrulama akışı (SMTP kurulunca)
- **Durum:** Şu an kayıt sonrası direkt login'e yönlendiriyor
- **Yapılacak:** SMTP kurulduktan sonra kayıt akışına email onay adımı eklenecek, onaysız giriş engellenecek
- **Öncelik:** Yüksek

### [TODO-3] Profil fotoğraf grid tasarımı dinamik olmalı ✅ TAMAMLANDI
- `PhotoGridSection.kt`: `chunked(3)` yerine `columns` değişkeni eklendi — 1 hücre→1 kolon, 2/4 hücre→2 kolon, diğerleri→3 kolon
- **Öncelik:** Orta

### [TODO-2] Fotoğraf sıralama — sürükle bırak ✅ TAMAMLANDI
- `PhotoGridSection.kt`: `onMovePhoto` parametresi eklendi, `detectDragGesturesAfterLongPress` ile uzun basıp sürükle-bırak desteği
- `EditProfileAction.PhotoMoved` + `ProfileSetupBasicAction.PhotoMoved` eklendi, ViewModel'lar local listeyi anlık güncelliyor
- Submit sırasında `photoRepository.reorderPhotos()` çağrılıyor (hem edit hem setup flow'da)
- **Öncelik:** Yüksek

### [TODO-4] Profil fotoğrafına tıklayınca detay açıldığı belli değil ✅ TAMAMLANDI
- `PhotoFan`'ın altına "Profilini önizle" tıklanabilir ipucu metni eklendi (Primary renk)


### [BUG-4] Günlük bonus — alındı ama UI tekrar "al" gösteriyor ✅ TAMAMLANDI
- `TokenWalletViewModel.refresh()` artık `checkDailyBonusStatus()` de çağırıyor; ekrana her dönüşte (ON_RESUME) bonus durumu yeniden sorgulanıyor

### [BUG-5] Token geçmişi — aynı sayfaya iki kez girilebiliyor ✅ TAMAMLANDI
- `navController.navigate(ProfileRoute.TransactionHistory) { launchSingleTop = true }` eklendi; diğer eksik navigasyonlar da (`AuthRoute.Register`, `NotificationPreferences`) `launchSingleTop` ile güncellendi

### [BUG-6] Bildirimler sayfası — sağ köşedeki ayarlar ikonu rengi yanlış ✅ TAMAMLANDI
- `tint = WickColors.TextPrimary` → `tint = WickColors.Primary` olarak değiştirildi

### [BUG-7] Veri dışa aktarma — başarı mesajı çok aşağıda kalıyor ✅ TAMAMLANDI
- Başarı mesajı `ProfileHomeScreen`'de scrollable içeriğin sonundan çıkarıldı, `WickTopBar` altına sabit konuma taşındı

### [TODO-5] Hesap silme — soft delete + 3 günlük geri alma süreci ✅ TAMAMLANDI
- `deleteAccount()`: email/şifre değiştirilmiyor, sadece `deletedAt = now` set ediliyor (3 günlük pencere için kimlik bilgileri korunuyor)
- `authenticate()`: `deletedAt` set ise otomatik reaktivasyon (null'a çekiliyor), `ACCOUNT_REACTIVATED` event log'lanıyor
- `AccountCleanupScheduler`: her gece 03:00'te çalışır, `deletedAt < 3 gün` geçmiş hesapları anonimleştirir
- `EventType.ACCOUNT_REACTIVATED` eklendi
- Android: "Hesabı Sil" modal metni 3 günlük reaksivasyon penceresini açıklıyor

### [BUG-8] Keşfet — silinmiş kullanıcılar hâlâ görünüyor ✅ TAMAMLANDI
- `UserRepository.findDiscoverableUsers()` sorgusuna `and u.deleted_at is null` filtresi eklendi


### [BUG-10] Match olunca liste gerçek zamanlı güncellenmiyor ✅ TAMAMLANDI
- BUG-12'de kurulan AppEventBus altyapısı + FCM "MATCH" → AppEvent.MatchCreated pipeline'ı ile çözüldü
- `MatchesViewModel` `AppEvent.MatchCreated/InvitationReceived/InvitationDeclined/SessionStarted/SessionLocked` event'lerinde `loadAll()` çağırıyor

### [BUG-11] Mesajlar ekranı — eski local cache uygulama açıkken temizlenmiyor ✅ TAMAMLANDI
- `listConversations()` artık önce network'ten taze veri çekiyor, Room'u güncelliyor ve taze sonucu döndürüyor; sadece network hatasında cached'e fallback yapıyor
- Backend sıfırlanınca boş liste anında görünür, eski cache'e dönülmez

### [BUG-12] Gerçek zamanlı güncelleme çalışmıyor — birden fazla ekran etkileniyor ✅ TAMAMLANDI
- **Ekranlar:** Bildirimler, Gelen Davetler, Eşleşmeler, Sohbet listesi
- **Sorun:** WebSocket bağlantısı var ama hiçbir ekran anlık güncellenmiyor:
  - Davet gönderilince karşı tarafın bildirimleri/davetleri gelmiyor
  - Davet kabul edilince gönderenin ekranı "Bekleniyor"dan "Aktif sohbet"e geçmiyor
  - Match olunca eşleşmeler listesi güncellenmiyor
  - Davet kabul edilince sohbet süresi başlıyor ama davet gönderen (Eren) bundan haberdar edilmiyor — süre farkında olmadan akıyor
  - Tab değiştirince ya da uygulama yeniden açılınca düzeliyor
  - Davet reddedilince bildirim pop-up'ı geliyor ✓ ama matches ekranı PENDING→DECLINED state değişimini yansıtmıyor ✗
- **Yapılacak:** WebSocket event'leri gelince ilgili tüm ekranlar/ViewModel'lar otomatik yenilenmeli
- **Öncelik:** Kritik

### [BUG-13] Gelen davetler — aynı kişiden 2 kart görünüyor ✅ TAMAMLANDI
- `IncomingInvitationsSection` (Accept/Decline butonlu) + `MatchRow` (IncomingPending state) aynı match'i iki kez gösteriyordu
- `MatchesScreen`'deki match listesi artık `incomingInvitations`'da olan match'leri filtreli hariç tutuyor

### [BUG-14] Sohbet — gönderilen mesaj 2 kez görünüyor ✅ TAMAMLANDI
- Backend `MessageResponse` kaydına `clientMessageId` alanı eklendi; Android `ChatViewModel.observeMessages` WebSocket echo'sunu `clientMessageId` ile eşleştirip pending listeden çıkarıyor

### [TODO-6] Sohbet — mesajların yanında profil fotoğrafı ve isim gösterilmeli ✅ TAMAMLANDI
- `MessageBubble` güncellendi: karşı tarafın mesajlarının yanında avatar (fotoğraf veya baş harf) + isim gösteriliyor

### [TODO-7] Şikayet sonrası kullanıcıya feedback gösterilmeli ✅ TAMAMLANDI
- Şikayet başarılı olunca `reportedSuccessfully = true` set ediliyor; ekranda 3 saniye `WickInlineSuccess` ("Şikayetin alındı") gösteriliyor, sonra otomatik kapanıyor
- Hem Keşfet ekranı hem Sohbet ekranı için uygulandı

### [BUG-15] Engelleme — sohbet ve eşleşme hâlâ aktif kalıyor ✅ TAMAMLANDI
- **Ekran:** Eşleşmeler, Sohbet
- **Sorun:** Elif Eren'i engelledikten sonra ikisi de mesaj atmaya devam edebiliyor, süre akıyor, eşleşmeler ekranında "Aktif sohbet" yazıyor
- **Yapılacak:** Engelleme anında sohbet oturumu sonlandırılmalı, eşleşme gizlenmeli, karşı tarafa mesaj gönderilemez hale gelmeli. Eşleşmeler listesinde engellenen kullanıcının profil fotoğrafı ve detayları görünmemeli
- **Öncelik:** Kritik

### [TODO-8] Eşleşmeler listesinde sohbet süresi canlı gösterilmeli ✅ TAMAMLANDI
- Backend `MatchResponse`'a `sessionEndsAt` eklendi
- Android: `MatchSummary`, DTO, Repository mapping güncellendi
- `MatchRow`'da aktif session varsa `produceState` ile canlı MM:SS countdown chip gösteriliyor; kilitli session'da kilit ikonu çıkıyor

### [TODO-9] Sohbet — "yazıyor..." göstergesi yok ✅ TAMAMLANDI
- Backend: `SessionMessageController.java` → `/sessions/{id}/typing` endpoint eklendi, `/topic/sessions/{id}/typing` konusuna userId yayınlıyor
- Android network: `ChatSocketClient` → `sendTyping()` + `subscribeToTyping()` eklendi; `KrossbowChatSocketClient` uygulama + üçüncü STOMP subscription
- Android data: `SessionRepository` / `SessionRepositoryImpl` delegate eklendi
- Android state: `ChatUiState.isOtherTyping` + `ChatViewModel` → throttled send (2s cooldown), incoming typing ile 3s auto-clear
- UI: `ChatScreen` → `isOtherTyping` true iken "%s yazıyor…" metni input bar üstünde gösteriliyor

### [TODO-10] Sohbet süresi başlayınca iki tarafa da bildirim gönderilmeli ✅ TAMAMLANDI
- `notifyInvitationAccepted()` artık hem daveti gönderene hem kabul edene SESSION_STARTED push notification gönderiyor

### [BUG-16] Kayıt olunca token bakiyesi oluşturulmuyor ✅ TAMAMLANDI
- `UserServiceImpl.register()` içinde kayıt sonrasına `tokenBalanceInitializer.ensureBalanceExists(saved)` çağrısı eklendi

### [BUG-17] Sohbet uzatma — yetersiz token hatası kullanıcıya açıkça gösterilmiyor ✅ TAMAMLANDI
- **Ekran:** Sohbet uzatma modalı
- **Sorun:** Token yokken "1 token ile uzat" butonuna basınca genel "Bir hata oluştu" çıkıyor
- **Yapılacak:** Backend 402 dönünce modal üzerinde "Yeterli tokenin yok" mesajı ve "Token Satın Al" butonu gösterilmeli, butona basınca token satın alma ekranına yönlendirilmeli
- **Öncelik:** Yüksek

### [TODO-11] Süre dolunca "Uzatma / Sonlandır" seçimi zorunlu olmamalı — kapat seçeneği eklenmeli ✅ TAMAMLANDI
- Extend modal'ın sağ üst köşesine X butonu (Close icon) eklendi; backdrop tıklamasının keşfedilemez davranışına ek olarak açık kapat seçeneği mevcut

### [BUG-18] "Sohbeti sonlandır" çalışmıyor — session kapanmıyor ✅ TAMAMLANDI
- Backend: `POST /api/v1/sessions/{id}/end` endpoint'i eklendi, `SessionService.end()` implement edildi (katılımcı kontrolü + `lock()` çağrısı)
- Android: `SessionApi.end()`, `SessionRepository.endSession()`, `ChatViewModel.endSession()` eklendi; "Sohbeti sonlandır" butonu artık `onEndSession` callback'ini çağırıyor

### [BUG-19] Engellenen kullanıcının fotoğrafı sohbet listesinde de gizleniyor ✅ TAMAMLANDI
- **Kök neden:** `BlockServiceImpl.block()` → `sessionRepository.lockIfActive()` → `status=LOCKED` → `isExpired=true` → `InitialAvatar` fotoğrafı `faded=true` ile gizliyordu
- **Karar:** Fotoğraf gizlenmemeli; soluk görünmeli
- **Çözüm:** `InitialAvatar`'da `if (photoUrl != null && !faded)` koşulu kaldırıldı; fotoğraf her zaman gösteriliyor, `faded=true` ise `graphicsLayer { alpha = 0.4f }` ile %40 opaklığa düşürülüyor

### [BUG-20] Eşleşmeler listesi — son mesaj önizlemesi güncellenmiyor ✅ TAMAMLANDI
- `AppEvent.MessageReceived` eklendi; FCM "MESSAGE" bildirimi gelince `AppEventBus`'a emit ediliyor
- `ConversationsViewModel` bu event'i dinleyip listeyi yeniliyor; son mesaj önizlemesi güncel kalıyor

### [BUG-21] Sohbet listesi — üstteki süre sayacı geç yükleniyor ✅ TAMAMLANDI
- `ConversationRow`'daki statik hesaplama `produceState` + 1 saniyelik döngüye çevrildi; sayaç anında görünüyor ve gerçek zamanlı ilerliyor

### [BUG-22] Süre dolunca eşleşmeler listesi "Aktif sohbet" yazmaya devam ediyor ✅ TAMAMLANDI
- Backend `MatchResponse`'a `sessionStatus` alanı eklendi; `MatchMapper` en son session'ın statusunu döndürüyor
- Android `MatchSummary`, `MatchResponseDto` güncellendi; `MatchRow` `sessionStatus == "LOCKED"` ise "Sohbet sona erdi" gösteriyor

### [BUG-23] Süresi dolan sohbete girilince mesajlar görünmüyor ve mesaj kutusu açık kalıyor ✅ TAMAMLANDI
- Backend: `GET /sessions/for-match` endpoint'i eklendi — status'tan bağımsız son session'ı döndürüyor
- Android: `ChatViewModel.loadSession` artık `getLatestSession` kullanıyor; LOCKED session'da `sessionExpired = true` set ediliyor, STOMP'a bağlanılmıyor, mesaj geçmişi yükleniyor

### [BUG-24] Sohbet başlığındaki süre ikonu kritik sürede kırmızıya dönmüyor ✅ TAMAMLANDI
- `ChatScreen.kt`'deki `CountdownPill`, `val isUrgent = clamped in 1..300` ile hem ikon hem metin için `WickColors.Urgency` kullanıyor — her iki yerde aynı eşik (300s)

### [BUG-25] Süresi dolmuş sohbet mesajlar listesinden kayboluyor ✅ TAMAMLANDI
- **Ekran:** Mesajlar tabı
- **Sorun:** Süresi dolmuş sohbet ilk girişte "Süresi doldu" + kilit ikonu ile görünüyor, çıkıp tekrar girilince kayboluyor ve "Aktif sohbet yok" yazıyor
- **Yapılacak:** Kilitli/süresi dolmuş sohbetler mesajlar listesinde ayrı bir bölümde ("Geçmiş Sohbetler" vb.) kalıcı olarak görünmeli
- **Öncelik:** Yüksek

### [TODO-12] Super like (yıldız) özel davranış yok ✅ TAMAMLANDI
- Backend: `MatchServiceImpl.swipe()` SUPER_LIKE yönünde token harcıyor (`TokenReason.SUPER_LIKE`) + `notifySuperLike()` çağırıyor
- Backend: `NotificationService.notifySuperLike()` eklendi — "Seni çok beğendi!" FCM bildirimi + inbox kaydı (`InboxNotificationType.SUPER_LIKE`)
- Backend: `AppConfigResponse.superLikeTokenCost` eklendi; `TokenProperties.superLikeCost` + `app.token.super-like-cost=1`
- Android: `AppConfigResponseDto` / `AppConfig` / `ConfigRepositoryImpl` / `DiscoverUiState` / `DiscoverViewModel` güncellendi
- Android: `SwipeButton` bileşenine `badge` parametresi eklendi — yıldız butonunda token maliyeti gösteriliyor
- Android: FCM `SUPER_LIKE` tipi `CHANNEL_MATCH`'e yönlendirildi, `AppEvent.MatchCreated` emit ediliyor
- **Öncelik:** Yüksek

### [TODO-13] "Yeniden dene" daveti sınırsız — token maliyeti ve limit yok ✅ TAMAMLANDI
- Backend: `InvitationServiceImpl.send()` DECLINED geçmişi kontrol ediyor; 1+ red varsa token ödettiriyor, `maxRetryInvitations` (2) aşılınca 429 dönüyor
- `InvitationRepository`: `countByMatchIdAndSenderIdAndStatus()` eklendi
- `InvitationProperties` + `application.properties`: `maxRetryInvitations=2` eklendi
- `AppConfigResponse` / `AppConfigResponseDto` / `AppConfig`: `maxRetryInvitations` alanı eklendi
- Android UI: "X token ile yeniden dene" butonu, limit aşılınca "Limit doldu" uyarısı

### [TODO-15] Kimlik doğrulama — admin paneli yok, moderasyon manuel ✅ TAMAMLANDI
- Backend: `GET /internal/verify/pending` — bekleyen doğrulamaları listeler (userId, displayName, email, submittedAt, photoUrl)
- Backend: `GET /internal/verify/{userId}/photo` — doğrulama selfie'sini JPEG olarak döner (X-Internal-Key gerekli)
- Backend: `User.verificationSubmittedAt` alanı + V13 migration eklendi; `submitVerification()` bu alanı set ediyor
- Backend: `VerificationTimeoutScheduler` — her gece 03:30'da 7 gün bekleyen PENDING başvuruları otomatik REJECTED yapar + bildirim gönderir
- Android: `VerificationUiState.isRejected` eklendi; `REJECTED` durumu VM'de ayrı tespit ediliyor
- Android: `RejectedContent` ekranı — "Doğrulama Reddedildi" açıklaması + "Tekrar Dene" (kameraya gönderir) + "Geri Dön" butonu
- Not: Admin web paneli yerine `curl` + `/internal/verify/pending` → foto incelenip `/approve` veya `/reject` şeklinde yeterli

### [TODO-14] Bekleyen daveti iptal etme özelliği yok ✅ TAMAMLANDI
- Backend: `CANCELLED` status eklendi, `DELETE /api/v1/invitations/{id}` endpoint'i + `InvitationService.cancel()` implement edildi
- Android: `InvitationApi.cancel()`, `InvitationRepository.cancelInvitation()`, `MatchesViewModel.cancelInvitation()` eklendi
- UI: `MatchDetailScreen`'de "Bekleniyor" durumunda "Daveti Geri Çek" butonu görünüyor

### [BUG-26] Push bildirimde ses/titreşim gelmiyor ✅ TAMAMLANDI
- `CHANNEL_MATCH` ve `CHANNEL_CHAT` kanallarına `enableVibration(true)` + `vibrationPattern` eklendi
- FCM servisinde INVITE/SESSION_STARTED/SESSION_LOCKED tipler artık `CHANNEL_MATCH`'e (IMPORTANCE_HIGH) yönlendiriliyor

### [BUG-30] Bildirime tıklayınca ilgili ekrana gitmiyor (deep link yok) ✅ TAMAMLANDI
- MATCH/INVITE/SESSION_REMINDER → Eşleşmeler, VERIFICATION → Profil (geri)

### [TODO-19] Match modalında aksiyon butonu yok — "Davet gönder" veya "Keşfete devam et" ✅ TAMAMLANDI
- "Davet Gönder" → Eşleşmeler tabına yönlendirir, "Keşfete Devam Et" → modalı kapatır


### [TODO-17] Okunmamış bildirim badge profil tab'ında ✅ TAMAMLANDI
- Keşfet ekranındaki profil tab ikonuna okunmamış bildirim varsa turuncu nokta badge eklendi

### [TODO-18] Uygulama arka plandayken WebSocket davranışı test edilmeli
- **Sorun:** Uygulama arka plana alındığında WebSocket bağlantısının kopup kopmadığı, ön plana dönünce yeniden bağlanıp bağlanmadığı test edilmedi
- **Not:** Foreground'da match modalı anında geliyor ✓ — arka planda uygulama kapatılıp açılınca geliyor (FCM yok) ✗
- **Yapılacak:** Push notification (FCM) entegrasyonu arka plan bildirimleri için şart — WebSocket yalnızca foreground'da çalışır, arka plan için FCM gerekli
- **Öncelik:** Yüksek

### [TODO-16] Her ekrana pull-to-refresh ekle ✅ TAMAMLANDI
- Conversations, Notifications, TransactionHistory ekranlarında zaten vardı
- `MatchesScreen`'e `PullToRefreshBox` eklendi; yukarı çekince `retry()` çalışıyor


### [BUG-27] Hızlı art arda tıklamada navigasyon bozuluyor ✅ TAMAMLANDI
- `rememberThrottledClick` (600ms) utility'si designsystem'e eklendi
- `WickButton`, `WickTopBar` (geri butonu), `WickBottomNav` (tab ikonları) otomatik olarak throttle'landı

## Açık Buglar

### [BUG-2] Verified rozeti kafa karıştırıcı ✅ TAMAMLANDI
- `user.verified` (email) yerine `verificationStatus == "APPROVED"` kullanılıyor artık (DiscoverCard + ProfileDetail)



### [BUG-1] Yasal Belgeler ekranında geri butonu yok ✅ TAMAMLANDI
- `LegalConsentRoute`/`LegalConsentScreen`'e `onBack` parametresi eklendi
- `WickTopBar(onBackClick = ...)` artık belge görüntülenirken belge listesine, listede iken önceki ekrana döner
