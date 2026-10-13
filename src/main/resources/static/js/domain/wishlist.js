window.UTEGear = window.UTEGear || {};
UTEGear.Wishlist = {
    pending: new Set(),
    get() { return UTEGear.API.get('/api/wishlist'); },
    async toggle(productId, button) {
        const id = UTEGear.Cart.id(productId);
        if (this.pending.has(id)) return;
        this.pending.add(id);
        if (button) button.disabled = true;
        try {
            const items = await this.get();
            const exists = items.some(item => Number(item.product.id) === id);
            if (exists) await UTEGear.API.delete(`/api/wishlist/items/${id}`);
            else await UTEGear.API.post('/api/wishlist/items', { productId: id });
            document.querySelectorAll(`[data-product-id="${id}"][data-action="toggle-wishlist"], button[onclick*="toggleWishlist(${id}"]`).forEach(el => {
                el.classList.toggle('active', !exists);
                el.setAttribute('aria-pressed', String(!exists));
                const icon = el.querySelector('i');
                if (icon) icon.className = exists ? 'bi bi-heart' : 'bi bi-heart-fill text-danger';
            });
            return !exists;
        } finally {
            this.pending.delete(id);
            if (button) button.disabled = false;
        }
    }
};
