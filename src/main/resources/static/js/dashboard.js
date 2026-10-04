/* Dashboard front-end: talks to the Spring Boot REST API under /api/* */

const THEME_KEYS = [
    'red', 'orange', 'yellow', 'green', 'teal', 'light-blue',
    'dark-blue', 'light-pink', 'hot-pink', 'lavender', 'purple', 'brown',
];

const state = {
    weekStart: startOfWeek(new Date()),
    settings: null,
    syllabusCandidates: [],
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
    document.getElementById('prevWeekBtn').addEventListener('click', () => shiftWeek(-7));
    document.getElementById('nextWeekBtn').addEventListener('click', () => shiftWeek(7));
    document.getElementById('todoForm').addEventListener('submit', onAddTodo);

    document.getElementById('settingsBtn').addEventListener('click', openSettingsModal);
    document.getElementById('closeSettingsBtn').addEventListener('click', () => toggleModal('settingsModal', false));
    document.getElementById('settingsForm').addEventListener('submit', onSaveSettings);
    document.getElementById('themeSwatches').addEventListener('click', onSwatchClick);

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

function todoItem(t) {
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
        const games = data.games || [];
        if (games.length === 0) {
            w.innerHTML = `<h3>🏈 Sports</h3><p class="hint">No games found right now.</p>`;
            return w;
        }
        const rows = games.map(g => `
            <li class="${g.favorite ? 'favorite' : ''}">
                <span class="matchup">${escapeHtml(g.awayTeam)} ${g.awayScore ?? ''} @ ${escapeHtml(g.homeTeam)} ${g.homeScore ?? ''}</span>
                <span class="meta">${escapeHtml(g.statusDetail || '')}</span>
            </li>`).join('');
        w.innerHTML = `<h3>🏈 Sports</h3><ul class="sports-list">${rows}</ul>`;
    } catch (err) {
        w.innerHTML = `<h3>🏈 Sports</h3><p class="hint">${escapeHtml(err.message)}</p>`;
    }
    return w;
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
