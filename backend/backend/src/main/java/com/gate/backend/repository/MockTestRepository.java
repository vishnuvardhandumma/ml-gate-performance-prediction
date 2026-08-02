package com.gate.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gate.backend.entity.MockTest;
import com.gate.backend.entity.User;

@Repository
public interface MockTestRepository extends JpaRepository<MockTest, Long> {

    List<MockTest> findByUserOrderByTakenAtDesc(User user);

    List<MockTest> findByUserAndSubject(User user, String subject);

    long countByUser(User user);
}