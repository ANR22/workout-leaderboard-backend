package com.workout.leaderboard.service;

import com.workout.leaderboard.dto.request.SubmitScoreRequest;
import com.workout.leaderboard.dto.response.ChallengeResponse;
import com.workout.leaderboard.dto.response.LeaderboardEntryResponse;
import com.workout.leaderboard.dto.response.LeaderboardResponse;
import com.workout.leaderboard.dto.response.SubmitScoreResponse;
import com.workout.leaderboard.dto.response.UserLeaderboardEntryResponse;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final ChallengeEventRepository challengeEventRepository;
    private final ChallengeUserMetricTotalRepository challengeUserMetricTotalRepository;
    private final MetricRepository metricRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public LeaderboardService(ChallengeEventRepository challengeEventRepository,
                            ChallengeUserMetricTotalRepository challengeUserMetricTotalRepository,
                            MetricRepository metricRepository,
                            ChallengeRepository challengeRepository,
                            UserRepository userRepository,
                            RedisTemplate<String, Object> redisTemplate) {
        this.challengeEventRepository = challengeEventRepository;
        this.challengeUserMetricTotalRepository = challengeUserMetricTotalRepository;
        this.metricRepository = metricRepository;
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
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
    public SubmitScoreResponse submitScore(SubmitScoreRequest request) {
        validateSubmitScoreRequest(request);

        Long eventId = request.getEventId();
        Long challengeId = request.getChallengeId();
        Long userId = request.getUserId();
        Long metricId = request.getMetricId();
        Double value = request.getValue();

        Challenge challenge = getChallengeOrThrow(challengeId);

        Metric metric = metricRepository.findById(metricId)
            .orElseThrow(() -> new ResourceNotFoundException("Metric not found with id: " + metricId));

        ChallengeEvent event = new ChallengeEvent(challenge, userId, metric, value, LocalDateTime.now());
        event.setEventId(eventId);
        ChallengeEvent savedEvent = challengeEventRepository.save(event);

        List<ChallengeEvent> allUserMetricEvents = challengeEventRepository.findByChallengeidAndUserIdAndMetricId(
            challengeId, userId, metricId
        );
        Double aggregatedScore = allUserMetricEvents.stream()
            .mapToDouble(ChallengeEvent::getMetricValue)
            .sum();

        ChallengeUserMetricTotalId id = new ChallengeUserMetricTotalId(challengeId, userId, metricId);
        var existingTotal = challengeUserMetricTotalRepository.findById(id);

        ChallengeUserMetricTotal metricTotal;
        if (existingTotal.isPresent()) {
            metricTotal = existingTotal.get();
            metricTotal.setTotalValue(aggregatedScore);
            metricTotal.setUpdatedAt(LocalDateTime.now());
        } else {
            metricTotal = new ChallengeUserMetricTotal(challengeId, userId, metric, aggregatedScore, LocalDateTime.now());
        }
        challengeUserMetricTotalRepository.save(metricTotal);

        String redisKey = "challenge:" + challengeId + ":leaderboard";
        String member = userId + ":" + metricId;
        redisTemplate.opsForZSet().add(redisKey, member, aggregatedScore);

        return new SubmitScoreResponse(savedEvent.getEventId(), aggregatedScore, "Score submitted successfully");
    }

    /**
     * Gets the leaderboard for a specific challenge from Redis.
     * Returns top N users ranked by aggregated score in descending order.
     * Reading from Redis sorted sets provides O(log(N)) complexity vs O(N log N) from database.
     * 
     * @param challengeId the challenge to fetch leaderboard for
     * @return leaderboard data with rankings
     */
    public LeaderboardResponse getLeaderboard(Long challengeId) {
        getChallengeOrThrow(challengeId);
        String redisKey = "challenge:" + challengeId + ":leaderboard";

        Set<ZSetOperations.TypedTuple<Object>> leaderboardSet = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(redisKey, 0, Double.MAX_VALUE);

        List<LeaderboardEntryResponse> leaderboard = new ArrayList<>();
        int rank = 1;

        Map<Long, String> userNamesById = Map.of();
        if (leaderboardSet != null && !leaderboardSet.isEmpty()) {
            List<Long> userIds = leaderboardSet.stream()
                .map(entry -> ((String) entry.getValue()).split(":"))
                .filter(parts -> parts.length >= 2)
                .map(parts -> Long.parseLong(parts[0]))
                .distinct()
                .collect(Collectors.toList());

            userNamesById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getFullName));
        }

        if (leaderboardSet != null) {
            for (ZSetOperations.TypedTuple<Object> entry : leaderboardSet) {
                String member = (String) entry.getValue();
                Double score = entry.getScore();

                String[] parts = member.split(":");
                Long userId = Long.parseLong(parts[0]);
                Long metricId = Long.parseLong(parts[1]);
                String fullName = userNamesById.getOrDefault(userId, "Unknown User");
                leaderboard.add(new LeaderboardEntryResponse(rank, userId, fullName, metricId, score));
                rank++;
            }
        }

        return new LeaderboardResponse(challengeId, leaderboard);
    }

    /**
     * Gets the leaderboard data for a specific user in a specific challenge.
     * Returns ChallengeUserMetricTotal records for the given challengeId and userId.
     */
    public UserLeaderboardResponse getUserLeaderboard(Long challengeId, Long userId) {
        getChallengeOrThrow(challengeId);
        List<ChallengeUserMetricTotal> userLeaderboardData = challengeUserMetricTotalRepository.findByChallengeIdAndUserId(challengeId, userId);
        List<UserLeaderboardEntryResponse> responseItems = userLeaderboardData.stream()
                .map(UserLeaderboardEntryResponse::from)
                .collect(Collectors.toList());
        return new UserLeaderboardResponse(challengeId, userId, responseItems);
    }

    /**
     * Retrieves all challenges present in the system.
     * This simply delegates to the repository and wraps the result in a standard response map.
     */
    public List<ChallengeResponse> getAllChallenges() {
        return challengeRepository.findAll().stream()
                .map(ChallengeResponse::from)
                .collect(Collectors.toList());
    }

    private Challenge getChallengeOrThrow(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found with id: " + challengeId));
    }

    private void validateSubmitScoreRequest(SubmitScoreRequest request) {
        if (request == null) {
            throw new BadRequestException("request body is required");
        }

        if (request.getEventId() == null ||
                request.getChallengeId() == null ||
                request.getUserId() == null ||
                request.getMetricId() == null ||
                request.getValue() == null) {
            throw new BadRequestException("eventId, challengeId, userId, metricId, and value are required");
        }
    }
}
