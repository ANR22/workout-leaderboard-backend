package com.workout.leaderboard.service;

import com.workout.leaderboard.dto.request.SubmitScoreRequest;
import com.workout.leaderboard.dto.response.ChallengeResponse;
import com.workout.leaderboard.dto.response.LeaderboardResponse;
import com.workout.leaderboard.dto.response.SubmitScoreResponse;
import com.workout.leaderboard.dto.response.UserLeaderboardResponse;
import com.workout.leaderboard.entity.Challenge;
import com.workout.leaderboard.entity.ChallengeEvent;
import com.workout.leaderboard.entity.ChallengeUserMetricTotal;
import com.workout.leaderboard.entity.ChallengeUserMetricTotalId;
import com.workout.leaderboard.entity.Metric;
import com.workout.leaderboard.entity.User;
import com.workout.leaderboard.exception.BadRequestException;
import com.workout.leaderboard.exception.ResourceNotFoundException;
import com.workout.leaderboard.repository.ChallengeEventRepository;
import com.workout.leaderboard.repository.ChallengeRepository;
import com.workout.leaderboard.repository.ChallengeUserMetricTotalRepository;
import com.workout.leaderboard.repository.MetricRepository;
import com.workout.leaderboard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTests {

    @Mock
    private ChallengeEventRepository challengeEventRepository;

    @Mock
    private ChallengeUserMetricTotalRepository challengeUserMetricTotalRepository;

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Test
    void submitScoreThrowsBadRequestWhenRequestIsNull() {
        assertThrows(BadRequestException.class, () -> leaderboardService.submitScore(null));

        verify(challengeEventRepository, never()).save(any());
    }

    @Test
    void submitScoreThrowsBadRequestWhenRequiredFieldIsMissing() {
        SubmitScoreRequest request = validRequest();
        request.setMetricId(null);

        assertThrows(BadRequestException.class, () -> leaderboardService.submitScore(request));

        verify(challengeRepository, never()).findById(any());
    }

    @Test
    void submitScoreThrowsNotFoundWhenChallengeDoesNotExist() {
        SubmitScoreRequest request = validRequest();
        when(challengeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leaderboardService.submitScore(request));

        verify(metricRepository, never()).findById(any());
    }

    @Test
    void submitScoreThrowsNotFoundWhenMetricDoesNotExist() {
        SubmitScoreRequest request = validRequest();
        Challenge challenge = challenge(10L, "Summer Challenge");

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(challenge));
        when(metricRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leaderboardService.submitScore(request));

        verify(challengeEventRepository, never()).save(any());
    }

    @Test
    void submitScoreUpdatesExistingTotalAndRedisLeaderboard() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        SubmitScoreRequest request = validRequest();
        Challenge challenge = challenge(10L, "Summer Challenge");
        Metric metric = metric(30L, "distance");

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(challenge));
        when(metricRepository.findById(30L)).thenReturn(Optional.of(metric));

        ChallengeEvent savedEvent = new ChallengeEvent(challenge, 20L, metric, 50.0, LocalDateTime.now());
        savedEvent.setEventId(100L);
        when(challengeEventRepository.save(any(ChallengeEvent.class))).thenReturn(savedEvent);

        List<ChallengeEvent> allEvents = List.of(
                new ChallengeEvent(challenge, 20L, metric, 50.0, LocalDateTime.now()),
                new ChallengeEvent(challenge, 20L, metric, 25.5, LocalDateTime.now())
        );
        when(challengeEventRepository.findByChallengeidAndUserIdAndMetricId(10L, 20L, 30L)).thenReturn(allEvents);

        ChallengeUserMetricTotal existingTotal = new ChallengeUserMetricTotal();
        existingTotal.setId(new ChallengeUserMetricTotalId(10L, 20L, 30L));
        existingTotal.setMetric(metric);
        existingTotal.setTotalValue(10.0);
        existingTotal.setUpdatedAt(LocalDateTime.now().minusDays(1));
        when(challengeUserMetricTotalRepository.findById(new ChallengeUserMetricTotalId(10L, 20L, 30L)))
                .thenReturn(Optional.of(existingTotal));

        SubmitScoreResponse response = leaderboardService.submitScore(request);

        assertEquals(100L, response.getEventId());
        assertEquals(75.5, response.getAggregatedScore());
        assertEquals("Score submitted successfully", response.getMessage());

        ArgumentCaptor<ChallengeUserMetricTotal> totalCaptor = ArgumentCaptor.forClass(ChallengeUserMetricTotal.class);
        verify(challengeUserMetricTotalRepository).save(totalCaptor.capture());
        assertEquals(75.5, totalCaptor.getValue().getTotalValue());
        verify(zSetOperations).add("challenge:10:leaderboard", "20:30", 75.5);
    }

    @Test
    void getLeaderboardThrowsNotFoundWhenChallengeDoesNotExist() {
        when(challengeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leaderboardService.getLeaderboard(99L));

        verify(zSetOperations, never()).reverseRangeByScoreWithScores(any(), any(Double.class), any(Double.class));
    }

    @Test
    void getLeaderboardReturnsRankedEntriesFromRedis() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        Challenge challenge = challenge(10L, "Summer Challenge");
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(challenge));

        Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("20:30", 90.0));
        tuples.add(new DefaultTypedTuple<>("21:30", 75.0));
        when(zSetOperations.reverseRangeByScoreWithScores("challenge:10:leaderboard", 0, Double.MAX_VALUE))
                .thenReturn(tuples);
        when(userRepository.findAllById(List.of(20L, 21L)))
            .thenReturn(List.of(user(20L, "Alice Runner"), user(21L, "Bob Lifter")));

        LeaderboardResponse response = leaderboardService.getLeaderboard(10L);

        assertEquals(10L, response.getChallengeId());
        assertEquals(2, response.getCount());
        assertEquals(1, response.getLeaderboard().get(0).getRank());
        assertEquals(20L, response.getLeaderboard().get(0).getUserId());
        assertEquals("Alice Runner", response.getLeaderboard().get(0).getFullName());
        assertEquals(90.0, response.getLeaderboard().get(0).getAggregatedScore());
        assertEquals(2, response.getLeaderboard().get(1).getRank());
        assertEquals(21L, response.getLeaderboard().get(1).getUserId());
        assertEquals("Bob Lifter", response.getLeaderboard().get(1).getFullName());
    }

    @Test
    void getLeaderboardUsesUnknownUserWhenNameNotFound() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        Challenge challenge = challenge(10L, "Summer Challenge");
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(challenge));

        Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("50:30", 42.0));
        when(zSetOperations.reverseRangeByScoreWithScores("challenge:10:leaderboard", 0, Double.MAX_VALUE))
                .thenReturn(tuples);
        when(userRepository.findAllById(List.of(50L))).thenReturn(List.of());

        LeaderboardResponse response = leaderboardService.getLeaderboard(10L);

        assertEquals(1, response.getCount());
        assertEquals("Unknown User", response.getLeaderboard().get(0).getFullName());
    }

    @Test
    void getUserLeaderboardReturnsMappedEntriesForUser() {
        Challenge challenge = challenge(10L, "Summer Challenge");
        Metric metric = metric(30L, "distance");

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(challenge));

        ChallengeUserMetricTotal total = new ChallengeUserMetricTotal();
        total.setId(new ChallengeUserMetricTotalId(10L, 20L, 30L));
        total.setMetric(metric);
        total.setTotalValue(123.4);
        total.setUpdatedAt(LocalDateTime.now());

        when(challengeUserMetricTotalRepository.findByChallengeIdAndUserId(10L, 20L)).thenReturn(List.of(total));

        UserLeaderboardResponse response = leaderboardService.getUserLeaderboard(10L, 20L);

        assertEquals(10L, response.getChallengeId());
        assertEquals(20L, response.getUserId());
        assertEquals(1, response.getCount());
        assertEquals(30L, response.getUserLeaderboard().get(0).getMetricId());
        assertEquals(123.4, response.getUserLeaderboard().get(0).getTotalValue());
    }

    @Test
    void getAllChallengesReturnsMappedChallengeResponses() {
        Challenge first = challenge(10L, "Summer Challenge");
        Challenge second = challenge(11L, "Winter Challenge");

        when(challengeRepository.findAll()).thenReturn(List.of(first, second));

        List<ChallengeResponse> response = leaderboardService.getAllChallenges();

        assertEquals(2, response.size());
        assertEquals(10L, response.get(0).getId());
        assertEquals("Summer Challenge", response.get(0).getName());
        assertEquals(11L, response.get(1).getId());
        assertEquals("Winter Challenge", response.get(1).getName());
    }

    private SubmitScoreRequest validRequest() {
        SubmitScoreRequest request = new SubmitScoreRequest();
        request.setEventId(100L);
        request.setChallengeId(10L);
        request.setUserId(20L);
        request.setMetricId(30L);
        request.setValue(50.0);
        return request;
    }

    private Challenge challenge(Long id, String name) {
        Challenge challenge = new Challenge();
        challenge.setId(id);
        challenge.setName(name);
        challenge.setDescription(name + " description");
        challenge.setStartDate(LocalDateTime.now().minusDays(1));
        challenge.setEndDate(LocalDateTime.now().plusDays(1));
        challenge.setCreatedAt(LocalDateTime.now().minusDays(2));
        return challenge;
    }

    private Metric metric(Long id, String name) {
        Metric metric = new Metric();
        metric.setId(id);
        metric.setName(name);
        metric.setAggregationType("SUM");
        return metric;
    }

    private User user(Long id, String fullName) {
        User user = new User();
        user.setUserId(id);
        user.setFullName(fullName);
        user.setEmail(fullName.toLowerCase().replace(" ", ".") + "@example.com");
        user.setPassword("encoded-password");
        return user;
    }
}
