package com.gate.backend.service;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gate.backend.dto.FullTestResponse;
import com.gate.backend.dto.MockTestQuestionsResponse;
import com.gate.backend.dto.QuestionResponse;
import com.gate.backend.dto.TestResultResponse;
import com.gate.backend.dto.TestSubmitRequest;
import com.gate.backend.entity.MockTest;
import com.gate.backend.entity.Question;
import com.gate.backend.entity.TestAnswer;
import com.gate.backend.entity.User;
import com.gate.backend.exception.AppException;
import com.gate.backend.repository.MockTestRepository;
import com.gate.backend.repository.QuestionRepository;
import com.gate.backend.repository.TestAnswerRepository;
import com.gate.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MockTestService {

    private final QuestionRepository questionRepository;
    private final MockTestRepository mockTestRepository;
    private final TestAnswerRepository testAnswerRepository;
    private final UserRepository userRepository;
    private final PredictionService predictionService;

    public List<QuestionResponse> getQuestions(String subject) {

        List<Question> questions =
                questionRepository.findRandomBySubject(subject, 10);

        if (questions.isEmpty()) {
            throw new AppException(
                "No questions found for subject: " + subject,
                HttpStatus.NOT_FOUND);
        }

        List<QuestionResponse> response = new ArrayList<>();
        for (Question q : questions) {
            response.add(QuestionResponse.builder()
                    .id(q.getId())
                    .subject(q.getSubject())
                    .topic(q.getTopic())
                    .questionText(q.getQuestionText())
                    .optionA(q.getOptionA())
                    .optionB(q.getOptionB())
                    .optionC(q.getOptionC())
                    .optionD(q.getOptionD())
                    .difficulty(q.getDifficulty())
                    .build());
        }
        return response;
    }
    public MockTestQuestionsResponse getMockTestQuestions(
        int mockTestNumber, String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException(
                "User not found", HttpStatus.NOT_FOUND));

    String[] subjects = {
        "Computer Science",
        "Mathematics",
        "General Aptitude"
    };

    List<MockTestQuestionsResponse.SectionDTO> sections =
            new ArrayList<>();
    int totalMarks = 0;

    for (String subject : subjects) {
        List<Question> questions =
            questionRepository.findRandomBySubject(subject, 10);

        List<QuestionResponse> qResponses = new ArrayList<>();
        for (Question q : questions) {
            totalMarks += (q.getMarks() != null ? q.getMarks() : 1);
            qResponses.add(QuestionResponse.builder()
                    .id(q.getId())
                    .subject(q.getSubject())
                    .topic(q.getTopic())
                    .questionText(q.getQuestionText())
                    .optionA(q.getOptionA())
                    .optionB(q.getOptionB())
                    .optionC(q.getOptionC())
                    .optionD(q.getOptionD())
                    .marks(q.getMarks())
                    .difficulty(q.getDifficulty())
                    .build());
        }

        sections.add(MockTestQuestionsResponse.SectionDTO.builder()
                .subject(subject)
                .questionCount(qResponses.size())
                .questions(qResponses)
                .build());
    }

    // Check previous attempt for this mock test number
    MockTestQuestionsResponse.PreviousAttempt previousAttempt = null;
    List<MockTest> history =
        mockTestRepository.findByUserOrderByTakenAtDesc(user);

    // Filter by mock test number stored in subject field
    String mockLabel = "Mock Test " + mockTestNumber;
    for (MockTest mt : history) {
        if (mockLabel.equals(mt.getSubject())) {
            previousAttempt =
                MockTestQuestionsResponse.PreviousAttempt.builder()
                    .testId(mt.getId())
                    .accuracy(mt.getAccuracy())
                    .correctAnswers(mt.getCorrectAnswers())
                    .totalQuestions(mt.getTotalQuestions())
                    .takenAt(mt.getTakenAt() != null
                        ? mt.getTakenAt().format(
                            DateTimeFormatter.ofPattern(
                                "dd MMM yyyy, hh:mm a"))
                        : "")
                    .build();
            break;
        }
    }

    return MockTestQuestionsResponse.builder()
            .mockTestNumber(mockTestNumber)
            .title("GATE Mock Test " + mockTestNumber)
            .totalQuestions(30)
            .totalMarks(totalMarks)
            .sections(sections)
            .previousAttempt(previousAttempt)
            .build();
}



    @Transactional
    public FullTestResponse submitTest(String email,
                                       TestSubmitRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(
                    "User not found", HttpStatus.NOT_FOUND));

        Map<Long, String> userAnswers = request.getAnswers();

        int correct = 0;
        int wrong = 0;
        List<TestAnswer> answerList = new ArrayList<>();
        double totalPossibleMarks = 0;
        double actualMarks = 0;

        for (Map.Entry<Long, String> entry : userAnswers.entrySet()) {
            Long questionId = entry.getKey();
            String selectedOption = entry.getValue();

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new AppException(
                            "Question not found", HttpStatus.NOT_FOUND));

            int qMarks = (question.getMarks() != null ? question.getMarks() : 1);
            totalPossibleMarks += qMarks;

            boolean isCorrect = false;
            if (selectedOption != null && !selectedOption.isBlank()) {
                if (selectedOption.equalsIgnoreCase(question.getCorrectOption())) {
                    correct++;
                    actualMarks += qMarks;
                    isCorrect = true;
                } else {
                    wrong++;
                    actualMarks -= (qMarks / 3.0); // Standard MCQ Negative Marking
                }
            }

            answerList.add(TestAnswer.builder()
                    .question(question)
                    .selectedOption(selectedOption)
                    .isCorrect(isCorrect)
                    .build());
        }

        int totalQuestions = (request.getSubject() != null && request.getSubject().startsWith("Mock Test")) ? 30 : 10;

        // Calculate Accuracy based on Marks (GATE style)
        double accuracy = totalPossibleMarks > 0
                ? Math.round((actualMarks / totalPossibleMarks) * 10000.0) / 100.0
                : 0.0;
        
        // Clip negative accuracy at 0 for simple reporting, but maintain precision
        accuracy = Math.max(0.0, accuracy);

        MockTest mockTest = MockTest.builder()
                .user(user)
                .subject(request.getSubject())
                .totalQuestions(totalQuestions)
                .correctAnswers(correct)
                .wrongAnswers(wrong)
                .accuracy(accuracy)
                .timeTakenSecs(request.getTimeTakenSecs())
                .build();

        MockTest savedTest = mockTestRepository.save(mockTest);

        for (TestAnswer answer : answerList) {
            answer.setMockTest(savedTest);
        }
        testAnswerRepository.saveAll(answerList);



        // FIXED: predict() now returns FullTestResponse with all components
        FullTestResponse fullResponse =
                predictionService.predict(user, savedTest);

        return fullResponse;
    }

    public List<TestResultResponse> getHistory(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(
                    "User not found", HttpStatus.NOT_FOUND));

        List<MockTest> tests =
                mockTestRepository.findByUserOrderByTakenAtDesc(user);

        List<TestResultResponse> history = new ArrayList<>();
        for (MockTest t : tests) {
            history.add(TestResultResponse.builder()
                    .testId(t.getId())
                    .subject(t.getSubject())
                    .totalQuestions(t.getTotalQuestions())
                    .correctAnswers(t.getCorrectAnswers())
                    .wrongAnswers(t.getWrongAnswers())
                    .accuracy(t.getAccuracy())
                    .timeTakenSecs(t.getTimeTakenSecs())
                    .build());
        }
        return history;
    }
}