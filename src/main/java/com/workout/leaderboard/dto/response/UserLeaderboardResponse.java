package com.workout.leaderboard.dto.response;

import java.util.List;

public class UserLeaderboardResponse {

    private Long challengeId;
    private Long userId;
    private List<UserLeaderboardEntryResponse> userLeaderboard;
    private int count;

    public UserLeaderboardResponse() {}

    public UserLeaderboardResponse(Long challengeId, Long userId, List<UserLeaderboardEntryResponse> userLeaderboard) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.userLeaderboard = userLeaderboard;
        this.count = userLeaderboard.size();
    }

    public Long getChallengeId() { return challengeId; }
    public Long getUserId() { return userId; }
    public List<UserLeaderboardEntryResponse> getUserLeaderboard() { return userLeaderboard; }
    public int getCount() { return count; }
}
