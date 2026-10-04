package com.academictaskmanager.service;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.model.TaskType;
import com.academictaskmanager.repository.AcademicTaskRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts important dates (assignment due dates, exam dates, etc.) from an
 * uploaded syllabus PDF so they can be added straight to the calendar.
 *
 * This is an MVP heuristic: it scans the extracted text line-by-line looking
 * for a recognizable date (e.g. "September 12", "9/12/2026", "Sep 12") and
 * keeps the surrounding line text as the task title/description. It will not
 * catch every syllabus format, but gets a usable first pass that the student
 * can review and edit before saving.
 */
@Service
public class SyllabusParsingService {

    private final AcademicTaskRepository taskRepository;

    // "September 12", "Sep 12", "Sep. 12, 2026"
    private static final Pattern MONTH_DAY = Pattern.compile(
            "\\b(Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|" +
                    "Sep(?:t|tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)\\.?\\s+(\\d{1,2})(?:st|nd|rd|th)?(?:,?\\s*(\\d{4}))?",
            Pattern.CASE_INSENSITIVE);

    // "9/12/2026", "9/12"
    private static final Pattern NUMERIC_DATE = Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?\\b");

    public SyllabusParsingService(AcademicTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /** Parses a PDF or plain-text syllabus and returns candidate tasks (not yet persisted). */
    public List<AcademicTask> extractCandidateTasks(MultipartFile file, Course course) throws IOException {
        String text = extractText(file);
        return parseDatesFromText(text, course);
    }

    /** Persists the previously-extracted candidate tasks the student chose to keep. */
    public List<AcademicTask> saveTasks(List<AcademicTask> tasks) {
        return taskRepository.saveAll(tasks);
    }

    private String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (filename.endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                return new PDFTextStripper().getText(document);
            }
        }
        // Treat anything else (.txt, pasted content) as plain text.
        return new String(file.getBytes());
    }

    private List<AcademicTask> parseDatesFromText(String text, Course course) {
        List<AcademicTask> candidates = new ArrayList<>();
        int currentYear = Year.now().getValue();

        for (String rawLine : text.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            LocalDate date = tryParseMonthDay(line, currentYear);
            if (date == null) {
                date = tryParseNumericDate(line, currentYear);
            }
            if (date == null) continue;

            AcademicTask task = new AcademicTask();
            task.setTitle(summarize(line));
            task.setDescription(line);
            task.setDueDate(date.atTime(23, 59));
            task.setType(guessType(line));
            task.setStatus(TaskStatus.PENDING);
            task.setFromSyllabus(true);
            task.setCourse(course);
            candidates.add(task);
        }
        return candidates;
    }

    private LocalDate tryParseMonthDay(String line, int currentYear) {
        Matcher m = MONTH_DAY.matcher(line);
        if (!m.find()) return null;
        try {
            String month = m.group(1);
            int day = Integer.parseInt(m.group(2));
            int year = m.group(3) != null ? Integer.parseInt(m.group(3)) : currentYear;
            String normalizedMonth = normalizeMonth(month);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);
            return LocalDate.parse(normalizedMonth + " " + day + " " + year, fmt);
        } catch (DateTimeParseException | NumberFormatException e) {
            return null;
        }
    }

    private LocalDate tryParseNumericDate(String line, int currentYear) {
        Matcher m = NUMERIC_DATE.matcher(line);
        if (!m.find()) return null;
        try {
            int month = Integer.parseInt(m.group(1));
            int day = Integer.parseInt(m.group(2));
            int year = m.group(3) != null ? normalizeYear(m.group(3)) : currentYear;
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            return null;
        }
    }

    private int normalizeYear(String yearStr) {
        int year = Integer.parseInt(yearStr);
        return yearStr.length() == 2 ? 2000 + year : year;
    }

    private String normalizeMonth(String month) {
        String m = month.toLowerCase(Locale.ROOT).replace(".", "");
        if (m.startsWith("jan")) return "Jan";
        if (m.startsWith("feb")) return "Feb";
        if (m.startsWith("mar")) return "Mar";
        if (m.startsWith("apr")) return "Apr";
        if (m.startsWith("may")) return "May";
        if (m.startsWith("jun")) return "Jun";
        if (m.startsWith("jul")) return "Jul";
        if (m.startsWith("aug")) return "Aug";
        if (m.startsWith("sep")) return "Sep";
        if (m.startsWith("oct")) return "Oct";
        if (m.startsWith("nov")) return "Nov";
        return "Dec";
    }

    private TaskType guessType(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        if (lower.contains("exam") || lower.contains("midterm") || lower.contains("final")) return TaskType.EXAM;
        if (lower.contains("quiz")) return TaskType.QUIZ;
        if (lower.contains("due") || lower.contains("assignment") || lower.contains("homework") || lower.contains("hw")) {
            return TaskType.ASSIGNMENT;
        }
        return TaskType.EVENT;
    }

    private String summarize(String line) {
        return line.length() > 80 ? line.substring(0, 80) + "…" : line;
    }
}
