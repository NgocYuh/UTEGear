/**
 * UTEGear Standard Cart Module
 * Single clean implementation of Cart operations.
 */
window.UTEGear = window.UTEGear || {};

UTEGear.Cart = {
    async add({ productId, variantId = null, quantity = 1 }) {
        if (!productId) {
            UTEGear.Toast.show('Vui lòng chọn sản phẩm', 'error');
            return;
        }

        try {
            const data = await UTEGear.API.post('/api/cart/add', {
                productId: Number(productId),
                variantId: variantId ? Number(variantId) : null,
                quantity: Number(quantity) || 1
            });

            UTEGear.Toast.show('Đã thêm sản phẩm vào giỏ hàng', 'success');

            // Update badge counts and trigger pulse animation
            const countElements = document.querySelectorAll('#cartCount, .cart-count');
            countElements.forEach(el => {
                if (data.cartCount !== undefined) {
                    el.textContent = data.cartCount;
                    el.classList.add('pulse');
                    setTimeout(() => el.classList.remove('pulse'), 400);
                }
            });
        } catch (e) {
            if (e.message !== 'UNAUTHORIZED') {
                UTEGear.Toast.show(e.message || 'Không thể thêm vào giỏ hàng', 'error');
            }
        }
    }
};

// Global unified function bridge
window.addToCart = function (productId, variantId, quantity) {
    if (typeof productId === 'object') {
        UTEGear.Cart.add(productId);
    } else {
        UTEGear.Cart.add({ productId, variantId, quantity: quantity || 1 });
    }
};

window.quickAddToCart = function (productId) {
    UTEGear.Cart.add({ productId, quantity: 1 });
};
