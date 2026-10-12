/* Accessible toast primitive; messages are text, never HTML. */
(function (scope) {
  'use strict';
  const app = scope.UTEGear = scope.UTEGear || {};
  app.Toast = {
    show(message, type = 'success', duration = 5000) {
      const allowed = ['success', 'error', 'info'];
      const variant = allowed.includes(type) ? type : 'info';
      let area = document.querySelector('[data-toast-area]');
      if (!area) {
        area = document.createElement('div');
        area.className = 'toast-area';
        area.dataset.toastArea = '';
        document.body.append(area);
      }
      const toast = document.createElement('div');
      toast.className = 'toast-msg ' + variant;
      toast.setAttribute('role', variant === 'error' ? 'alert' : 'status');
      toast.setAttribute('aria-atomic', 'true');
      const text = document.createElement('span');
      text.className = 'toast-message';
      const dismiss = document.createElement('button');
      dismiss.type = 'button';
      dismiss.className = 'toast-dismiss';
      dismiss.textContent = '×';
      dismiss.setAttribute('aria-label', 'Đóng thông báo');
      let timer;
      const close = () => { clearTimeout(timer); toast.remove(); };
      const pause = () => clearTimeout(timer);
      const resume = () => {
        pause();
        // Errors remain until dismissed; other messages pause while read/focused.
        if (variant !== 'error' && Number.isFinite(duration) && duration > 0) timer = setTimeout(close, duration);
      };
      dismiss.addEventListener('click', close);
      toast.addEventListener('mouseenter', pause);
      toast.addEventListener('mouseleave', () => { if (!toast.contains(document.activeElement)) resume(); });
      toast.addEventListener('focusin', pause);
      toast.addEventListener('focusout', () => { if (!toast.matches(':hover')) resume(); });
      toast.append(text, dismiss);
      area.append(toast);
      // Set text after inserting the live region so assistive tech can announce it.
      text.textContent = String(message);
      resume();
      return close;
    }
  };
})(globalThis);
