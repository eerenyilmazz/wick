package com.wick.legal.service.impl;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.ApiException;
import com.wick.legal.domain.LegalDocument;
import com.wick.legal.domain.LegalDocumentType;
import com.wick.legal.domain.UserConsent;
import com.wick.legal.repository.LegalDocumentRepository;
import com.wick.legal.repository.UserConsentRepository;
import com.wick.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegalServiceImplTest {

    @Mock
    private LegalDocumentRepository legalDocumentRepository;

    @Mock
    private UserConsentRepository userConsentRepository;

    private LegalServiceImpl legalService;

    private final User user = TestEntities.user("eren@example.com", "hash", "Eren");

    {
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        legalService = new LegalServiceImpl(legalDocumentRepository, userConsentRepository);
    }

    @Test
    void recordConsent_savesConsent_whenAcceptedVersionMatchesLatest() {
        LegalDocument latest = TestEntities.legalDocument(LegalDocumentType.PRIVACY_POLICY, 2, "content", Instant.now());
        when(legalDocumentRepository.findTopByTypeOrderByVersionDesc(LegalDocumentType.PRIVACY_POLICY))
                .thenReturn(Optional.of(latest));

        legalService.recordConsent(user, LegalDocumentType.PRIVACY_POLICY, 2);

        ArgumentCaptor<UserConsent> captor = ArgumentCaptor.forClass(UserConsent.class);
        verify(userConsentRepository).save(captor.capture());
        assertThat(captor.getValue().getVersion()).isEqualTo(2);
        assertThat(captor.getValue().getDocumentType()).isEqualTo(LegalDocumentType.PRIVACY_POLICY);
    }

    @Test
    void recordConsent_throws_whenAcceptedVersionIsStale() {
        LegalDocument latest = TestEntities.legalDocument(LegalDocumentType.TERMS_OF_SERVICE, 3, "content", Instant.now());
        when(legalDocumentRepository.findTopByTypeOrderByVersionDesc(LegalDocumentType.TERMS_OF_SERVICE))
                .thenReturn(Optional.of(latest));

        assertThatThrownBy(() -> legalService.recordConsent(user, LegalDocumentType.TERMS_OF_SERVICE, 2))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void getLatest_throws_whenNoDocumentPublished() {
        when(legalDocumentRepository.findTopByTypeOrderByVersionDesc(LegalDocumentType.PRIVACY_POLICY))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalService.getLatest(LegalDocumentType.PRIVACY_POLICY))
                .isInstanceOf(com.wick.common.exception.ResourceNotFoundException.class);
    }
}
