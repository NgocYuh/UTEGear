window.UTEGear = window.UTEGear || {};
UTEGear.Orders = {
    allowed: { PENDING: ['CONFIRMED','CANCELLED'], CONFIRMED: ['PROCESSING','CANCELLED'], PROCESSING: ['SHIPPING','CANCELLED'], SHIPPING: ['COMPLETED'], COMPLETED: [], CANCELLED: [] },
    labels: { PENDING: 'Chờ xác nhận', CONFIRMED: 'Đã xác nhận', PROCESSING: 'Đang chuẩn bị hàng', SHIPPING: 'Đang giao hàng', COMPLETED: 'Hoàn tất', CANCELLED: 'Đã hủy' },
    paymentLabels: { PENDING: 'Chưa thanh toán', UNPAID: 'Chưa thanh toán', PAID: 'Đã thanh toán', FAILED: 'Thanh toán thất bại', REFUNDED: 'Đã hoàn tiền', CANCELLED: 'Đã hủy' },
    date(value) {
        const date = new Date(value);
        return value && Number.isFinite(date.getTime()) ? new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', timeZone: 'Asia/Ho_Chi_Minh' }).format(date) : 'Chưa cập nhật';
    },
    badge(status) {
        return UTEGear.Views.node('span', this.labels[status] || 'Đang cập nhật', 'order-status-badge status-' + (Object.hasOwn(this.labels, status) ? status.toLowerCase() : 'unknown'));
    },
    customerDetail(host, detail) {
        const V = UTEGear.Views;
        host.hidden = false;
        const header = V.node('header', null, 'order-detail-heading');
        const title = V.node('div');
        title.append(V.node('p', 'Chi tiết đơn hàng', 'text-muted mb-1'), V.node('h2', detail.orderCode, 'h4 mb-1'), V.node('p', 'Đặt lúc ' + this.date(detail.createdAt), 'text-muted small mb-0'));
        header.append(title, this.badge(detail.status)); host.append(header);
        const grid = V.node('div', null, 'order-information-grid');
        const section = heading => { const block = V.node('section', null, 'order-information-block'); block.append(V.node('h3', heading, 'h6')); return block; };
        const recipient = section('Thông tin nhận hàng');
        recipient.append(V.node('strong', detail.customer?.name || 'Chưa cập nhật'));
        if (detail.customer?.phone) recipient.append(V.node('p', 'Điện thoại: ' + detail.customer.phone));
        if (detail.customer?.email) recipient.append(V.node('p', 'Email: ' + detail.customer.email));
        const address = [detail.shipping?.street, detail.shipping?.ward, detail.shipping?.district, detail.shipping?.province].filter(Boolean).join(', ');
        recipient.append(V.node('p', address || 'Địa chỉ chưa cập nhật', 'order-shipping-address'));
        const payment = section('Giao hàng & thanh toán');
        const methods = { COD: 'Thanh toán khi nhận hàng (COD)', BANK_TRANSFER: 'Chuyển khoản ngân hàng' };
        payment.append(V.node('p', methods[detail.payment?.method] || 'Phương thức chưa cập nhật'), V.node('p', this.paymentLabels[detail.payment?.status] || 'Trạng thái thanh toán chưa cập nhật'));
        if (detail.fulfillmentStore?.name) payment.append(V.node('p', 'Cửa hàng xử lý: ' + detail.fulfillmentStore.name));
        if (detail.shipping?.method) payment.append(V.node('p', detail.shipping.method === 'STANDARD' ? 'Giao hàng tiêu chuẩn' : 'Giao hàng đến địa chỉ đã chọn'));
        grid.append(recipient, payment); host.append(grid);
        const products = section('Sản phẩm đã đặt'); products.classList.add('order-product-list');
        for (const item of detail.items) {
            const row = V.node('article', null, 'order-product-row');
            const copy = V.node('div', null, 'order-product-copy');
            copy.append(V.node('h4', item.productName, 'h6 mb-1'), V.node('p', 'Mã sản phẩm: ' + item.sku, 'text-muted small'));
            const options = Object.entries(item.optionValues || {}).map(([key, value]) => key + ': ' + value).join(' · ');
            copy.append(V.node('p', options || 'Phiên bản mặc định', 'small'), V.node('p', 'Số lượng: ' + item.quantity + ' × ' + V.money(item.unitPrice), 'mb-0'));
            const amount = V.node('div', null, 'order-product-amount');
            amount.append(V.node('span', 'Thành tiền', 'text-muted small'), V.node('strong', V.money(item.subtotal)));
            row.append(V.image(item.imageUrl, item.productName), copy, amount); products.append(row);
        }
        host.append(products);
        const bottom = V.node('div', null, 'order-information-grid');
        const note = section('Ghi chú đơn hàng'); note.append(V.node('p', detail.note || 'Không có ghi chú.'));
        const totals = section('Chi tiết thanh toán'); totals.classList.add('order-totals');
        const list = V.node('dl');
        const line = (label, value, className) => { const row = V.node('div', null, className); row.append(V.node('dt', label), V.node('dd', value)); list.append(row); };
        line('Tiền sản phẩm', V.money(detail.subtotal));
        line('Phí vận chuyển', V.money(detail.shippingFee));
        line('Giảm giá' + (detail.couponCode ? ' (' + detail.couponCode + ')' : ''), (Number(detail.discount) > 0 ? '−' : '') + V.money(detail.discount));
        line('Tổng thanh toán', V.money(detail.total), 'order-grand-total');
        totals.append(list); bottom.append(note, totals); host.append(bottom);
        if (detail.cancelledAt) host.append(V.node('p', 'Đã hủy lúc ' + this.date(detail.cancelledAt), 'text-muted small'));
    },
    async detail(root, id) {
        const admin = root.dataset.ordersView === 'admin', V = UTEGear.Views;
        const revision = Number(root.dataset.detailRevision || 0) + 1; root.dataset.detailRevision = String(revision);
        const detail = await UTEGear.API.get((admin ? '/api/admin/orders/' : '/api/orders/') + UTEGear.Cart.id(id));
        if (!UTEGear.Session.get() || Number(root.dataset.detailRevision) !== revision) return;
        const host = root.querySelector('[data-order-detail]'); host.replaceChildren();
        if (!admin) this.customerDetail(host, detail);
        else {
            host.append(V.node('h2', 'Đơn hàng ' + detail.orderCode, 'h4'), V.node('p', 'Trạng thái: ' + detail.status));
            if (detail.customer) host.append(V.node('p', `${detail.customer.name} · ${detail.customer.email}`));
            for (const item of detail.items) host.append(V.node('p', `${item.productName} · ${item.sku} · ${item.quantity} × ${V.money(item.unitPrice)}`));
            host.append(V.node('strong', 'Tổng tiền: ' + V.money(detail.total)));
        }
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
            }, root));
            const cancel = V.node('details', null, 'order-cancel-panel');
            cancel.append(V.node('summary', 'Bạn muốn hủy đơn hàng?'), V.node('p', 'Nhập lý do bên dưới và xác nhận để hủy đơn.', 'text-muted small mt-2'), form);
            host.append(cancel);
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
            const row = V.node('article', null, 'surface-card p-3 mb-3 order-summary-row');
            const meta = V.node('div', null, 'order-summary-meta');
            meta.append(V.node('strong', order.orderCode), V.node('span', this.date(order.createdAt), 'text-muted small'));
            if (!admin && order.itemCount != null) meta.append(V.node('span', order.itemCount + ' loại sản phẩm', 'text-muted small'));
            const total = V.node('div', null, 'order-summary-total');
            total.append(V.node('span', 'Tổng thanh toán', 'text-muted small'), V.node('strong', V.money(order.total)));
            row.append(meta, this.badge(order.status), total, V.button('Xem chi tiết', () => this.detail(root, order.id), root)); host.append(row);
        }
        const pager = root.querySelector('[data-order-pagination]'); pager.replaceChildren();
        if (result.page > 0) pager.append(V.button('Trang trước', () => this.load(root, result.page - 1), root));
        if (result.page + 1 < result.totalPages) pager.append(V.button('Trang sau', () => this.load(root, result.page + 1), root));
        V.status(root, result.content.length ? '' : 'Chưa có đơn hàng phù hợp.');
        if (root.dataset.focus) await this.detail(root, UTEGear.Cart.id(root.dataset.focus));
        const login = root.querySelector('a[href="/login"]'); if (login) login.hidden = true;
    },
    init() {
        document.querySelectorAll('[data-orders-view]').forEach(root => {
            if (!UTEGear.Session.get()) { UTEGear.Views.status(root, 'Vui lòng đăng nhập để xem đơn hàng.'); return; }
            const focus = new URLSearchParams(location.search).get('focus');
            if (focus) { try { root.dataset.focus = UTEGear.Cart.id(focus); } catch (error) { UTEGear.Views.status(root, error.message, true); return; } }
            const refresh = () => this.load(root).catch(error => UTEGear.Views.status(root, error.message, true));
            root.querySelector('[data-order-filter]').addEventListener('change', () => {
                if (root.dataset.ordersView === 'customer') {
                    delete root.dataset.focus;
                    const detail = root.querySelector('[data-order-detail]'); detail.replaceChildren(); detail.hidden = true;
                    const url = new URL(location.href); url.searchParams.delete('focus'); history.replaceState(null, '', url);
                }
                refresh();
            });
            window.addEventListener('utegear:admin-orders-refresh', refresh);
            window.addEventListener('utegear:logout', () => {
                root.querySelector('[data-view-items]').replaceChildren(); root.querySelector('[data-order-detail]').replaceChildren();
                if (root.dataset.ordersView === 'customer') root.querySelector('[data-order-detail]').hidden = true;
                UTEGear.Views.status(root, 'Vui lòng đăng nhập để xem đơn hàng.');
            });
            refresh();
        });
    }
};
document.addEventListener('DOMContentLoaded', () => UTEGear.Orders.init());
