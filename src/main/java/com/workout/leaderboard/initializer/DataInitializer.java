package com.workout.leaderboard.initializer;

import com.workout.leaderboard.entity.Challenge;
import com.workout.leaderboard.entity.ChallengeEvent;
import com.workout.leaderboard.entity.ChallengeUserMetricTotal;
import com.workout.leaderboard.entity.ChallengeUserMetricTotalId;
import com.workout.leaderboard.entity.Metric;
import com.workout.leaderboard.repository.ChallengeEventRepository;
import com.workout.leaderboard.repository.ChallengeRepository;
import com.workout.leaderboard.repository.ChallengeUserMetricTotalRepository;
import com.workout.leaderboard.repository.MetricRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MetricRepository metricRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeEventRepository challengeEventRepository;
    private final ChallengeUserMetricTotalRepository challengeUserMetricTotalRepository;

    public DataInitializer(MetricRepository metricRepository,
                          ChallengeRepository challengeRepository,
                          ChallengeEventRepository challengeEventRepository,
                          ChallengeUserMetricTotalRepository challengeUserMetricTotalRepository) {
        this.metricRepository = metricRepository;
        this.challengeRepository = challengeRepository;
        this.challengeEventRepository = challengeEventRepository;
        this.challengeUserMetricTotalRepository = challengeUserMetricTotalRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeMetrics();
        initializeChallenges();
        initializeChallengeEvents();
        initializeChallengeUserMetricTotals();
    }

    private void initializeMetrics() {
        Metric steps = new Metric("Steps", "SUM");
        Metric calories = new Metric("Calories Burned", "SUM");
        Metric distance = new Metric("Distance Covered", "SUM");
        Metric workoutTime = new Metric("Workout Time (mins)", "SUM");

        metricRepository.save(steps);
        metricRepository.save(calories);
        metricRepository.save(distance);
        metricRepository.save(workoutTime);

        System.out.println("✓ Initialized 4 metrics");
    }

    private void initializeChallenges() {
        LocalDateTime now = LocalDateTime.now();

        Challenge challenge1 = new Challenge(
                "January Steps Challenge",
                "Walk 100,000 steps in January",
                now.minusDays(14),
                now.plusDays(16),
                now
        );

        Challenge challenge2 = new Challenge(
                "Calorie Burner February",
                "Burn 50,000 calories in February",
                now.minusDays(7),
                now.plusDays(23),
                now
        );

        Challenge challenge3 = new Challenge(
                "Marathon Training",
                "Complete 500km of running",
                now.minusDays(30),
                now.plusDays(60),
                now
        );

        challengeRepository.save(challenge1);
        challengeRepository.save(challenge2);
        challengeRepository.save(challenge3);

        System.out.println("✓ Initialized 3 challenges");
    }

    private void initializeChallengeEvents() {
        LocalDateTime now = LocalDateTime.now();
        long eventId = 1L;

        // Get all metrics and challenges
        var metrics = metricRepository.findAll();
        var stepMetric = metrics.stream().filter(m -> m.getName().equals("Steps")).findFirst().orElse(null);
        var calorieMetric = metrics.stream().filter(m -> m.getName().equals("Calories Burned")).findFirst().orElse(null);
        var distanceMetric = metrics.stream().filter(m -> m.getName().equals("Distance Covered")).findFirst().orElse(null);

        var challenges = challengeRepository.findAll();
        var challenge1 = challenges.size() > 0 ? challenges.get(0) : null;
        var challenge2 = challenges.size() > 1 ? challenges.get(1) : null;
        var challenge3 = challenges.size() > 2 ? challenges.get(2) : null;

        // Challenge 1 - User 1
        // if (challenge1 != null && stepMetric != null) {
        //     ChallengeEvent event1 = new ChallengeEvent(challenge1, 1L, stepMetric, 5000.0, now.minusDays(5));
        //     event1.setEventId(eventId++);
        //     challengeEventRepository.save(event1);
            
        //     ChallengeEvent event2 = new ChallengeEvent(challenge1, 1L, stepMetric, 8000.0, now.minusDays(4));
        //     event2.setEventId(eventId++);
        //     challengeEventRepository.save(event2);
            
        //     ChallengeEvent event3 = new ChallengeEvent(challenge1, 1L, stepMetric, 12000.0, now.minusDays(3));
        //     event3.setEventId(eventId++);
        //     challengeEventRepository.save(event3);
        // }

        // Challenge 1 - User 2
        if (challenge1 != null && stepMetric != null) {
            ChallengeEvent event4 = new ChallengeEvent(challenge1, 2L, stepMetric, 7000.0, now.minusDays(5));
            event4.setEventId(eventId++);
            challengeEventRepository.save(event4);
            
            ChallengeEvent event5 = new ChallengeEvent(challenge1, 2L, stepMetric, 9000.0, now.minusDays(4));
            event5.setEventId(eventId++);
            challengeEventRepository.save(event5);
        }

        // Challenge 2 - User 1
        if (challenge2 != null && calorieMetric != null) {
            ChallengeEvent event6 = new ChallengeEvent(challenge2, 1L, calorieMetric, 500.0, now.minusDays(3));
            event6.setEventId(eventId++);
            challengeEventRepository.save(event6);
            
            ChallengeEvent event7 = new ChallengeEvent(challenge2, 1L, calorieMetric, 600.0, now.minusDays(2));
            event7.setEventId(eventId++);
            challengeEventRepository.save(event7);
        }

        // Challenge 2 - User 3
        if (challenge2 != null && calorieMetric != null) {
            ChallengeEvent event8 = new ChallengeEvent(challenge2, 3L, calorieMetric, 450.0, now.minusDays(4));
            event8.setEventId(eventId++);
            challengeEventRepository.save(event8);
            
            ChallengeEvent event9 = new ChallengeEvent(challenge2, 3L, calorieMetric, 550.0, now.minusDays(2));
            event9.setEventId(eventId++);
            challengeEventRepository.save(event9);
        }

        // Challenge 3 - User 2
        if (challenge3 != null && distanceMetric != null) {
            ChallengeEvent event10 = new ChallengeEvent(challenge3, 2L, distanceMetric, 25.5, now.minusDays(6));
            event10.setEventId(eventId++);
            challengeEventRepository.save(event10);
            
            ChallengeEvent event11 = new ChallengeEvent(challenge3, 2L, distanceMetric, 30.0, now.minusDays(5));
            event11.setEventId(eventId++);
            challengeEventRepository.save(event11);
        }

        System.out.println("✓ Initialized challenge events");
    }

    private void initializeChallengeUserMetricTotals() {
        LocalDateTime now = LocalDateTime.now();

        var metrics = metricRepository.findAll();
        var stepMetric = metrics.stream().filter(m -> m.getName().equals("Steps")).findFirst().orElse(null);
        var calorieMetric = metrics.stream().filter(m -> m.getName().equals("Calories Burned")).findFirst().orElse(null);
        var distanceMetric = metrics.stream().filter(m -> m.getName().equals("Distance Covered")).findFirst().orElse(null);



        // Challenge 1 - User 2 - Steps
        if (stepMetric != null) {
            ChallengeUserMetricTotal total = new ChallengeUserMetricTotal(
                    1L, 2L, stepMetric, 16000.0, now
            );
            challengeUserMetricTotalRepository.save(total);
        }

        // Challenge 2 - User 1 - Calories
        if (calorieMetric != null) {
            ChallengeUserMetricTotal total = new ChallengeUserMetricTotal(
                    2L, 1L, calorieMetric, 1100.0, now
            );
            challengeUserMetricTotalRepository.save(total);
        }

        // Challenge 2 - User 3 - Calories
        if (calorieMetric != null) {
            ChallengeUserMetricTotal total = new ChallengeUserMetricTotal(
                    2L, 3L, calorieMetric, 1000.0, now
            );
            challengeUserMetricTotalRepository.save(total);
        }

        // Challenge 3 - User 2 - Distance
        if (distanceMetric != null) {
            ChallengeUserMetricTotal total = new ChallengeUserMetricTotal(
                    3L, 2L, distanceMetric, 55.5, now
            );
            challengeUserMetricTotalRepository.save(total);
        }

        System.out.println("✓ Initialized challenge user metric totals");
    }
}
