package com.workout.leaderboard;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workout.leaderboard.service.LeaderboardService;

@RestController
public class LeaderboardController {
    
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }
    
    @PostMapping("/submit-score")
    public ResponseEntity<?> recieveEvent(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = leaderboardService.submitScore(payload);
        return ResponseEntity.accepted().body(result);
    }


    @GetMapping("/leaderboard/challenge/{challengeId}")
    public ResponseEntity<?> getLeaderboard(@PathVariable Long challengeId, @RequestParam(required = false) int limit) {
        Map<String, Object> result = leaderboardService.getLeaderboard(challengeId);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/leaderboard/challenge/{challengeId}/user/{userId}")
    public ResponseEntity<?> getUserLeaderboard(@PathVariable Long challengeId, @PathVariable Long userId) {
        Map<String, Object> result = leaderboardService.getUserLeaderboard(challengeId, userId);
        return ResponseEntity.ok(result);
    }
}
