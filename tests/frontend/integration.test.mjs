import test from 'node:test';
import assert from 'node:assert/strict';
import vm from 'node:vm';
import fs from 'node:fs';

function setup() {
    const calls = [], responses = [], store = new Map(), events = [];
    const context = vm.createContext({
        URL, URLSearchParams, Headers, FormData, Event,
        CustomEvent: class extends Event { constructor(name, options) { super(name); this.detail = options.detail; } },
        location: { origin: 'https://utegear.test', pathname: '/cart', search: '' },
        document: { addEventListener() {}, querySelectorAll: () => [] },
        sessionStorage: { getItem: key => store.get(key) || null, setItem: (key, value) => store.set(key, value), removeItem: key => store.delete(key) },
        dispatchEvent: event => events.push(event), addEventListener() {}, removeEventListener() {},
        fetch: async (url, options) => {
            calls.push({ url, ...options });
            const response = responses.shift();
            assert.ok(response, 'Unexpected request: ' + url);
            return { ok: response.status < 400, status: response.status, text: async () => response.raw ?? JSON.stringify(response.data) };
        },
        setTimeout, clearTimeout
    });
    context.window = context;
    for (const file of ['core/api.js', 'domain/cart.js', 'domain/wishlist.js', 'domain/admin.js', 'domain/realtime.js', 'app.js', 'domain/views.js']) {
        vm.runInContext(fs.readFileSync(new URL('../../src/main/resources/static/js/' + file, import.meta.url), 'utf8'), context, { filename: file });
    }
    const session = {
        accessToken: 'test-only-token', tokenType: 'Bearer', expiresAt: new Date(Date.now() + 60000).toISOString(),
        user: { fullName: 'QA Admin', email: 'qa-admin@example.test', roles: ['ADMIN'] }
    };
    context.UTEGear.Session.save(session);
    return { context, api: context.UTEGear, calls, responses, events, session };
}

test('session navigation shows authenticated and admin controls from the stored JWT session', () => {
    const { context, api } = setup();
    const guest = { hidden: false }, authenticated = { hidden: true }, admin = { hidden: true };
    const name = { textContent: '' }, email = { textContent: '' };
    const label = { textContent: '' }, title = { textContent: '' };
    const login = {
        href: '/login', attributes: {},
        setAttribute(key, value) { this.attributes[key] = value; },
        querySelector(selector) { return selector === '[data-session-label]' ? label : selector === '.header-action-title' ? title : null; }
    };
    const elements = new Map([
        ['[data-customer-view] a[href="/login"]', []],
        ['[data-session-guest]', [guest]],
        ['[data-session-authenticated]', [authenticated]],
        ['[data-admin-link]', [admin]],
        ['[data-session-name]', [name]],
        ['[data-session-email]', [email]],
        ['[data-session-login]', [login]]
    ]);
    context.document.querySelectorAll = selector => elements.get(selector) || [];
    context.document.querySelector = () => null;

    api.Views.session();
    assert.equal(guest.hidden, true);
    assert.equal(authenticated.hidden, false);
    assert.equal(admin.hidden, false);
    assert.equal(name.textContent, 'QA Admin');
    assert.equal(email.textContent, 'qa-admin@example.test');
    assert.equal(login.href, '/profile');
    assert.equal(login.attributes['aria-label'], 'Trang cá nhân');
    assert.equal(label.textContent, 'Xin chào');
    assert.equal(title.textContent, 'QA Admin');

    api.Session.clear();
    api.Views.session();
    assert.equal(guest.hidden, false);
    assert.equal(authenticated.hidden, true);
    assert.equal(admin.hidden, true);
    assert.equal(login.href, '/login');
});

test('admin page guard requires a stored ADMIN session before revealing the shell', () => {
    const { context, api, session } = setup();
    let redirect = null;
    const body = {
        hidden: true,
        matches: selector => selector === '[data-admin-session-guard]',
        removeAttribute(name) { this.removed = name; }
    };
    context.document.body = body;
    context.location.pathname = '/admin/orders';
    context.location.search = '?status=PENDING';
    context.location.replace = value => { redirect = value; };

    assert.equal(api.Views.adminAccess(), true);
    assert.equal(body.hidden, false);
    assert.equal(body.removed, 'aria-busy');

    body.hidden = true;
    api.Session.save({ ...session, user: { ...session.user, roles: ['CUSTOMER'] } });
    assert.equal(api.Views.adminAccess(), false);
    assert.equal(redirect, '/?adminAccess=denied');
    assert.equal(body.hidden, true);

    api.Session.clear();
    assert.equal(api.Views.adminAccess(), false);
    assert.equal(redirect, '/login?redirect=%2Fadmin%2Forders%3Fstatus%3DPENDING');
});

test('API sends JWT, preserves caller headers and handles 204', async () => {
    const { api, calls, responses } = setup();
    responses.push({ status: 204 });
    assert.equal(await api.API.delete('/api/cart/items', { headers: { 'X-Request-ID': 'example' } }), null);
    assert.equal(calls[0].headers.get('Authorization'), 'Bearer test-only-token');
    assert.equal(calls[0].headers.get('X-Request-ID'), 'example');
    assert.equal(calls[0].redirect, 'error');
});

test('public login stores session; incorrect credentials preserve current session', async () => {
    const { api, calls, responses, session } = setup();
    responses.push({ status: 200, data: session });
    await api.Auth.login('sample@example.test', 'test-only-password');
    assert.equal(calls[0].url, '/api/auth/login');
    assert.equal(calls[0].headers.has('Authorization'), false);
    responses.push({ status: 401, data: { code: 'INVALID_CREDENTIALS', message: 'Invalid credentials' } });
    await assert.rejects(api.Auth.login('sample@example.test', 'test-only-password'), /Invalid credentials/);
    assert.ok(api.Session.get());
});

test('protected 401 clears session while 403 preserves it', async () => {
    const { api, responses, events } = setup();
    responses.push({ status: 403, data: { code: 'FORBIDDEN' } });
    await assert.rejects(api.API.get('/api/admin/orders'), error => error.status === 403);
    assert.ok(api.Session.get());
    responses.push({ status: 401, data: { code: 'UNAUTHORIZED' } });
    await assert.rejects(api.API.get('/api/cart'), error => error.status === 401);
    assert.equal(api.Session.get(), null);
    assert.ok(events.some(event => event.type === 'utegear:unauthorized'));
});

test('API prevents token leakage and redirects stay on origin', async () => {
    const { api, calls } = setup();
    await assert.rejects(api.API.get('https://other.test/api/cart'), /cùng máy chủ/);
    await assert.rejects(api.API.get('/login'), /cùng máy chủ/);
    assert.equal(calls.length, 0);
    assert.equal(api.Session.redirectTarget('//other.test/path'), '/');
    assert.equal(api.Session.redirectTarget('/cart?view=full'), '/cart?view=full');
});

test('cart quick add resolves default variant and posts no client prices/productId', async () => {
    const { api, responses, calls } = setup();
    responses.push({ status: 200, data: { defaultVariantId: 7, variants: [{ id: 7, isDefault: true }] } }, { status: 201, data: { itemCount: 2 } });
    await api.Cart.add({ productId: 3, quantity: 2 });
    assert.equal(calls[0].url, '/api/products/3');
    assert.equal(calls[1].url, '/api/cart/items');
    assert.deepEqual(JSON.parse(calls[1].body), { variantId: 7, quantity: 2 });
    await assert.rejects(api.Cart.add({ variantId: 7, quantity: 1.5 }), /nguyên/);
});

test('cart update/delete/clear use item endpoints', async () => {
    const { api, responses, calls } = setup();
    responses.push({ status: 200, data: { itemCount: 2 } }, { status: 204 }, { status: 204 });
    await api.Cart.update(9, 2);
    await api.Cart.remove(9);
    await api.Cart.clear();
    assert.deepEqual(calls.map(call => [call.method, call.url]), [
        ['PUT', '/api/cart/items/9'], ['DELETE', '/api/cart/items/9'], ['DELETE', '/api/cart/items']
    ]);
});

test('wishlist reads state and uses idempotent add or delete', async () => {
    const { api, responses, calls } = setup();
    responses.push({ status: 200, data: [] }, { status: 201, data: {} });
    assert.equal(await api.Wishlist.toggle(3), true);
    responses.push({ status: 200, data: [{ product: { id: 3 } }] }, { status: 204 });
    assert.equal(await api.Wishlist.toggle(3), false);
    assert.equal(calls[1].url, '/api/wishlist/items');
    assert.equal(calls[3].url, '/api/wishlist/items/3');
});

test('checkout sends variant IDs/server quote token and rejects expired quotes', async () => {
    const { api, responses, calls } = setup();
    const request = { addressId: 1, fulfillmentStoreId: 2, paymentMethod: 'COD', items: [{ variantId: 7, quantity: 2, unitPrice: 1 }] };
    const quote = { quoteToken: 'test-quote', expiresAt: new Date(Date.now() + 60000).toISOString() };
    responses.push({ status: 200, data: quote }, { status: 201, data: { id: 8 } });
    await api.Transactions.quote(request);
    await api.Transactions.place(request, quote);
    assert.deepEqual(JSON.parse(calls[1].body).items, [{ variantId: 7, quantity: 2 }]);
    assert.equal(JSON.parse(calls[1].body).quoteToken, 'test-quote');
    await assert.rejects(api.Transactions.place(request, { ...quote, expiresAt: '2020-01-01' }), /hết hạn/);
});

test('admin inventory uses Store + Variant and rejects invalid transfer', async () => {
    const { api, responses, calls } = setup();
    responses.push({ status: 200, data: {} });
    await api.Admin.inventory(2, 7, 0);
    assert.equal(calls[0].url, '/api/admin/stores/2/inventory/7');
    assert.throws(() => api.Admin.transfer(2, 2, 7, 3), /không hợp lệ/);
});

test('public catalog/store requests use contract query and variant availability', async () => {
    const { api, responses, calls } = setup();
    responses.push({ status: 200, data: { content: [] } }, { status: 200, data: { variantId: 7, available: false } });
    await api.Catalog.products({ q: 'keyboard', page: 0 });
    await api.Stores.availability(2, 7);
    assert.equal(calls[0].url, '/api/products?q=keyboard&page=0');
    assert.equal(calls[1].url, '/api/stores/2/availability?variantId=7');
    assert.equal(calls[1].headers.has('Authorization'), false);
});

test('media keeps FormData boundary and server publicId/secureUrl', async () => {
    const { api, responses, calls } = setup();
    responses.push({ status: 201, data: { secureUrl: 'https://images.example.test/a.png', publicId: 'test-image-id' } });
    const file = new Blob(['test-only-image'], { type: 'image/png' });
    const result = await api.Admin.upload(3, file);
    assert.equal(result.publicId, 'test-image-id');
    assert.equal(calls[0].url, '/api/admin/products/3/images');
    assert.ok(calls[0].body instanceof FormData);
    assert.equal(calls[0].headers.has('Content-Type'), false);
    assert.equal(calls[0].headers.get('Authorization'), 'Bearer test-only-token');
});

test('STOMP uses native WebSocket, JWT, resync and deduplicated envelopes', async () => {
    const { context, api } = setup();
    let destination, receive, headers, socketUrl, disconnected = false, synced = false;
    const delivered = [];
    context.WebSocket = class { constructor(url) { socketUrl = url; } close() {} };
    context.Stomp = { over: ws => ({ ws, connect(value, ready) { headers = value; void ready(); }, subscribe(value, handler) { destination = value; receive = handler; }, disconnect() { disconnected = true; } }) };
    const realtime = api.Realtime.start({ orderId: 8, resync: async () => { synced = true; }, onEvent: event => { assert.ok(synced); delivered.push(event); } });
    await new Promise(resolve => setImmediate(resolve));
    const event = { eventId: 'test-event', occurredAt: new Date().toISOString(), type: 'ORDER_STATUS_CHANGED', data: { orderId: 8, status: 'COMPLETED' } };
    receive({ body: JSON.stringify(event) });
    receive({ body: JSON.stringify(event) });
    receive({ body: JSON.stringify({ ...event, eventId: 'other-order', data: { orderId: 9, status: 'COMPLETED' } }) });
    assert.equal(socketUrl, 'wss://utegear.test/ws');
    assert.equal(headers.Authorization, 'Bearer test-only-token');
    assert.equal(destination, '/user/queue/orders');
    assert.equal(delivered.length, 1);
    realtime.stop();
    assert.ok(disconnected);
});

test('STOMP server refusal stops the socket without scheduling reconnect', () => {
    const { context, api } = setup();
    const scheduled = [], states = [];
    let refused, disconnected = false, closed = false;
    context.setTimeout = callback => { scheduled.push(callback); return scheduled.length; };
    context.clearTimeout = () => {};
    context.WebSocket = class { close() { closed = true; } };
    context.Stomp = { over: ws => ({ ws, connect(headers, ready, failed) { refused = failed; }, disconnect() { disconnected = true; } }) };
    api.Realtime.start({ orderId: 8, resync: async () => {}, onEvent() {}, onState: state => states.push(state) });
    refused({ command: 'ERROR', body: 'subscription refused' });
    assert.equal(scheduled.length, 1, 'Only the initial token-expiry timer; no reconnect timer');
    assert.ok(closed && disconnected);
    assert.equal(states.at(-1), 'authentication-required');
});

test('Expired local session clears private UI through the logout event', () => {
    const { context, api, session, events } = setup();
    context.sessionStorage.setItem(api.Session.key, JSON.stringify({ ...session, expiresAt: '2020-01-01T00:00:00Z' }));
    assert.equal(api.Session.get(), null);
    assert.equal(context.sessionStorage.getItem(api.Session.key), null);
    assert.ok(events.some(event => event.type === 'utegear:logout'));
});

function purchaseFixture(quantity = '2', stock = '5') {
    const buy = { disabled: false }, add = { disabled: false };
    const option = { value: '77', dataset: { stock } };
    const status = { textContent: '', classList: { toggle() {} } };
    const nodes = new Map([
        ['[data-detail-buy-now]', buy], ['[data-detail-add]', add],
        ['[data-detail-variant]', { selectedOptions: [option] }],
        ['#quantity', { value: quantity }], ['[data-view-status]', status]
    ]);
    return { buy, add, status, root: {
        dataset: { productId: '22', stockReady: 'true' },
        querySelector: key => nodes.get(key), setAttribute() {},
        syncPurchaseButtons() { buy.disabled = false; add.disabled = false; }
    } };
}

test('Buy now adds the selected variant then opens checkout without placing an order', async () => {
    const { context, api, calls, responses } = setup();
    const { root } = purchaseFixture();
    responses.push({ status: 200, data: { itemCount: 2, items: [] } });
    await api.Views.buyNow(root);
    assert.equal(calls.length, 1);
    assert.equal(calls[0].url, '/api/cart/items');
    assert.deepEqual(JSON.parse(calls[0].body), { variantId: 77, quantity: 2 });
    assert.equal(context.location.href, '/checkout');
});

test('Buy now asks guests to log in with a product-page return URL', async () => {
    const { context, api, calls } = setup();
    api.Session.clear(); context.location.pathname = '/products/22';
    await api.Views.buyNow(purchaseFixture().root);
    assert.equal(context.location.href, '/login?redirect=%2Fproducts%2F22');
    assert.equal(calls.length, 0);
});

test('Buy now rejects quantities above verified stock without changing the cart', async () => {
    const { api, calls } = setup();
    const { root, status } = purchaseFixture('6', '5');
    await api.Views.buyNow(root);
    assert.equal(calls.length, 0);
    assert.match(status.textContent, /tồn kho/);
});

test('Buy now prevents duplicate requests while the first click is pending', async () => {
    const { context, api } = setup();
    const { root } = purchaseFixture();
    let finish, requests = 0;
    api.Cart.add = async () => { requests++; await new Promise(resolve => { finish = resolve; }); };
    const first = api.Views.buyNow(root);
    await api.Views.buyNow(root);
    assert.equal(requests, 1);
    finish(); await first;
    assert.equal(context.location.href, '/checkout');
});

test('A cart error keeps the customer on the product page and permits retry', async () => {
    const { context, api, responses } = setup();
    const { root, status, buy } = purchaseFixture();
    responses.push({ status: 409, data: { message: 'Không đủ tồn kho' } });
    await api.Views.buyNow(root);
    assert.equal(context.location.href, undefined);
    assert.equal(buy.disabled, false);
    assert.match(status.textContent, /Không đủ tồn kho/);
});
