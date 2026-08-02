package com.gate.backend.repository;

import com.gate.backend.entity.PredictionResult;
import com.gate.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionResultRepository
        extends JpaRepository<PredictionResult, Long> {

    List<PredictionResult> findByUserOrderByPredictedAtDesc(User user);

    Optional<PredictionResult> findByMockTestId(Long mockTestId);
}