// Explicit development accounts only. Private fixture JSON is never committed.
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { randomUUID } from 'node:crypto';
import { createBrowser } from './browser-harness.mjs';

const [base, cdp, fixtureFile, outputDir] = process.argv.slice(2);
for (const value of [base, cdp]) assert.ok(['localhost', '127.0.0.1'].includes(new URL(value).hostname));
const fixture = JSON.parse(fs.readFileSync(fixtureFile, 'utf8'));
const output = path.resolve(outputDir || 'target/support-chat-browser');
fs.mkdirSync(output, { recursive: true });
const browsers = [];
const customer = await createBrowser({ base, cdp, browsers });
const staff = await createBrowser({ base, cdp, browsers });
const account = fixture.employees.find(item => item.account.storeId === fixture.conversations[0].storeId).account;
const storeId = account.storeId;
const text = 'Khách hỏi ' + randomUUID(), reply = 'Nhân viên trả lời ' + randomUUID();
try {
    await customer.login(fixture.email, fixture.password);
    assert.equal(await customer.evaluate(`getComputedStyle(document.querySelector('#support-chat-panel')).display`), 'none');
    await customer.evaluate(`document.querySelector('.support-chat__trigger').click()`);
    await customer.wait(`document.querySelector('#support-store').options.length === 4`);
    await customer.evaluate(`document.querySelector('#support-store').value=${JSON.stringify(String(storeId))};document.querySelector('#support-store').dispatchEvent(new Event('change'))`);
    await customer.wait(`!document.querySelector('#support-chat-input').disabled`);
    assert.ok((await customer.evaluate(`document.querySelector('#support-chat-state').textContent`)).includes('NV-'));

    await staff.navigate('/login');
    await staff.fill('[data-auth=login]', { email: account.email, password: account.password });
    await staff.wait(`location.pathname === '/admin/support' && document.querySelector('[data-support-list] button')`);
    await staff.evaluate(`document.querySelector('[data-support-list] button').click()`);
    await staff.wait(`!document.querySelector('[data-support-form]').hidden`);
    await customer.wait(`window.__qaSockets.some(socket=>socket.readyState===1)`);
    await staff.wait(`window.__qaSockets.some(socket=>socket.readyState===1)`);
    for (const browser of [customer, staff]) await browser.evaluate(`window.__qaSupportMessages=0;window.__qaSockets.forEach(socket=>socket.addEventListener('message',event=>{if(String(event.data).includes('SUPPORT_MESSAGE_CREATED'))window.__qaSupportMessages++}))`);
    await customer.evaluate(`document.querySelector('#support-chat-input').value=${JSON.stringify(text)};document.querySelector('#support-chat-form').requestSubmit()`);
    await staff.wait(`document.querySelector('[data-support-detail]').textContent.includes(${JSON.stringify(text)})`);
    await staff.evaluate(`document.querySelector('#employee-support-input').value=${JSON.stringify(reply)};document.querySelector('[data-support-form]').requestSubmit()`);
    await customer.wait(`document.querySelector('#support-chat-messages').textContent.includes(${JSON.stringify(reply)})`);
    assert.equal(await customer.evaluate(`Array.from(document.querySelectorAll('#support-chat-messages p')).filter(item=>item.textContent.includes(${JSON.stringify(reply)})).length`), 1);
    assert.ok(customer.stomp.some(item => item.command === 'SUBSCRIBE'));
    assert.ok(staff.stomp.some(item => item.command === 'SUBSCRIBE'));
    assert.ok(await customer.evaluate('window.__qaSupportMessages > 0'));
    assert.ok(await staff.evaluate('window.__qaSupportMessages > 0'));

    // Drop the native socket. REST resync must preserve the reply after reconnection.
    await customer.evaluate(`window.__qaSockets.forEach(socket=>socket.close())`);
    await customer.wait(`window.__qaSockets.filter(socket=>socket.readyState===1).length===1`);
    await customer.wait(`document.querySelector('#support-chat-messages').textContent.includes(${JSON.stringify(reply)})`);

    await customer.send('Emulation.setDeviceMetricsOverride', { width: 390, height: 844, deviceScaleFactor: 1, mobile: true });
    assert.ok(await customer.evaluate(`document.documentElement.scrollWidth <= innerWidth`));
    let screenshot = await customer.send('Page.captureScreenshot', { format: 'png' });
    fs.writeFileSync(path.join(output, 'customer-mobile.png'), Buffer.from(screenshot.data, 'base64'));
    await staff.send('Emulation.setDeviceMetricsOverride', { width: 390, height: 844, deviceScaleFactor: 1, mobile: true });
    assert.ok(await staff.evaluate(`document.documentElement.scrollWidth <= innerWidth`));
    screenshot = await staff.send('Page.captureScreenshot', { format: 'png' });
    fs.writeFileSync(path.join(output, 'employee-mobile.png'), Buffer.from(screenshot.data, 'base64'));

    await staff.evaluate(`document.querySelector('[data-support-close]').click()`);
    await staff.wait(`document.querySelector('[data-support-form]').hidden`);
    await customer.evaluate(`document.querySelector('#support-chat-input').value='closed';document.querySelector('#support-store').dispatchEvent(new Event('change'))`);
    await customer.wait(`!document.querySelector('#support-chat-input').disabled`);
    assert.deepEqual(customer.errors, []); assert.deepEqual(staff.errors, []);
    console.log('PASS: employee login redirect, 3 branches, two-way chat, private STOMP, no duplicate messages, reconnect resync, close/new conversation and mobile overflow.');
} finally {
    await Promise.all(browsers.map(browser => browser.close()));
}
