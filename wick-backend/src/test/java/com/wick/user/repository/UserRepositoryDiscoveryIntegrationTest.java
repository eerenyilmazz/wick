package com.wick.user.repository;

import com.wick.testsupport.TestEntities;

import com.wick.AbstractIntegrationTest;
import com.wick.match.repository.LikeRepository;
import com.wick.photo.repository.UserPhotoRepository;
import com.wick.safety.repository.BlockRepository;
import com.wick.user.domain.Gender;
import com.wick.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class UserRepositoryDiscoveryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private UserPhotoRepository userPhotoRepository;

    private User completeUser(String email, String displayName) {
        User user = TestEntities.user(email, "hash", displayName);
        user.setDateOfBirth(LocalDate.now().minusYears(25));
        user.setGender(Gender.MALE);
        user.setProvince("Istanbul");
        user.setDistrict("Kadikoy");
        user.setBio("Hello there");
        return user;
    }

    @Test
    void findDiscoverableUsers_excludesSelfAlreadyLikedAndBlockedUsersInEitherDirection() {
        User viewer = userRepository.save(completeUser("discover-viewer@wick.com", "Viewer"));
        User alreadyLiked = userRepository.save(completeUser("discover-liked@wick.com", "AlreadyLiked"));
        User blockedByViewer = userRepository.save(completeUser("discover-blocked-by-viewer@wick.com", "BlockedByViewer"));
        User blockedViewer = userRepository.save(completeUser("discover-blocked-viewer@wick.com", "BlockedViewer"));
        User discoverable = userRepository.save(completeUser("discover-candidate@wick.com", "Discoverable"));
        userPhotoRepository.save(TestEntities.userPhoto(discoverable, "discover-candidate-photo", 0));

        likeRepository.insertIfAbsent(UUID.randomUUID(), viewer.getId(), alreadyLiked.getId());
        blockRepository.insertIfAbsent(UUID.randomUUID(), viewer.getId(), blockedByViewer.getId());
        blockRepository.insertIfAbsent(UUID.randomUUID(), blockedViewer.getId(), viewer.getId());

        Page<User> result = userRepository.findDiscoverableUsers(
                viewer.getId(),
                LocalDate.now().minusYears(18),
                LocalDate.now().minusYears(101),
                List.of(Gender.values()).stream().map(Enum::name).toList(),
                null,
                null,
                null,
                PageRequest.of(0, 10_000));

        assertThat(result.getContent()).extracting(User::getId)
                .contains(discoverable.getId())
                .doesNotContain(viewer.getId(), alreadyLiked.getId(), blockedByViewer.getId(), blockedViewer.getId());
    }
}
