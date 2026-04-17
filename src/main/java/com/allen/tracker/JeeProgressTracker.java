package com.allen.tracker;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Industry-style JEE progress tracker for class 12 learners.
 *
 * Core features:
 * - Topic-level mastery tracking
 * - Subject-wise time and completion analytics
 * - Mock-test history with weighted trend scores
 * - Productivity metrics: streaks + weekly consistency
 * - Priority queue for weak topics to focus revision planning
 */
public class JeeProgressTracker {

    public static void main(String[] args) {
        TrackerService tracker = new TrackerService("Student-001");
        seedDemoData(tracker);

        System.out.println("=== ALLEN-Style JEE Progress Tracker (Class 12) ===");
        System.out.println("1) View dashboard");
        System.out.println("2) Add study session");
        System.out.println("3) Add mock test score");
        System.out.println("4) View weak topics plan");
        System.out.println("5) Exit");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.print("\nEnter choice: ");
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1" -> printDashboard(tracker);
                    case "2" -> addStudySessionInteractive(scanner, tracker);
                    case "3" -> addMockTestInteractive(scanner, tracker);
                    case "4" -> printWeakTopicsPlan(tracker);
                    case "5" -> running = false;
                    default -> System.out.println("Invalid choice. Please enter 1-5.");
                }
            }
        }

        System.out.println("Good luck for JEE! Keep improving 1% daily.");
    }

    private static void seedDemoData(TrackerService tracker) {
        tracker.logStudySession(new StudySession(Subject.PHYSICS, "Electrostatics", 2.0, 78, LocalDate.now().minusDays(6)));
        tracker.logStudySession(new StudySession(Subject.CHEMISTRY, "Coordination Compounds", 1.5, 65, LocalDate.now().minusDays(5)));
        tracker.logStudySession(new StudySession(Subject.MATHEMATICS, "Definite Integration", 2.5, 72, LocalDate.now().minusDays(4)));
        tracker.logStudySession(new StudySession(Subject.PHYSICS, "Current Electricity", 2.0, 81, LocalDate.now().minusDays(3)));
        tracker.logStudySession(new StudySession(Subject.CHEMISTRY, "Aldehydes and Ketones", 1.5, 70, LocalDate.now().minusDays(2)));
        tracker.logStudySession(new StudySession(Subject.MATHEMATICS, "Probability", 2.2, 68, LocalDate.now().minusDays(1)));
        tracker.logStudySession(new StudySession(Subject.PHYSICS, "Magnetism", 1.8, 75, LocalDate.now()));

        tracker.recordMockTest(new MockTestResult("FT-01", 122, 300, LocalDate.now().minusDays(14)));
        tracker.recordMockTest(new MockTestResult("FT-02", 138, 300, LocalDate.now().minusDays(7)));
        tracker.recordMockTest(new MockTestResult("FT-03", 151, 300, LocalDate.now().minusDays(1)));
    }

    private static void addStudySessionInteractive(Scanner scanner, TrackerService tracker) {
        try {
            System.out.print("Subject (PHYSICS/CHEMISTRY/MATHEMATICS): ");
            Subject subject = Subject.valueOf(scanner.nextLine().trim().toUpperCase(Locale.ROOT));

            System.out.print("Topic: ");
            String topic = scanner.nextLine().trim();

            System.out.print("Hours studied: ");
            double hours = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Mastery score (0-100): ");
            int mastery = Integer.parseInt(scanner.nextLine().trim());

            tracker.logStudySession(new StudySession(subject, topic, hours, mastery, LocalDate.now()));
            System.out.println("Study session saved.");
        } catch (Exception e) {
            System.out.println("Invalid input. Session not saved.");
        }
    }

    private static void addMockTestInteractive(Scanner scanner, TrackerService tracker) {
        try {
            System.out.print("Test name (e.g., FT-10): ");
            String name = scanner.nextLine().trim();

            System.out.print("Marks obtained: ");
            int obtained = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Total marks: ");
            int total = Integer.parseInt(scanner.nextLine().trim());

            tracker.recordMockTest(new MockTestResult(name, obtained, total, LocalDate.now()));
            System.out.println("Mock test recorded.");
        } catch (Exception e) {
            System.out.println("Invalid input. Test not recorded.");
        }
    }

    private static void printDashboard(TrackerService tracker) {
        DashboardSnapshot snapshot = tracker.generateDashboard();

        System.out.println("\n--- Dashboard ---");
        System.out.printf("Student ID: %s%n", snapshot.studentId());
        System.out.printf("Overall Preparation Index: %.2f/100%n", snapshot.overallPreparationIndex());
        System.out.printf("Current Study Streak: %d day(s)%n", snapshot.currentStreakDays());
        System.out.printf("Weekly Consistency: %.2f%%%n", snapshot.weeklyConsistencyPercent());
        System.out.printf("Projected Percentile (estimated): %.2f%n", snapshot.projectedPercentile());

        System.out.println("\nSubject Analytics:");
        snapshot.subjectAnalytics().forEach((subject, analytics) -> System.out.printf(
            "- %s: Hours=%.1f, Avg Mastery=%.1f, Completion=%.1f%%%n",
            subject, analytics.totalHours(), analytics.averageMastery(), analytics.completionPercent()
        ));

        if (!snapshot.recentTestTrends().isEmpty()) {
            System.out.println("\nRecent Mock Tests:");
            snapshot.recentTestTrends().forEach(test -> System.out.printf(
                "- %s (%s): %d/%d (%.1f%%)%n",
                test.testName(), test.date(), test.marksObtained(), test.totalMarks(), test.percentScore()
            ));
        }
    }

    private static void printWeakTopicsPlan(TrackerService tracker) {
        System.out.println("\n--- Top Weak Topics (Revision Priority) ---");
        List<TopicInsight> weakTopics = tracker.getWeakTopics(8);
        if (weakTopics.isEmpty()) {
            System.out.println("No topics found. Start logging sessions first.");
            return;
        }

        int rank = 1;
        for (TopicInsight topic : weakTopics) {
            System.out.printf(
                "%d) %s - %s | Mastery=%.1f | Hours=%.1f | PriorityScore=%.2f%n",
                rank++, topic.subject(), topic.topicName(), topic.averageMastery(), topic.totalHours(), topic.revisionPriorityScore()
            );
        }
    }
}

enum Subject {
    PHYSICS,
    CHEMISTRY,
    MATHEMATICS
}

record StudySession(Subject subject, String topic, double hoursStudied, int masteryScore, LocalDate date) {
    StudySession {
        Objects.requireNonNull(subject, "subject cannot be null");
        Objects.requireNonNull(topic, "topic cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        if (hoursStudied <= 0 || masteryScore < 0 || masteryScore > 100) {
            throw new IllegalArgumentException("Invalid hours or mastery score");
        }
    }
}

record MockTestResult(String testName, int marksObtained, int totalMarks, LocalDate date) {
    MockTestResult {
        Objects.requireNonNull(testName, "testName cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        if (marksObtained < 0 || totalMarks <= 0 || marksObtained > totalMarks) {
            throw new IllegalArgumentException("Invalid mock test marks");
        }
    }

    double percentScore() {
        return (marksObtained * 100.0) / totalMarks;
    }
}

record SubjectAnalytics(double totalHours, double averageMastery, double completionPercent) {}

record TopicInsight(Subject subject, String topicName, double averageMastery, double totalHours, double revisionPriorityScore) {}

record DashboardSnapshot(
    String studentId,
    double overallPreparationIndex,
    int currentStreakDays,
    double weeklyConsistencyPercent,
    double projectedPercentile,
    Map<Subject, SubjectAnalytics> subjectAnalytics,
    List<MockTestResult> recentTestTrends
) {}

class TrackerService {
    private static final int WEEKLY_TARGET_STUDY_DAYS = 6;

    private final String studentId;
    private final List<StudySession> studySessions = new ArrayList<>();
    private final List<MockTestResult> mockTests = new ArrayList<>();

    TrackerService(String studentId) {
        this.studentId = studentId;
    }

    void logStudySession(StudySession session) {
        studySessions.add(session);
    }

    void recordMockTest(MockTestResult testResult) {
        mockTests.add(testResult);
        mockTests.sort(Comparator.comparing(MockTestResult::date));
    }

    DashboardSnapshot generateDashboard() {
        Map<Subject, SubjectAnalytics> subjectData = computeSubjectAnalytics();
        int streakDays = computeCurrentStreak();
        double consistency = computeWeeklyConsistency();
        double weightedMockScore = computeWeightedMockPercent();

        double masteryAvg = subjectData.values().stream()
            .mapToDouble(SubjectAnalytics::averageMastery)
            .average()
            .orElse(0.0);

        double preparationIndex = (masteryAvg * 0.45) + (consistency * 0.20) + (weightedMockScore * 0.35);
        double projectedPercentile = estimatePercentile(weightedMockScore, masteryAvg, consistency);

        List<MockTestResult> recentTests = mockTests.stream()
            .sorted(Comparator.comparing(MockTestResult::date).reversed())
            .limit(5)
            .collect(Collectors.toList());

        return new DashboardSnapshot(
            studentId,
            round(preparationIndex),
            streakDays,
            round(consistency),
            round(projectedPercentile),
            subjectData,
            recentTests
        );
    }

    List<TopicInsight> getWeakTopics(int topN) {
        return studySessions.stream()
            .collect(Collectors.groupingBy(s -> s.subject() + "::" + s.topic()))
            .entrySet()
            .stream()
            .map(entry -> {
                String[] parts = entry.getKey().split("::", 2);
                Subject subject = Subject.valueOf(parts[0]);
                String topic = parts[1];

                double avgMastery = entry.getValue().stream().mapToInt(StudySession::masteryScore).average().orElse(0.0);
                double totalHours = entry.getValue().stream().mapToDouble(StudySession::hoursStudied).sum();

                // Higher score = higher revision urgency.
                double priority = (100 - avgMastery) * 0.75 + Math.max(0, 2.5 - totalHours) * 10;
                return new TopicInsight(subject, topic, round(avgMastery), round(totalHours), round(priority));
            })
            .sorted(Comparator.comparingDouble(TopicInsight::revisionPriorityScore).reversed())
            .limit(topN)
            .collect(Collectors.toList());
    }

    private Map<Subject, SubjectAnalytics> computeSubjectAnalytics() {
        Map<Subject, SubjectAnalytics> results = new EnumMap<>(Subject.class);
        for (Subject subject : Subject.values()) {
            List<StudySession> subjectSessions = studySessions.stream()
                .filter(s -> s.subject() == subject)
                .collect(Collectors.toList());

            double totalHours = subjectSessions.stream().mapToDouble(StudySession::hoursStudied).sum();
            double avgMastery = subjectSessions.stream().mapToInt(StudySession::masteryScore).average().orElse(0.0);
            long uniqueTopics = subjectSessions.stream().map(StudySession::topic).distinct().count();

            // For demo, we assume class-12 JEE scope target topics per subject is 18.
            double completion = Math.min(100.0, (uniqueTopics * 100.0) / 18.0);
            results.put(subject, new SubjectAnalytics(round(totalHours), round(avgMastery), round(completion)));
        }
        return results;
    }

    private int computeCurrentStreak() {
        if (studySessions.isEmpty()) {
            return 0;
        }

        List<LocalDate> days = studySessions.stream()
            .map(StudySession::date)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        int streak = 0;
        LocalDate cursor = LocalDate.now();

        if (!days.contains(cursor) && days.contains(cursor.minusDays(1))) {
            cursor = cursor.minusDays(1);
        }

        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        return streak;
    }

    private double computeWeeklyConsistency() {
        WeekFields wf = WeekFields.of(Locale.getDefault());
        int currentWeek = LocalDate.now().get(wf.weekOfWeekBasedYear());
        int currentYear = LocalDate.now().getYear();

        long studiedDaysThisWeek = studySessions.stream()
            .map(StudySession::date)
            .distinct()
            .filter(date -> date.getYear() == currentYear && date.get(wf.weekOfWeekBasedYear()) == currentWeek)
            .count();

        return Math.min(100.0, (studiedDaysThisWeek * 100.0) / WEEKLY_TARGET_STUDY_DAYS);
    }

    private double computeWeightedMockPercent() {
        if (mockTests.isEmpty()) {
            return 0.0;
        }

        double totalWeight = 0.0;
        double weightedSum = 0.0;

        for (int i = 0; i < mockTests.size(); i++) {
            // More recent tests get higher weight.
            double weight = i + 1;
            weightedSum += mockTests.get(i).percentScore() * weight;
            totalWeight += weight;
        }

        return weightedSum / totalWeight;
    }

    private double estimatePercentile(double weightedMockPercent, double mastery, double consistency) {
        // Simplified estimation formula for planning purposes only.
        double composite = (weightedMockPercent * 0.65) + (mastery * 0.20) + (consistency * 0.15);
        double percentile = Math.min(99.9, Math.max(20.0, 20 + (composite * 0.8)));
        return percentile;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
