package com.workout.leaderboard.repository;

import com.workout.leaderboard.entity.ChallengeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeEventRepository extends JpaRepository<ChallengeEvent, Long> {
}
