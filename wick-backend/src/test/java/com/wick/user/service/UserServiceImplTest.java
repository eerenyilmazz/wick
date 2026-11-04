package com.wick.user.service;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.ApiException;
import com.wick.common.exception.ConflictException;
import com.wick.eventlog.service.EventLogService;
import com.wick.legal.service.LegalService;
import com.wick.location.service.LocationService;
import com.wick.user.config.UserProperties;
import com.wick.user.service.AuthRateLimiter;
import com.wick.user.domain.User;
import com.wick.user.dto.RegisterRequest;
import com.wick.user.dto.UpdateProfileRequest;
import com.wick.user.repository.DiscoveryPreferenceRepository;
import com.wick.user.repository.UserRepository;
import com.wick.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventLogService eventLogService;

    @Mock
    private LegalService legalService;

    @Mock
    private AuthRateLimiter authRateLimiter;

    @Mock
    private LocationService locationService;

    @Mock
    private DiscoveryPreferenceRepository discoveryPreferenceRepository;

    @Mock
    private com.wick.user.repository.PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private com.wick.token.service.impl.TokenBalanceInitializer tokenBalanceInitializer;

    private UserServiceImpl userService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        UserProperties userProperties = new UserProperties();
        userProperties.setMinimumAge(18);
        userProperties.setMaximumAge(100);
        userService = new UserServiceImpl(userRepository, passwordEncoder, userProperties, eventLogService, legalService, authRateLimiter, locationService, discoveryPreferenceRepository, passwordResetTokenRepository, tokenBalanceInitializer);
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("eren@example.com", "password123", "Eren", 1, 1);
        when(userRepository.existsByEmail("eren@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, org.mockito.Mockito.never()).saveAndFlush(any());
    }

    @Test
    void register_savesEncodedPassword_whenEmailIsNew() {
        RegisterRequest request = new RegisterRequest("eren@example.com", "password123", "Eren", 1, 1);
        when(userRepository.existsByEmail("eren@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-hash");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(request);

        assertThat(saved.getPasswordHash()).isEqualTo("encoded-hash");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("eren@example.com");
    }

    @Test
    void register_throwsConflict_whenSaveRacesWithConcurrentRegistration() {
        RegisterRequest request = new RegisterRequest("eren@example.com", "password123", "Eren", 1, 1);
        when(userRepository.existsByEmail("eren@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-hash");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate email"));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void authenticate_throwsUnauthorized_whenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate("missing@example.com", "any"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void authenticate_propagatesRateLimitException_whenLoginAttemptsExceeded() {
        org.mockito.Mockito.doThrow(new com.wick.common.exception.TooManyRequestsException("limit"))
                .when(authRateLimiter).consumeLoginAttemptOrThrow("eren@example.com");

        assertThatThrownBy(() -> userService.authenticate("eren@example.com", "any"))
                .isInstanceOf(com.wick.common.exception.TooManyRequestsException.class);

        verify(userRepository, org.mockito.Mockito.never()).findByEmail(any());
    }

    @Test
    void register_propagatesRateLimitException_whenRegisterAttemptsExceeded() {
        RegisterRequest request = new RegisterRequest("eren@example.com", "password123", "Eren", 1, 1);
        org.mockito.Mockito.doThrow(new com.wick.common.exception.TooManyRequestsException("limit"))
                .when(authRateLimiter).consumeRegisterAttemptOrThrow("eren@example.com");

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(com.wick.common.exception.TooManyRequestsException.class);

        verify(userRepository, org.mockito.Mockito.never()).saveAndFlush(any());
    }

    @Test
    void authenticate_throwsUnauthorized_whenPasswordDoesNotMatch() {
        User user = TestEntities.user("eren@example.com", "stored-hash", "Eren");
        when(userRepository.findByEmail("eren@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticate("eren@example.com", "wrong"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void authenticate_returnsUser_whenCredentialsAreValid() {
        User user = TestEntities.user("eren@example.com", "stored-hash", "Eren");
        ReflectionTestUtils.setField(user, "id", java.util.UUID.randomUUID());
        when(userRepository.findByEmail("eren@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        User result = userService.authenticate("eren@example.com", "correct");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void updateProfile_throwsBadRequest_whenUserIsUnderMinimumAge() {
        User user = TestEntities.user("eren@example.com", "stored-hash", "Eren");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        UpdateProfileRequest request = new UpdateProfileRequest(null, LocalDate.now().minusYears(17), null, null, null, null, null, null, null);

        assertThatThrownBy(() -> userService.updateProfile(user.getId(), request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void updateProfile_throwsBadRequest_whenUserExceedsMaximumAge() {
        User user = TestEntities.user("eren@example.com", "stored-hash", "Eren");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        UpdateProfileRequest request = new UpdateProfileRequest(null, LocalDate.now().minusYears(101), null, null, null, null, null, null, null);

        assertThatThrownBy(() -> userService.updateProfile(user.getId(), request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void updateProfile_updatesDateOfBirth_whenUserMeetsMinimumAge() {
        User user = TestEntities.user("eren@example.com", "stored-hash", "Eren");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDate dateOfBirth = LocalDate.now().minusYears(25);
        UpdateProfileRequest request = new UpdateProfileRequest(null, dateOfBirth, null, null, null, null, null, null, null);

        User result = userService.updateProfile(user.getId(), request);

        assertThat(result.getDateOfBirth()).isEqualTo(dateOfBirth);
    }

    @Test
    void deleteAccount_softDeletesAndKeepsCredentialsForRecoveryWindow() {
        User user = TestEntities.user("eren@example.com", "stored-hash", "Eren");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setBio("some bio");
        user.setFcmToken("some-fcm-token");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("stored-password", "stored-hash")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.deleteAccount(user.getId(), "stored-password");

        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("eren@example.com");
        assertThat(user.getBio()).isEqualTo("some bio");
        assertThat(user.getFcmToken()).isNull();
        assertThat(user.getDisplayName()).isEqualTo("Eren");
    }
}
