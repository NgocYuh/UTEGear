(function (scope) {
  'use strict';
  const app = scope.UTEGear = scope.UTEGear || {};
  app.initShell = function () {
    const toggle = document.querySelector('[data-menu-toggle]');
    const nav = toggle && document.getElementById(toggle.getAttribute('aria-controls'));
    if (!nav || toggle.dataset.initialized) return;
    toggle.dataset.initialized = 'true';
    nav.dataset.collapsible = '';
    const setOpen = open => {
      toggle.setAttribute('aria-expanded', String(open));
      nav.toggleAttribute('data-open', open);
    };
    toggle.addEventListener('click', () => setOpen(toggle.getAttribute('aria-expanded') !== 'true'));
    document.addEventListener('keydown', event => {
      if (event.key === 'Escape' && toggle.getAttribute('aria-expanded') === 'true') {
        setOpen(false);
        toggle.focus();
      }
    });
    nav.addEventListener('click', event => {
      if (event.target.closest('a') && scope.matchMedia('(max-width: 767.98px)').matches) setOpen(false);
    });
    const mobile = scope.matchMedia('(max-width: 767.98px)');
    mobile.addEventListener('change', () => setOpen(false));
  };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', app.initShell, { once: true });
  else app.initShell();
})(globalThis);
