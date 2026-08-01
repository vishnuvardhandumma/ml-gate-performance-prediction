package com.gate.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gate.backend.entity.MockTest;
import com.gate.backend.entity.TestAnswer;
import java.util.List;

@Repository
public interface TestAnswerRepository extends JpaRepository<TestAnswer, Long> {
    
    // NEW: Find all test answers for a specific mock test
    List<TestAnswer> findByMockTest(MockTest mockTest);
}