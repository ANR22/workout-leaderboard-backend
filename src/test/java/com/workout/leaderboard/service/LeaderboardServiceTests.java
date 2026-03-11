package com.workout.leaderboard.service;

import com.workout.leaderboard.entity.Challenge;
import com.workout.leaderboard.repository.ChallengeEventRepository;
import com.workout.leaderboard.repository.ChallengeRepository;
import com.workout.leaderboard.repository.ChallengeUserMetricTotalRepository;
import com.workout.leaderboard.repository.MetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class LeaderboardServiceTests {
    private ChallengeEventRepository challengeEventRepository;
    private ChallengeUserMetricTotalRepository challengeUserMetricTotalRepository;
    private MetricRepository metricRepository;
    private ChallengeRepository challengeRepository;
    private RedisTemplate<String, Object> redisTemplate;
    private LeaderboardService service;

    @BeforeEach
    void setup() {
        challengeEventRepository = Mockito.mock(ChallengeEventRepository.class);
        challengeUserMetricTotalRepository = Mockito.mock(ChallengeUserMetricTotalRepository.class);
        metricRepository = Mockito.mock(MetricRepository.class);
        challengeRepository = Mockito.mock(ChallengeRepository.class);
        redisTemplate = Mockito.mock(RedisTemplate.class);
        service = new LeaderboardService(challengeEventRepository,
                challengeUserMetricTotalRepository,
                metricRepository,
                challengeRepository,
                redisTemplate);
    }

    @Test
    void getAllChallenges_returnsRepositoryList() {
        Challenge c1 = new Challenge();
        c1.setName("First");
        c1.setDescription("desc");
        c1.setId(1L);
        Challenge c2 = new Challenge();
        c2.setName("Second");
        c2.setDescription("desc");
        c2.setId(2L);
        when(challengeRepository.findAll()).thenReturn(List.of(c1, c2));

        Map<String, Object> response = service.getAllChallenges();

        assertThat(response.get("status")).isEqualTo("SUCCESS");
        assertThat(response.get("challenges")).isInstanceOf(List.class);
        List<?> challenges = (List<?>) response.get("challenges");
        assertThat(challenges).hasSize(2);
        assertThat(response.get("count")).isEqualTo(2);
    }
}
