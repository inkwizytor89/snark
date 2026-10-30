function initApp() {
    const term = {
        write: () => {},
        clear: () => {},
    };

    let ws;
    let instances = [];
    let startTemplates = [];
    let selectedInstance = null;
    let activeMenuInstanceId = null;
    let pendingInstanceCreation = null;
    let activeDetailsTab = 'overview';
    const coordinatesState = new Map();

    const createSection = document.getElementById('createInstanceForm');
    const createToggleBtn = document.getElementById('createToggleBtn');
    const instancesList = document.getElementById('instancesList');
    const instanceDetails = document.getElementById('instanceDetails');
    const workspace = document.querySelector('.workspace');
    const startTemplateSelect = document.getElementById('startTemplateSelect');
    const placeholderModal = document.getElementById('placeholderModal');
    const placeholderForm = document.getElementById('placeholderForm');
    const placeholderFields = document.getElementById('placeholderFields');
    const placeholderModalSubtitle = document.getElementById('placeholderModalSubtitle');

    const configThreadModal = document.getElementById('configThreadModal');
    const configThreadForm = document.getElementById('configThreadForm');
    const configThreadExistingModule = document.getElementById('configThreadExistingModule');
    const configThreadNewModule = document.getElementById('configThreadNewModule');
    const configThreadSelect = document.getElementById('configThreadSelect');
    const configThreadDefinitionFields = document.getElementById('configThreadDefinitionFields');
    const configThreadModalSubtitle = document.getElementById('configThreadModalSubtitle');
    const configThreadModalMessage = document.getElementById('configThreadModalMessage');
    const saveConfigThreadBtn = document.getElementById('saveConfigThreadBtn');

    const configState = createEmptyConfigState();

    function createEmptyConfigState() {
        return {
            instanceId: null,
            status: 'idle',
            configs: [],
            error: '',
            bannerMessage: '',
            bannerType: '',
            dirtyValues: {},
            rowErrors: {},
            pendingActions: {},
            definitions: [],
            definitionsStatus: 'idle',
            definitionsError: '',
            modalThread: '',
            modalExistingModule: '',
            modalNewModule: '',
            modalValues: {},
            modalError: '',
            modalSaving: false
        };
    }

    function resetConfigState(instanceId) {
        const next = createEmptyConfigState();
        next.instanceId = instanceId;
        Object.assign(configState, next);
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function sortInstances(list) {
        return [...list].sort((a, b) => {
            const runningDiff = Number(!!b.running) - Number(!!a.running);
            if (runningDiff !== 0) {
                return runningDiff;
            }

            return String(a.id).localeCompare(String(b.id));
        });
    }

    function getSelectedInstance() {
        return instances.find((inst) => inst.id === selectedInstance) || null;
    }

    function formatNavigatorStatusText(navigatorStatus) {
        if (!navigatorStatus) {
            return '';
        }
        const fleetCount = navigatorStatus.fleetCount ?? 0;
        const fleetMax = navigatorStatus.fleetMax ?? 0;
        const expeditionCount = navigatorStatus.expeditionCount ?? 0;
        const expeditionMax = navigatorStatus.expeditionMax ?? 0;
        const baseText = `${fleetCount} / ${fleetMax} ${expeditionCount} / ${expeditionMax}`;
        const missions = navigatorStatus.missions && typeof navigatorStatus.missions === 'object'
            ? Object.entries(navigatorStatus.missions)
                .filter(([_, count]) => Number(count) > 0)
                .sort(([left], [right]) => left.localeCompare(right))
                .map(([mission, count]) => `${String(mission).charAt(0)}:${count}`)
            : [];
        return missions.length ? `${baseText} ${missions.join(' ')}` : baseText;
    }

    function getInstanceStatusText(inst) {
        if (inst.state === 'starting') {
            return 'Instance is starting and waiting for its first status update.';
        }
        if (inst.state === 'logged_out') {
            return inst.statusMessage || 'Waiting for the next retry.';
        }
        if (inst.state === 'disconnected') {
            return 'Worker disconnected; conductor is waiting for recovery.';
        }
        if (inst.state === 'restarting') {
            return 'Worker requested a restart; conductor is stopping and relaunching the instance.';
        }
        if (inst.state === 'stopped') {
            return 'Instance is stopped. Start it to receive status updates.';
        }

        const navigatorStatus = inst.serverStatus && inst.serverStatus.navigatorStatus;
        if (navigatorStatus) {
            const formatted = formatNavigatorStatusText(navigatorStatus);
            if (formatted) {
                return formatted;
            }
        }

        if (inst.statusMessage && String(inst.statusMessage).trim()) {
            return inst.statusMessage;
        }

        return 'Awaiting the next status update...';
    }

    function getInstanceStateLabel(inst) {
        switch (inst.state) {
            case 'starting':
                return 'Starting';
            case 'logged_out':
                return 'Logged out';
            case 'disconnected':
                return 'Disconnected';
            case 'restarting':
                return 'Restarting';
            case 'running':
                return 'Running';
            default:
                return 'Stopped';
        }
    }

    function getInstanceStateClass(inst) {
        switch (inst.state) {
            case 'starting':
                return 'status-starting';
            case 'logged_out':
                return 'status-disconnected';
            case 'disconnected':
                return 'status-disconnected';
            case 'restarting':
                return 'status-restarting';
            case 'running':
                return 'status-running';
            default:
                return 'status-stopped';
        }
    }

    function syncConfigStateSelection() {
        if (configState.instanceId !== selectedInstance) {
            resetConfigState(selectedInstance);
            closeConfigThreadModal();
        }
    }

    async function loadInstances() {
        try {
            const response = await fetch('/api/instances');
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const previousSelection = selectedInstance;
            instances = sortInstances(await response.json());

            if (!selectedInstance || !instances.some((inst) => inst.id === selectedInstance)) {
                const preferredInstance = instances.find((inst) => inst.running) || instances[0] || null;
                selectedInstance = preferredInstance ? preferredInstance.id : null;
            }

            if (previousSelection !== selectedInstance) {
                syncConfigStateSelection();
            }

            renderInstances();
            renderInstanceDetails();
            await refreshInstanceStatuses();
        } catch (error) {
            term.write('✗ Failed to load instances: ' + error.message + '\r\n');
            instancesList.innerHTML = '<div class="loading">Error loading instances</div>';
        }
    }

    async function refreshInstanceStatuses() {
        if (!instances.length) {
            renderInstanceDetails();
            return;
        }

        const updates = await Promise.all(instances.map(async (inst) => {
            try {
                const response = await fetch(`/api/instances/${encodeURIComponent(inst.id)}/status`);

                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }

                const status = await response.json();
                return {
                    id: inst.id,
                    running: status.running,
                    state: status.state,
                    pid: status.pid,
                    statusMessage: status.statusMessage,
                    serverStatus: status.serverStatus,
                    properties: status.properties,
                    autoStart: status.autoStart
                };
            } catch (error) {
                return {
                    id: inst.id,
                    statusMessage: 'Status unavailable right now.'
                };
            }
        }));

        const updatesById = new Map(updates.map((update) => [update.id, update]));
        instances = sortInstances(instances.map((inst) => ({
            ...inst,
            ...(updatesById.get(inst.id) || {})
        })));
        renderInstances();
        renderInstanceDetails();
    }

    async function loadStartTemplates() {
        try {
            const response = await fetch('/api/instances/templates');

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            startTemplates = await response.json();

            if (!startTemplates.length) {
                startTemplateSelect.innerHTML = '<option value="">No templates available</option>';
                startTemplateSelect.disabled = true;
                return;
            }

            startTemplateSelect.disabled = false;
            startTemplateSelect.innerHTML = '';
            startTemplates.forEach((template, index) => {
                const option = document.createElement('option');
                option.value = template.name;
                option.textContent = template.name;
                if (index === 0) {
                    option.selected = true;
                }
                startTemplateSelect.appendChild(option);
            });
        } catch (error) {
            term.write('✗ Failed to load templates: ' + error.message + '\r\n');
            startTemplateSelect.innerHTML = '<option value="">No templates available</option>';
            startTemplateSelect.disabled = true;
        }
    }

    function renderInstances() {
        if (instances.length === 0) {
            instancesList.innerHTML = '<div class="loading">No instances</div>';
            return;
        }

        if (activeMenuInstanceId && !instances.some((inst) => inst.id === activeMenuInstanceId)) {
            activeMenuInstanceId = null;
        }

        instancesList.innerHTML = instances.map((inst) => `
            <div class="instance-item ${selectedInstance === inst.id ? 'selected' : ''}" data-id="${escapeHtml(inst.id)}">
                <div class="instance-top">
                    <div class="instance-header">
                        <div class="instance-title-row">
                            <div class="instance-name">${escapeHtml(inst.id)}</div>
                            <span class="status-badge ${getInstanceStateClass(inst)}">
                                ${getInstanceStateLabel(inst)}
                            </span>
                        </div>
                    </div>
                    <div class="instance-menu-wrapper">
                        <button
                            type="button"
                            class="instance-menu-btn"
                            data-menu-button="true"
                            data-id="${escapeHtml(inst.id)}"
                            aria-haspopup="true"
                            aria-expanded="${activeMenuInstanceId === inst.id ? 'true' : 'false'}"
                            title="Instance actions"
                        >⋮</button>
                        <div class="instance-menu ${activeMenuInstanceId === inst.id ? 'open' : ''}" data-menu-for="${escapeHtml(inst.id)}">
                            <button type="button" class="instance-menu-action" data-action="toggle-autostart" data-id="${escapeHtml(inst.id)}">
                                ${inst.autoStart ? 'Disable auto-start' : 'Enable auto-start'}
                            </button>
                            <button type="button" class="instance-menu-action danger" data-action="delete" data-id="${escapeHtml(inst.id)}">
                                Delete instance
                            </button>
                        </div>
                    </div>
                </div>
                <div class="instance-status-line">
                    <span class="instance-status-message">${escapeHtml(getInstanceStatusText(inst))}</span>
                </div>
                <div class="instance-actions">
                    ${inst.running
                        ? `<button class="btn-action btn-stop" onclick="stopInstance('${escapeHtml(inst.id)}')">Stop</button>
                           <button class="btn-action btn-restart" onclick="restartInstance('${escapeHtml(inst.id)}')">Restart</button>`
                        : `<button class="btn-action btn-start" onclick="startInstance('${escapeHtml(inst.id)}')">Start</button>`
                    }
                </div>
            </div>
        `).join('');
    }

    function renderInstanceDetails() {
        const inst = getSelectedInstance();

        if (!inst) {
            instanceDetails.innerHTML = '<div class="loading">Select an instance to see details</div>';
            return;
        }

        instanceDetails.innerHTML = `
            <div class="detail-card">
                <div class="detail-header">
                    <div class="detail-title">${escapeHtml(inst.id)}</div>
                    <span class="status-badge ${getInstanceStateClass(inst)}">${getInstanceStateLabel(inst)}</span>
                </div>
                <div class="detail-tabs">
                    <button type="button" class="detail-tab ${activeDetailsTab === 'overview' ? 'is-active' : ''}" data-detail-tab="overview">Overview</button>
                    <button type="button" class="detail-tab ${activeDetailsTab === 'coordinates' ? 'is-active' : ''}" data-detail-tab="coordinates">Coordinates</button>
                    <button type="button" class="detail-tab ${activeDetailsTab === 'configs' ? 'is-active' : ''}" data-detail-tab="configs">Configs</button>
                </div>
                <div class="detail-tab-panel">
                    ${activeDetailsTab === 'configs'
                        ? renderConfigsTab(inst)
                        : activeDetailsTab === 'coordinates'
                            ? renderCoordinatesTab(inst)
                            : renderOverviewTab(inst)}
                </div>
            </div>
        `;
    }

    function renderCoordinatesTab(inst) {
        const state = coordinatesState.get(inst.id) || { status: 'idle', colonies: [] };
        const feedback = state.status === 'loading'
            ? '<div class="loading">Loading planets and moons...</div>'
            : state.status === 'error'
                ? `<div class="config-feedback is-error">${escapeHtml(state.error)}</div>`
                : state.status === 'loaded' && state.colonies.length === 0
                    ? '<div class="loading">No planets or moons found</div>'
                    : state.status === 'idle'
                        ? '<div class="loading">Load coordinate data from the instance to view its planets and moons.</div>'
                        : '';
        const coordinates = state.details;
        const colonies = state.colonies || [];
        const colonyItems = colonies.length
            ? colonies.map((colony) => {
                const ships = colony.ships && Object.keys(colony.ships).length
                    ? Object.entries(colony.ships)
                        .map(([ship, count]) => `${ship}: ${formatCoordinateValue(count)}`)
                        .join('\n')
                    : '-';
                return `
                    <article class="coordinate-colony">
                        <h3 class="coordinate-colony-title">${escapeHtml(colony.coordinate || '-')}</h3>
                        <div class="coordinate-colony-details">
                            <div>
                                <span class="detail-label">Resources</span>
                                <span class="detail-value">Metal: ${escapeHtml(formatCoordinateValue(colony.metal))}</span>
                                <span class="detail-value">Crystal: ${escapeHtml(formatCoordinateValue(colony.crystal))}</span>
                                <span class="detail-value">Deuterium: ${escapeHtml(formatCoordinateValue(colony.deuterium))}</span>
                                <span class="detail-value">Energy: ${escapeHtml(formatCoordinateValue(colony.energy))}</span>
                            </div>
                            <div>
                                <span class="detail-label">Ships</span>
                                <span class="detail-value">${escapeHtml(ships)}</span>
                            </div>
                        </div>
                    </article>
                `;
            }).join('')
            : '';

        return `
            <div class="detail-tab-toolbar">
                <span class="detail-tab-caption">Coordinate data is fetched on demand from ${escapeHtml(inst.id)}</span>
                <div class="detail-tab-actions">
                    <button type="button" class="btn-primary btn-small" data-coordinates-action="load"
                        ${!inst.running || state.status === 'loading' ? 'disabled' : ''}>
                        ${state.status === 'loading' ? 'Loading...' : state.status === 'loaded' ? 'Refresh coordinates' : 'Load coordinates'}
                    </button>
                </div>
            </div>
            ${feedback}
            ${coordinates ? `
                <div class="coordinates-summary">
                    <div class="detail-item">
                        <span class="detail-label">Planets</span>
                        <span class="detail-value">${escapeHtml(formatCoordinateValue(coordinates.planetCount))}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Moons</span>
                        <span class="detail-value">${escapeHtml(formatCoordinateValue(coordinates.moonCount))}</span>
                    </div>
                </div>
                <div class="coordinates-colonies">${colonyItems}</div>
            ` : ''}
        `;
    }

    async function loadCoordinates(instanceId) {
        const currentState = coordinatesState.get(instanceId);
        if (currentState && currentState.status === 'loading') {
            return;
        }
        coordinatesState.set(instanceId, { status: 'loading', colonies: [] });
        if (selectedInstance === instanceId && activeDetailsTab === 'coordinates') {
            renderInstanceDetails();
        }

        try {
            const pageSize = 25;
            const firstPage = await apiRequestJson(
                `/api/instances/${encodeURIComponent(instanceId)}/status/colonies?offset=0&limit=${pageSize}`
            );
            const colonies = [...(Array.isArray(firstPage.colonies) ? firstPage.colonies : [])];
            for (let offset = pageSize; offset < firstPage.total; offset += pageSize) {
                const page = await apiRequestJson(
                    `/api/instances/${encodeURIComponent(instanceId)}/status/colonies?offset=${offset}&limit=${pageSize}`
                );
                colonies.push(...(Array.isArray(page.colonies) ? page.colonies : []));
            }
            coordinatesState.set(instanceId, {
                status: 'loaded',
                details: { ...firstPage, colonies },
                colonies
            });
        } catch (error) {
            coordinatesState.set(instanceId, { status: 'error', error: error.message, colonies: [] });
        }

        if (selectedInstance === instanceId && activeDetailsTab === 'coordinates') {
            renderInstanceDetails();
        }
    }

    function formatCoordinateValue(value) {
        return value === null || value === undefined ? '-' : String(value);
    }

    function renderOverviewTab(inst) {
        const pidText = inst.pid && inst.pid !== -1 ? inst.pid : '-';
        const statusText = getInstanceStatusText(inst);
        const serverStatus = inst.serverStatus;
        const navigatorLastUpdate = serverStatus && serverStatus.navigatorStatus
            ? formatStatusTime(serverStatus.navigatorStatus.lastUpdate)
            : '';
        const activeThreads = serverStatus && serverStatus.activeThreads.length
            ? serverStatus.activeThreads.join('\n')
            : '-';
        const notifications = serverStatus && serverStatus.notifications
            ? serverStatus.notifications
            : '-';
        const propertiesText = inst.properties ? inst.properties : 'Properties file not found';
        const propertiesClass = inst.properties ? '' : 'detail-value-error';

        return `
            <div class="detail-grid">
                <div class="detail-item">
                    <span class="detail-label">PID</span>
                    <span class="detail-value">${escapeHtml(pidText)}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Auto-start</span>
                    <span class="detail-value">${inst.autoStart ? 'Enabled' : 'Disabled'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Database</span>
                    <span class="detail-value">${inst.databaseName ? escapeHtml(inst.databaseName) : '-'}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Properties</span>
                    <span class="detail-value ${propertiesClass}">${escapeHtml(propertiesText)}</span>
                </div>
                <div class="detail-item detail-item-wide">
                    <span class="detail-label">Latest status${navigatorLastUpdate ? ` (${navigatorLastUpdate})` : ''}</span>
                    <span class="detail-value">${escapeHtml(statusText)}</span>
                </div>
                <div class="detail-item detail-item-wide">
                    <span class="detail-label">Active threads</span>
                    <span class="detail-value">${escapeHtml(activeThreads)}</span>
                </div>
                <div class="detail-item detail-item-wide">
                    <span class="detail-label">Command deque</span>
                    <span class="detail-value">${escapeHtml(serverStatus ? serverStatus.commandDeque : '-')}</span>
                </div>
                <div class="detail-item detail-item-wide">
                    <span class="detail-label">Notifications</span>
                    <span class="detail-value">${escapeHtml(notifications)}</span>
                </div>
            </div>
        `;
    }

    function formatStatusTime(timestamp) {
        if (!timestamp) {
            return '';
        }
        const time = timestamp.split('T')[1];
        if (!time || !/^\d{2}:\d{2}:\d{2}/.test(time)) {
            return '';
        }
        return time.substring(0, 8);
    }

    function renderConfigsTab(inst) {
        const feedback = configState.bannerMessage
            ? `<div class="config-feedback ${configState.bannerType === 'error' ? 'is-error' : 'is-success'}">${escapeHtml(configState.bannerMessage)}</div>`
            : '';

        let body = '';
        if (configState.status === 'loading') {
            body = '<div class="loading">Loading configs...</div>';
        } else if (configState.status === 'error') {
            body = `<div class="config-feedback is-error">${escapeHtml(configState.error)}</div>`;
        } else if (configState.status === 'loaded' && configState.configs.length === 0) {
            body = '<div class="loading">No configs configured</div>';
        } else if (configState.status === 'loaded') {
            body = renderConfigGroups(configState.configs);
        } else {
            body = '<div class="loading">Open this tab to load configs</div>';
        }

        return `
            <div class="detail-tab-toolbar">
                <span class="detail-tab-caption">Runtime configuration for ${escapeHtml(inst.id)}</span>
                <div class="detail-tab-actions">
                    <button type="button" class="btn-secondary btn-small" data-config-action="refresh">Refresh</button>
                    <button type="button" class="btn-primary btn-small" data-config-action="add-thread">Add thread</button>
                </div>
            </div>
            ${feedback}
            ${body}
        `;
    }

    function renderConfigGroups(configs) {
        const modules = new Map();

        configs.forEach((config) => {
            const moduleName = normalizeModuleDisplayName(config.module);
            if (!modules.has(moduleName)) {
                modules.set(moduleName, new Map());
            }

            const threads = modules.get(moduleName);
            const threadName = config.thread || '(no thread)';
            if (!threads.has(threadName)) {
                threads.set(threadName, []);
            }

            threads.get(threadName).push(config);
        });

        const moduleMarkup = [...modules.entries()]
            .sort((a, b) => a[0].localeCompare(b[0]))
            .map(([moduleName, threads]) => `
                <section class="config-module-card">
                    <div class="config-module-header">
                        <div class="config-module-title">${escapeHtml(moduleName)}</div>
                    </div>
                    <div class="config-thread-list">
                        ${[...threads.entries()]
                            .sort((a, b) => a[0].localeCompare(b[0]))
                            .map(([threadName, items]) => renderConfigThread(threadName, items))
                            .join('')}
                    </div>
                </section>
            `)
            .join('');

        return `<div class="config-groups">${moduleMarkup}</div>`;
    }

    function renderConfigThread(threadName, configs) {
        return `
            <section class="config-thread-card">
                <div class="config-thread-header">
                    <div class="config-thread-title">${escapeHtml(threadName)}</div>
                </div>
                <div class="config-thread-body">
                    ${configs
                        .slice()
                        .sort((a, b) => String(a.key).localeCompare(String(b.key)))
                        .map((config) => renderConfigRow(config))
                        .join('')}
                </div>
            </section>
        `;
    }

    function renderConfigRow(config) {
        const rowKey = getConfigRowKey(config);
        const pendingAction = configState.pendingActions[rowKey] || '';
        const dirtyValue = Object.prototype.hasOwnProperty.call(configState.dirtyValues, rowKey)
            ? configState.dirtyValues[rowKey]
            : config.value ?? '';
        const isChanged = String(dirtyValue) !== String(config.value ?? '');
        const isBusy = pendingAction !== '';
        const descriptionParts = [];

        if (config.description) {
            descriptionParts.push(config.description);
        }
        if (config.temporary) {
            descriptionParts.push('Temporary');
        }

        return `
            <div class="config-row-shell" data-row-key="${escapeHtml(rowKey)}">
                <div class="config-row">
                    <div class="config-key">
                        <span class="config-key-name">${escapeHtml(config.key)}</span>
                        ${descriptionParts.length ? `<span class="config-description-text">${escapeHtml(descriptionParts.join(' · '))}</span>` : ''}
                    </div>
                    <div>
                        <input
                            type="text"
                            class="config-value-input"
                            data-config-input="${escapeHtml(rowKey)}"
                            value="${escapeHtml(dirtyValue)}"
                            ${isBusy ? 'disabled' : ''}
                        />
                    </div>
                    <div class="config-row-actions">
                        <button
                            type="button"
                            class="btn-primary btn-small"
                            data-config-action="save-row"
                            data-row-key="${escapeHtml(rowKey)}"
                            ${!isChanged || isBusy ? 'disabled' : ''}
                        >
                            ${pendingAction === 'saving' ? 'Saving...' : 'Save'}
                        </button>
                    </div>
                    <div class="config-row-actions">
                        <button
                            type="button"
                            class="btn-danger btn-small"
                            data-config-action="delete-row"
                            data-row-key="${escapeHtml(rowKey)}"
                            ${isBusy ? 'disabled' : ''}
                        >
                            ${pendingAction === 'deleting' ? 'Deleting...' : 'Delete'}
                        </button>
                    </div>
                </div>
                ${configState.rowErrors[rowKey] ? `<div class="config-row-error">${escapeHtml(configState.rowErrors[rowKey])}</div>` : ''}
            </div>
        `;
    }

    function normalizeModuleDisplayName(moduleName) {
        const trimmed = String(moduleName ?? '').trim();
        return trimmed ? trimmed : 'General';
    }

    function getConfigRowKey(config) {
        return `${config.module ?? ''}::${config.thread ?? ''}::${config.key ?? ''}`;
    }

    function findConfigByRowKey(rowKey) {
        return configState.configs.find((config) => getConfigRowKey(config) === rowKey) || null;
    }

    function setConfigBanner(message, type) {
        configState.bannerMessage = message;
        configState.bannerType = type;
    }

    async function apiRequestJson(url, options = {}) {
        const response = await fetch(url, options);
        const rawText = await response.text();
        let payload = null;

        if (rawText) {
            try {
                payload = JSON.parse(rawText);
            } catch (error) {
                payload = rawText;
            }
        }

        if (!response.ok) {
            const message = payload && typeof payload === 'object'
                ? payload.error || payload.message || `HTTP ${response.status}: ${response.statusText}`
                : rawText || `HTTP ${response.status}: ${response.statusText}`;
            throw new Error(message);
        }

        return payload;
    }

    async function loadConfigs(forceReload = false) {
        const inst = getSelectedInstance();
        if (!inst) {
            return;
        }

        if (!forceReload
                && configState.instanceId === inst.id
                && (configState.status === 'loading' || configState.status === 'loaded')) {
            return;
        }

        resetConfigState(inst.id);
        configState.status = 'loading';
        renderInstanceDetails();

        try {
            const payload = await apiRequestJson(`/api/instances/${encodeURIComponent(inst.id)}/configs`);
            configState.status = 'loaded';
            configState.configs = Array.isArray(payload.configs) ? payload.configs : [];
            setConfigBanner('', '');
        } catch (error) {
            configState.status = 'error';
            configState.error = error.message;
        }

        renderInstanceDetails();
    }

    async function saveConfigRow(rowKey) {
        const inst = getSelectedInstance();
        const config = findConfigByRowKey(rowKey);
        if (!inst || !config) {
            return;
        }

        const updatedValue = Object.prototype.hasOwnProperty.call(configState.dirtyValues, rowKey)
            ? configState.dirtyValues[rowKey]
            : config.value ?? '';

        if (String(updatedValue) === String(config.value ?? '')) {
            return;
        }

        configState.pendingActions[rowKey] = 'saving';
        delete configState.rowErrors[rowKey];
        renderInstanceDetails();

        try {
            const payload = await apiRequestJson(`/api/instances/${encodeURIComponent(inst.id)}/configs`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    configs: [{
                        ...config,
                        value: updatedValue
                    }]
                })
            });

            configState.status = 'loaded';
            configState.configs = Array.isArray(payload.configs) ? payload.configs : [];
            configState.dirtyValues = {};
            configState.rowErrors = {};
            configState.pendingActions = {};
            setConfigBanner('Config saved', 'success');
        } catch (error) {
            delete configState.pendingActions[rowKey];
            configState.rowErrors[rowKey] = error.message;
            setConfigBanner(error.message, 'error');
        }

        renderInstanceDetails();
    }

    async function deleteConfigRow(rowKey) {
        const inst = getSelectedInstance();
        const config = findConfigByRowKey(rowKey);
        if (!inst || !config) {
            return;
        }

        if (!window.confirm(`Delete config "${config.key}" from thread "${config.thread}"?`)) {
            return;
        }

        configState.pendingActions[rowKey] = 'deleting';
        delete configState.rowErrors[rowKey];
        renderInstanceDetails();

        try {
            const payload = await apiRequestJson(`/api/instances/${encodeURIComponent(inst.id)}/configs`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    configs: [config]
                })
            });

            configState.status = 'loaded';
            configState.configs = Array.isArray(payload.configs) ? payload.configs : [];
            configState.dirtyValues = {};
            configState.rowErrors = {};
            configState.pendingActions = {};
            setConfigBanner('Config deleted', 'success');
        } catch (error) {
            delete configState.pendingActions[rowKey];
            configState.rowErrors[rowKey] = error.message;
            setConfigBanner(error.message, 'error');
        }

        renderInstanceDetails();
    }

    async function openAddThreadModal() {
        const inst = getSelectedInstance();
        if (!inst) {
            return;
        }

        configState.definitionsStatus = 'loading';
        configState.definitionsError = '';
        configState.modalThread = '';
        configState.modalExistingModule = '';
        configState.modalNewModule = '';
        configState.modalValues = {};
        configState.modalError = '';
        configState.modalSaving = false;
        configThreadModalSubtitle.textContent = `Instance: ${inst.id}`;
        configThreadModal.hidden = false;
        renderConfigThreadModal();

        try {
            const payload = await apiRequestJson(`/api/instances/${encodeURIComponent(inst.id)}/configs/definitions`);
            configState.definitions = Array.isArray(payload.definitions) ? payload.definitions : [];
            configState.definitionsStatus = 'loaded';
            configState.modalThread = getAvailableThreads()[0] || '';
        } catch (error) {
            configState.definitionsStatus = 'error';
            configState.definitionsError = error.message;
        }

        renderConfigThreadModal();
    }

    function closeConfigThreadModal() {
        configThreadModal.hidden = true;
        configThreadExistingModule.innerHTML = '';
        configThreadNewModule.value = '';
        configThreadSelect.innerHTML = '';
        configThreadDefinitionFields.innerHTML = '';
        configThreadModalMessage.hidden = true;
        configThreadModalMessage.textContent = '';
        configState.definitionsStatus = 'idle';
        configState.definitionsError = '';
        configState.modalThread = '';
        configState.modalExistingModule = '';
        configState.modalNewModule = '';
        configState.modalValues = {};
        configState.modalError = '';
        configState.modalSaving = false;
    }

    function getAvailableModules() {
        return [...new Map(
            configState.configs.map((config) => [String(config.module ?? ''), normalizeModuleDisplayName(config.module)])
        ).entries()]
            .sort((a, b) => a[1].localeCompare(b[1]));
    }

    function getAvailableThreads() {
        return [...new Set(configState.definitions
            .map((definition) => String(definition.thread ?? '').trim())
            .filter(Boolean))]
            .sort((a, b) => a.localeCompare(b));
    }

    function getSelectedThreadDefinitions() {
        return configState.definitions
            .filter((definition) => String(definition.thread ?? '') === configState.modalThread)
            .slice()
            .sort((a, b) => String(a.key).localeCompare(String(b.key)));
    }

    function renderConfigThreadModal() {
        configState.modalExistingModule = configThreadExistingModule.value;
        configState.modalNewModule = configThreadNewModule.value;
        configThreadDefinitionFields.querySelectorAll('[data-thread-config-key]').forEach((input) => {
            configState.modalValues[input.dataset.threadConfigKey] = input.value;
        });

        const modules = getAvailableModules();
        configThreadExistingModule.innerHTML = [
            '<option value="">Select existing module</option>',
            ...modules.map(([value, label]) => `<option value="${escapeHtml(value)}">${escapeHtml(label)}</option>`)
        ].join('');
        configThreadExistingModule.value = configState.modalExistingModule;
        configThreadNewModule.value = configState.modalNewModule;

        if (configState.definitionsStatus === 'loading') {
            configThreadSelect.innerHTML = '<option value="">Loading threads...</option>';
            configThreadSelect.disabled = true;
            configThreadDefinitionFields.innerHTML = '<div class="loading">Loading config definitions...</div>';
            saveConfigThreadBtn.disabled = true;
            renderConfigThreadModalMessage('', '');
            return;
        }

        if (configState.definitionsStatus === 'error') {
            configThreadSelect.innerHTML = '<option value="">No threads available</option>';
            configThreadSelect.disabled = true;
            configThreadDefinitionFields.innerHTML = '';
            saveConfigThreadBtn.disabled = true;
            renderConfigThreadModalMessage(configState.definitionsError, 'error');
            return;
        }

        const threads = getAvailableThreads();
        configThreadSelect.innerHTML = threads.length
            ? threads.map((thread) => `<option value="${escapeHtml(thread)}" ${thread === configState.modalThread ? 'selected' : ''}>${escapeHtml(thread)}</option>`).join('')
            : '<option value="">No threads available</option>';
        configThreadSelect.disabled = !threads.length || configState.modalSaving;

        const definitions = getSelectedThreadDefinitions();
        if (!definitions.length) {
            configThreadDefinitionFields.innerHTML = '<div class="loading">Select a thread to load fields</div>';
            saveConfigThreadBtn.disabled = true;
        } else {
            configThreadDefinitionFields.innerHTML = definitions.map((definition) => `
                <div class="config-thread-field-card">
                    <div class="config-key">
                        <span class="config-key-name">${escapeHtml(definition.key)}</span>
                        ${definition.description ? `<span class="config-description-text">${escapeHtml(definition.description)}</span>` : ''}
                    </div>
                    <div>
                        <input
                            type="text"
                            class="config-thread-field-input"
                            data-thread-config-key="${escapeHtml(definition.key)}"
                            data-thread-config-description="${escapeHtml(definition.description || '')}"
                            value="${escapeHtml(configState.modalValues[definition.key] || '')}"
                            ${configState.modalSaving ? 'disabled' : ''}
                        />
                    </div>
                </div>
            `).join('');
            saveConfigThreadBtn.disabled = configState.modalSaving;
        }

        if (configState.modalSaving) {
            saveConfigThreadBtn.textContent = 'Saving...';
            saveConfigThreadBtn.disabled = true;
        } else {
            saveConfigThreadBtn.textContent = 'Save thread';
        }

        renderConfigThreadModalMessage(configState.modalError, configState.modalError ? 'error' : '');
    }

    function renderConfigThreadModalMessage(message, type) {
        if (!message) {
            configThreadModalMessage.hidden = true;
            configThreadModalMessage.textContent = '';
            configThreadModalMessage.className = 'config-feedback';
            return;
        }

        configThreadModalMessage.hidden = false;
        configThreadModalMessage.textContent = message;
        configThreadModalMessage.className = `config-feedback ${type === 'error' ? 'is-error' : 'is-success'}`;
    }

    async function saveThreadConfigs(event) {
        event.preventDefault();

        const inst = getSelectedInstance();
        if (!inst) {
            return;
        }

        const newModule = configThreadNewModule.value.trim();
        const existingModule = configThreadExistingModule.value;
        const moduleName = newModule || existingModule;

        if (!moduleName.trim()) {
            configState.modalError = 'Module is required';
            renderConfigThreadModal();
            return;
        }

        const definitions = getSelectedThreadDefinitions();
        if (!definitions.length) {
            configState.modalError = 'Thread is required';
            renderConfigThreadModal();
            return;
        }

        const configsToSave = definitions.map((definition) => {
            const input = configThreadDefinitionFields.querySelector(`[data-thread-config-key="${CSS.escape(definition.key)}"]`);
            const value = input ? input.value : '';
            return {
                module: moduleName.trim(),
                thread: definition.thread,
                key: definition.key,
                value: value,
                temporary: false,
                description: definition.description || ''
            };
        }).filter((config) => String(config.value).trim() !== '');

        if (!configsToSave.length) {
            configState.modalError = 'No values to save';
            renderConfigThreadModal();
            return;
        }

        configState.modalSaving = true;
        configState.modalError = '';
        renderConfigThreadModal();

        try {
            const payload = await apiRequestJson(`/api/instances/${encodeURIComponent(inst.id)}/configs`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ configs: configsToSave })
            });

            configState.status = 'loaded';
            configState.configs = Array.isArray(payload.configs) ? payload.configs : [];
            configState.dirtyValues = {};
            configState.rowErrors = {};
            configState.pendingActions = {};
            setConfigBanner('Thread configs saved', 'success');
            closeConfigThreadModal();
            renderInstanceDetails();
        } catch (error) {
            configState.modalSaving = false;
            configState.modalError = error.message;
            renderConfigThreadModal();
            setConfigBanner(error.message, 'error');
        }
    }

    function openPlaceholderModal(instanceId, placeholders, templateName, defaultValues = {}, creationData = {}) {
        pendingInstanceCreation = {
            instanceId,
            placeholders,
            creationData
        };

        placeholderModalSubtitle.textContent = `Template: ${templateName}`;
        placeholderFields.innerHTML = '';
        placeholders.forEach((placeholder) => {
            const label = document.createElement('label');
            label.className = 'placeholder-field';
            label.htmlFor = `placeholder-${placeholder}`;

            const text = document.createElement('span');
            text.textContent = placeholder;

            const input = document.createElement('input');
            input.type = 'text';
            input.id = `placeholder-${placeholder}`;
            input.dataset.placeholderName = placeholder;
            if (Object.prototype.hasOwnProperty.call(defaultValues, placeholder)) {
                input.value = defaultValues[placeholder];
            }

            label.appendChild(text);
            label.appendChild(input);
            placeholderFields.appendChild(label);
        });

        placeholderModal.hidden = false;
        const firstInput = placeholderFields.querySelector('input');
        if (firstInput) {
            firstInput.focus();
        }
    }

    function closePlaceholderModal() {
        pendingInstanceCreation = null;
        placeholderFields.innerHTML = '';
        placeholderModalSubtitle.textContent = '';
        placeholderModal.hidden = true;
    }

    function closeInstanceMenu() {
        if (activeMenuInstanceId !== null) {
            activeMenuInstanceId = null;
            renderInstances();
        }
    }

    function toggleInstanceMenu(id) {
        activeMenuInstanceId = activeMenuInstanceId === id ? null : id;
        renderInstances();
    }

    async function toggleAutoStart(id) {
        try {
            const response = await fetch(`/api/instances/${encodeURIComponent(id)}/autostart/toggle`, { method: 'POST' });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            term.write(`✓ ${data.message}\r\n`);
            activeMenuInstanceId = null;
            await loadInstances();
        } catch (error) {
            term.write(`✗ Error: ${error.message}\r\n`);
        }
    }

    async function deleteInstance(id) {
        const confirmed = window.confirm('Are you sure you want to delete this instance?');
        if (!confirmed) {
            return;
        }

        try {
            const response = await fetch(`/api/instances/${encodeURIComponent(id)}`, { method: 'DELETE' });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            term.write(`✓ ${data.message}\r\n`);
            if (selectedInstance === id) {
                selectedInstance = null;
            }
            activeMenuInstanceId = null;
            await loadInstances();
        } catch (error) {
            term.write(`✗ Error: ${error.message}\r\n`);
        }
    }

    async function loadInstanceStatus(id) {
        try {
            const response = await fetch(`/api/instances/${encodeURIComponent(id)}/status`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const status = await response.json();
            instances = sortInstances(instances.map((inst) => inst.id === id ? {
                ...inst,
                running: status.running,
                state: status.state,
                pid: status.pid,
                statusMessage: status.statusMessage,
                serverStatus: status.serverStatus,
                autoStart: status.autoStart,
                properties: status.properties
            } : inst));
            renderInstances();
            renderInstanceDetails();
        } catch (error) {
            term.write(`\r\n✗ Failed to load status: ${error.message}\r\n`);
        }
    }

    window.startInstance = async function(id) {
        try {
            const response = await fetch(`/api/instances/${encodeURIComponent(id)}/start`, { method: 'POST' });
            const data = await response.json();
            term.write(`✓ ${data.message}\r\n`);
            await loadInstances();
        } catch (error) {
            term.write(`✗ Error: ${error.message}\r\n`);
        }
    };

    window.stopInstance = async function(id) {
        try {
            const response = await fetch(`/api/instances/${encodeURIComponent(id)}/stop`, { method: 'POST' });
            const data = await response.json();
            term.write(`✓ ${data.message}\r\n`);
            await loadInstances();
        } catch (error) {
            term.write(`✗ Error: ${error.message}\r\n`);
        }
    };

    window.restartInstance = async function(id) {
        try {
            const response = await fetch(`/api/instances/${encodeURIComponent(id)}/restart`, { method: 'POST' });
            const data = await response.json();
            term.write(`✓ ${data.message}\r\n`);
            await loadInstances();
        } catch (error) {
            term.write(`✗ Error: ${error.message}\r\n`);
        }
    };

    function toggleCreateForm() {
        const isHidden = createSection.hasAttribute('hidden');
        if (isHidden) {
            createSection.removeAttribute('hidden');
            createToggleBtn.setAttribute('aria-expanded', 'true');
            createToggleBtn.classList.add('is-active');
            createToggleBtn.title = 'Hide create form';
            document.getElementById('newInstanceServer').focus();
            return;
        }

        createSection.setAttribute('hidden', '');
        createToggleBtn.setAttribute('aria-expanded', 'false');
        createToggleBtn.classList.remove('is-active');
        createToggleBtn.title = 'Create new instance';
    }

    function updateDatabaseNameField() {
        const checkbox = document.getElementById('newInstanceUseDatabaseCheckbox');
        const wrapper = document.getElementById('newInstanceDatabaseNameWrapper');
        const input = document.getElementById('newInstanceDatabaseName');
        const server = document.getElementById('newInstanceServer').value.trim();
        const login = document.getElementById('newInstanceLogin').value.trim();

        if (checkbox.checked) {
            wrapper.style.display = 'block';
            if (!input.value && server && login) {
                input.value = `${server}#${login}`;
            }
            input.focus();
        } else {
            wrapper.style.display = 'none';
            input.value = '';
        }
    }

    async function createInstance(event) {
        if (event) {
            event.preventDefault();
        }

        const serverInput = document.getElementById('newInstanceServer');
        const loginInput = document.getElementById('newInstanceLogin');
        const databaseCheckbox = document.getElementById('newInstanceUseDatabaseCheckbox');
        const databaseNameInput = document.getElementById('newInstanceDatabaseName');
        const templateName = startTemplateSelect.value;

        const server = serverInput.value.trim();
        const login = loginInput.value.trim();

        if (!server) {
            term.write('✗ Server cannot be empty\r\n');
            return;
        }

        if (!login) {
            term.write('✗ Login cannot be empty\r\n');
            return;
        }

        if (!templateName) {
            term.write('✗ Please select a start template\r\n');
            return;
        }

        const databaseName = databaseCheckbox.checked ? databaseNameInput.value.trim() : null;

        try {
            const response = await fetch('/api/instances', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    server: server,
                    login: login,
                    templateName: templateName,
                    databaseName: databaseName
                })
            });

            if (response.status === 409) {
                term.write(`✗ Instance ${server}#${login} already exists\r\n`);
                return;
            }

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            term.write(`✓ ${data.message}\r\n`);

            if (data.placeholders && data.placeholders.length) {
                openPlaceholderModal(data.id, data.placeholders, templateName, {
                    login: login,
                    server_name: server
                }, {
                    server: server,
                    login: login,
                    templateName: templateName,
                    databaseName: databaseName
                });
            } else {
                serverInput.value = '';
                loginInput.value = '';
                databaseCheckbox.checked = false;
                updateDatabaseNameField();
                await loadInstances();
            }
        } catch (error) {
            term.write(`✗ Error: ${error.message}\r\n`);
        }
    }

    async function submitPlaceholderValues(event) {
        event.preventDefault();

        if (!pendingInstanceCreation) {
            closePlaceholderModal();
            return;
        }

        const values = {};
        placeholderFields.querySelectorAll('input[data-placeholder-name]').forEach((input) => {
            values[input.dataset.placeholderName] = input.value;
        });

        try {
            const response = await fetch(`/api/instances/${encodeURIComponent(pendingInstanceCreation.instanceId)}/placeholders`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    ...pendingInstanceCreation.creationData,
                    placeholders: values
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            term.write(`✓ ${data.message}\r\n`);
            document.getElementById('newInstanceServer').value = '';
            document.getElementById('newInstanceLogin').value = '';
            document.getElementById('newInstanceUseDatabaseCheckbox').checked = false;
            document.getElementById('newInstanceDatabaseName').value = '';
            closePlaceholderModal();
            await loadInstances();
        } catch (error) {
            term.write(`✗ Error: ${error.message}\r\n`);
        }
    }

    document.getElementById('createInstanceForm').addEventListener('submit', createInstance);
    document.getElementById('createToggleBtn').addEventListener('click', toggleCreateForm);
    document.getElementById('placeholderForm').addEventListener('submit', submitPlaceholderValues);
    document.getElementById('cancelPlaceholderBtn').addEventListener('click', closePlaceholderModal);
    document.getElementById('refreshBtn').addEventListener('click', loadInstances);
    configThreadForm.addEventListener('submit', saveThreadConfigs);
    document.getElementById('cancelConfigThreadBtn').addEventListener('click', closeConfigThreadModal);

    const dbCheckbox = document.getElementById('newInstanceUseDatabaseCheckbox');
    if (dbCheckbox) {
        dbCheckbox.addEventListener('change', updateDatabaseNameField);
    }

    const serverInput = document.getElementById('newInstanceServer');
    if (serverInput) {
        serverInput.addEventListener('input', updateDatabaseNameField);
    }

    const loginInput = document.getElementById('newInstanceLogin');
    if (loginInput) {
        loginInput.addEventListener('input', updateDatabaseNameField);
    }

    configThreadSelect.addEventListener('change', () => {
        configState.modalThread = configThreadSelect.value;
        configState.modalError = '';
        renderConfigThreadModal();
    });

    configThreadExistingModule.addEventListener('change', () => {
        configState.modalExistingModule = configThreadExistingModule.value;
    });

    configThreadNewModule.addEventListener('input', () => {
        configState.modalNewModule = configThreadNewModule.value;
    });

    configThreadDefinitionFields.addEventListener('input', (e) => {
        const input = e.target.closest('[data-thread-config-key]');
        if (!input) {
            return;
        }

        configState.modalValues[input.dataset.threadConfigKey] = input.value;
    });

    instancesList.addEventListener('click', (e) => {
        const menuButton = e.target.closest('.instance-menu-btn');
        if (menuButton) {
            e.stopPropagation();
            toggleInstanceMenu(menuButton.dataset.id);
            return;
        }

        const menuAction = e.target.closest('.instance-menu-action');
        if (menuAction) {
            e.stopPropagation();
            const { action, id } = menuAction.dataset;
            if (action === 'toggle-autostart') {
                toggleAutoStart(id);
            } else if (action === 'delete') {
                deleteInstance(id);
            }
            return;
        }

        const item = e.target.closest('.instance-item');
        if (item) {
            selectedInstance = item.dataset.id;
            syncConfigStateSelection();
            renderInstances();
            renderInstanceDetails();
            loadInstanceStatus(item.dataset.id);
            if (activeDetailsTab === 'configs') {
                loadConfigs();
            }
        }
    });

    instanceDetails.addEventListener('click', (e) => {
        const tabButton = e.target.closest('[data-detail-tab]');
        if (tabButton) {
            activeDetailsTab = tabButton.dataset.detailTab;
            renderInstanceDetails();
            if (activeDetailsTab === 'configs') {
                loadConfigs();
            }
            return;
        }

        const configAction = e.target.closest('[data-config-action]');
        const coordinatesAction = e.target.closest('[data-coordinates-action]');
        if (coordinatesAction) {
            loadCoordinates(selectedInstance);
            return;
        }
        if (!configAction) {
            return;
        }

        const action = configAction.dataset.configAction;
        if (action === 'refresh') {
            loadConfigs(true);
        } else if (action === 'add-thread') {
            openAddThreadModal();
        } else if (action === 'save-row') {
            saveConfigRow(configAction.dataset.rowKey);
        } else if (action === 'delete-row') {
            deleteConfigRow(configAction.dataset.rowKey);
        }
    });

    instanceDetails.addEventListener('input', (e) => {
        const input = e.target.closest('[data-config-input]');
        if (!input) {
            return;
        }

        const rowKey = input.dataset.configInput;
        const config = findConfigByRowKey(rowKey);
        if (!config) {
            return;
        }

        if (String(input.value) === String(config.value ?? '')) {
            delete configState.dirtyValues[rowKey];
        } else {
            configState.dirtyValues[rowKey] = input.value;
        }

        delete configState.rowErrors[rowKey];
        const saveButton = instanceDetails.querySelector(`[data-config-action="save-row"][data-row-key="${CSS.escape(rowKey)}"]`);
        if (saveButton) {
            saveButton.disabled = !Object.prototype.hasOwnProperty.call(configState.dirtyValues, rowKey);
        }
    });

    document.addEventListener('click', (e) => {
        if (!e.target.closest('.instance-menu-wrapper')) {
            closeInstanceMenu();
        }
    });

    loadStartTemplates();
    loadInstances();
    setInterval(loadInstances, 30000);
}

document.addEventListener('DOMContentLoaded', initApp);
