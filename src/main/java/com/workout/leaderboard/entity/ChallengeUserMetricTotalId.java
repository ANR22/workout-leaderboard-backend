package com.workout.leaderboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ChallengeUserMetricTotalId implements Serializable {

    @Column(name = "challenge_id")
    private Long challengeId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "metric_id")
    private Long metricId;

    public ChallengeUserMetricTotalId() {
    }

    public ChallengeUserMetricTotalId(Long challengeId, Long userId, Long metricId) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.metricId = metricId;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMetricId() {
        return metricId;
    }

    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeUserMetricTotalId that = (ChallengeUserMetricTotalId) o;
        return Objects.equals(challengeId, that.challengeId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(metricId, that.metricId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(challengeId, userId, metricId);
    }
}
