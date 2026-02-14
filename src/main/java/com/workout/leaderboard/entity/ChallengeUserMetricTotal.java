package com.workout.leaderboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_user_metric_totals")
public class ChallengeUserMetricTotal {

    @EmbeddedId
    private ChallengeUserMetricTotalId id;

    @ManyToOne
    @MapsId("metricId")
    @JoinColumn(name = "metric_id")
    private Metric metric;

    @Column(name = "total_value", nullable = false)
    private Double totalValue;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ChallengeUserMetricTotal() {
    }

    public ChallengeUserMetricTotal(Long challengeId, Long userId, Metric metric, Double totalValue, LocalDateTime updatedAt) {
        this.id = new ChallengeUserMetricTotalId(challengeId, userId, metric.getId());
        this.metric = metric;
        this.totalValue = totalValue;
        this.updatedAt = updatedAt;
    }

    public ChallengeUserMetricTotalId getId() {
        return id;
    }

    public void setId(ChallengeUserMetricTotalId id) {
        this.id = id;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getChallengeId() {
        return id.getChallengeId();
    }

    public Long getUserId() {
        return id.getUserId();
    }

    public Long getMetricId() {
        return id.getMetricId();
    }
}
