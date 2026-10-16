(() => {
    'use strict';
    document.querySelectorAll('[data-account-dropdown]').forEach(menu => {
        const trigger = menu.querySelector('summary');
        const sync = () => trigger.setAttribute('aria-expanded', String(menu.open));
        const close = () => { menu.open = false; sync(); };
        menu.addEventListener('toggle', sync);
        menu.addEventListener('pointerenter', event => {
            if (event.pointerType !== 'mouse') return;
            menu.open = true;
            sync();
        });
        menu.addEventListener('pointerleave', event => {
            if (event.pointerType === 'mouse' && !menu.contains(document.activeElement)) close();
        });
        menu.addEventListener('focusout', event => {
            if (!menu.contains(event.relatedTarget)) close();
        });
        menu.addEventListener('keydown', event => {
            if (event.key !== 'Escape') return;
            event.preventDefault();
            close();
            trigger.focus();
        });
        document.addEventListener('pointerdown', event => { if (!menu.contains(event.target)) close(); });
        window.addEventListener('utegear:logout', close);
        sync();
    });
})();
