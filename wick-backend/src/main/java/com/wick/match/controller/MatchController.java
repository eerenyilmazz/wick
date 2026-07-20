package com.wick.match.controller;

import com.wick.common.dto.PageResponse;
import com.wick.common.exception.ApiException;
import com.wick.location.service.LocationService;
import com.wick.match.dto.MatchResponse;
import com.wick.match.dto.SwipeRequest;
import com.wick.match.dto.SwipeResponse;
import com.wick.match.mapper.MatchMapper;
import com.wick.match.service.MatchService;
import com.wick.user.domain.User;
import com.wick.user.dto.UserResponse;
import com.wick.user.mapper.UserMapper;
import com.wick.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final MatchMapper matchMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final LocationService locationService;

    @PostMapping("/swipe")
    public SwipeResponse swipe(@AuthenticationPrincipal String userId, @Valid @RequestBody SwipeRequest request) {
        User user = userService.getById(UUID.fromString(userId));
        if (!user.isVerified()) throw new ApiException("Email not verified", HttpStatus.FORBIDDEN);
        return matchMapper.toSwipeResponse(
                matchService.swipe(UUID.fromString(userId), request.targetUserId(), request.direction()));
    }

    @GetMapping("/discover")
    public PageResponse<UserResponse> discover(@AuthenticationPrincipal String userId,
                                                 @PageableDefault(size = 20) Pageable pageable) {
        User viewer = userService.getById(UUID.fromString(userId));
        if (!viewer.isVerified()) throw new ApiException("Email not verified", HttpStatus.FORBIDDEN);
        return PageResponse.from(matchService.getDiscoverableUsers(UUID.fromString(userId), pageable)
                .map(candidate -> userMapper.toResponse(candidate, locationService.calculateDistanceKm(viewer, candidate))));
    }

    @GetMapping
    public PageResponse<MatchResponse> getMyMatches(@AuthenticationPrincipal String userId,
                                                      @PageableDefault(size = 20) Pageable pageable) {
        UUID id = UUID.fromString(userId);
        return PageResponse.from(matchService.getMatchesForUser(id, pageable)
                .map(match -> matchMapper.toResponse(match, id)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unmatch(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        matchService.unmatch(id, UUID.fromString(userId));
    }
}
