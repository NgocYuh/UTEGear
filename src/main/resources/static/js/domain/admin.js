window.UTEGear = window.UTEGear || {};
UTEGear.Admin = {
    init() {
        document.querySelectorAll('form[data-admin-order]').forEach(form => {
            form.addEventListener('submit', async event => {
                event.preventDefault();
                if (form.dataset.busy || !form.reportValidity()) return;
                const status = form.querySelector('[data-admin-status]');
                const button = form.querySelector('[type="submit"]');
                form.dataset.busy = 'true';
                if (button) button.disabled = true;
                try {
                    const fields = new FormData(form);
                    const order = await this.status(form.dataset.adminOrder, fields.get('status'));
                    if (status) status.textContent = 'Đã cập nhật: ' + order.status;
                } catch (error) {
                    if (status) status.textContent = error.message;
                } finally {
                    delete form.dataset.busy;
                    if (button) button.disabled = false;
                }
            });
        });
    },
    id(value) {
        const id = Number(value);
        if (!Number.isSafeInteger(id) || id < 1) throw new Error('ID không hợp lệ');
        return id;
    },
    orders(filters = {}) { return UTEGear.API.get('/api/admin/orders?' + new URLSearchParams(filters)); },
    order(id) { return UTEGear.API.get(`/api/admin/orders/${this.id(id)}`); },
    status(id, status) {
        if (!['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'COMPLETED', 'CANCELLED'].includes(status)) throw new Error('Trạng thái không hợp lệ');
        return UTEGear.API.put(`/api/admin/orders/${this.id(id)}/status`, { status });
    },
    inventory(storeId, variantId, quantity) {
        if (!Number.isInteger(Number(quantity)) || Number(quantity) < 0) throw new Error('Tồn kho phải là số nguyên không âm');
        return UTEGear.API.put(`/api/admin/stores/${this.id(storeId)}/inventory/${this.id(variantId)}`, { quantity: Number(quantity) });
    },
    transfer(sourceStoreId, destinationStoreId, variantId, quantity) {
        const source = this.id(sourceStoreId), destination = this.id(destinationStoreId);
        if (source === destination || !Number.isInteger(Number(quantity)) || Number(quantity) < 1) throw new Error('Điều chuyển không hợp lệ');
        return UTEGear.API.post('/api/admin/inventory/transfers', {
            sourceStoreId: source, destinationStoreId: destination, variantId: this.id(variantId), quantity: Number(quantity)
        });
    },
    upload(productId, file, { altText = '', primary = false } = {}) {
        if (!file || !['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || file.size > 5 * 1024 * 1024) throw new Error('Chọn ảnh JPEG, PNG hoặc WebP tối đa 5 MiB');
        const body = new FormData();
        body.set('file', file);
        body.set('altText', altText);
        body.set('primary', String(primary));
        return UTEGear.API.request(`/api/admin/products/${this.id(productId)}/images`, { method: 'POST', body });
    }
};
