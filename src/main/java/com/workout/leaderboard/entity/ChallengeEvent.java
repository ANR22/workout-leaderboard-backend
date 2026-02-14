package com.workout.leaderboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "challenge_events",
    indexes = {
        @Index(name = "idx_challenge_id", columnList = "challenge_id"),
        @Index(name = "idx_challenge_user", columnList = "challenge_id, user_id"),
        @Index(name = "idx_challenge_metric", columnList = "challenge_id, metric_id")
    }
)
public class ChallengeEvent {

    @Id
    @Column(name = "event_id")
    private Long eventId;

    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false, foreignKey = @ForeignKey(name = "fk_challenge_event_challenge"))
    private Challenge challenge;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "metric_id", nullable = false)
    private Metric metric;

    @Column(name = "metric_value", nullable = false)
    private Double metricValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChallengeEvent() {
    }

    public ChallengeEvent(Challenge challenge, Long userId, Metric metric, Double metricValue, LocalDateTime createdAt) {
        this.challenge = challenge;
        this.userId = userId;
        this.metric = metric;
        this.metricValue = metricValue;
        this.createdAt = createdAt;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getChallengeId() {
        return challenge != null ? challenge.getId() : null;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
