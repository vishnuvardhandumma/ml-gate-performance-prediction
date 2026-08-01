package com.gate.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prediction_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_test_id")
    private MockTest mockTest;

    @Column(name = "predicted_score_min")
    private Double predictedScoreMin;

    @Column(name = "predicted_score_max")
    private Double predictedScoreMax;

    @Column(name = "cutoff_probability")
    private Double cutoffProbability;

    @Column(name = "weak_subjects", columnDefinition = "TEXT")
    private String weakSubjects;

    @Column(name = "predicted_at")
    private LocalDateTime predictedAt;

    @PrePersist
    protected void onCreate() {
        predictedAt = LocalDateTime.now();
    }
}