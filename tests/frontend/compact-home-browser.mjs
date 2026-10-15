// Run only with the disposable H2 storefront fixture and a separate headless browser.
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { randomUUID } from 'node:crypto';
import { createBrowser } from './browser-harness.mjs';

const base = process.argv[2] || 'http://127.0.0.1:18082';
const cdp = process.argv[3] || 'http://127.0.0.1:9240';
const output = path.resolve(process.argv[4] || 'target/compact-home');
for (const value of [base, cdp]) {
    const url = new URL(value);
    assert.ok(['localhost', '127.0.0.1'].includes(url.hostname) && !url.username && !url.password);
}
const fixture = await (await fetch(base + '/api/products/100')).json();
assert.equal(fixture.slug, 'qa-keyboard-100', 'Disposable fixture required');
fs.mkdirSync(output, { recursive: true });
const browser = await createBrowser({ base, cdp });
const checks = [];
const check = (value, name) => { assert.ok(value, name); checks.push(name); };
try {
    await browser.send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 1000, deviceScaleFactor: 1, mobile: false });
    await browser.navigate('/');
    await browser.wait(`document.querySelector('[data-countdown-seconds]').textContent !== '--'`);
    await browser.evaluate('document.fonts.ready');
    check(await browser.evaluate(`Array.from(document.querySelectorAll('.home-hero img')).every(img => img.complete && img.naturalWidth > 0)`), 'Hero assets loaded');
    check(await browser.evaluate(`getComputedStyle(document.querySelector('.home-hero')).gridTemplateColumns.split(' ').length === 2`), 'Desktop hero has two columns');
    check(await browser.evaluate(`document.querySelectorAll('[role=tabpanel]:not([hidden])').length === 1`), 'One initial panel');
    check(await browser.evaluate(`document.querySelectorAll('#panel-featured .product-card').length <= 4`), 'Featured preview is limited to four cards');
    check(await browser.evaluate(`Array.from(document.querySelectorAll('.home-compact .product-card')).every(card => card.querySelector('[data-action="add-to-cart"][data-variant-id]') && card.querySelector('[data-action="toggle-wishlist"]'))`), 'Cart variant and wishlist actions preserved');
    const saleCount = await browser.evaluate(`document.querySelectorAll('.home-sale .product-card').length`);
    if (saleCount > 0) {
        check(saleCount <= 4, 'Sale preview is limited to four cards');
        check(await browser.evaluate(`Array.from(document.querySelectorAll('.home-sale .product-card')).every(card => card.querySelector('.badge-discount') && card.querySelector('del.price-original'))`), 'Real sale prices render discount badges and original prices');
    }
    await browser.evaluate(`document.querySelector('#tab-bestsellers').click()`);
    check(await browser.evaluate(`!document.querySelector('#panel-bestsellers').hidden && document.querySelector('#panel-bestsellers .home-empty')`), 'Best sellers empty state');
    await browser.evaluate(`document.querySelector('#tab-latest').focus()`);
    await browser.send('Input.dispatchKeyEvent', { type: 'keyDown', key: 'Home', code: 'Home' });
    await browser.send('Input.dispatchKeyEvent', { type: 'keyUp', key: 'Home', code: 'Home' });
    check(await browser.evaluate(`document.activeElement.id === 'tab-featured' && !document.querySelector('#panel-featured').hidden`), 'Keyboard tab selection');
    const email = 'compact-' + randomUUID() + '@example.test', password = randomUUID() + 'Aa1!';
    const registered = await fetch(base + '/api/auth/register', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ fullName: 'QA Giao diện', email, password }) });
    check(registered.status === 201, 'Disposable customer registration');
    await browser.login(email, password);
    await browser.evaluate(`document.querySelector('#panel-featured [data-action="add-to-cart"]').click()`);
    await browser.wait(`document.querySelector('#cartCount').textContent === '1'`);
    check(true, 'Home quick add updates the real cart badge');
    await browser.evaluate(`document.querySelector('#panel-featured [data-action="toggle-wishlist"]').click()`);
    await browser.wait(`document.querySelector('#panel-featured [data-action="toggle-wishlist"]').classList.contains('active')`);
    check(true, 'Home wishlist button updates after the real API response');
    await browser.wait(`!document.querySelector('.toast-msg')`);
    const position = await browser.evaluate(`(()=>{const r=document.querySelector('.account-trigger').getBoundingClientRect();return {x:r.x+r.width/2,y:r.y+r.height/2}})()`);
    await browser.send('Input.dispatchMouseEvent', { type: 'mouseMoved', ...position });
    await browser.wait(`document.querySelector('[data-account-dropdown]').open`);
    check(await browser.evaluate(`(()=>{const p=document.querySelector('.account-panel'),r=p.getBoundingClientRect();return p.contains(document.elementFromPoint(r.x+20,r.y+20))})()`), 'Account panel is above navigation');
    check(await browser.evaluate(`document.querySelector('.account-identity').textContent.includes('QA Giao diện') && document.querySelector('.account-panel [data-admin-link]').hidden`), 'Customer identity and ADMIN visibility');
    for (const width of [1440, 768, 390]) {
        await browser.send('Emulation.setDeviceMetricsOverride', { width, height: 1000, deviceScaleFactor: 1, mobile: width < 576 });
        check(await browser.evaluate(`document.documentElement.scrollWidth <= innerWidth + 1`), 'No page overflow at ' + width);
        await browser.evaluate(`document.querySelector('[data-account-dropdown]').open=true`);
        check(await browser.evaluate(`(()=>{const r=document.querySelector('.account-panel').getBoundingClientRect();return r.left>=0 && r.right<=innerWidth+1})()`), 'Account panel fits at ' + width);
        const menuShot = await browser.send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true });
        fs.writeFileSync(path.join(output, 'home-account-' + width + '.png'), Buffer.from(menuShot.data, 'base64'));
        await browser.evaluate(`document.querySelector('[data-account-dropdown]').open=false`);
        const shot = await browser.send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true });
        fs.writeFileSync(path.join(output, 'home-' + width + '.png'), Buffer.from(shot.data, 'base64'));
    }
    check(browser.errors.length === 0, 'No browser exceptions');
    fs.writeFileSync(path.join(output, 'summary.json'), JSON.stringify({ status: 'PASS', checks }, null, 2));
    console.log('PASS:', checks.length, 'browser checks; screenshots:', output);
} finally { await browser.close(); }
