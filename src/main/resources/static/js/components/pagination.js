(function (scope) {
  'use strict';
  const app = scope.UTEGear = scope.UTEGear || {};
  app.Pagination = {
    render(container, page, onChange) {
      if (!container) throw new TypeError('Pagination container is required');
      container.replaceChildren();
      const valid = page && Number.isInteger(page.page) && Number.isInteger(page.totalPages)
        && page.page >= 0 && page.totalPages >= 0 && (page.totalPages === 0 || page.page < page.totalPages);
      if (!valid) throw new TypeError('Invalid ProductPage pagination');
      container.hidden = page.totalPages <= 1;
      if (container.hidden) return;
      const list = document.createElement('ul');
      list.className = 'pagination';
      const addButton = (label, index, disabled, current = false) => {
        const item = document.createElement('li');
        item.className = 'page-item' + (current ? ' active' : '') + (disabled ? ' disabled' : '');
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'page-link';
        button.textContent = label;
        button.disabled = disabled;
        if (current) button.setAttribute('aria-current', 'page');
        else if (!disabled) button.addEventListener('click', () => onChange(index));
        item.append(button);
        list.append(item);
      };
      const first = Math.max(0, Math.min(page.page - 2, page.totalPages - 5));
      addButton('Trước', page.page - 1, page.page === 0);
      for (let index = first; index < Math.min(first + 5, page.totalPages); index++) {
        addButton(String(index + 1), index, false, index === page.page);
      }
      addButton('Sau', page.page + 1, page.page === page.totalPages - 1);
      container.append(list);
    }
  };
})(globalThis);
