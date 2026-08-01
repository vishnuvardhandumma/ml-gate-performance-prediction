package com.gate.backend.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gate.backend.entity.Question;

public interface QuestionRepository
        extends JpaRepository<Question, Long> {

    List<Question> findBySubject(String subject);

    List<Question> findByMockTestNumber(Integer mockTestNumber);

    default List<Question> findRandomBySubject(String subject, int limit) {
        List<Question> questions = new ArrayList<>(findBySubject(subject));
        if (limit <= 0 || questions.isEmpty()) {
            return questions;
        }
        Collections.shuffle(questions);
        return questions.size() <= limit ? questions : questions.subList(0, limit);
    }

    List<Question> findBySubjectAndMockTestNumber(String subject, Integer mockTestNumber);

    default List<Question> findRandomBySubjectAndMockTest(String subject, Integer mockTestNumber, int limit) {
        List<Question> questions = new ArrayList<>(findBySubjectAndMockTestNumber(subject, mockTestNumber));
        if (limit <= 0 || questions.isEmpty()) {
            return questions;
        }
        Collections.shuffle(questions);
        return questions.size() <= limit ? questions : questions.subList(0, limit);
    }

    void deleteBySubject(String subject);

    long countBySubject(String subject);

    long countByMockTestNumber(Integer mockTestNumber);
}