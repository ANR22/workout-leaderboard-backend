package com.workout.leaderboard.service;

import com.workout.leaderboard.entity.Challenge;
import com.workout.leaderboard.entity.ChallengeEvent;
import com.workout.leaderboard.entity.ChallengeUserMetricTotal;
import com.workout.leaderboard.entity.Metric;
import com.workout.leaderboard.repository.ChallengeEventRepository;
import com.workout.leaderboard.repository.ChallengeRepository;
import com.workout.leaderboard.repository.ChallengeUserMetricTotalRepository;
import com.workout.leaderboard.repository.MetricRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class LeaderboardService {

    private final ChallengeEventRepository challengeEventRepository;
    private final ChallengeUserMetricTotalRepository challengeUserMetricTotalRepository;
    private final MetricRepository metricRepository;
    private final ChallengeRepository challengeRepository;

    public LeaderboardService(ChallengeEventRepository challengeEventRepository,
                            ChallengeUserMetricTotalRepository challengeUserMetricTotalRepository,
                            MetricRepository metricRepository,
                            ChallengeRepository challengeRepository) {
        this.challengeEventRepository = challengeEventRepository;
        this.challengeUserMetricTotalRepository = challengeUserMetricTotalRepository;
        this.metricRepository = metricRepository;
        this.challengeRepository = challengeRepository;
    }

    /**
     * Submits a score event and adds it to the ChallengeEvent table
     * Expected payload format:
     * {
     *     "eventId": 1,
     *     "challengeId": 1,
     *     "userId": 1,
     *     "metricId": 1,
     *     "value": 100.0
     * }
     */
    public Map<String, Object> submitScore(Map<String, Object> payload) {
        try {
            Long eventId = Long.parseLong(payload.get("eventId").toString());
            Long challengeId = Long.parseLong(payload.get("challengeId").toString());
            Long userId = Long.parseLong(payload.get("userId").toString());
            Long metricId = Long.parseLong(payload.get("metricId").toString());
            Double value = Double.parseDouble(payload.get("value").toString());

            // Fetch the challenge
            Challenge challenge = challengeRepository.findById(challengeId)
                    .orElseThrow(() -> new RuntimeException("Challenge not found with id: " + challengeId));

            // Fetch the metric
            Metric metric = metricRepository.findById(metricId)
                    .orElseThrow(() -> new RuntimeException("Metric not found with id: " + metricId));

            // Create and save the challenge event
            ChallengeEvent event = new ChallengeEvent(challenge, userId, metric, value, LocalDateTime.now());
            event.setEventId(eventId);
            ChallengeEvent savedEvent = challengeEventRepository.save(event);

            return Map.of(
                    "status", "SUCCESS",
                    "eventId", savedEvent.getEventId(),
                    "message", "Score submitted successfully"
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * Gets the leaderboard for a specific challenge
     * Returns all ChallengeUserMetricTotal records for the given challengeId
     */
    public Map<String, Object> getLeaderboard(Long challengeId) {
        try {
            List<ChallengeUserMetricTotal> leaderboardData = challengeUserMetricTotalRepository.findByChallengeId(challengeId);
            
            return Map.of(
                    "status", "SUCCESS",
                    "challengeId", challengeId,
                    "leaderboard", leaderboardData,
                    "count", leaderboardData.size()
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * Gets the leaderboard data for a specific user in a specific challenge
     * Returns ChallengeUserMetricTotal records for the given challengeId and userId
     */
    public Map<String, Object> getUserLeaderboard(Long challengeId, Long userId) {
        try {
            List<ChallengeUserMetricTotal> userLeaderboardData = challengeUserMetricTotalRepository.findByChallengeIdAndUserId(challengeId, userId);
            
            return Map.of(
                    "status", "SUCCESS",
                    "challengeId", challengeId,
                    "userId", userId,
                    "userLeaderboard", userLeaderboardData,
                    "count", userLeaderboardData.size()
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            );
        }
    }
}
