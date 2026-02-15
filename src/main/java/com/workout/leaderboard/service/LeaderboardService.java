package com.workout.leaderboard.service;

import com.workout.leaderboard.entity.Challenge;
import com.workout.leaderboard.entity.ChallengeEvent;
import com.workout.leaderboard.entity.ChallengeUserMetricTotal;
import com.workout.leaderboard.entity.ChallengeUserMetricTotalId;
import com.workout.leaderboard.entity.Metric;
import com.workout.leaderboard.repository.ChallengeEventRepository;
import com.workout.leaderboard.repository.ChallengeRepository;
import com.workout.leaderboard.repository.ChallengeUserMetricTotalRepository;
import com.workout.leaderboard.repository.MetricRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class LeaderboardService {

    private final ChallengeEventRepository challengeEventRepository;
    private final ChallengeUserMetricTotalRepository challengeUserMetricTotalRepository;
    private final MetricRepository metricRepository;
    private final ChallengeRepository challengeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public LeaderboardService(ChallengeEventRepository challengeEventRepository,
                            ChallengeUserMetricTotalRepository challengeUserMetricTotalRepository,
                            MetricRepository metricRepository,
                            ChallengeRepository challengeRepository,
                            RedisTemplate<String, Object> redisTemplate) {
        this.challengeEventRepository = challengeEventRepository;
        this.challengeUserMetricTotalRepository = challengeUserMetricTotalRepository;
        this.metricRepository = metricRepository;
        this.challengeRepository = challengeRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Submits a score event and updates the aggregated leaderboard.
     * 
     * Process:
     * 1. Save the event in ChallengeEvent table
     * 2. Calculate aggregated score for the user-challenge combo
     * 3. Update ChallengeUserMetricTotal with new aggregated score
     * 4. Update Redis sorted set for fast leaderboard queries
     * 
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

            // Step 1: Fetch and validate references
            Challenge challenge = challengeRepository.findById(challengeId)
                    .orElseThrow(() -> new RuntimeException("Challenge not found with id: " + challengeId));

            Metric metric = metricRepository.findById(metricId)
                    .orElseThrow(() -> new RuntimeException("Metric not found with id: " + metricId));

            // Step 2: Save the event to ChallengeEvent table
            ChallengeEvent event = new ChallengeEvent(challenge, userId, metric, value, LocalDateTime.now());
            event.setEventId(eventId);
            ChallengeEvent savedEvent = challengeEventRepository.save(event);

            // Step 3: Calculate aggregated score for user in challenge
            // Query all events for this user and metric in the challenge
            List<ChallengeEvent> allUserMetricEvents = challengeEventRepository.findByChallengeidAndUserIdAndMetricId(
                    challengeId, userId, metricId
            );
            Double aggregatedScore = allUserMetricEvents.stream()
                    .mapToDouble(ChallengeEvent::getMetricValue)
                    .sum();

            // Step 4: Update the ChallengeUserMetricTotal record
            ChallengeUserMetricTotalId id = new ChallengeUserMetricTotalId(challengeId, userId, metricId);
            Optional<ChallengeUserMetricTotal> existingTotal = challengeUserMetricTotalRepository.findById(id);
            
            ChallengeUserMetricTotal metricTotal;
            if (existingTotal.isPresent()) {
                metricTotal = existingTotal.get();
                metricTotal.setTotalValue(aggregatedScore);
                metricTotal.setUpdatedAt(LocalDateTime.now());
            } else {
                metricTotal = new ChallengeUserMetricTotal(challengeId, userId, metric, aggregatedScore, LocalDateTime.now());
            }
            challengeUserMetricTotalRepository.save(metricTotal);

            // Step 5: Update Redis sorted set for leaderboard performance
            // Key format: "challenge:{challengeId}:leaderboard"
            // Member format: "{userId}:{metricId}" (user:metric combination)
            // Score: aggregated metric value
            String redisKey = "challenge:" + challengeId + ":leaderboard";
            String member = userId + ":" + metricId;
            redisTemplate.opsForZSet().add(redisKey, member, aggregatedScore);

            return Map.of(
                    "status", "SUCCESS",
                    "eventId", savedEvent.getEventId(),
                    "aggregatedScore", aggregatedScore,
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
     * Gets the leaderboard for a specific challenge from Redis.
     * Returns top N users ranked by aggregated score in descending order.
     * Reading from Redis sorted sets provides O(log(N)) complexity vs O(N log N) from database.
     * 
     * @param challengeId the challenge to fetch leaderboard for
     * @return leaderboard data with rankings
     */
    public Map<String, Object> getLeaderboard(Long challengeId) {
        try {
            String redisKey = "challenge:" + challengeId + ":leaderboard";
            
            // Fetch all members from Redis sorted set in reverse score order (highest score first)
            Set<ZSetOperations.TypedTuple<Object>> leaderboardSet = redisTemplate.opsForZSet()
                    .reverseRangeByScoreWithScores(redisKey, 0, Double.MAX_VALUE);
            
            List<Map<String, Object>> leaderboard = new ArrayList<>();
            int rank = 1;
            
            if (leaderboardSet != null) {
                for (ZSetOperations.TypedTuple<Object> entry : leaderboardSet) {
                    String member = (String) entry.getValue();
                    Double score = entry.getScore();
                    
                    String[] parts = member.split(":");
                    Long userId = Long.parseLong(parts[0]);
                    Long metricId = Long.parseLong(parts[1]);
                    
                    Map<String, Object> leaderboardEntry = new HashMap<>();
                    leaderboardEntry.put("rank", rank);
                    leaderboardEntry.put("userId", userId);
                    leaderboardEntry.put("metricId", metricId);
                    leaderboardEntry.put("aggregatedScore", score);
                    
                    leaderboard.add(leaderboardEntry);
                    rank++;
                }
            }
            
            return Map.of(
                    "status", "SUCCESS",
                    "challengeId", challengeId,
                    "leaderboard", leaderboard,
                    "count", leaderboard.size()
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * Gets the leaderboard data for a specific user in a specific challenge.
     * Returns ChallengeUserMetricTotal records for the given challengeId and userId.
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
