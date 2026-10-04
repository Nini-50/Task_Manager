# Academic Task Manager

A Java (Spring Boot) web app that helps students track assignments, quizzes,
exams, and personal to-dos in one customizable dashboard — with optional
Canvas LMS sync and syllabus-to-calendar import.

## Features (MVP)

- **Accounts** — create an account (username + password) so your courses, tasks, and settings are kept private to you, even on a shared browser/deployment.
- **Dashboard** — weekly calendar view of upcoming assignments/quizzes/exams/events, plus a to-do list widget.
- **To-Do lists** — add, prioritize, complete, and delete personal tasks.
- **Canvas LMS sync** — connect your school's Canvas instance with a personal access token to pull in courses and assignment due dates (`/api/canvas/sync`).
- **Syllabus upload** — upload a PDF/text syllabus; the app scans it for dated items (due dates, exams) and lets you review/confirm before adding them to your calendar.
- **Customization** — theme color/mode, and optional widgets (weather, sports — wired up as placeholders ready for an API key) via the ⚙️ settings panel.

## Tech stack

- Java 21, Spring Boot 3.3 (Web MVC, Spring Data JPA, Spring Security, Thymeleaf, Validation)
- H2 file-based database (no external DB setup needed)
- Apache PDFBox for syllabus text extraction
- Vanilla HTML/CSS/JS front end (no build step required)

## Getting started

```bash
./mvnw spring-boot:run
```

Then open http://localhost:8080 — you'll be redirected to **/login**. Click
**Create an account**, pick a username and password (8+ characters), then log
in. Everything you add from there (courses, tasks, settings) is scoped to
your account. Data is stored in `./data/` (H2 file database, git-ignored) so
it persists across restarts — accounts and data are per-browser-session
(simple form login + cookie session), not a hosted multi-device identity
system. The H2 web console is available at http://localhost:8080/h2-console
for inspecting data during development (JDBC URL:
`jdbc:h2:file:./data/academic-task-manager`).

## Connecting Canvas

1. In Canvas, go to **Account → Settings → New Access Token** to generate a personal access token.
2. In the app, open **⚙️ Customize** and fill in your Canvas base URL (e.g. `https://yourschool.instructure.com`) and the token.
3. Click **Sync Canvas** on the dashboard to pull in your active courses and assignments.

## Project layout

```
src/main/java/com/academictaskmanager/
  model/        JPA entities (Course, AcademicTask, UserSettings, enums)
  repository/   Spring Data repositories
  service/      Business logic (Canvas sync, syllabus parsing, task/course/settings services)
  controller/   REST API + the dashboard page controller
src/main/resources/
  templates/    Thymeleaf dashboard page
  static/       CSS/JS for the dashboard
```

## Roadmap ideas

- Multi-user accounts/authentication
- Live weather and sports score widgets (wire up a provider API key)
- Smarter syllabus parsing (NLP-based date/assignment extraction)
- Push/email reminders for upcoming due dates
- Mobile-friendly layout
