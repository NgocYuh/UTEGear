// QA-04: real HTTP/DOM only. No fetch interception, fixture data or API writes.
// Node 22+, optional Chromium remote debugging endpoint for rendered UI checks.
import fs from 'node:fs';
import path from 'node:path';

const options = Object.fromEntries(process.argv.slice(2).map(argument => {
    const split = argument.indexOf('=');
    if (!argument.startsWith('--') || split < 0) throw Error('Use --name=value');
    return [argument.slice(2, split), argument.slice(split + 1)];
}));
function localUrl(value) {
    const url = new URL(value);
    if (!['http:', 'https:'].includes(url.protocol) || !['localhost', '127.0.0.1', '[::1]'].includes(url.hostname)
        || url.username || url.password || url.pathname !== '/' || url.search || url.hash) {
        throw Error('QA-04 requires a loopback origin without credentials/path/query/hash');
    }
    return url.origin;
}
const base = localUrl(options.base || 'http://127.0.0.1:18080');
const cdp = options.cdp ? localUrl(options.cdp) : null;
const output = path.resolve(options.report || 'target/qa04/live-report.json');
const cases = [];
function record(id, status, detail) { cases.push({ id, status, detail }); }
async function request(route) {
    return fetch(base + route, { redirect: 'manual', signal: AbortSignal.timeout(10000) });
}
async function apiCheck(id, route, expected, validate) {
    try {
        const response = await request(route);
        let data;
        try { data = await response.json(); }
        catch {
            record(id, 'FAIL', `GET ${route}: HTTP ${response.status}; response is not valid JSON`);
            return;
        }
        record(id, response.status === expected && validate(data) ? 'PASS' : 'FAIL',
            `GET ${route}: HTTP ${response.status}; expected ${expected} and contract response`);
    } catch (error) { record(id, 'BLOCKED', `GET ${route}: ${error.name}`); }
}

const pages = [
    ['/', /<main\b/], ['/wishlist', /<main\b/],
    ['/login', /data-auth="login"/], ['/register', /data-auth="register"/],
    ['/products', /<main\b/], ['/stores', /<main\b/], ['/cart', /<main\b/],
    ['/checkout', /id="checkoutForm"|class="cart-empty-box"/], ['/orders', /<main\b/],
    ['/profile', /<main\b/], ['/admin/orders', /<main\b/]
];
const readyPages = [];
for (const [route, marker] of pages) {
    try {
        const response = await request(route);
        const html = await response.text();
        const ready = response.status === 200 && /text\/html/i.test(response.headers.get('content-type') || '')
            && marker.test(html) && !/layout:decorate=|layout:fragment=|th:action=/.test(html);
        record(`UI ${route}`, ready ? 'PASS' : 'BLOCKED', ready
            ? 'Rendered HTML available; browser checks still required'
            : `HTTP ${response.status}; rendered target page unavailable (raw template is not a rendered page)`);
        if (ready) readyPages.push(route);
    } catch (error) { record(`UI ${route}`, 'BLOCKED', `Application unavailable: ${error.name}`); }
}

await apiCheck('Public categories', '/api/categories', 200, Array.isArray);
await apiCheck('Public brands', '/api/brands', 200, Array.isArray);
await apiCheck('Public catalog', '/api/products?q=qa04-no-match&page=0&size=5', 200,
    data => Array.isArray(data.content) && typeof data.totalElements === 'number' && typeof data.empty === 'boolean');
await apiCheck('Public stores', '/api/stores', 200, Array.isArray);
await apiCheck('Catalog 404', '/api/products/9223372036854775807', 404, data => data.code === 'PRODUCT_NOT_FOUND');
for (const route of ['/api/profile', '/api/cart', '/api/wishlist', '/api/notifications', '/api/admin/orders']) {
    await apiCheck(`Unauthenticated ${route}`, route, 401, data => data.code === 'UNAUTHORIZED');
}

async function browserChecks() {
    if (!cdp || !readyPages.length) {
        record('Rendered browser checks', 'BLOCKED', !readyPages.length
            ? 'No application pages available; cannot verify responsive/keyboard/validation/API error UI'
            : 'Pass --cdp=<local Chromium debugging origin> to check rendered pages');
        return;
    }
    let socket, target;
    const pending = new Map();
    let serial = 0;
    try {
        const response = await fetch(cdp + '/json/new?about:blank', { method: 'PUT', signal: AbortSignal.timeout(10000) });
        if (!response.ok) throw Error('Cannot create browser target');
        target = await response.json();
        const wsUrl = new URL(target.webSocketDebuggerUrl);
        if (!['ws:', 'wss:'].includes(wsUrl.protocol) || !['localhost', '127.0.0.1', '[::1]'].includes(wsUrl.hostname)) throw Error('Nonlocal CDP socket');
        socket = new WebSocket(wsUrl.href);
        await new Promise((resolve, reject) => {
            const timer = setTimeout(() => reject(Error('CDP connection timed out')), 10000);
            socket.onopen = () => { clearTimeout(timer); resolve(); };
            socket.onerror = () => { clearTimeout(timer); reject(Error('CDP connection failed')); };
        });
        const runtimeErrors = [];
        socket.onmessage = event => {
            const value = JSON.parse(event.data);
            if (value.method === 'Runtime.exceptionThrown') runtimeErrors.push(value.params.exceptionDetails.text);
            const wait = pending.get(value.id);
            if (wait) {
                clearTimeout(wait.timer); pending.delete(value.id);
                value.error ? wait.reject(Error(value.error.message)) : wait.resolve(value.result);
            }
        };
        const send = (method, params = {}) => new Promise((resolve, reject) => {
            const id = ++serial;
            const timer = setTimeout(() => { pending.delete(id); reject(Error('CDP command timed out')); }, 15000);
            pending.set(id, { resolve, reject, timer });
            socket.send(JSON.stringify({ id, method, params }));
        });
        const evaluate = async expression => {
            const value = await send('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true });
            if (value.exceptionDetails) throw Error('Evaluation failed');
            return value.result.value;
        };
        await send('Page.enable');
        await send('Runtime.enable');
        await send('Page.setLifecycleEventsEnabled', { enabled: true });
        for (const route of readyPages) {
            runtimeErrors.length = 0;
            await send('Page.navigate', { url: base + route });
            let ready = false;
            for (let attempt = 0; attempt < 50; attempt++) {
                ready = await evaluate(`location.pathname === ${JSON.stringify(route)} && document.readyState === 'complete' && !!document.querySelector('main')`);
                if (ready) break;
                await new Promise(resolve => setTimeout(resolve, 100));
            }
            if (!ready) { record(`Browser ${route}`, 'BLOCKED', 'Target page did not finish rendering or redirected'); continue; }
            for (const width of [1440, 768, 390]) {
                await send('Emulation.setDeviceMetricsOverride', { width, height: 1000, deviceScaleFactor: 1, mobile: width === 390 });
                const overflow = await evaluate('document.documentElement.scrollWidth > innerWidth + 1');
                record(`Responsive ${route} ${width}`, overflow ? 'FAIL' : 'PASS', 'Horizontal overflow check on actual rendered page');
                const screenshot = await send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true });
                fs.writeFileSync(path.join(path.dirname(output), (route === '/' ? 'home' : route.slice(1).replaceAll('/', '-')) + `-${width}.png`), Buffer.from(screenshot.data, 'base64'));
            }
            const unnamed = await evaluate(`Array.from(document.querySelectorAll('input:not([type=hidden]), select, textarea')).filter(el => el.getClientRects().length && !el.labels?.length && !el.getAttribute('aria-label') && !el.getAttribute('aria-labelledby')).length`);
            record(`Labels ${route}`, unnamed ? 'FAIL' : 'PASS', `${unnamed} visible form controls without a label`);
            await evaluate('document.activeElement?.blur()');
            await send('Input.dispatchKeyEvent', { type: 'keyDown', key: 'Tab', code: 'Tab', windowsVirtualKeyCode: 9 });
            await send('Input.dispatchKeyEvent', { type: 'keyUp', key: 'Tab', code: 'Tab', windowsVirtualKeyCode: 9 });
            const focus = await evaluate(`(() => { const el = document.activeElement; const style = getComputedStyle(el); return el !== document.body && el !== document.documentElement && (style.outlineStyle !== 'none' || style.boxShadow !== 'none'); })()`);
            record(`Keyboard focus ${route}`, focus ? 'PASS' : 'FAIL', 'Tab reaches an element with a visible focus style');
            if (route === '/login' || route === '/register') {
                const required = await evaluate(`(() => { const form = document.querySelector('form[data-auth]'); form.reset(); return !form.reportValidity(); })()`);
                record(`Required fields ${route}`, required ? 'PASS' : 'FAIL', 'Actual browser rejects empty form without an API request');
                const email = await evaluate(`(() => { const el = document.querySelector('form[data-auth] input[name=email]'); el.value = 'not-an-email'; return el.validity.typeMismatch; })()`);
                record(`Email validation ${route}`, email ? 'PASS' : 'FAIL', 'Actual browser rejects invalid email');
                if (route === '/register') {
                    await evaluate(`(() => {
                        const form = document.querySelector('form[data-auth]');
                        for (const [name, value] of Object.entries({fullName:'QA review',email:'qa04@example.test',password:'test-only-password',confirmPassword:'different-password'})) form.elements.namedItem(name).value=value;
                        form.requestSubmit();
                    })()`);
                    await new Promise(resolve => setTimeout(resolve, 50));
                    const mismatch = await evaluate(`document.querySelector('[data-auth-status]').textContent.includes('khớp') && !document.querySelector('form[data-auth]').dataset.busy`);
                    record('Register mismatch', mismatch ? 'PASS' : 'FAIL', 'Client rejects mismatched passwords and unlocks submit before any registration API call');
                }
            }
            if (route === '/products') {
                const menuExists = await evaluate(`!!document.querySelector('[data-bs-target="#mobileNavDrawer"]') && !!window.bootstrap`);
                if (menuExists) {
                    await evaluate(`document.querySelector('[data-bs-target="#mobileNavDrawer"]').click()`);
                    await new Promise(resolve => setTimeout(resolve, 350));
                    const opened = await evaluate(`document.querySelector('#mobileNavDrawer').classList.contains('show')`);
                    await send('Input.dispatchKeyEvent', { type: 'keyDown', key: 'Escape', code: 'Escape', windowsVirtualKeyCode: 27 });
                    await send('Input.dispatchKeyEvent', { type: 'keyUp', key: 'Escape', code: 'Escape', windowsVirtualKeyCode: 27 });
                    await new Promise(resolve => setTimeout(resolve, 350));
                    const closed = await evaluate(`!document.querySelector('#mobileNavDrawer').classList.contains('show') && document.activeElement === document.querySelector('[data-bs-target="#mobileNavDrawer"]')`);
                    record('Mobile menu Escape/focus', opened && closed ? 'PASS' : 'FAIL', 'Open mobile menu, Escape closes it and restores trigger focus');
                } else record('Mobile menu Escape/focus', 'BLOCKED', 'Menu trigger or Bootstrap unavailable');
            }
            record(`Runtime ${route}`, runtimeErrors.length ? 'FAIL' : 'PASS', `${runtimeErrors.length} uncaught browser exceptions`);
        }
    } catch (error) { record('Rendered browser checks', 'BLOCKED', error.message); }
    finally {
        for (const wait of pending.values()) { clearTimeout(wait.timer); wait.reject(Error('CDP closed')); }
        socket?.close();
        if (target?.id) await fetch(cdp + '/json/close/' + encodeURIComponent(target.id)).catch(() => {});
    }
}

fs.mkdirSync(path.dirname(output), { recursive: true });
await browserChecks();
for (const flow of ['Login/register validation and server errors', 'JWT customer ownership and ADMIN role UI',
    'Cart/wishlist interaction against real API', 'Checkout quote/place and history', 'WebSocket disconnect/reconnect and fresh REST state']) {
    record(flow, 'BLOCKED', 'Outside this read-only runner; use storefront-browser.mjs and qa04-risks.mjs for real authenticated coverage');
}
const summary = { PASS: 0, FAIL: 0, BLOCKED: 0 };
for (const value of cases) summary[value.status]++;
const status = summary.FAIL ? 'FAIL' : summary.BLOCKED ? 'BLOCKED' : 'PASS';
fs.writeFileSync(output, JSON.stringify({ task: 'QA-04', base, timestamp: new Date().toISOString(), status, summary, cases,
    limitations: ['Read-only live checks; no production data', 'REST preflight is not UI integration coverage',
        'No mocked APIs', 'Interactive authenticated flows require a separate manual run; never auto-marked PASS'] }, null, 2) + '\n');
console.log(JSON.stringify({ status, summary, report: output }));
process.exitCode = status === 'FAIL' ? 1 : status === 'BLOCKED' ? 2 : 0;
