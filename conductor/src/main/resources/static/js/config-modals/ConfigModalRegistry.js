window.ConfigModalRegistry = (() => {
    const modals = new Map();

    function normalizeType(type) {
        if (typeof type === 'string') {
            return type;
        }
        if (type && typeof type === 'object') {
            return type.name || type.typeName || type.value || '';
        }
        return '';
    }

    function register(type, modal) {
        modals.set(normalizeType(type).trim(), modal);
    }

    function open(type, context) {
        const normalized = normalizeType(type).trim();
        const isString = !normalized
            || normalized === 'String'
            || normalized === 'java.lang.String'
            || normalized.endsWith('.String');
        const modal = modals.get(normalized) || (isString ? modals.get('String') : null);

        if (modal) {
            modal.open(context);
        } else if (modals.has('String')) {
            modals.get('String').open(context);
        }
    }

    return { register, open, normalizeType };
})();
