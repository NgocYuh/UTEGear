/**
 * UTEGear Master Application Entry Point
 * Event Delegation & Module Initializer.
 */
document.addEventListener('DOMContentLoaded', () => {
    UTEGear.Auth.init();
    // 1. Initialize Sub-modules
    if (UTEGear.Autocomplete) UTEGear.Autocomplete.init();
    if (UTEGear.Filter) UTEGear.Filter.init();

    // 2. Global Event Delegation Architecture
    document.body.addEventListener('click', (e) => {
        const target = e.target.closest('[data-action]');
        if (!target) return;

        const action = target.getAttribute('data-action');
        const productId = target.getAttribute('data-product-id');
        const variantId = target.getAttribute('data-variant-id');

        switch (action) {
            case 'add-to-cart':
                e.preventDefault();
                window.addToCart(productId, variantId, 1);
                break;
            case 'toggle-wishlist':
                e.preventDefault();
                window.toggleWishlist(productId, target);
                break;
            case 'quick-view':
                e.preventDefault();
                if (window.quickView) window.quickView(productId);
                break;
            default:
                break;
        }
    });

    // 3. Track Recently Viewed Product on Detail Pages
    const detailContainer = document.querySelector('[data-page="product-detail"]');
    if (detailContainer) {
        const pId = detailContainer.getAttribute('data-product-id');
        if (pId && UTEGear.Utils) {
            UTEGear.Utils.addRecentlyViewed(Number(pId));
        }
    }
});

UTEGear.Auth = {
    async login(email, password) {
        const session = await UTEGear.API.post('/api/auth/login', { email, password }, { public: true });
        UTEGear.Session.save(session);
        return session;
    },
    register({ fullName, email, phone, password, confirmPassword }) {
        if (password !== confirmPassword) throw new Error('Mật khẩu xác nhận không khớp');
        return UTEGear.API.post('/api/auth/register', { fullName, email, phone: phone || null, password }, { public: true });
    },
    profile() { return UTEGear.API.get('/api/profile'); },
    async logout() {
        try { await UTEGear.API.post('/api/auth/logout'); }
        finally { UTEGear.Session.clear(); }
    },
    init() {
        document.querySelectorAll('form[data-auth]').forEach(form => {
            form.addEventListener('submit', async event => {
                event.preventDefault();
                if (!form.reportValidity() || form.dataset.busy) return;
                form.dataset.busy = 'true';
                const button = form.querySelector('[type="submit"]');
                if (button) button.disabled = true;
                const status = form.querySelector('[data-auth-status]');
                if (status) status.textContent = 'Đang xử lý…';
                try {
                    const fields = Object.fromEntries(new FormData(form));
                    if (form.dataset.auth === 'login') {
                        await this.login(fields.email, fields.password);
                        location.href = UTEGear.Session.redirectTarget(new URLSearchParams(location.search).get('redirect'));
                    } else {
                        await this.register(fields);
                        location.href = '/login';
                    }
                } catch (error) {
                    if (status) status.textContent = error.message;
                } finally {
                    delete form.dataset.busy;
                    if (button) button.disabled = false;
                }
            });
        });
        window.addEventListener('utegear:unauthorized', () => {
            const redirect = encodeURIComponent(location.pathname + location.search);
            location.href = '/login?redirect=' + redirect;
        });
    }
};

// Only implemented BE endpoints are exposed here. No fallback to legacy form POSTs.
UTEGear.Transactions = {
    async quote(request) {
        return UTEGear.API.post('/api/checkout/quote', this.request(request));
    },
    async place(request, quote) {
        if (!quote?.quoteToken || !Number.isFinite(Date.parse(quote.expiresAt)) || Date.parse(quote.expiresAt) <= Date.now()) {
            throw new Error('Báo giá đã hết hạn; vui lòng tính lại trước khi đặt hàng');
        }
        return UTEGear.API.post('/api/orders', { ...this.request(request), quoteToken: quote.quoteToken });
    },
    request(request) {
        if (!['COD', 'BANK_TRANSFER'].includes(request.paymentMethod)) throw new Error('Phương thức thanh toán không hợp lệ');
        if (!request.items?.length) throw new Error('Giỏ hàng đang trống');
        return {
            addressId: UTEGear.Cart.id(request.addressId),
            fulfillmentStoreId: UTEGear.Cart.id(request.fulfillmentStoreId),
            couponCode: request.couponCode || null,
            paymentMethod: request.paymentMethod,
            note: request.note || null,
            items: request.items.map(item => {
                if (!Number.isInteger(Number(item.quantity)) || Number(item.quantity) < 1) throw new Error('Số lượng không hợp lệ');
                return { variantId: UTEGear.Cart.id(item.variantId), quantity: Number(item.quantity) };
            })
        };
    }
};

UTEGear.Catalog = {
    products(filters = {}) { return UTEGear.API.get('/api/products?' + new URLSearchParams(filters), { public: true }); },
    product(id) { return UTEGear.API.get(`/api/products/${UTEGear.Cart.id(id)}`, { public: true }); },
    categories() { return UTEGear.API.get('/api/categories', { public: true }); },
    brands() { return UTEGear.API.get('/api/brands', { public: true }); }
};

UTEGear.Stores = {
    list(filters = {}) { return UTEGear.API.get('/api/stores?' + new URLSearchParams(filters), { public: true }); },
    detail(id) { return UTEGear.API.get(`/api/stores/${UTEGear.Cart.id(id)}`, { public: true }); },
    availability(storeId, variantId) {
        return UTEGear.API.get(`/api/stores/${UTEGear.Cart.id(storeId)}/availability?variantId=${UTEGear.Cart.id(variantId)}`, { public: true });
    }
};
