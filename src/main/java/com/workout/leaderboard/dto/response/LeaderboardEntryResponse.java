package com.workout.leaderboard.dto.response;

public class LeaderboardEntryResponse {

    private int rank;
    private Long userId;
    private String fullName;
    private Long metricId;
    private Double aggregatedScore;

    public LeaderboardEntryResponse() {}

    public LeaderboardEntryResponse(int rank, Long userId, String fullName, Long metricId, Double aggregatedScore) {
        this.rank = rank;
        this.userId = userId;
        this.fullName = fullName;
        this.metricId = metricId;
        this.aggregatedScore = aggregatedScore;
    }

    public int getRank() { return rank; }
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public Long getMetricId() { return metricId; }
    public Double getAggregatedScore() { return aggregatedScore; }
}
