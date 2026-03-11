package com.workout.leaderboard.dto.response;

public class SubmitScoreResponse {

    private Long eventId;
    private Double aggregatedScore;
    private String message;

    public SubmitScoreResponse() {}

    public SubmitScoreResponse(Long eventId, Double aggregatedScore, String message) {
        this.eventId = eventId;
        this.aggregatedScore = aggregatedScore;
        this.message = message;
    }

    public Long getEventId() { return eventId; }
    public Double getAggregatedScore() { return aggregatedScore; }
    public String getMessage() { return message; }
}
