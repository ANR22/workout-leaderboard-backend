package com.workout.leaderboard.dto.response;

import com.workout.leaderboard.entity.ChallengeUserMetricTotal;

import java.time.LocalDateTime;

public class UserLeaderboardEntryResponse {

    private Long challengeId;
    private Long userId;
    private Long metricId;
    private Double totalValue;
    private LocalDateTime updatedAt;

    public UserLeaderboardEntryResponse() {}

    public static UserLeaderboardEntryResponse from(ChallengeUserMetricTotal entity) {
        UserLeaderboardEntryResponse dto = new UserLeaderboardEntryResponse();
        dto.challengeId = entity.getChallengeId();
        dto.userId = entity.getUserId();
        dto.metricId = entity.getMetricId();
        dto.totalValue = entity.getTotalValue();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }

    public Long getChallengeId() { return challengeId; }
    public Long getUserId() { return userId; }
    public Long getMetricId() { return metricId; }
    public Double getTotalValue() { return totalValue; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
