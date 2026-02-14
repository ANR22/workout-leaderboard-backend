package com.workout.leaderboard.repository;

import com.workout.leaderboard.entity.ChallengeUserMetricTotal;
import com.workout.leaderboard.entity.ChallengeUserMetricTotalId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChallengeUserMetricTotalRepository extends JpaRepository<ChallengeUserMetricTotal, ChallengeUserMetricTotalId> {
    
    @Query("SELECT c FROM ChallengeUserMetricTotal c WHERE c.id.challengeId = :challengeId")
    List<ChallengeUserMetricTotal> findByChallengeId(@Param("challengeId") Long challengeId);
    
    @Query("SELECT c FROM ChallengeUserMetricTotal c WHERE c.id.challengeId = :challengeId AND c.id.userId = :userId")
    List<ChallengeUserMetricTotal> findByChallengeIdAndUserId(@Param("challengeId") Long challengeId, @Param("userId") Long userId);
}
