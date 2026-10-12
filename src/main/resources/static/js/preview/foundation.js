/* Preview controller only; never authenticates or calls production APIs. */
(async function () {
  'use strict';
  const app = globalThis.UTEGear;
  const status = document.querySelector('[data-demo-status]');
  const empty = document.querySelector('[data-demo-empty]');
  try {
    await app.MockData.load();
    status.textContent = 'Dữ liệu mẫu đã sẵn sàng.';
    document.querySelectorAll('[data-demo]').forEach(button => {
      button.addEventListener('click', () => {
        switch (button.dataset.demo) {
          case 'success': app.Toast.show('Đã lưu thông tin mẫu.'); break;
          case 'error': app.Toast.show(app.MockData.get('forbidden').message, 'error'); break;
          case 'empty': {
            const page = app.MockData.get('productsEmpty');
            empty.hidden = !page.empty;
            status.textContent = 'Không có kết quả.';
            break;
          }
        }
      });
    });
    // The committed fixture has one page; pagination must hide in this case.
    app.Pagination.render(document.querySelector('[data-pagination]'), app.MockData.get('productsPage'), () => {});
  } catch (error) {
    status.textContent = 'Không tải được dữ liệu mẫu.';
    app.Toast.show(error.message, 'error');
  }
})();
