window.UTEGear = window.UTEGear || {};
UTEGear.Orders = {
    allowed: { PENDING: ['CONFIRMED','CANCELLED'], CONFIRMED: ['PROCESSING','CANCELLED'], PROCESSING: ['SHIPPING','CANCELLED'], SHIPPING: ['COMPLETED'], COMPLETED: [], CANCELLED: [] },
    async detail(root, id) {
        const admin = root.dataset.ordersView === 'admin', V = UTEGear.Views;
        const revision = Number(root.dataset.detailRevision || 0) + 1; root.dataset.detailRevision = String(revision);
        const detail = await UTEGear.API.get((admin ? '/api/admin/orders/' : '/api/orders/') + UTEGear.Cart.id(id));
        if (!UTEGear.Session.get() || Number(root.dataset.detailRevision) !== revision) return;
        const host = root.querySelector('[data-order-detail]'); host.replaceChildren();
        host.append(V.node('h2', 'Đơn hàng ' + detail.orderCode, 'h4'), V.node('p', 'Trạng thái: ' + detail.status));
        if (detail.customer) host.append(V.node('p', `${detail.customer.name} · ${detail.customer.email}`));
        for (const item of detail.items) host.append(V.node('p', `${item.productName} · ${item.sku} · ${item.quantity} × ${V.money(item.unitPrice)}`));
        host.append(V.node('strong', 'Tổng tiền: ' + V.money(detail.total)));
        const form = V.node('form', null, 'my-3'); form.onsubmit = () => false;
        if (admin && this.allowed[detail.status]?.length) {
            const label = V.node('label', 'Trạng thái mới', 'form-label'); label.htmlFor = 'order-next-status';
            const select = V.node('select'); select.id = 'order-next-status'; select.className = 'form-select';
            for (const value of this.allowed[detail.status]) select.add(new Option(value, value));
            form.append(label, select, V.button('Cập nhật trạng thái', async () => {
                await UTEGear.Admin.status(id, select.value); await this.load(root, Number(root.dataset.page || 0));
            }, root)); host.append(form);
        } else if (!admin && ['PENDING','CONFIRMED','PROCESSING'].includes(detail.status) && detail.payment?.status !== 'PAID') {
            const label = V.node('label', 'Lý do hủy', 'form-label'); label.htmlFor = 'order-cancel-reason';
            const reason = V.node('textarea'); reason.id = 'order-cancel-reason'; reason.required = true; reason.maxLength = 500; reason.className = 'form-control';
            form.append(label, reason, V.button('Hủy đơn hàng', async () => {
                if (!reason.reportValidity() || !reason.value.trim()) return;
                await UTEGear.API.post('/api/orders/' + UTEGear.Cart.id(id) + '/cancel', { reason: reason.value.trim() });
                await this.load(root, Number(root.dataset.page || 0));
            }, root)); host.append(form);
        }
        root.dataset.focus = String(id);
        const url = new URL(location.href); url.searchParams.set('focus', id); history.replaceState(null, '', url);
        if (!admin) UTEGear.RealtimeUI?.customer(id, () => this.detail(root, id), root);
    },
    async load(root, page = 0) {
        const admin = root.dataset.ordersView === 'admin', V = UTEGear.Views;
        const params = new URLSearchParams({ page, size: 20 });
        const status = root.querySelector('[data-order-filter]').value; if (status) params.set('status', status);
        const result = await UTEGear.API.get((admin ? '/api/admin/orders' : '/api/orders') + '?' + params);
        if (!UTEGear.Session.get()) return;
        root.dataset.page = String(result.page);
        const host = root.querySelector('[data-view-items]'); host.replaceChildren();
        for (const order of result.content) {
            const row = V.node('article', null, 'surface-card p-3 mb-3 d-flex flex-wrap gap-3 align-items-center');
            row.append(V.node('strong', order.orderCode), V.node('span', order.status), V.node('span', V.money(order.total)));
            row.append(V.button('Xem chi tiết', () => this.detail(root, order.id), root)); host.append(row);
        }
        const pager = root.querySelector('[data-order-pagination]'); pager.replaceChildren();
        if (result.page > 0) pager.append(V.button('Trang trước', () => this.load(root, result.page - 1), root));
        if (result.page + 1 < result.totalPages) pager.append(V.button('Trang sau', () => this.load(root, result.page + 1), root));
        V.status(root, result.content.length ? '' : 'Chưa có đơn hàng phù hợp.');
        if (root.dataset.focus) await this.detail(root, UTEGear.Cart.id(root.dataset.focus));
    },
    init() {
        document.querySelectorAll('[data-orders-view]').forEach(root => {
            if (!UTEGear.Session.get()) { UTEGear.Views.status(root, 'Vui lòng đăng nhập để xem đơn hàng.'); return; }
            const focus = new URLSearchParams(location.search).get('focus');
            if (focus) { try { root.dataset.focus = UTEGear.Cart.id(focus); } catch (error) { UTEGear.Views.status(root, error.message, true); return; } }
            const refresh = () => this.load(root).catch(error => UTEGear.Views.status(root, error.message, true));
            root.querySelector('[data-order-filter]').addEventListener('change', refresh);
            window.addEventListener('utegear:admin-orders-refresh', refresh);
            window.addEventListener('utegear:logout', () => {
                root.querySelector('[data-view-items]').replaceChildren(); root.querySelector('[data-order-detail]').replaceChildren();
                UTEGear.Views.status(root, 'Vui lòng đăng nhập để xem đơn hàng.');
            });
            refresh();
        });
    }
};
document.addEventListener('DOMContentLoaded', () => UTEGear.Orders.init());
