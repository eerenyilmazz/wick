package com.wick.notification.service.impl;

import com.wick.notification.domain.NotificationPreferences;
import com.wick.notification.dto.NotificationPreferencesResponse;
import com.wick.notification.dto.UpdateNotificationPreferencesRequest;
import com.wick.notification.repository.NotificationPreferencesRepository;
import com.wick.notification.service.NotificationPreferencesService;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationPreferencesServiceImpl implements NotificationPreferencesService {

    private final NotificationPreferencesRepository repository;
    private final UserService userService;

    @Override
    public NotificationPreferencesResponse getPreferences(UUID userId) {
        return repository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> new NotificationPreferencesResponse(true, true, true, false, false));
    }

    @Override
    @Transactional
    public NotificationPreferencesResponse updatePreferences(UUID userId, UpdateNotificationPreferencesRequest request) {
        NotificationPreferences prefs = repository.findByUserId(userId).orElseGet(() -> {
            User user = userService.getById(userId);
            NotificationPreferences newPrefs = new NotificationPreferences();
            newPrefs.setUser(user);
            return newPrefs;
        });

        prefs.setNewMatches(request.newMatches());
        prefs.setInvites(request.invites());
        prefs.setMessages(request.messages());
        prefs.setReminders(request.reminders());
        prefs.setPromos(request.promos());

        return toResponse(repository.save(prefs));
    }

    private NotificationPreferencesResponse toResponse(NotificationPreferences prefs) {
        return new NotificationPreferencesResponse(
                prefs.isNewMatches(),
                prefs.isInvites(),
                prefs.isMessages(),
                prefs.isReminders(),
                prefs.isPromos()
        );
    }
}
