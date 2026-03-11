package com.workout.leaderboard.dto.response;

import java.util.List;

public class LeaderboardResponse {

    private Long challengeId;
    private List<LeaderboardEntryResponse> leaderboard;
    private int count;

    public LeaderboardResponse() {}

    public LeaderboardResponse(Long challengeId, List<LeaderboardEntryResponse> leaderboard) {
        this.challengeId = challengeId;
        this.leaderboard = leaderboard;
        this.count = leaderboard.size();
    }

    public Long getChallengeId() { return challengeId; }
    public List<LeaderboardEntryResponse> getLeaderboard() { return leaderboard; }
    public int getCount() { return count; }
}
