/* Dashboard front-end: talks to the Spring Boot REST API under /api/* */

const THEME_KEYS = [
    'red', 'orange', 'yellow', 'green', 'teal', 'light-blue',
    'dark-blue', 'light-pink', 'hot-pink', 'lavender', 'purple', 'brown',
];

const state = {
    monthCursor: startOfMonth(new Date()),
    settings: null,
    syllabusCandidates: [],
    courses: [],
    allTasks: [],
    selectedCourseId: null,
    classInnerTab: 'overview',
    notes: [],
    selectedNoteId: null,
};

document.addEventListener('DOMContentLoaded', init);

async function init() {
    await loadSettings();
    applyTheme();
    await renderWidgets();
    await renderCalendar();
    await renderTodos();
    wireEvents();
}

function wireEvents() {
    document.getElementById('prevWeekBtn').addEventListener('click', () => shiftMonth(-1));
    document.getElementById('nextWeekBtn').addEventListener('click', () => shiftMonth(1));
    document.getElementById('todoForm').addEventListener('submit', onAddTodo);

    document.getElementById('addEventBtn').addEventListener('click', openEventModal);
    document.getElementById('closeEventBtn').addEventListener('click', () => toggleModal('eventModal', false));
    document.getElementById('closeDetailsBtn').addEventListener('click', () => toggleModal('detailsModal', false));
    document.getElementById('eventForm').addEventListener('submit', onSaveEvent);

    document.getElementById('settingsBtn').addEventListener('click', openSettingsModal);
    document.getElementById('closeSettingsBtn').addEventListener('click', () => toggleModal('settingsModal', false));
    document.getElementById('settingsForm').addEventListener('submit', onSaveSettings);
    document.getElementById('themeSwatches').addEventListener('click', onSwatchClick);

    document.getElementById('syncCanvasBtn').addEventListener('click', onSyncCanvas);

    document.getElementById('uploadSyllabusBtn').addEventListener('click', () => toggleModal('syllabusModal', true));
    document.getElementById('closeSyllabusBtn').addEventListener('click', () => toggleModal('syllabusModal', false));
    document.getElementById('previewSyllabusBtn').addEventListener('click', onPreviewSyllabus);
    document.getElementById('confirmSyllabusBtn').addEventListener('click', onConfirmSyllabus);

    document.querySelectorAll('.view-tab').forEach(btn => {
        btn.addEventListener('click', () => switchView(btn.dataset.view));
    });
    document.getElementById('addClassBtn').addEventListener('click', () => openClassModal(null));
    document.getElementById('closeClassBtn').addEventListener('click', () => toggleModal('classModal', false));
    document.getElementById('classForm').addEventListener('submit', onSaveClass);
}

/* ---------------- Calendar ---------------- */

function startOfWeek(date) {
    const d = new Date(date);
    const day = d.getDay();
    d.setDate(d.getDate() - day);
    d.setHours(0, 0, 0, 0);
    return d;
}

function startOfMonth(date) {
    const d = new Date(date.getFullYear(), date.getMonth(), 1);
    d.setHours(0, 0, 0, 0);
    return d;
}

async function shiftMonth(months) {
    state.monthCursor.setMonth(state.monthCursor.getMonth() + months);
    await renderCalendar();
}

const WEEKDAY_LABELS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

async function renderCalendar() {
    const monthStart = startOfMonth(state.monthCursor);
    // Grid always spans 6 full weeks (42 days) so the month is shown as a
    // standard calendar page, including the trailing/leading days needed
    // to fill out the first and last weeks.
    const gridStart = startOfWeek(monthStart);
    const gridEnd = new Date(gridStart);
    gridEnd.setDate(gridStart.getDate() + 42);

    document.getElementById('calendarRangeLabel').textContent =
        monthStart.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });

    const [tasks, events] = await Promise.all([
        api(`/api/tasks/upcoming?start=${toIso(gridStart)}&end=${toIso(gridEnd)}`),
        api(`/api/events/upcoming?start=${toIso(gridStart)}&end=${toIso(gridEnd)}`),
    ]);

    const weekdaysRow = document.getElementById('calendarWeekdays');
    if (weekdaysRow && !weekdaysRow.childElementCount) {
        weekdaysRow.innerHTML = WEEKDAY_LABELS.map(w => `<span>${w}</span>`).join('');
    }

    const body = document.getElementById('calendarBody');
    body.innerHTML = '';

    const today = new Date();
    for (let i = 0; i < 42; i++) {
        const day = new Date(gridStart);
        day.setDate(gridStart.getDate() + i);
        const dayTasks = tasks.filter(t => t.dueDate && sameDay(new Date(t.dueDate), day));
        const dayEvents = events.filter(e => e.startDateTime && sameDay(new Date(e.startDateTime), day));
        const inMonth = day.getMonth() === monthStart.getMonth();

        const col = document.createElement('div');
        col.className = [
            'calendar-day',
            inMonth ? '' : 'calendar-day-outside',
            sameDay(day, today) ? 'calendar-day-today' : '',
        ].filter(Boolean).join(' ');
        col.innerHTML = `<div class="day-label">${day.getDate()}</div>`;
        dayEvents.forEach(ev => {
            const tag = document.createElement('div');
            tag.className = 'calendar-event';
            const timeLabel = formatTime(ev.startDateTime) + (ev.endDateTime ? ` – ${formatTime(ev.endDateTime)}` : '');
            tag.title = `${ev.title} — ${timeLabel}${ev.location ? ' @ ' + ev.location : ''} (click for details)`;
            tag.textContent = `🕒 ${timeLabel} ${ev.title}`;
            tag.addEventListener('click', () => openEventDetails(ev));
            col.appendChild(tag);
        });
        dayTasks.forEach(t => {
            const urgency = urgencyFor(t.priority);
            const isCompleted = t.status === 'COMPLETED';
            const isOverdue = !isCompleted && new Date(t.dueDate).getTime() < Date.now();
            const tag = document.createElement('div');
            tag.className = ['calendar-task', urgency.className, isOverdue ? 'overdue' : ''].filter(Boolean).join(' ');
            tag.title = `${t.title} — ${urgency.label} urgency${isOverdue ? ' (overdue)' : ''} (click for details)`;
            tag.textContent = `${urgency.icon} ${t.title}`;
            tag.addEventListener('click', () => openTaskDetails(t));
            col.appendChild(tag);
        });
        body.appendChild(col);
    }
}

function sameDay(a, b) {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

function toIso(d) {
    return d.toISOString().slice(0, 19);
}

function formatTime(dateStr) {
    return new Date(dateStr).toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' });
}

/* ---------------- Events (time-based calendar entries) ---------------- */

function openEventModal() {
    document.getElementById('eventForm').reset();
    const today = new Date();
    document.getElementById('eventDate').value = today.toISOString().slice(0, 10);
    toggleModal('eventModal', true);
}

async function onSaveEvent(e) {
    e.preventDefault();
    const date = document.getElementById('eventDate').value;
    const startTime = document.getElementById('eventStartTime').value;
    const endTime = document.getElementById('eventEndTime').value;
    const payload = {
        title: document.getElementById('eventTitle').value,
        startDateTime: `${date}T${startTime}:00`,
        endDateTime: endTime ? `${date}T${endTime}:00` : null,
        location: document.getElementById('eventLocation').value || null,
    };
    await api('/api/events', 'POST', payload);
    toggleModal('eventModal', false);
    await renderCalendar();
}

function formatDateTime(dateStr) {
    return new Date(dateStr).toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
}

function openEventDetails(ev) {
    const timeLabel = formatTime(ev.startDateTime) + (ev.endDateTime ? ` – ${formatTime(ev.endDateTime)}` : '');
    document.getElementById('detailsTitle').textContent = ev.title;
    document.getElementById('detailsBody').innerHTML = `
        <dl>
            <dt>When</dt>
            <dd>${escapeHtml(formatDateTime(ev.startDateTime))} · ${escapeHtml(timeLabel)}</dd>
            ${ev.location ? `<dt>Location</dt><dd>${escapeHtml(ev.location)}</dd>` : ''}
            ${ev.course ? `<dt>Class</dt><dd>${escapeHtml(ev.course.name)}</dd>` : ''}
            ${ev.description ? `<dt>Description</dt><dd>${escapeHtml(ev.description)}</dd>` : ''}
        </dl>`;
    document.getElementById('detailsCompleteBtn').classList.add('hidden');
    document.getElementById('detailsDeleteBtn').onclick = async () => {
        if (!confirm(`Delete "${ev.title}"?`)) return;
        await api(`/api/events/${ev.id}`, 'DELETE');
        toggleModal('detailsModal', false);
        await renderCalendar();
    };
    toggleModal('detailsModal', true);
}

function openTaskDetails(t) {
    const urgency = urgencyFor(t.priority);
    const isCompleted = t.status === 'COMPLETED';
    const dueDate = t.dueDate ? new Date(t.dueDate) : null;
    const isOverdue = !!dueDate && !isCompleted && dueDate.getTime() < Date.now();
    const due = dueDate
        ? dueDate.toLocaleString(undefined, { month: 'long', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit' })
        : 'No due date';
    document.getElementById('detailsTitle').textContent = t.title;
    document.getElementById('detailsBody').innerHTML = `
        <dl>
            <dt>Urgency</dt>
            <dd>${urgency.icon} ${urgency.label}</dd>
            <dt>Due</dt>
            <dd class="${isOverdue ? 'overdue-text' : ''}">${isOverdue ? '⚠️ Overdue: ' : '📅 '}${escapeHtml(due)}</dd>
            <dt>Status</dt>
            <dd>${isCompleted ? '✅ Completed' : '⏳ Pending'}</dd>
            ${t.course ? `<dt>Class</dt><dd>${escapeHtml(t.course.name)}</dd>` : ''}
            ${t.description ? `<dt>Description</dt><dd>${escapeHtml(t.description)}</dd>` : ''}
        </dl>`;
    const completeBtn = document.getElementById('detailsCompleteBtn');
    completeBtn.classList.toggle('hidden', isCompleted);
    completeBtn.textContent = 'Mark complete';
    completeBtn.onclick = async () => {
        await api(`/api/tasks/${t.id}/complete`, 'POST');
        toggleModal('detailsModal', false);
        await renderTodos();
        await renderCalendar();
    };
    document.getElementById('detailsDeleteBtn').onclick = async () => {
        if (!confirm(`Delete "${t.title}"?`)) return;
        await api(`/api/tasks/${t.id}`, 'DELETE');
        toggleModal('detailsModal', false);
        await renderTodos();
        await renderCalendar();
    };
    toggleModal('detailsModal', true);
}

/* ---------------- To-dos ---------------- */

async function renderTodos() {
    const todos = await api('/api/tasks/todos');
    const list = document.getElementById('todoList');
    list.innerHTML = '';
    todos.forEach(t => list.appendChild(todoItem(t)));
}

const URGENCY_LEVELS = {
    low: { max: 2, label: 'Low', className: 'urgency-low', icon: '🟢' },
    medium: { max: 3, label: 'Medium', className: 'urgency-medium', icon: '🟡' },
    high: { max: 5, label: 'Urgent', className: 'urgency-high', icon: '🔴' },
};

function urgencyFor(priority) {
    const p = Number(priority) || 0;
    if (p <= URGENCY_LEVELS.low.max) return URGENCY_LEVELS.low;
    if (p <= URGENCY_LEVELS.medium.max) return URGENCY_LEVELS.medium;
    return URGENCY_LEVELS.high;
}

function buildTaskListItem(t, { onComplete, onDelete }) {
    const li = document.createElement('li');
    const urgency = urgencyFor(t.priority);
    const isCompleted = t.status === 'COMPLETED';
    const dueDate = t.dueDate ? new Date(t.dueDate) : null;
    const isOverdue = !!dueDate && !isCompleted && dueDate.getTime() < Date.now();
    const due = dueDate ? dueDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' }) : 'No due date';

    li.className = ['todo-item', urgency.className, isCompleted ? 'completed' : '', isOverdue ? 'overdue' : '']
        .filter(Boolean).join(' ');
    li.innerHTML = `
        <div class="todo-main">
            <div class="todo-title-row">
                <span class="todo-title">${escapeHtml(t.title)}</span>
                <span class="urgency-badge ${urgency.className}" title="${urgency.label} urgency">${urgency.icon} ${urgency.label}</span>
            </div>
            <div class="meta">
                ${t.course ? escapeHtml(t.course.name) + ' · ' : ''}
                <span class="due-date${isOverdue ? ' overdue-text' : ''}">${isOverdue ? '⚠️ Overdue: ' : '📅 '}${due}</span>
            </div>
        </div>
        <div class="todo-actions">
            <button data-action="complete" title="Mark complete">✓</button>
            <button data-action="delete" title="Delete">✕</button>
        </div>`;
    li.querySelector('[data-action="complete"]').addEventListener('click', onComplete);
    li.querySelector('[data-action="delete"]').addEventListener('click', onDelete);
    return li;
}

function todoItem(t) {
    return buildTaskListItem(t, {
        onComplete: () => completeTodo(t.id),
        onDelete: () => deleteTodo(t.id),
    });
}


async function onAddTodo(e) {
    e.preventDefault();
    const title = document.getElementById('todoTitle').value.trim();
    const priority = parseInt(document.getElementById('todoPriority').value, 10);
    const dueDateValue = document.getElementById('todoDueDate').value;
    if (!title) return;
    const payload = { title, priority, type: 'TODO', status: 'PENDING' };
    if (dueDateValue) {
        // Date-only input; treat the due date as end-of-day so same-day tasks aren't marked overdue early.
        payload.dueDate = `${dueDateValue}T23:59:00`;
    }
    await api('/api/tasks', 'POST', payload);
    document.getElementById('todoTitle').value = '';
    document.getElementById('todoDueDate').value = '';
    await renderTodos();
    await renderCalendar();
}

async function completeTodo(id) {
    await api(`/api/tasks/${id}/complete`, 'POST');
    await renderTodos();
}

async function deleteTodo(id) {
    await api(`/api/tasks/${id}`, 'DELETE');
    await renderTodos();
    await renderCalendar();
}

/* ---------------- Classes ---------------- */

const CLASS_INNER_TABS = ['overview', 'tasks', 'syllabus', 'notes'];
const CLASS_INNER_TAB_LABELS = { overview: 'Overview', tasks: 'Tasks', syllabus: 'Syllabus', notes: 'Notes' };

function switchView(view) {
    document.querySelectorAll('.view-tab').forEach(btn => btn.classList.toggle('active', btn.dataset.view === view));
    document.getElementById('dashboardView').classList.toggle('hidden', view !== 'dashboard');
    document.getElementById('classesView').classList.toggle('hidden', view !== 'classes');
    if (view === 'classes') {
        loadClassesView();
    }
}

async function loadClassesView() {
    [state.courses, state.allTasks] = await Promise.all([api('/api/courses'), api('/api/tasks')]);
    if (state.selectedCourseId && !state.courses.some(c => c.id === state.selectedCourseId)) {
        state.selectedCourseId = null;
    }
    renderClassList();
    renderClassDetail();
}

function renderClassList() {
    const list = document.getElementById('classList');
    list.innerHTML = '';
    if (state.courses.length === 0) {
        list.innerHTML = `<li class="hint">No classes yet. Add one to get started.</li>`;
        return;
    }
    state.courses.forEach(c => {
        const li = document.createElement('li');
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = `class-list-item${c.id === state.selectedCourseId ? ' active' : ''}`;
        btn.innerHTML = `
            <span class="class-color-dot" style="background:${escapeHtml(c.colorHex || '#4f46e5')}"></span>
            <span class="class-name-col">
                <span class="class-name">${escapeHtml(c.name)}</span>
                <span class="class-code">${escapeHtml(c.code || '')}</span>
            </span>`;
        btn.addEventListener('click', () => {
            state.selectedCourseId = c.id;
            state.classInnerTab = 'overview';
            state.selectedNoteId = null;
            renderClassList();
            renderClassDetail();
        });
        li.appendChild(btn);
        list.appendChild(li);
    });
}

function renderClassDetail() {
    const detail = document.getElementById('classDetail');
    const course = state.courses.find(c => c.id === state.selectedCourseId);
    if (!course) {
        detail.innerHTML = `<p class="hint">Select a class on the left, or add a new one, to see its details.</p>`;
        return;
    }

    const tabsHtml = CLASS_INNER_TABS.map(tab => `
        <button type="button" class="inner-tab${tab === state.classInnerTab ? ' active' : ''}" data-tab="${tab}">
            ${CLASS_INNER_TAB_LABELS[tab]}
        </button>`).join('');

    detail.innerHTML = `
        <div class="class-detail-header">
            <span class="class-color-dot" style="background:${escapeHtml(course.colorHex || '#4f46e5')}"></span>
            <h2>${escapeHtml(course.name)}</h2>
        </div>
        <div class="class-detail-meta">${escapeHtml(course.code || '')}${course.code && course.instructor ? ' · ' : ''}${escapeHtml(course.instructor || '')}</div>
        <div class="inner-tabs">${tabsHtml}</div>
        <div id="classInnerContent"></div>`;

    detail.querySelectorAll('.inner-tab').forEach(btn => {
        btn.addEventListener('click', () => {
            state.classInnerTab = btn.dataset.tab;
            renderClassDetail();
        });
    });

    const content = document.getElementById('classInnerContent');
    if (state.classInnerTab === 'overview') {
        renderClassOverview(content, course);
    } else if (state.classInnerTab === 'tasks') {
        renderClassTasks(content, course);
    } else if (state.classInnerTab === 'syllabus') {
        renderClassSyllabus(content, course);
    } else {
        renderClassNotes(content, course);
    }
}

function renderClassOverview(content, course) {
    content.innerHTML = `
        <div class="class-overview-fields">
            <p><strong>Name:</strong> ${escapeHtml(course.name)}</p>
            <p><strong>Code:</strong> ${escapeHtml(course.code || '—')}</p>
            <p><strong>Instructor:</strong> ${escapeHtml(course.instructor || '—')}</p>
            <p><strong>Source:</strong> ${course.canvasCourseId ? 'Synced from Canvas' : 'Manually added'}</p>
        </div>
        <div class="class-detail-actions">
            <button type="button" class="btn btn-outline" id="editClassBtn">Edit</button>
            <button type="button" class="btn btn-outline" id="deleteClassBtn">Delete class</button>
        </div>`;
    content.querySelector('#editClassBtn').addEventListener('click', () => openClassModal(course));
    content.querySelector('#deleteClassBtn').addEventListener('click', () => onDeleteClass(course));
}

function renderClassTasks(content, course) {
    const tasks = state.allTasks
        .filter(t => t.course && t.course.id === course.id)
        .sort((a, b) => (a.dueDate || '').localeCompare(b.dueDate || ''));

    content.innerHTML = `
        <form id="classTaskForm" class="todo-form">
            <input type="text" id="classTaskTitle" placeholder="Add a task for this class..." required>
            <input type="date" id="classTaskDueDate" aria-label="Due date">
            <select id="classTaskPriority" aria-label="Urgency">
                <option value="1">Low</option>
                <option value="3" selected>Medium</option>
                <option value="5">Urgent</option>
            </select>
            <button type="submit" class="btn btn-primary">Add</button>
        </form>
        ${tasks.length === 0
            ? `<p class="hint">No tasks for this class yet. Add one above.</p>`
            : `<ul class="todo-list" id="classTaskList"></ul>`}`;

    if (tasks.length > 0) {
        const list = content.querySelector('#classTaskList');
        tasks.forEach(t => list.appendChild(classTaskItem(t, course, content)));
    }

    content.querySelector('#classTaskForm').addEventListener('submit', (e) => onAddClassTask(e, course, content));
}

function classTaskItem(t, course, content) {
    return buildTaskListItem(t, {
        onComplete: async () => {
            await api(`/api/tasks/${t.id}/complete`, 'POST');
            state.allTasks = await api('/api/tasks');
            renderClassTasks(content, course);
        },
        onDelete: async () => {
            await api(`/api/tasks/${t.id}`, 'DELETE');
            state.allTasks = await api('/api/tasks');
            renderClassTasks(content, course);
        },
    });
}

async function onAddClassTask(e, course, content) {
    e.preventDefault();
    const title = content.querySelector('#classTaskTitle').value.trim();
    const priority = parseInt(content.querySelector('#classTaskPriority').value, 10);
    const dueDateValue = content.querySelector('#classTaskDueDate').value;
    if (!title) return;
    const payload = { title, priority, type: 'TODO', status: 'PENDING', course: { id: course.id } };
    if (dueDateValue) {
        // Date-only input; treat the due date as end-of-day so same-day tasks aren't marked overdue early.
        payload.dueDate = `${dueDateValue}T23:59:00`;
    }
    await api('/api/tasks', 'POST', payload);
    state.allTasks = await api('/api/tasks');
    renderClassTasks(content, course);
    await renderTodos();
    await renderCalendar();
}

function renderClassSyllabus(content, course) {
    const tasks = state.allTasks
        .filter(t => t.course && t.course.id === course.id && t.fromSyllabus)
        .sort((a, b) => (a.dueDate || '').localeCompare(b.dueDate || ''));
    if (tasks.length === 0) {
        content.innerHTML = `<p class="hint">No syllabus-derived dates for this class yet. Upload a syllabus from the Dashboard and assign items to this course.</p>`;
        return;
    }
    const list = document.createElement('ul');
    list.className = 'todo-list';
    tasks.forEach(t => list.appendChild(todoItem(t)));
    content.innerHTML = '';
    content.appendChild(list);
}

async function renderClassNotes(content, course) {
    content.innerHTML = `<p class="hint">Loading notes…</p>`;
    state.notes = await api(`/api/notes?courseId=${course.id}`);
    if (state.selectedNoteId && !state.notes.some(n => n.id === state.selectedNoteId)) {
        state.selectedNoteId = null;
    }
    if (!state.selectedNoteId && state.notes.length > 0) {
        state.selectedNoteId = state.notes[0].id;
    }
    renderNotesBody(content, course);
}

function renderNotesBody(content, course) {
    const notes = state.notes;
    const tabsHtml = notes.map(n => `
        <button type="button" class="note-tab${n.id === state.selectedNoteId ? ' active' : ''}" data-id="${n.id}">
            ${escapeHtml(n.title)}
        </button>`).join('');
    const note = notes.find(n => n.id === state.selectedNoteId);

    content.innerHTML = `
        <div class="note-tabs-row">
            <div class="note-tabs">${tabsHtml}</div>
            <button type="button" class="btn btn-outline btn-small" id="addNoteBtn">+ New note</button>
        </div>
        ${notes.length === 0
            ? `<p class="hint">No notes yet for this class. Add one to start taking notes.</p>`
            : `<div class="note-editor">
                   <input type="text" id="noteTitleInput" class="note-title-input" placeholder="Note title" value="${escapeHtml(note.title)}">
                   <textarea id="noteContentInput" class="note-content-input" placeholder="Write your notes here...">${escapeHtml(note.content || '')}</textarea>
                   <div class="class-detail-actions">
                       <button type="button" class="btn btn-primary" id="saveNoteBtn">Save note</button>
                       <button type="button" class="btn btn-outline" id="deleteNoteBtn">Delete note</button>
                   </div>
               </div>`}`;

    content.querySelectorAll('.note-tab').forEach(btn => {
        btn.addEventListener('click', () => {
            state.selectedNoteId = Number(btn.dataset.id);
            renderNotesBody(content, course);
        });
    });
    content.querySelector('#addNoteBtn').addEventListener('click', () => onAddNote(course, content));

    if (note) {
        content.querySelector('#saveNoteBtn').addEventListener('click', () => onSaveNote(note, course, content));
        content.querySelector('#deleteNoteBtn').addEventListener('click', () => onDeleteNote(note, course, content));
    }
}

async function onAddNote(course, content) {
    const created = await api('/api/notes', 'POST', { title: 'Untitled note', content: '', course: { id: course.id } });
    state.notes.push(created);
    state.selectedNoteId = created.id;
    renderNotesBody(content, course);
}

async function onSaveNote(note, course, content) {
    const title = content.querySelector('#noteTitleInput').value.trim() || 'Untitled note';
    const body = content.querySelector('#noteContentInput').value;
    const updated = await api(`/api/notes/${note.id}`, 'PUT', { title, content: body, course: { id: course.id } });
    const idx = state.notes.findIndex(n => n.id === note.id);
    if (idx !== -1) state.notes[idx] = updated;
    renderNotesBody(content, course);
}

async function onDeleteNote(note, course, content) {
    if (!confirm(`Delete note "${note.title}"?`)) return;
    await api(`/api/notes/${note.id}`, 'DELETE');
    state.notes = state.notes.filter(n => n.id !== note.id);
    state.selectedNoteId = state.notes.length > 0 ? state.notes[0].id : null;
    renderNotesBody(content, course);
}

function openClassModal(course) {
    document.getElementById('classModalTitle').textContent = course ? 'Edit Class' : 'Add Class';
    document.getElementById('classId').value = course ? course.id : '';
    document.getElementById('className').value = course ? course.name : '';
    document.getElementById('classCode').value = course ? (course.code || '') : '';
    document.getElementById('classInstructor').value = course ? (course.instructor || '') : '';
    document.getElementById('classColor').value = course ? (course.colorHex || '#4f46e5') : '#4f46e5';
    toggleModal('classModal', true);
}

async function onSaveClass(e) {
    e.preventDefault();
    const id = document.getElementById('classId').value;
    const payload = {
        name: document.getElementById('className').value.trim(),
        code: document.getElementById('classCode').value.trim(),
        instructor: document.getElementById('classInstructor').value.trim(),
        colorHex: document.getElementById('classColor').value,
    };
    if (!payload.name) return;
    const saved = id ? await api(`/api/courses/${id}`, 'PUT', payload) : await api('/api/courses', 'POST', payload);
    toggleModal('classModal', false);
    state.selectedCourseId = saved.id;
    state.classInnerTab = 'overview';
    await loadClassesView();
}

async function onDeleteClass(course) {
    if (!confirm(`Delete "${course.name}"? This will also delete all of its tasks.`)) return;
    await api(`/api/courses/${course.id}`, 'DELETE');
    state.selectedCourseId = null;
    await loadClassesView();
}

/* ---------------- Settings / customization ---------------- */

async function loadSettings() {
    state.settings = await api('/api/settings');
}

function applyTheme() {
    const s = state.settings;
    document.body.classList.remove(...THEME_KEYS.map(k => `theme-${k}`));
    if (s.themeColor && THEME_KEYS.includes(s.themeColor)) {
        document.body.classList.add(`theme-${s.themeColor}`);
    }
    document.body.classList.toggle('theme-dark', s.themeMode === 'dark');
}

function selectSwatch(key) {
    document.getElementById('themeColor').value = key;
    document.querySelectorAll('#themeSwatches .swatch').forEach(btn => {
        btn.classList.toggle('selected', btn.dataset.theme === key);
    });
}

function onSwatchClick(e) {
    const btn = e.target.closest('.swatch');
    if (!btn) return;
    selectSwatch(btn.dataset.theme);
}

function openSettingsModal() {
    const s = state.settings;
    document.getElementById('displayName').value = s.displayName || '';
    selectSwatch(THEME_KEYS.includes(s.themeColor) ? s.themeColor : THEME_KEYS[0]);
    document.getElementById('themeMode').value = s.themeMode || 'light';
    document.getElementById('weatherWidgetEnabled').checked = !!s.weatherWidgetEnabled;
    document.getElementById('weatherLocation').value = s.weatherLocation || '';
    document.getElementById('sportsWidgetEnabled').checked = !!s.sportsWidgetEnabled;
    document.getElementById('sportsLeague').value = s.sportsLeague || 'football/nfl';
    document.getElementById('sportsTeam').value = s.sportsTeam || '';
    document.getElementById('canvasBaseUrl').value = s.canvasBaseUrl || '';
    document.getElementById('canvasApiToken').value = '';
    toggleModal('settingsModal', true);
}

async function onSaveSettings(e) {
    e.preventDefault();
    const s = { ...state.settings };
    s.displayName = document.getElementById('displayName').value;
    s.themeColor = document.getElementById('themeColor').value;
    s.themeMode = document.getElementById('themeMode').value;
    s.weatherWidgetEnabled = document.getElementById('weatherWidgetEnabled').checked;
    s.weatherLocation = document.getElementById('weatherLocation').value;
    s.sportsWidgetEnabled = document.getElementById('sportsWidgetEnabled').checked;
    s.sportsLeague = document.getElementById('sportsLeague').value;
    s.sportsTeam = document.getElementById('sportsTeam').value;
    s.canvasBaseUrl = document.getElementById('canvasBaseUrl').value;
    const token = document.getElementById('canvasApiToken').value;
    if (token) {
        s.canvasApiToken = token;
        s.canvasSyncEnabled = true;
    }
    state.settings = await api('/api/settings', 'PUT', s);
    applyTheme();
    await renderWidgets();
    toggleModal('settingsModal', false);
}

function toggleModal(id, show) {
    document.getElementById(id).classList.toggle('hidden', !show);
}

/* ---------------- Optional widgets (live weather via Open-Meteo, live scores via ESPN) ---------------- */

async function renderWidgets() {
    const panel = document.getElementById('widgetPanel');
    panel.innerHTML = '';
    const s = state.settings;

    if (s.weatherWidgetEnabled) {
        panel.appendChild(await buildWeatherWidget());
    }
    if (s.sportsWidgetEnabled) {
        panel.appendChild(await buildSportsWidget());
    }
    if (!s.weatherWidgetEnabled && !s.sportsWidgetEnabled) {
        const w = document.createElement('div');
        w.className = 'widget';
        w.innerHTML = `<h3>✨ Customize</h3><p class="hint">Enable weather, sports, and more from the ⚙️ settings menu.</p>`;
        panel.appendChild(w);
    }
}

async function buildWeatherWidget() {
    const w = document.createElement('div');
    w.className = 'widget';
    w.innerHTML = `<h3>🌤️ Weather</h3><p class="hint">Loading…</p>`;
    try {
        const data = await api('/api/widgets/weather');
        if (data.status !== 'ok') throw new Error(data.message || 'Weather unavailable');
        const wx = data.weather;
        w.innerHTML = `
            <h3>${wx.emoji} Weather</h3>
            <p class="widget-primary">${Math.round(wx.temperatureF)}°F — ${escapeHtml(wx.condition)}</p>
            <p class="hint">${escapeHtml(wx.location)} · wind ${Math.round(wx.windMph)} mph</p>`;
    } catch (err) {
        w.innerHTML = `<h3>🌤️ Weather</h3><p class="hint">${escapeHtml(err.message)}</p>`;
    }
    return w;
}

async function buildSportsWidget() {
    const w = document.createElement('div');
    w.className = 'widget';
    w.innerHTML = `<h3>🏈 Sports</h3><p class="hint">Loading…</p>`;
    try {
        const data = await api('/api/widgets/sports');
        if (data.status !== 'ok') throw new Error(data.message || 'Scores unavailable');

        // When a favorite team is set and ESPN recognizes it, the API returns the team's
        // previous and next games directly instead of a generic league scoreboard.
        if ('previousGame' in data || 'nextGame' in data) {
            const rows = [];
            if (data.previousGame) rows.push(sportsGameRow(data.previousGame, 'Previous game'));
            if (data.nextGame) rows.push(sportsGameRow(data.nextGame, 'Next game'));
            if (rows.length === 0) {
                w.innerHTML = `<h3>🏈 Sports</h3><p class="hint">No schedule found for your favorite team yet.</p>`;
                return w;
            }
            w.innerHTML = `<h3>🏈 Sports</h3><ul class="sports-list">${rows.join('')}</ul>`;
            return w;
        }

        const games = data.games || [];
        if (games.length === 0) {
            w.innerHTML = `<h3>🏈 Sports</h3><p class="hint">No games found right now.</p>`;
            return w;
        }
        const rows = games.map(g => `
            <li class="${g.favorite ? 'favorite' : ''}">
                ${gameLink(g, `${scoreboardTable(g)}<span class="meta">${escapeHtml(g.statusDetail || '')}</span>`)}
            </li>`).join('');
        w.innerHTML = `<h3>🏈 Sports</h3><ul class="sports-list">${rows}</ul>`;
    } catch (err) {
        w.innerHTML = `<h3>🏈 Sports</h3><p class="hint">${escapeHtml(err.message)}</p>`;
    }
    return w;
}

function sportsGameRow(g, label) {
    const dateStr = g.date ? new Date(g.date).toLocaleDateString(undefined, { month: 'short', day: 'numeric' }) : '';
    const inner = `
        <span class="meta">${escapeHtml(label)}${dateStr ? ' · ' + dateStr : ''}</span>
        ${scoreboardTable(g)}
        <span class="meta">${escapeHtml(g.statusDetail || '')}</span>`;
    return `<li class="favorite">${gameLink(g, inner)}</li>`;
}

/**
 * Wraps a game bubble's inner markup in a link to its espn.com game page when one is available,
 * so clicking the bubble takes the student to full ESPN coverage; falls back to a plain span
 * (no link) if the backend couldn't resolve an ESPN event id for this game.
 */
function gameLink(g, innerHtml) {
    if (!g.espnUrl) return `<span class="game-link">${innerHtml}</span>`;
    // Navigates in the same tab (no target="_blank") so the click always works even in
    // sandboxed/embedded browser contexts that silently block new-tab popups.
    return `<a class="game-link" href="${escapeHtml(g.espnUrl)}" rel="noopener noreferrer">${innerHtml}</a>`;
}

/**
 * Renders a game's two teams as stacked rows with right-aligned scores (away team on top, home
 * team on bottom, matching the familiar scoreboard layout) instead of a single run-on line like
 * "Team A 35 @ Team B 14", which was easy to misread as to who scored what.
 */
function scoreboardTable(g) {
    const hasScore = g.awayScore != null && g.homeScore != null;
    const awayWins = hasScore && g.completed && Number(g.awayScore) > Number(g.homeScore);
    const homeWins = hasScore && g.completed && Number(g.homeScore) > Number(g.awayScore);
    return `
        <table class="scoreboard">
            <tr class="${awayWins ? 'winner' : ''}">
                <td class="team-name">${escapeHtml(g.awayTeam)}<span class="away-tag">away</span></td>
                <td class="team-score">${g.awayScore ?? '–'}</td>
            </tr>
            <tr class="${homeWins ? 'winner' : ''}">
                <td class="team-name">${escapeHtml(g.homeTeam)}<span class="home-tag">home</span></td>
                <td class="team-score">${g.homeScore ?? '–'}</td>
            </tr>
        </table>`;
}

/* ---------------- Canvas sync ---------------- */

async function onSyncCanvas() {
    const btn = document.getElementById('syncCanvasBtn');
    btn.disabled = true;
    btn.textContent = 'Syncing…';
    try {
        const result = await api('/api/canvas/sync', 'POST');
        if (result.status === 'ok') {
            alert(`Synced ${result.tasksSynced} Canvas items.`);
            await renderCalendar();
            await renderTodos();
        } else {
            alert(result.message || 'Canvas sync failed.');
        }
    } finally {
        btn.disabled = false;
        btn.textContent = 'Sync Canvas';
    }
}

/* ---------------- Syllabus upload ---------------- */

async function onPreviewSyllabus() {
    const fileInput = document.getElementById('syllabusFile');
    if (!fileInput.files.length) {
        alert('Choose a file first.');
        return;
    }
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const res = await fetch('/api/syllabus/preview', {
        method: 'POST',
        headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : undefined,
        body: formData,
    });
    const candidates = await res.json();
    if (!Array.isArray(candidates)) {
        alert(candidates.message || 'Could not parse file.');
        return;
    }
    state.syllabusCandidates = candidates;
    const list = document.getElementById('syllabusCandidates');
    list.innerHTML = '';
    candidates.forEach((c, idx) => {
        const li = document.createElement('li');
        li.className = 'todo-item';
        li.innerHTML = `
            <label style="display:flex; gap:8px; align-items:center;">
                <input type="checkbox" data-idx="${idx}" checked>
                <span>${escapeHtml(c.title)} — ${new Date(c.dueDate).toLocaleDateString()}</span>
            </label>`;
        list.appendChild(li);
    });
}

async function onConfirmSyllabus() {
    const checked = Array.from(document.querySelectorAll('#syllabusCandidates input[type="checkbox"]:checked'))
        .map(cb => state.syllabusCandidates[parseInt(cb.dataset.idx, 10)]);
    if (!checked.length) {
        toggleModal('syllabusModal', false);
        return;
    }
    await api('/api/syllabus/confirm', 'POST', checked);
    toggleModal('syllabusModal', false);
    await renderCalendar();
    await renderTodos();
}

/* ---------------- Helpers ---------------- */

async function api(url, method = 'GET', body) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const headers = body ? { 'Content-Type': 'application/json' } : {};
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
    const res = await fetch(url, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });
    if (!res.ok) {
        const text = await res.text();
        throw new Error(`API error ${res.status}: ${text}`);
    }
    const contentType = res.headers.get('content-type') || '';
    return contentType.includes('application/json') ? res.json() : null;
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str ?? '';
    return div.innerHTML;
}
