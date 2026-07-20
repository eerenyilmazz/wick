package com.wick.photo.service.impl;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.ApiException;
import com.wick.photo.config.PhotoProperties;
import com.wick.photo.domain.UserPhoto;
import com.wick.photo.repository.UserPhotoRepository;
import com.wick.photo.service.PhotoProcessor;
import com.wick.photo.service.PhotoStorageService;
import com.wick.photo.service.PhotoUploadRateLimiter;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPhotoServiceImplTest {

    @Mock
    private UserPhotoRepository userPhotoRepository;

    @Mock
    private PhotoStorageService photoStorageService;

    @Mock
    private PhotoProcessor photoProcessor;

    @Mock
    private UserService userService;

    @Mock
    private PhotoUploadRateLimiter photoUploadRateLimiter;

    private PhotoProperties photoProperties;
    private UserPhotoServiceImpl userPhotoService;

    private User user;

    @BeforeEach
    void setUp() {
        photoProperties = new PhotoProperties();
        photoProperties.setMaxPhotosPerUser(6);

        userPhotoService = new UserPhotoServiceImpl(userPhotoRepository, photoStorageService, photoProcessor, photoProperties, userService, photoUploadRateLimiter);

        user = TestEntities.user("a@example.com", "hash", "A");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    }

    private MultipartFile jpegFile() {
        return new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Test
    void upload_throwsBadRequest_whenContentTypeNotAllowed() {
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> userPhotoService.upload(user.getId(), file))
                .isInstanceOf(ApiException.class);

        verify(photoProcessor, never()).process(any());
        verify(photoStorageService, never()).store(any());
    }

    @Test
    void upload_throwsBadRequest_whenContentTypeIsWebp() {
        MultipartFile file = new MockMultipartFile("file", "photo.webp", "image/webp", new byte[]{1});

        assertThatThrownBy(() -> userPhotoService.upload(user.getId(), file))
                .isInstanceOf(ApiException.class);

        verify(photoProcessor, never()).process(any());
    }

    @Test
    void upload_throwsConflict_whenMaxPhotosReached() {
        when(userPhotoRepository.countByUserId(user.getId())).thenReturn(6);

        assertThatThrownBy(() -> userPhotoService.upload(user.getId(), jpegFile()))
                .isInstanceOf(ApiException.class);

        verify(photoProcessor, never()).process(any());
        verify(photoStorageService, never()).store(any());
    }

    @Test
    void upload_propagatesRateLimitException_whenDailyUploadLimitExceeded() {
        org.mockito.Mockito.doThrow(new com.wick.common.exception.TooManyRequestsException("limit"))
                .when(photoUploadRateLimiter).consumeOrThrow(user.getId());

        assertThatThrownBy(() -> userPhotoService.upload(user.getId(), jpegFile()))
                .isInstanceOf(com.wick.common.exception.TooManyRequestsException.class);

        verify(photoProcessor, never()).process(any());
        verify(photoStorageService, never()).store(any());
    }

    @Test
    void upload_processesThenStoresPhotoAtNextDisplayOrder_whenValid() {
        byte[] resizedJpeg = new byte[]{9, 9, 9};
        when(userPhotoRepository.countByUserId(user.getId())).thenReturn(2);
        when(userService.getById(user.getId())).thenReturn(user);
        when(photoProcessor.process(any())).thenReturn(resizedJpeg);
        when(photoStorageService.store(resizedJpeg)).thenReturn("stored-key.jpg");
        when(userPhotoRepository.save(any(UserPhoto.class))).thenAnswer(inv -> inv.getArgument(0));

        UserPhoto result = userPhotoService.upload(user.getId(), jpegFile());

        assertThat(result.getDisplayOrder()).isEqualTo(2);
        assertThat(result.getStorageKey()).isEqualTo("stored-key.jpg");
        verify(photoStorageService).store(resizedJpeg);
    }

    @Test
    void upload_reusesFreedDisplayOrder_whenUploadedAfterADeletion() {
        when(userPhotoRepository.countByUserId(user.getId())).thenReturn(0);
        when(userService.getById(user.getId())).thenReturn(user);
        when(photoProcessor.process(any())).thenReturn(new byte[]{1});
        when(photoStorageService.store(any())).thenReturn("replacement-key.jpg");
        when(userPhotoRepository.save(any(UserPhoto.class))).thenAnswer(inv -> inv.getArgument(0));

        UserPhoto result = userPhotoService.upload(user.getId(), jpegFile());

        assertThat(result.getDisplayOrder()).isZero();
        assertThat(result.getStorageKey()).isEqualTo("replacement-key.jpg");
    }

    @Test
    void delete_throwsForbidden_whenUserDoesNotOwnPhoto() {
        User otherUser = TestEntities.user("b@example.com", "hash", "B");
        ReflectionTestUtils.setField(otherUser, "id", UUID.randomUUID());
        UserPhoto photo = TestEntities.userPhoto(otherUser, "key.jpg", 0);
        UUID photoId = UUID.randomUUID();
        ReflectionTestUtils.setField(photo, "id", photoId);
        when(userPhotoRepository.findById(photoId)).thenReturn(java.util.Optional.of(photo));

        assertThatThrownBy(() -> userPhotoService.delete(user.getId(), photoId))
                .isInstanceOf(ApiException.class);

        verify(photoStorageService, never()).delete(any());
    }

    @Test
    void delete_removesFileAndRepacksRemainingDisplayOrder() {
        UserPhoto toDelete = TestEntities.userPhoto(user, "key-0.jpg", 0);
        UUID photoId = UUID.randomUUID();
        ReflectionTestUtils.setField(toDelete, "id", photoId);
        when(userPhotoRepository.findById(photoId)).thenReturn(java.util.Optional.of(toDelete));

        UserPhoto remaining = TestEntities.userPhoto(user, "key-1.jpg", 1);
        when(userPhotoRepository.findAllByUserIdOrderByDisplayOrderAsc(user.getId()))
                .thenReturn(List.of(remaining));
        when(userPhotoRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        userPhotoService.delete(user.getId(), photoId);

        verify(photoStorageService).delete("key-0.jpg");
        verify(userPhotoRepository).delete(toDelete);
        assertThat(remaining.getDisplayOrder()).isZero();
    }

    @Test
    void reorder_throwsBadRequest_whenPhotoIdsDoNotMatchExisting() {
        UserPhoto photo = TestEntities.userPhoto(user, "key.jpg", 0);
        UUID existingId = UUID.randomUUID();
        ReflectionTestUtils.setField(photo, "id", existingId);
        when(userPhotoRepository.findAllByUserIdOrderByDisplayOrderAsc(user.getId())).thenReturn(List.of(photo));

        assertThatThrownBy(() -> userPhotoService.reorder(user.getId(), List.of(UUID.randomUUID())))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void reorder_updatesDisplayOrder_whenIdsMatchExisting() {
        UserPhoto photoA = TestEntities.userPhoto(user, "a.jpg", 0);
        UUID idA = UUID.randomUUID();
        ReflectionTestUtils.setField(photoA, "id", idA);
        UserPhoto photoB = TestEntities.userPhoto(user, "b.jpg", 1);
        UUID idB = UUID.randomUUID();
        ReflectionTestUtils.setField(photoB, "id", idB);

        when(userPhotoRepository.findAllByUserIdOrderByDisplayOrderAsc(user.getId()))
                .thenReturn(List.of(photoA, photoB));
        when(userPhotoRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<UserPhoto> result = userPhotoService.reorder(user.getId(), List.of(idB, idA));

        assertThat(photoB.getDisplayOrder()).isZero();
        assertThat(photoA.getDisplayOrder()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(idB);
    }

    @Test
    void deleteAllForUser_deletesStorageFilesAndAllRows() {
        UserPhoto photoA = TestEntities.userPhoto(user, "a.jpg", 0);
        UserPhoto photoB = TestEntities.userPhoto(user, "b.jpg", 1);
        when(userPhotoRepository.findAllByUserIdOrderByDisplayOrderAsc(user.getId()))
                .thenReturn(List.of(photoA, photoB));

        userPhotoService.deleteAllForUser(user.getId());

        verify(photoStorageService).delete("a.jpg");
        verify(photoStorageService).delete("b.jpg");
        verify(userPhotoRepository).deleteAll(List.of(photoA, photoB));
    }
}
