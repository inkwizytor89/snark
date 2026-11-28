window.StringModal = (() => {
    let activeContext = null;

    function open(context) {
        activeContext = context;
        const modal = document.getElementById('configStringModal');
        const input = document.getElementById('configStringModalInput');
        document.getElementById('configStringModalTitle').textContent = context.key || 'Configuration value';
        input.value = context.value ?? '';
        modal.hidden = false;
        requestAnimationFrame(() => input.focus());
    }

    function close() {
        activeContext = null;
        document.getElementById('configStringModal').hidden = true;
    }

    function submit() {
        if (activeContext) {
            activeContext.onConfirm(document.getElementById('configStringModalInput').value);
        }
        close();
    }

    document.addEventListener('DOMContentLoaded', () => {
        document.getElementById('cancelConfigStringModalBtn').addEventListener('click', close);
        document.getElementById('configStringModalForm').addEventListener('submit', (event) => {
            event.preventDefault();
            submit();
        });
        document.getElementById('configStringModal').addEventListener('click', (event) => {
            if (event.target === event.currentTarget) {
                close();
            }
        });
    });

    return { open, close };
})();
