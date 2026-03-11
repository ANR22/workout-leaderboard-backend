package com.workout.leaderboard.dto.request;

public class SubmitScoreRequest {

    private Long eventId;
    private Long challengeId;
    private Long userId;
    private Long metricId;
    private Double value;

    public SubmitScoreRequest() {}

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getChallengeId() { return challengeId; }
    public void setChallengeId(Long challengeId) { this.challengeId = challengeId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getMetricId() { return metricId; }
    public void setMetricId(Long metricId) { this.metricId = metricId; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
