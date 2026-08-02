package com.gate.backend.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mock_tests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String subject;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "correct_answers")
    private Integer correctAnswers;

    @Column(name = "wrong_answers")
    private Integer wrongAnswers;

    @Column(nullable = false)
    private Double accuracy;

    @Column(name = "time_taken_secs")
    private Integer timeTakenSecs;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL)
    private List<TestAnswer> answers;

    @PrePersist
    protected void onCreate() {
        takenAt = LocalDateTime.now();
    }
}