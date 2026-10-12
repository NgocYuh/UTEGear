/**
 * UTEGear Wishlist Module
 */
window.UTEGear = window.UTEGear || {};

UTEGear.Wishlist = {
    async toggle(productId, buttonElement) {
        if (!productId) return;

        try {
            const data = await UTEGear.API.post('/api/wishlist/toggle', { productId: Number(productId) });
            const isAdded = data.added !== undefined ? data.added : (data.inWishlist !== undefined ? data.inWishlist : false);
            const message = data.message || (isAdded ? 'Đã thêm vào danh sách yêu thích' : 'Đã bỏ khỏi danh sách yêu thích');

            UTEGear.Toast.show(message, isAdded ? 'success' : 'info');

            // Update UI icons across page
            const targetBtns = document.querySelectorAll(`button[data-product-id="${productId}"].action-btn-icon, button[onclick*="toggleWishlist(${productId})"]`);
            targetBtns.forEach(btn => {
                const icon = btn.querySelector('i');
                if (icon) {
                    if (isAdded) {
                        icon.className = 'bi bi-heart-fill text-danger';
                        btn.classList.add('active');
                    } else {
                        icon.className = 'bi bi-heart';
                        btn.classList.remove('active');
                    }
                }
            });
        } catch (e) {
            if (e.message !== 'UNAUTHORIZED') {
                UTEGear.Toast.show(e.message || 'Không thể cập nhật danh sách yêu thích', 'error');
            }
        }
    }
};

window.toggleWishlist = function (productId, btn) {
    UTEGear.Wishlist.toggle(productId, btn);
};
