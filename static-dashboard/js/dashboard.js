// TICKET-ADV100: theme toggle click handler
document.addEventListener('DOMContentLoaded', () => {
    const toggleBtn = document.getElementById('theme-toggle');

    if (toggleBtn) {
        // Reflect current state on load (in case theme.js already set it)
        const current = document.documentElement.getAttribute('data-theme') || 'light';
        toggleBtn.setAttribute('aria-pressed', current === 'dark' ? 'true' : 'false');

        toggleBtn.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
            const next = currentTheme === 'dark' ? 'light' : 'dark';

            document.documentElement.setAttribute('data-theme', next);
            localStorage.setItem('reconx-theme', next);
            toggleBtn.setAttribute('aria-pressed', next === 'dark' ? 'true' : 'false');
        });
    }
});
