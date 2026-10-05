package com.academictaskmanager.service;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.TaskType;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.AcademicTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SyllabusParsingServiceTest {

    @Mock
    private AcademicTaskRepository taskRepository;

    private SyllabusParsingService syllabusParsingService;

    @BeforeEach
    void setUp() {
        syllabusParsingService = new SyllabusParsingService(taskRepository);
    }

    @Test
    void extractsMonthDayDatesFromPlainText() throws Exception {
        int year = Year.now().getValue();
        String content = "Homework 1 due September 12\nMidterm Exam on Oct 20, " + year + "\nJust a regular line";
        MockMultipartFile file = new MockMultipartFile("file", "syllabus.txt", "text/plain", content.getBytes());

        List<AcademicTask> candidates = syllabusParsingService.extractCandidateTasks(file, null);

        assertThat(candidates).hasSize(2);
        assertThat(candidates.get(0).getDueDate().toLocalDate()).isEqualTo(LocalDate.of(year, 9, 12));
        assertThat(candidates.get(0).getType()).isEqualTo(TaskType.ASSIGNMENT);
        assertThat(candidates.get(1).getDueDate().toLocalDate()).isEqualTo(LocalDate.of(year, 10, 20));
        assertThat(candidates.get(1).getType()).isEqualTo(TaskType.EXAM);
    }

    @Test
    void extractsNumericDatesFromPlainText() throws Exception {
        String content = "Quiz 2 due 10/15/2026";
        MockMultipartFile file = new MockMultipartFile("file", "syllabus.txt", "text/plain", content.getBytes());

        List<AcademicTask> candidates = syllabusParsingService.extractCandidateTasks(file, null);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getDueDate().toLocalDate()).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(candidates.get(0).getType()).isEqualTo(TaskType.QUIZ);
    }

    @Test
    void linesWithoutDatesProduceNoCandidates() throws Exception {
        String content = "Welcome to the course!\nNo dates here.";
        MockMultipartFile file = new MockMultipartFile("file", "syllabus.txt", "text/plain", content.getBytes());

        List<AcademicTask> candidates = syllabusParsingService.extractCandidateTasks(file, null);

        assertThat(candidates).isEmpty();
    }

    @Test
    void extractsTopicSuggestionsFromScheduleHeadings() throws Exception {
        String content = "Week 1: Introduction to Java\n"
                + "Some filler text about office hours.\n"
                + "Week 2 - Control Flow\n"
                + "Unit 3: Recursion\n"
                + "week 1: introduction to java\n"; // duplicate (case-insensitive) should be ignored
        MockMultipartFile file = new MockMultipartFile("file", "syllabus.txt", "text/plain", content.getBytes());

        List<String> suggestions = syllabusParsingService.extractTopicSuggestions(file);

        assertThat(suggestions).containsExactly("Introduction to Java", "Control Flow", "Recursion");
    }

    @Test
    void extractKeyTermSuggestionsFromGlossaryLines() throws Exception {
        String content = "Recursion: a function that calls itself to solve a problem\n"
                + "Base case - the condition that stops recursion from continuing\n"
                + "Homework 1 due September 12\n" // should be skipped as a date line
                + "Week 1: Introduction to Java\n" // should be skipped: a topic heading, not a glossary entry
                + "Just a regular sentence with no colon\n";
        MockMultipartFile file = new MockMultipartFile("file", "syllabus.txt", "text/plain", content.getBytes());

        List<com.academictaskmanager.dto.KeyTermSuggestion> suggestions =
                syllabusParsingService.extractKeyTermSuggestions(file);

        assertThat(suggestions).hasSize(2);
        assertThat(suggestions.get(0).getTerm()).isEqualTo("Recursion");
        assertThat(suggestions.get(0).getDefinition()).isEqualTo("a function that calls itself to solve a problem");
        assertThat(suggestions.get(1).getTerm()).isEqualTo("Base case");
    }

    @Test
    void saveTasksDelegatesToRepository() {
        List<AcademicTask> tasks = List.of(new AcademicTask());
        User owner = new User();

        syllabusParsingService.saveTasks(tasks, owner);

        verify(taskRepository).saveAll(tasks);
    }
}
