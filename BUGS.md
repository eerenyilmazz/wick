# Bug & Development Notes

Items marked ✅ **DONE** are resolved; the rest are still open. Turkish strings in quotes are user-facing app text, kept verbatim and followed by an English gloss.

## Open Items

### [TODO-1] Email verification flow (once SMTP is configured)
- **Status:** After registration the user is currently redirected straight to login.
- **To do:** Once SMTP is set up, add an email confirmation step to the registration flow and block sign-in for unconfirmed accounts.
- **Priority:** High

### [TODO-18] Background WebSocket behaviour needs testing
- **Problem:** It has not been tested whether the WebSocket connection drops when the app is backgrounded, or whether it reconnects on return to the foreground.
- **Note:** In the foreground the match modal arrives instantly ✓. After the app is closed and reopened in the background, it does not arrive (no FCM) ✗.
- **To do:** Push notification (FCM) integration is required for background notifications. The WebSocket only works in the foreground.
- **Priority:** High

## Completed Items

### [TODO-3] Profile photo grid layout must be dynamic ✅ DONE
- `PhotoGridSection.kt`: added a `columns` variable instead of `chunked(3)`. 1 cell → 1 column, 2 or 4 cells → 2 columns, otherwise 3 columns.
- **Priority:** Medium

### [TODO-2] Photo reordering via drag and drop ✅ DONE
- `PhotoGridSection.kt`: added an `onMovePhoto` parameter; long-press drag-and-drop is supported through `detectDragGesturesAfterLongPress`.
- Added `EditProfileAction.PhotoMoved` and `ProfileSetupBasicAction.PhotoMoved`; the ViewModels update the local list immediately.
- On submit, `photoRepository.reorderPhotos()` is called (in both the edit and setup flows).
- **Priority:** High

### [TODO-4] It is not obvious that tapping the profile photo opens the detail view ✅ DONE
- Added a tappable hint below `PhotoFan` ("Profilini önizle", "Preview your profile") in the primary colour.

### [BUG-4] Daily bonus claimed, but the UI still offers "claim" ✅ DONE
- `TokenWalletViewModel.refresh()` now also calls `checkDailyBonusStatus()`; the bonus status is re-queried every time the screen is resumed (ON_RESUME).

### [BUG-5] Token history: the same page could be opened twice ✅ DONE
- Added `navController.navigate(ProfileRoute.TransactionHistory) { launchSingleTop = true }`. Other navigations that lacked it (`AuthRoute.Register`, `NotificationPreferences`) were updated to use `launchSingleTop` as well.

### [BUG-6] Notifications page: settings icon in the top-right corner had the wrong colour ✅ DONE
- Changed `tint = WickColors.TextPrimary` to `tint = WickColors.Primary`.

### [BUG-7] Data export: success message appeared too far down ✅ DONE
- The success message in `ProfileHomeScreen` was removed from the end of the scrollable content and pinned below `WickTopBar`.

### [TODO-5] Account deletion: soft delete with a 3-day recovery window ✅ DONE
- `deleteAccount()`: email and password are left untouched; only `deletedAt = now` is set, so credentials are preserved for the 3-day window.
- `authenticate()`: if `deletedAt` is set, the account is reactivated automatically (reset to null) and an `ACCOUNT_REACTIVATED` event is logged.
- `AccountCleanupScheduler`: runs every night at 03:00 and anonymises accounts whose `deletedAt` is older than 3 days.
- Added `EventType.ACCOUNT_REACTIVATED`.
- Android: the "Hesabı Sil" ("Delete Account") modal text explains the 3-day reactivation window.

### [BUG-8] Discover: deleted users were still visible ✅ DONE
- Added the `and u.deleted_at is null` filter to the `UserRepository.findDiscoverableUsers()` query.

### [BUG-10] Match list did not update in real time ✅ DONE
- Fixed using the AppEventBus infrastructure built for BUG-12 and the FCM "MATCH" → `AppEvent.MatchCreated` pipeline.
- `MatchesViewModel` calls `loadAll()` on `AppEvent.MatchCreated`, `InvitationReceived`, `InvitationDeclined`, `SessionStarted` and `SessionLocked`.

### [BUG-11] Messages screen: stale local cache was not cleared while the app was open ✅ DONE
- `listConversations()` now fetches fresh data from the network first, updates Room and returns the fresh result. It falls back to the cache only on a network error.
- After a backend reset the empty list appears immediately instead of falling back to the old cache.

### [BUG-12] Real-time updates not working: multiple screens affected ✅ DONE
- **Screens:** Notifications, Incoming Invitations, Matches, Conversation list.
- **Problem:** The WebSocket connection was up, but no screen updated instantly:
  - When an invitation was sent, the recipient's notifications and invitations did not update.
  - When an invitation was accepted, the sender's screen did not move from "waiting" to "active chat".
  - When a match occurred, the matches list did not update.
  - When an invitation was accepted, the chat timer started but the sender was not told, so the time ran down unnoticed.
  - Switching tabs or reopening the app fixed it.
  - When an invitation was declined, the notification pop-up appeared ✓ but the matches screen did not reflect the PENDING → DECLINED state change ✗.
- **To do:** When a WebSocket event arrives, all related screens and ViewModels must refresh automatically.
- **Priority:** Critical

### [BUG-13] Incoming invitations: the same person appeared as two cards ✅ DONE
- `IncomingInvitationsSection` (with Accept/Decline buttons) and `MatchRow` (IncomingPending state) were showing the same match twice.
- The match list in `MatchesScreen` now excludes matches that are already in `incomingInvitations`.

### [BUG-14] Chat: a sent message appeared twice ✅ DONE
- Added a `clientMessageId` field to the backend `MessageResponse` record. Android `ChatViewModel.observeMessages` matches the WebSocket echo by `clientMessageId` and removes the message from the pending list.

### [TODO-6] Chat: show the sender's profile photo and name next to messages ✅ DONE
- `MessageBubble` updated: the other user's messages now show an avatar (photo or initial) and their name.

### [TODO-7] Show feedback to the user after a report ✅ DONE
- On a successful report, `reportedSuccessfully = true` is set, and `WickInlineSuccess` ("Şikayetin alındı", "Your report was received") is shown for 3 seconds before closing automatically.
- Implemented on both the Discover and Chat screens.

### [BUG-15] Blocking: chat and match stayed active ✅ DONE
- **Screens:** Matches, Chat.
- **Problem:** After one user blocked the other, both could keep messaging, the timer kept running, and the matches screen still showed "Aktif sohbet" ("Active chat").
- **To do:** Blocking must end the chat session immediately, hide the match, and make it impossible to send messages to the other party. The blocked user's profile photo and details must not appear in the matches list.
- **Priority:** Critical

### [TODO-8] Show the chat time remaining live in the matches list ✅ DONE
- Backend `MatchResponse` now includes `sessionEndsAt`.
- Android: `MatchSummary`, the DTO and the repository mapping were updated.
- `MatchRow` shows a live MM:SS countdown chip (via `produceState`) when a session is active, and a lock icon when the session is locked.

### [TODO-9] Chat: no "typing…" indicator ✅ DONE
- Backend: added a `/sessions/{id}/typing` endpoint in `SessionMessageController.java`; it publishes the userId to the `/topic/sessions/{id}/typing` topic.
- Android network: `ChatSocketClient` gained `sendTyping()` and `subscribeToTyping()`; `KrossbowChatSocketClient` implements them with a third STOMP subscription.
- Android data: delegates added to `SessionRepository` / `SessionRepositoryImpl`.
- Android state: `ChatUiState.isOtherTyping`; `ChatViewModel` throttles sends (2 s cooldown) and auto-clears an incoming typing signal after 3 s.
- UI: while `isOtherTyping` is true, `ChatScreen` shows a "%s yazıyor…" ("%s is typing…") label above the input bar.

### [TODO-10] Notify both sides when a chat session starts ✅ DONE
- `notifyInvitationAccepted()` now sends a SESSION_STARTED push notification to both the inviter and the accepter.

### [BUG-16] Token balance was not created at registration ✅ DONE
- Added a `tokenBalanceInitializer.ensureBalanceExists(saved)` call after registration in `UserServiceImpl.register()`.

### [BUG-17] Chat extension: insufficient-token error was not shown clearly ✅ DONE
- **Screen:** Chat extension modal.
- **Problem:** With no tokens, tapping the "extend with 1 token" button showed a generic "Bir hata oluştu" ("Something went wrong") error.
- **To do:** When the backend returns 402, the modal must show "Yeterli tokenin yok" ("You don't have enough tokens") and a "Token Satın Al" ("Buy Tokens") button that navigates to the token purchase screen.
- **Priority:** High

### [TODO-11] The "extend / end" choice at expiry must not be mandatory: add a close option ✅ DONE
- Added an X (Close icon) button to the top-right corner of the extend modal. This supplements the undiscoverable backdrop-tap dismissal with an explicit close option.

### [BUG-18] "End chat" did not work: session did not close ✅ DONE
- Backend: added the `POST /api/v1/sessions/{id}/end` endpoint and implemented `SessionService.end()` (participant check + `lock()` call).
- Android: added `SessionApi.end()`, `SessionRepository.endSession()` and `ChatViewModel.endSession()`; the "Sohbeti sonlandır" ("End chat") button now invokes the `onEndSession` callback.

### [BUG-19] A blocked user's photo was also hidden in the chat list ✅ DONE
- **Root cause:** `BlockServiceImpl.block()` → `sessionRepository.lockIfActive()` → `status=LOCKED` → `isExpired=true` → `InitialAvatar` hid the photo when `faded=true`.
- **Decision:** The photo should not be hidden; it should appear faded.
- **Fix:** Removed the `if (photoUrl != null && !faded)` condition in `InitialAvatar`. The photo is always shown; when `faded=true` it is rendered at 40% opacity via `graphicsLayer { alpha = 0.4f }`.

### [BUG-20] Matches list: last-message preview did not update ✅ DONE
- Added `AppEvent.MessageReceived`; it is emitted to `AppEventBus` when an FCM "MESSAGE" notification arrives.
- `ConversationsViewModel` listens for this event and refreshes the list, keeping the last-message preview current.

### [BUG-21] Chat list: countdown timer at the top loaded late ✅ DONE
- The static calculation in `ConversationRow` was replaced with `produceState` plus a 1-second loop; the timer now appears immediately and advances in real time.

### [BUG-22] After expiry, the matches list kept showing "Aktif sohbet" ✅ DONE
- Backend `MatchResponse` gained a `sessionStatus` field; `MatchMapper` returns the status of the latest session.
- Android `MatchSummary` and `MatchResponseDto` were updated; `MatchRow` shows "Sohbet sona erdi" ("Chat ended") when `sessionStatus == "LOCKED"`.

### [BUG-23] Entering an expired chat: messages were missing and the input stayed enabled ✅ DONE
- Backend: added the `GET /sessions/for-match` endpoint, which returns the latest session regardless of status.
- Android: `ChatViewModel.loadSession` now uses `getLatestSession`. For a LOCKED session it sets `sessionExpired = true`, does not connect to STOMP, and loads the message history.

### [BUG-24] Timer icon in the chat header did not turn red at the critical time ✅ DONE
- `CountdownPill` in `ChatScreen.kt` uses `val isUrgent = clamped in 1..300` and applies `WickColors.Urgency` to both the icon and the text, so both share the same threshold (300 s).

### [BUG-25] Expired chat disappeared from the messages list ✅ DONE
- **Screen:** Messages tab.
- **Problem:** On first entry an expired chat showed "Süresi doldu" ("Expired") with a lock icon; after leaving and returning it vanished and the screen showed "Aktif sohbet yok" ("No active chats").
- **To do:** Locked or expired chats must remain permanently visible in the messages list in a separate section (for example "Geçmiş Sohbetler", "Past Chats").
- **Priority:** High

### [TODO-12] Super like (star) had no special behaviour ✅ DONE
- Backend: `MatchServiceImpl.swipe()` spends tokens for the SUPER_LIKE direction (`TokenReason.SUPER_LIKE`) and calls `notifySuperLike()`.
- Backend: added `NotificationService.notifySuperLike()`, which sends the FCM notification "Seni çok beğendi!" ("They really like you!") and creates an inbox record (`InboxNotificationType.SUPER_LIKE`).
- Backend: added `AppConfigResponse.superLikeTokenCost`, `TokenProperties.superLikeCost` and `app.token.super-like-cost=1`.
- Android: updated `AppConfigResponseDto`, `AppConfig`, `ConfigRepositoryImpl`, `DiscoverUiState` and `DiscoverViewModel`.
- Android: added a `badge` parameter to the `SwipeButton` component; the star button shows its token cost.
- Android: the FCM `SUPER_LIKE` type is routed to `CHANNEL_MATCH` and emits `AppEvent.MatchCreated`.
- **Priority:** High

### [TODO-13] "Retry" invitations were unlimited, with no token cost or cap ✅ DONE
- Backend: `InvitationServiceImpl.send()` checks the DECLINED history; with one or more declines it charges tokens, and returns 429 once `maxRetryInvitations` (2) is exceeded.
- `InvitationRepository`: added `countByMatchIdAndSenderIdAndStatus()`.
- `InvitationProperties` and `application.properties`: added `maxRetryInvitations=2`.
- `AppConfigResponse`, `AppConfigResponseDto`, `AppConfig`: added the `maxRetryInvitations` field.
- Android UI: an "X token ile yeniden dene" ("Retry for X tokens") button, and a "Limit doldu" ("Limit reached") warning once the cap is hit.

### [TODO-15] Identity verification: no admin panel, moderation was manual ✅ DONE
- Backend: `GET /internal/verify/pending` lists pending verifications (userId, displayName, email, submittedAt, photoUrl).
- Backend: `GET /internal/verify/{userId}/photo` returns the verification selfie as JPEG (requires `X-Internal-Key`).
- Backend: added the `User.verificationSubmittedAt` field and a V13 migration; `submitVerification()` sets it.
- Backend: `VerificationTimeoutScheduler` runs every night at 03:30, automatically marks PENDING applications older than 7 days as REJECTED, and sends a notification.
- Android: added `VerificationUiState.isRejected`; the ViewModel detects the `REJECTED` state separately.
- Android: `RejectedContent` screen with a "Doğrulama Reddedildi" ("Verification Rejected") explanation, a "Tekrar Dene" ("Try Again", opens the camera) button and a "Geri Dön" ("Go Back") button.
- Note: Instead of a web admin panel, `curl` against `/internal/verify/pending`, inspecting the photo, and then calling `/approve` or `/reject` is sufficient.

### [TODO-14] No way to cancel a pending invitation ✅ DONE
- Backend: added the `CANCELLED` status, the `DELETE /api/v1/invitations/{id}` endpoint and `InvitationService.cancel()`.
- Android: added `InvitationApi.cancel()`, `InvitationRepository.cancelInvitation()` and `MatchesViewModel.cancelInvitation()`.
- UI: `MatchDetailScreen` shows a "Daveti Geri Çek" ("Withdraw Invitation") button in the "Bekleniyor" ("Waiting") state.

### [BUG-26] Push notifications had no sound or vibration ✅ DONE
- Added `enableVibration(true)` and a `vibrationPattern` to the `CHANNEL_MATCH` and `CHANNEL_CHAT` channels.
- The FCM service now routes the INVITE, SESSION_STARTED and SESSION_LOCKED types to `CHANNEL_MATCH` (IMPORTANCE_HIGH).

### [BUG-30] Tapping a notification did not open the relevant screen (no deep link) ✅ DONE
- MATCH, INVITE and SESSION_REMINDER → Matches; VERIFICATION → Profile (back).

### [TODO-19] Match modal had no action button ("Send invitation" or "Keep discovering") ✅ DONE
- "Davet Gönder" ("Send Invitation") → navigates to the Matches tab; "Keşfete Devam Et" ("Keep Discovering") → closes the modal.

### [TODO-17] Unread-notification badge on the profile tab ✅ DONE
- Added an orange dot badge to the profile tab icon on the Discover screen when there are unread notifications.

### [TODO-16] Add pull-to-refresh to every screen ✅ DONE
- Conversations, Notifications and TransactionHistory already had it.
- Added `PullToRefreshBox` to `MatchesScreen`; pulling down runs `retry()`.

### [BUG-27] Navigation broke on rapid repeated taps ✅ DONE
- Added a `rememberThrottledClick` (600 ms) utility to the design system.
- `WickButton`, `WickTopBar` (back button) and `WickBottomNav` (tab icons) are now throttled automatically.

### [BUG-2] Verified badge was confusing ✅ DONE
- Now uses `verificationStatus == "APPROVED"` instead of `user.verified` (email) in `DiscoverCard` and `ProfileDetail`.

### [BUG-1] Legal Documents screen had no back button ✅ DONE
- Added an `onBack` parameter to `LegalConsentRoute` / `LegalConsentScreen`.
- `WickTopBar(onBackClick = ...)` now returns to the document list while a document is open, and to the previous screen while on the list.
