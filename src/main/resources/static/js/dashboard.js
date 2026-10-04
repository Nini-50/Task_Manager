/* Dashboard front-end: talks to the Spring Boot REST API under /api/* */

const state = {
    weekStart: startOfWeek(new Date()),
    settings: null,
    syllabusCandidates: [],
};

document.addEventListener('DOMContentLoaded', init);

async function init() {
    await loadSettings();
    applyTheme();
    renderWidgets();
    await renderCalendar();
    await renderTodos();
    wireEvents();
}

function wireEvents() {
    document.getElementById('prevWeekBtn').addEventListener('click', () => shiftWeek(-7));
    document.getElementById('nextWeekBtn').addEventListener('click', () => shiftWeek(7));
    document.getElementById('todoForm').addEventListener('submit', onAddTodo);

    document.getElementById('settingsBtn').addEventListener('click', openSettingsModal);
    document.getElementById('closeSettingsBtn').addEventListener('click', () => toggleModal('settingsModal', false));
    document.getElementById('settingsForm').addEventListener('submit', onSaveSettings);

    document.getElementById('syncCanvasBtn').addEventListener('click', onSyncCanvas);

    document.getElementById('uploadSyllabusBtn').addEventListener('click', () => toggleModal('syllabusModal', true));
    document.getElementById('closeSyllabusBtn').addEventListener('click', () => toggleModal('syllabusModal', false));
    document.getElementById('previewSyllabusBtn').addEventListener('click', onPreviewSyllabus);
    document.getElementById('confirmSyllabusBtn').addEventListener('click', onConfirmSyllabus);
}

/* ---------------- Calendar ---------------- */

function startOfWeek(date) {
    const d = new Date(date);
    const day = d.getDay();
    d.setDate(d.getDate() - day);
    d.setHours(0, 0, 0, 0);
    return d;
}

async function shiftWeek(days) {
    state.weekStart.setDate(state.weekStart.getDate() + days);
    await renderCalendar();
}

async function renderCalendar() {
    const start = new Date(state.weekStart);
    const end = new Date(start);
    end.setDate(end.getDate() + 7);

    document.getElementById('calendarRangeLabel').textContent =
        `${formatShort(start)} – ${formatShort(new Date(end.getTime() - 86400000))}`;

    const tasks = await api(`/api/tasks/upcoming?start=${toIso(start)}&end=${toIso(end)}`);
    const body = document.getElementById('calendarBody');
    body.innerHTML = '';

    for (let i = 0; i < 7; i++) {
        const day = new Date(start);
        day.setDate(day.getDate() + i);
        const dayTasks = tasks.filter(t => t.dueDate && sameDay(new Date(t.dueDate), day));

        const col = document.createElement('div');
        col.className = 'calendar-day';
        col.innerHTML = `<div class="day-label">${day.toLocaleDateString(undefined, { weekday: 'short', day: 'numeric' })}</div>`;
        dayTasks.forEach(t => {
            const tag = document.createElement('div');
            tag.className = 'calendar-task';
            tag.title = t.title;
            tag.textContent = t.title;
            col.appendChild(tag);
        });
        body.appendChild(col);
    }
}

function sameDay(a, b) {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

function formatShort(d) {
    return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

function toIso(d) {
    return d.toISOString().slice(0, 19);
}

/* ---------------- To-dos ---------------- */

async function renderTodos() {
    const todos = await api('/api/tasks/todos');
    const list = document.getElementById('todoList');
    list.innerHTML = '';
    todos.forEach(t => list.appendChild(todoItem(t)));
}

function todoItem(t) {
    const li = document.createElement('li');
    li.className = 'todo-item' + (t.status === 'COMPLETED' ? ' completed' : '');
    const due = t.dueDate ? new Date(t.dueDate).toLocaleDateString() : 'No due date';
    li.innerHTML = `
        <div>
            <div>${escapeHtml(t.title)}</div>
            <div class="meta">${t.course ? escapeHtml(t.course.name) + ' · ' : ''}${due}</div>
        </div>
        <div>
            <button data-action="complete" data-id="${t.id}" title="Mark complete">✓</button>
            <button data-action="delete" data-id="${t.id}" title="Delete">✕</button>
        </div>`;
    li.querySelector('[data-action="complete"]').addEventListener('click', () => completeTodo(t.id));
    li.querySelector('[data-action="delete"]').addEventListener('click', () => deleteTodo(t.id));
    return li;
}

async function onAddTodo(e) {
    e.preventDefault();
    const title = document.getElementById('todoTitle').value.trim();
    const priority = parseInt(document.getElementById('todoPriority').value, 10);
    if (!title) return;
    await api('/api/tasks', 'POST', { title, priority, type: 'TODO', status: 'PENDING' });
    document.getElementById('todoTitle').value = '';
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

/* ---------------- Settings / customization ---------------- */

async function loadSettings() {
    state.settings = await api('/api/settings');
}

function applyTheme() {
    const s = state.settings;
    document.documentElement.style.setProperty('--theme-color', s.themeColor || '#4f46e5');
    document.body.classList.toggle('theme-dark', s.themeMode === 'dark');
}

function openSettingsModal() {
    const s = state.settings;
    document.getElementById('displayName').value = s.displayName || '';
    document.getElementById('themeColor').value = s.themeColor || '#4f46e5';
    document.getElementById('themeMode').value = s.themeMode || 'light';
    document.getElementById('weatherWidgetEnabled').checked = !!s.weatherWidgetEnabled;
    document.getElementById('weatherLocation').value = s.weatherLocation || '';
    document.getElementById('sportsWidgetEnabled').checked = !!s.sportsWidgetEnabled;
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
    s.sportsTeam = document.getElementById('sportsTeam').value;
    s.canvasBaseUrl = document.getElementById('canvasBaseUrl').value;
    const token = document.getElementById('canvasApiToken').value;
    if (token) {
        s.canvasApiToken = token;
        s.canvasSyncEnabled = true;
    }
    state.settings = await api('/api/settings', 'PUT', s);
    applyTheme();
    renderWidgets();
    toggleModal('settingsModal', false);
}

function toggleModal(id, show) {
    document.getElementById(id).classList.toggle('hidden', !show);
}

/* ---------------- Optional widgets (weather/sports placeholders) ---------------- */

function renderWidgets() {
    const panel = document.getElementById('widgetPanel');
    panel.innerHTML = '';
    const s = state.settings;

    if (s.weatherWidgetEnabled) {
        const w = document.createElement('div');
        w.className = 'widget';
        w.innerHTML = `<h3>🌤️ Weather</h3><p class="hint">${s.weatherLocation ? escapeHtml(s.weatherLocation) : 'Set a location in settings'}</p>
            <p class="hint">Connect a weather API key to show live conditions.</p>`;
        panel.appendChild(w);
    }
    if (s.sportsWidgetEnabled) {
        const w = document.createElement('div');
        w.className = 'widget';
        w.innerHTML = `<h3>🏈 Sports</h3><p class="hint">${s.sportsTeam ? escapeHtml(s.sportsTeam) : 'Pick a favorite team in settings'}</p>
            <p class="hint">Connect a sports scores API key to show live scores.</p>`;
        panel.appendChild(w);
    }
    if (!s.weatherWidgetEnabled && !s.sportsWidgetEnabled) {
        const w = document.createElement('div');
        w.className = 'widget';
        w.innerHTML = `<h3>✨ Customize</h3><p class="hint">Enable weather, sports, and more from the ⚙️ settings menu.</p>`;
        panel.appendChild(w);
    }
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
    const res = await fetch('/api/syllabus/preview', { method: 'POST', body: formData });
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
    const res = await fetch(url, {
        method,
        headers: body ? { 'Content-Type': 'application/json' } : undefined,
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
