package com.workout.leaderboard.dto.response;

import com.workout.leaderboard.entity.Challenge;

import java.time.LocalDateTime;

public class ChallengeResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;

    public ChallengeResponse() {}

    public static ChallengeResponse from(Challenge challenge) {
        ChallengeResponse dto = new ChallengeResponse();
        dto.id = challenge.getId();
        dto.name = challenge.getName();
        dto.description = challenge.getDescription();
        dto.startDate = challenge.getStartDate();
        dto.endDate = challenge.getEndDate();
        dto.createdAt = challenge.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
