(() => {
    'use strict';
    document.querySelectorAll('[data-product-tabs]').forEach(root => {
        const tabs = [...root.querySelectorAll('[role="tab"]')];
        const panels = [...root.querySelectorAll('[role="tabpanel"]')];
        function select(tab, focus = false) {
            tabs.forEach(item => {
                const active = item === tab;
                item.setAttribute('aria-selected', String(active));
                item.tabIndex = active ? 0 : -1;
            });
            panels.forEach(panel => { panel.hidden = panel.id !== tab.getAttribute('aria-controls'); });
            if (focus) tab.focus();
        }
        tabs.forEach((tab, index) => {
            tab.addEventListener('click', () => select(tab));
            tab.addEventListener('keydown', event => {
                let next;
                if (event.key === 'ArrowRight') next = (index + 1) % tabs.length;
                if (event.key === 'ArrowLeft') next = (index + tabs.length - 1) % tabs.length;
                if (event.key === 'Home') next = 0;
                if (event.key === 'End') next = tabs.length - 1;
                if (next === undefined) return;
                event.preventDefault();
                select(tabs[next], true);
            });
        });
    });
})();
