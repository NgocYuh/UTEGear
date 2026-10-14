window.UTEGear = window.UTEGear || {};

UTEGear.Views = {
    node(tag, text, className) {
        const el = document.createElement(tag);
        if (text != null) el.textContent = String(text);
        if (className) el.className = className;
        return el;
    },
    money(value) { return UTEGear.Utils.formatPrice(value); },
    status(root, message, error = false) {
        const status = root.querySelector('[data-view-status]');
        if (status) { status.textContent = message; status.classList.toggle('text-danger', error); }
        root.setAttribute('aria-busy', 'false');
    },
    async action(button, work, root) {
        if (button.disabled) return;
        button.disabled = true;
        try { await work(); }
        catch (error) { this.status(root, error.message, true); }
        finally { button.disabled = false; }
    },
    button(text, work, root, className = 'btn btn-outline-primary') {
        const button = this.node('button', text, className);
        button.type = 'button';
        button.addEventListener('click', () => this.action(button, work, root));
        return button;
    },
    productLink(product) {
        const link = this.node('a', product.name);
        link.href = '/products/' + UTEGear.Cart.id(product.id);
        return link;
    },
    image(url, name) {
        const image = this.node('img');
        // Use an actual URL for DOM assignment, not HTML-escaped markup.
        try {
            const target = new URL(url || '/images/placeholder.svg', location.origin);
            image.src = target.protocol === 'https:' || target.origin === location.origin ? target.href : '/images/placeholder.svg';
        } catch { image.src = '/images/placeholder.svg'; }
        image.alt = name || '';
        image.width = 80; image.height = 80;
        image.className = 'rounded object-fit-contain';
        image.onerror = () => { image.onerror = null; image.src = '/images/placeholder.svg'; };
        return image;
    },
    async cart(root) {
        const cart = await UTEGear.Cart.get();
        if (!UTEGear.Session.get()) return;
        const list = root.querySelector('[data-view-items]');
        list.replaceChildren();
        for (const item of cart.items) {
            const row = this.node('article', null, 'surface-card p-3 mb-3 d-flex flex-wrap align-items-center gap-3');
            row.append(this.image(item.product.primaryImage?.secureUrl, item.product.name));
            const info = this.node('div', null, 'flex-grow-1');
            info.append(this.productLink(item.product), this.node('p', Object.entries(item.variant.optionValues || {}).map(([k, v]) => `${k}: ${v}`).join(', ')));
            info.append(this.node('p', this.money(item.unitPrice)));
            const label = this.node('label', 'Số lượng', 'form-label');
            const quantity = this.node('input');
            quantity.type = 'number'; quantity.min = '1'; quantity.step = '1'; quantity.value = item.quantity;
            quantity.id = 'cart-quantity-' + UTEGear.Cart.id(item.id); quantity.className = 'form-control';
            label.htmlFor = quantity.id;
            info.append(label, quantity);
            if (!item.available) info.append(this.node('p', 'Không đủ hàng; vui lòng giảm số lượng hoặc xóa sản phẩm.', 'text-danger'));
            row.append(info, this.node('strong', this.money(item.lineTotal)));
            row.append(this.button('Cập nhật', async () => {
                if (!quantity.reportValidity()) return;
                try { await UTEGear.Cart.update(item.id, Number(quantity.value)); }
                catch (error) { quantity.value = item.quantity; throw error; }
                await this.cart(root);
            }, root));
            row.append(this.button('Xóa', async () => { await UTEGear.Cart.remove(item.id); await this.cart(root); }, root));
            list.append(row);
        }
        UTEGear.Cart.badges(cart);
        root.querySelector('[data-view-total]').textContent = this.money(cart.subtotal);
        const controls = root.querySelector('[data-view-actions]'); controls.replaceChildren();
        if (cart.items.length) {
            controls.append(this.button('Xóa toàn bộ giỏ', async () => { await UTEGear.Cart.clear(); await this.cart(root); }, root));
            const checkout = this.node('a', 'Thanh toán', 'btn btn-utama'); checkout.href = '/checkout'; controls.append(checkout);
        }
        this.status(root, cart.items.length ? '' : 'Giỏ hàng của bạn đang trống.');
    },
    async wishlist(root) {
        const items = await UTEGear.Wishlist.get();
        if (!UTEGear.Session.get()) return;
        const list = root.querySelector('[data-view-items]'); list.replaceChildren();
        for (const { product } of items) {
            const row = this.node('article', null, 'surface-card p-3 mb-3 d-flex flex-wrap align-items-center gap-3');
            row.append(this.image(product.primaryImage?.secureUrl, product.name), this.productLink(product), this.node('strong', this.money(product.effectivePrice)));
            const add = this.button('Thêm vào giỏ', async () => {
                await UTEGear.Cart.add({ productId: product.id, variantId: product.defaultVariantId }); this.status(root, 'Đã thêm vào giỏ hàng.');
            }, root);
            add.disabled = !product.inStock;
            row.append(add, this.button('Bỏ yêu thích', async () => {
                await UTEGear.API.delete('/api/wishlist/items/' + UTEGear.Cart.id(product.id)); await this.wishlist(root);
            }, root));
            list.append(row);
        }
        document.querySelectorAll('[data-wishlist-count]').forEach(el => { el.textContent = items.length; });
        this.status(root, items.length ? '' : 'Danh sách yêu thích đang trống.');
    },
    async profile(root) {
        const profile = await UTEGear.Auth.profile();
        if (!UTEGear.Session.get()) return;
        const list = root.querySelector('[data-view-items]'); list.replaceChildren();
        for (const [label, value] of [['Họ tên', profile.fullName], ['Email', profile.email], ['Điện thoại', profile.phone || 'Chưa cung cấp']]) {
            list.append(this.node('dt', label), this.node('dd', value));
        }
        this.status(root, '');
        UTEGear.Account?.bindProfile(root, profile);
    },
    async checkout(root) {
        const [cart, stores] = await Promise.all([UTEGear.Cart.get(), UTEGear.Stores.list()]);
        if (!UTEGear.Session.get()) return;
        const form = root.querySelector('form');
        if (!cart.items.length) { form.hidden = true; this.status(root, 'Giỏ hàng của bạn đang trống.'); return; }
        root.querySelector('[data-view-items]').textContent = `${cart.itemCount} sản phẩm · Tạm tính ${this.money(cart.subtotal)}`;
        const address = form.elements.namedItem('addressId'), store = form.elements.namedItem('fulfillmentStoreId');
        const fill = (select, data, label) => {
            select.replaceChildren(new Option('Vui lòng chọn', ''));
            data.forEach(item => select.add(new Option(label(item), UTEGear.Cart.id(item.id))));
        };
        fill(store, stores.filter(item => item.active), item => item.name + ' — ' + item.address);
        let addresses;
        try { addresses = await UTEGear.API.get('/api/addresses'); }
        catch (error) {
            this.status(root, error.status === 404 ? 'Chưa tải được địa chỉ giao hàng. Vui lòng thử lại sau.' : error.message, true);
            return;
        }
        fill(address, addresses, item => `${item.fullName} — ${item.street}, ${item.province}`);
        if (!addresses.length || !store.options.length || store.options.length === 1) {
            this.status(root, 'Cần địa chỉ giao hàng và cửa hàng đang hoạt động trước khi thanh toán.', true); return;
        }
        let quote = null, quotedRequest = null, busy = false, revision = 0;
        const quoteButton = form.querySelector('[data-quote]'), placeButton = form.querySelector('[data-place]');
            quoteButton.disabled = false;
        const request = () => ({ ...Object.fromEntries(new FormData(form)), items: cart.items.map(item => ({ variantId: item.variantId, quantity: item.quantity })) });
        form.addEventListener('input', () => { revision++; quote = null; placeButton.disabled = true; });
        quoteButton.addEventListener('click', () => this.action(quoteButton, async () => {
            if (!form.reportValidity() || busy) return;
            busy = true; const current = revision;
            try {
                const payload = request(); const result = await UTEGear.Transactions.quote(payload);
                if (current !== revision) { this.status(root, 'Thông tin đã thay đổi; vui lòng tính lại tổng tiền.'); return; }
                quote = result; quotedRequest = payload;
                root.querySelector('[data-view-total]').textContent = this.money(result.total);
                placeButton.disabled = false; this.status(root, 'Đã tính tổng tiền. Vui lòng kiểm tra trước khi đặt hàng.');
            } finally { busy = false; }
        }, root));
        form.addEventListener('submit', event => {
            event.preventDefault();
            this.action(placeButton, async () => {
                if (busy || !form.reportValidity()) return;
                busy = true;
                try {
                    const order = await UTEGear.Transactions.place(quotedRequest, quote);
                    form.hidden = true; UTEGear.Cart.badges({ itemCount: 0 });
                    this.status(root, 'Đã đặt đơn ' + order.orderCode + '.');
                    const link = this.node('a', 'Xem đơn hàng', 'btn btn-outline-primary mt-3');
                    link.href = '/orders?focus=' + UTEGear.Cart.id(order.id); root.append(link);
                } catch (error) { quote = null; placeButton.disabled = true; throw error; }
                finally { busy = false; }
            }, root).finally(() => { if (!quote) placeButton.disabled = true; });
        });
        this.status(root, 'Chọn địa chỉ và cửa hàng, sau đó tính tổng tiền.');
    },
    session() {
        const session = UTEGear.Session.get();
        document.querySelectorAll('[data-customer-view] a[href="/login"]').forEach(link => { link.hidden = !!session; });
        const controls = document.querySelector('[data-session-controls]');
        if (controls) {
            controls.hidden = !session;
            const name = controls.querySelector('[data-session-name]');
            if (name) name.textContent = session?.user?.fullName || 'Tài khoản';
        }
        document.querySelectorAll('[data-session-login]').forEach(link => {
            link.href = session ? '/profile' : '/login';
            const title = link.querySelector('.header-action-title');
            if (title) title.textContent = session?.user?.fullName || 'Đăng nhập';
        });
    },
    init() {
        document.querySelectorAll('header a[href="/login"], .topbar a[href="/login"], .mobile-nav-offcanvas a[href="/login"]').forEach(link => { link.dataset.sessionLogin = ''; });
        this.session();
        window.addEventListener('utegear:logout', () => {
            this.session();
            document.querySelectorAll('[data-customer-view]').forEach(root => {
                root.querySelector('[data-view-items]')?.replaceChildren();
                root.querySelector('form')?.setAttribute('hidden', '');
                const management = root.querySelector('[data-profile-management]'); if (management) management.hidden = true;
                root.querySelector('[data-address-items]')?.replaceChildren();
                this.status(root, 'Vui lòng đăng nhập để xem thông tin.');
            });
        });
        document.querySelector('[data-session-logout]')?.addEventListener('click', async () => {
            try { await UTEGear.Auth.logout(); } finally { location.href = '/login'; }
        });
        document.querySelectorAll('[data-customer-view]').forEach(root => {
            if (!UTEGear.Session.get()) { this.status(root, 'Vui lòng đăng nhập để xem thông tin.'); return; }
            root.setAttribute('aria-busy', 'true');
            this[root.dataset.customerView](root).catch(error => this.status(root, error.message, true));
        });
        const detail = document.querySelector('[data-page="product-detail"]');
        if (detail) {
            const select = detail.querySelector('[data-detail-variant]');
            let generation = 0;
            const update = async () => {
                const current = ++generation, option = select.selectedOptions[0];
                if (!option) return;
                const add = detail.querySelector('[data-detail-add]');
                add.dataset.variantId = option.value;
                add.disabled = Number(option.dataset.stock) <= 0;
                detail.querySelector('[data-variant-price]').textContent = this.money(option.dataset.price);
                detail.querySelector('[data-variant-stock]').textContent = Number(option.dataset.stock) > 0 ? 'Còn ' + option.dataset.stock + ' sản phẩm' : 'Hết hàng';
                const host = detail.querySelector('[data-product-availability]'); host.textContent = 'Đang tải tồn kho…';
                try {
                    const data = await UTEGear.API.get('/api/products/' + UTEGear.Cart.id(detail.dataset.productId) + '/availability', { public: true });
                    if (current !== generation) return;
                    const variant = data.variants.find(item => Number(item.variantId) === Number(option.value));
                    host.replaceChildren();
                    for (const store of variant?.availableStores || []) {
                        if (store.active) host.append(this.node('p', `${store.storeName}: ${store.quantity} sản phẩm`));
                    }
                    if (!host.childNodes.length) host.textContent = 'Chưa có hàng tại cửa hàng đang hoạt động.';
                } catch (error) { if (current === generation) host.textContent = error.message; }
            };
            select.addEventListener('change', update); update();
            detail.querySelectorAll('[data-gallery-url]').forEach(button => button.addEventListener('click', () => {
                const url = this.image(button.dataset.galleryUrl, '').src;
                detail.querySelector('#mainProductImage').src = url;
            }));
        }
    }
};
document.addEventListener('DOMContentLoaded', () => UTEGear.Views.init());
