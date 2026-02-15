package com.workout.leaderboard.repository;

import com.workout.leaderboard.entity.ChallengeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChallengeEventRepository extends JpaRepository<ChallengeEvent, Long> {
    
    /**
     * Finds all events for a specific user in a specific challenge for a specific metric.
     * Used to calculate aggregated scores.
     */
    @Query("SELECT ce FROM ChallengeEvent ce WHERE ce.challenge.id = :challengeId AND ce.userId = :userId AND ce.metric.id = :metricId")
    List<ChallengeEvent> findByChallengeidAndUserIdAndMetricId(
            @Param("challengeId") Long challengeId,
            @Param("userId") Long userId,
            @Param("metricId") Long metricId
    );}