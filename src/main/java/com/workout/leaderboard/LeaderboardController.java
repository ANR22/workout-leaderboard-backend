package com.workout.leaderboard;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeaderboardController {
    
    @PostMapping("/submit-score")
    public ResponseEntity<?> recieveEvent(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.accepted().body(Map.of("status", "ACCEPTED"));
    }


    @GetMapping("/leaderboard/challenge/{challengeId}")
    public ResponseEntity<?> getLeaderboard(@PathVariable String challengeId, @RequestParam int limit) {
        return ResponseEntity.ok(Map.of("challengeId", challengeId, "leaderboard", "mock leaderboard data"));
    }


    @GetMapping("/leaderboard/challenge/{challengeId}/user/{userId}")
    public ResponseEntity<?> getUserLeaderboard(@PathVariable String challengeId, @PathVariable String userId) {
        return ResponseEntity.ok(Map.of("challengeId", challengeId, "userId", userId, "userLeaderboard", "mock user leaderboard data"));
    }
}
