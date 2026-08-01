package com.gate.backend.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gate.backend.entity.Question;
import com.gate.backend.exception.AppException;
import com.gate.backend.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getBySubject(String subject) {
        return questionRepository.findBySubject(subject);
    }

    public List<Question> getByMockTestNumber(Integer mockTestNumber) {
        return questionRepository.findByMockTestNumber(mockTestNumber);
    }

    public Question addQuestion(Question question) {
        if (question.getMarks() == null) question.setMarks(1);
        if (question.getDifficulty() == null) question.setDifficulty(1);
        return questionRepository.save(question);
    }

    public Question updateQuestion(Long id, Question question) {
        Question existing = questionRepository.findById(id)
            .orElseThrow(() -> new AppException(
                "Question not found", HttpStatus.NOT_FOUND));
        
        existing.setSubject(question.getSubject());
        existing.setTopic(question.getTopic());
        existing.setQuestionText(question.getQuestionText());
        existing.setOptionA(question.getOptionA());
        existing.setOptionB(question.getOptionB());
        existing.setOptionC(question.getOptionC());
        existing.setOptionD(question.getOptionD());
        existing.setCorrectOption(question.getCorrectOption());
        
        if (question.getMarks() != null) 
            existing.setMarks(question.getMarks());
        if (question.getDifficulty() != null) 
            existing.setDifficulty(question.getDifficulty());
        if (question.getMockTestNumber() != null)
            existing.setMockTestNumber(question.getMockTestNumber());
        
        return questionRepository.save(existing);
    }

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    public String importFromExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(
                "File is empty", HttpStatus.BAD_REQUEST);
        }

        String filename = file.getOriginalFilename();
        if (filename == null ||
            !filename.endsWith(".xlsx")) {
            throw new AppException(
                "Only .xlsx files are supported",
                HttpStatus.BAD_REQUEST);
        }

        List<Question> questions = new ArrayList<>();
        int rowsRead    = 0;
        int rowsSkipped = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row — start from row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String subject    = getCellValue(row, 0);
                    String topic      = getCellValue(row, 1);
                    String questionTx = getCellValue(row, 2);
                    String optionA    = getCellValue(row, 3);
                    String optionB    = getCellValue(row, 4);
                    String optionC    = getCellValue(row, 5);
                    String optionD    = getCellValue(row, 6);
                    String correct    = getCellValue(row, 7);
                    int marks         = getIntValue(row, 8, 1);
                    int difficulty    = getIntValue(row, 9, 1);

                    if (subject.isBlank() || questionTx.isBlank()
                            || correct.isBlank()) {
                        rowsSkipped++;
                        continue;
                    }

                    questions.add(Question.builder()
                            .subject(subject.trim())
                            .topic(topic.trim())
                            .questionText(questionTx.trim())
                            .optionA(optionA.trim())
                            .optionB(optionB.trim())
                            .optionC(optionC.trim())
                            .optionD(optionD.trim())
                            .correctOption(correct.trim().toUpperCase())
                            .marks(marks)
                            .difficulty(difficulty)
                            .build());
                    rowsRead++;

                } catch (Exception e) {
                    log.warn("Skipping row {}: {}", i, e.getMessage());
                    rowsSkipped++;
                }
            }

            questionRepository.saveAll(questions);

            return String.format(
                "Import complete. %d questions added, %d rows skipped.",
                rowsRead, rowsSkipped);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(
                "Failed to read Excel file: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String importFromExcel(MultipartFile file, Integer mockTestNumber) {
        if (file.isEmpty()) {
            throw new AppException(
                "File is empty", HttpStatus.BAD_REQUEST);
        }

        String filename = file.getOriginalFilename();
        if (filename == null ||
            !filename.endsWith(".xlsx")) {
            throw new AppException(
                "Only .xlsx files are supported",
                HttpStatus.BAD_REQUEST);
        }

        List<Question> questions = new ArrayList<>();
        int rowsRead    = 0;
        int rowsSkipped = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row — start from row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String subject    = getCellValue(row, 0);
                    String topic      = getCellValue(row, 1);
                    String questionTx = getCellValue(row, 2);
                    String optionA    = getCellValue(row, 3);
                    String optionB    = getCellValue(row, 4);
                    String optionC    = getCellValue(row, 5);
                    String optionD    = getCellValue(row, 6);
                    String correct    = getCellValue(row, 7);
                    int marks         = getIntValue(row, 8, 1);
                    int difficulty    = getIntValue(row, 9, 1);

                    if (subject.isBlank() || questionTx.isBlank()
                            || correct.isBlank()) {
                        rowsSkipped++;
                        continue;
                    }

                    questions.add(Question.builder()
                            .subject(subject.trim())
                            .topic(topic.trim())
                            .questionText(questionTx.trim())
                            .optionA(optionA.trim())
                            .optionB(optionB.trim())
                            .optionC(optionC.trim())
                            .optionD(optionD.trim())
                            .correctOption(correct.trim().toUpperCase())
                            .marks(marks)
                            .difficulty(difficulty)
                            .mockTestNumber(mockTestNumber)
                            .build());
                    rowsRead++;

                } catch (Exception e) {
                    log.warn("Skipping row {}: {}", i, e.getMessage());
                    rowsSkipped++;
                }
            }

            questionRepository.saveAll(questions);

            return String.format(
                "Import complete for Mock Test %d. %d questions added, %d rows skipped.",
                mockTestNumber, rowsRead, rowsSkipped);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(
                "Failed to read Excel file: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getCellValue(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(
                (long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private int getIntValue(Row row, int col, int defaultVal) {
        try {
            Cell cell = row.getCell(col);
            if (cell == null) return defaultVal;
            if (cell.getCellType() == CellType.NUMERIC)
                return (int) cell.getNumericCellValue();
            return Integer.parseInt(
                cell.getStringCellValue().trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}