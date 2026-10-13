window.UTEGear = window.UTEGear || {};
UTEGear.Cart = {
    id(value) {
        const id = Number(value);
        if (!Number.isSafeInteger(id) || id < 1) throw new Error('ID không hợp lệ');
        return id;
    },
    get() { return UTEGear.API.get('/api/cart'); },
    async add({ productId, variantId, quantity = 1 }) {
        const qty = Number(quantity);
        if (!Number.isInteger(qty) || qty < 1) throw new Error('Số lượng phải là số nguyên dương');
        let id = variantId;
        if (id == null || id === '') {
            const product = await UTEGear.API.get(`/api/products/${this.id(productId)}`, { public: true });
            id = product.defaultVariantId;
            if (!product.variants?.some(variant => variant.id === id && variant.isDefault)) {
                throw new Error('Sản phẩm chưa có biến thể mặc định hợp lệ');
            }
        }
        const cart = await UTEGear.API.post('/api/cart/items', { variantId: this.id(id), quantity: qty });
        this.badges(cart);
        return cart;
    },
    async update(itemId, quantity) {
        const qty = Number(quantity);
        if (!Number.isInteger(qty) || qty < 1) throw new Error('Số lượng phải là số nguyên dương');
        const cart = await UTEGear.API.put(`/api/cart/items/${this.id(itemId)}`, { quantity: qty });
        this.badges(cart);
        return cart;
    },
    remove(itemId) { return UTEGear.API.delete(`/api/cart/items/${this.id(itemId)}`); },
    clear() { return UTEGear.API.delete('/api/cart/items'); },
    badges(cart) {
        document.querySelectorAll('#cartCount, #mobileCartCount, .cart-count').forEach(el => { el.textContent = cart.itemCount; });
    }
};
