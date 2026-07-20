package com.wick.user.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.common.exception.ConflictException;
import com.wick.common.exception.ResourceNotFoundException;
import com.wick.eventlog.domain.EventType;
import com.wick.eventlog.service.EventLogService;
import com.wick.legal.domain.LegalDocumentType;
import com.wick.legal.service.LegalService;
import com.wick.location.service.LocationService;
import com.wick.user.config.UserProperties;
import com.wick.user.domain.DiscoveryPreference;
import com.wick.user.domain.Gender;
import com.wick.user.domain.User;
import com.wick.user.dto.RegisterRequest;
import com.wick.user.dto.UpdateDiscoveryPreferenceRequest;
import com.wick.user.dto.UpdateProfileRequest;
import com.wick.user.domain.PasswordResetToken;
import com.wick.user.repository.DiscoveryPreferenceRepository;
import com.wick.user.repository.PasswordResetTokenRepository;
import com.wick.user.repository.UserRepository;
import com.wick.token.service.impl.TokenBalanceInitializer;
import com.wick.user.service.AuthRateLimiter;
import com.wick.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final Duration PASSWORD_RESET_TOKEN_TTL = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProperties userProperties;
    private final EventLogService eventLogService;
    private final LegalService legalService;
    private final AuthRateLimiter authRateLimiter;
    private final LocationService locationService;
    private final DiscoveryPreferenceRepository discoveryPreferenceRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenBalanceInitializer tokenBalanceInitializer;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        authRateLimiter.consumeRegisterAttemptOrThrow(request.email());
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setVerified(true); // TODO-1: e-posta doğrulama aktif edilince kaldır
        User saved;
        try {
            saved = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException alreadyRegisteredConcurrently) {
            throw new ConflictException("Email is already registered");
        }
        legalService.recordConsent(saved, LegalDocumentType.PRIVACY_POLICY, request.acceptedPrivacyPolicyVersion());
        legalService.recordConsent(saved, LegalDocumentType.TERMS_OF_SERVICE, request.acceptedTermsVersion());
        tokenBalanceInitializer.ensureBalanceExists(saved);
        eventLogService.log(EventType.USER_REGISTERED, saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    public boolean isEmailAvailable(String email) {
        authRateLimiter.consumeEmailCheckAttemptOrThrow(email);
        return !userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User authenticate(String email, String rawPassword) {
        authRateLimiter.consumeLoginAttemptOrThrow(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        if (user.getDeletedAt() != null) {
            user.setDeletedAt(null);
            userRepository.save(user);
            eventLogService.log(EventType.ACCOUNT_REACTIVATED, user.getId(), null);
        }
        eventLogService.log(EventType.USER_LOGIN, user.getId(), null);
        return user;
    }

    @Override
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public Page<User> getDiscoverableUsers(UUID userId, Pageable pageable) {
        DiscoveryPreference preference = resolveDiscoveryPreference(userId);
        User viewer = getById(userId);
        LocalDate today = LocalDate.now();
        LocalDate maxBirthDate = today.minusYears(preference.getMinAge());
        LocalDate minBirthDate = today.minusYears(preference.getMaxAge() + 1).plusDays(1);
        List<String> genders = (preference.getGenders().isEmpty()
                ? Arrays.asList(Gender.values())
                : List.copyOf(preference.getGenders()))
                .stream().map(Enum::name).toList();
        return userRepository.findDiscoverableUsers(
                userId, maxBirthDate, minBirthDate, genders,
                preference.getRadiusKm(), viewer.getLatitude(), viewer.getLongitude(), pageable);
    }

    @Override
    public DiscoveryPreference getDiscoveryPreference(UUID userId) {
        return resolveDiscoveryPreference(userId);
    }

    @Override
    @Transactional
    public DiscoveryPreference updateDiscoveryPreference(UUID userId, UpdateDiscoveryPreferenceRequest request) {
        if (request.minAge() > request.maxAge()) {
            throw new ApiException("minAge cannot be greater than maxAge", HttpStatus.BAD_REQUEST);
        }
        DiscoveryPreference preference = discoveryPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    DiscoveryPreference created = new DiscoveryPreference();
                    created.setUser(getById(userId));
                    return created;
                });
        preference.setMinAge(request.minAge());
        preference.setMaxAge(request.maxAge());
        preference.setRadiusKm(request.radiusKm());
        preference.setGenders(request.genders());
        return discoveryPreferenceRepository.save(preference);
    }

    private DiscoveryPreference resolveDiscoveryPreference(UUID userId) {
        return discoveryPreferenceRepository.findByUserId(userId).orElseGet(() -> {
            DiscoveryPreference defaults = new DiscoveryPreference();
            defaults.setMinAge(userProperties.getMinimumAge());
            defaults.setMaxAge(userProperties.getMaximumAge());
            return defaults;
        });
    }

    @Override
    @Transactional
    public User updateProfile(UUID id, UpdateProfileRequest request) {
        User user = getById(id);
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.dateOfBirth() != null) {
            int age = Period.between(request.dateOfBirth(), LocalDate.now()).getYears();
            int minimumAge = userProperties.getMinimumAge();
            int maximumAge = userProperties.getMaximumAge();
            if (age < minimumAge) {
                throw new ApiException("You must be at least " + minimumAge + " years old", HttpStatus.BAD_REQUEST);
            }
            if (age > maximumAge) {
                throw new ApiException("Date of birth is invalid", HttpStatus.BAD_REQUEST);
            }
            user.setDateOfBirth(request.dateOfBirth());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.interests() != null) {
            user.setInterests(request.interests());
        }
        if (request.province() != null) {
            if (!locationService.isValidProvince(request.province())) {
                throw new ApiException("Unknown province", HttpStatus.BAD_REQUEST);
            }
            user.setProvince(request.province());
        }
        if (request.district() != null) {
            String effectiveProvince = request.province() != null ? request.province() : user.getProvince();
            if (effectiveProvince == null || !locationService.isValidDistrict(effectiveProvince, request.district())) {
                throw new ApiException("Unknown district for the selected province", HttpStatus.BAD_REQUEST);
            }
            user.setDistrict(request.district());
        }
        if (request.latitude() != null) {
            user.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            user.setLongitude(request.longitude());
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User verify(UUID id) {
        User user = getById(id);
        user.setVerified(true);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User grantPremium(UUID id) {
        User user = getById(id);
        user.setPremium(true);
        User saved = userRepository.save(user);
        eventLogService.log(EventType.PREMIUM_GRANTED, id, null);
        return saved;
    }

    @Override
    @Transactional
    public User revokePremium(UUID id) {
        User user = getById(id);
        user.setPremium(false);
        User saved = userRepository.save(user);
        eventLogService.log(EventType.PREMIUM_REVOKED, id, null);
        return saved;
    }

    @Override
    @Transactional
    public User updateDeviceToken(UUID id, String fcmToken) {
        User user = getById(id);
        user.setFcmToken(fcmToken);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAccount(UUID id, String rawPassword) {
        User user = getById(id);
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        user.setDeletedAt(Instant.now());
        user.setFcmToken(null);
        userRepository.save(user);
        eventLogService.log(EventType.ACCOUNT_DELETED, id, null);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) return;

        passwordResetTokenRepository.deleteAllByEmail(email);

        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(
                new PasswordResetToken(token, email, Instant.now().plus(PASSWORD_RESET_TOKEN_TTL)));

        log.debug("[DEV] Password reset link: wickapp://reset-password?token={}", token);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid or expired token", HttpStatus.BAD_REQUEST));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new ApiException("Invalid or expired token", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
