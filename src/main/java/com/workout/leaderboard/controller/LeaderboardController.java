package com.workout.leaderboard.controller;

import com.workout.leaderboard.dto.request.SubmitScoreRequest;
import com.workout.leaderboard.dto.response.ChallengeResponse;
import com.workout.leaderboard.dto.response.LeaderboardResponse;
import com.workout.leaderboard.dto.response.SubmitScoreResponse;
import com.workout.leaderboard.dto.response.UserLeaderboardResponse;
import com.workout.leaderboard.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LeaderboardController {
    
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }
    
    @PostMapping("/submit-score")
    public ResponseEntity<SubmitScoreResponse> recieveEvent(@RequestBody SubmitScoreRequest request) {
        SubmitScoreResponse result = leaderboardService.submitScore(request);
        return ResponseEntity.accepted().body(result);
    }


    @GetMapping("/leaderboard/challenge/{challengeId}")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(@PathVariable Long challengeId, @RequestParam(required = false) Integer limit) {
        LeaderboardResponse result = leaderboardService.getLeaderboard(challengeId);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/leaderboard/challenge/{challengeId}/user/{userId}")
    public ResponseEntity<UserLeaderboardResponse> getUserLeaderboard(@PathVariable Long challengeId, @PathVariable Long userId) {
        UserLeaderboardResponse result = leaderboardService.getUserLeaderboard(challengeId, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/leaderboard/challenges")
    public ResponseEntity<List<ChallengeResponse>> getAllChallenges() {
        List<ChallengeResponse> result = leaderboardService.getAllChallenges();
        return ResponseEntity.ok(result);
    }

    
}
