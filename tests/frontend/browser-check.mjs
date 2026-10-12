// Run against a rendered preview and a local Chromium debugging endpoint.
// Node 22+ provides fetch and WebSocket; no npm dependencies required.
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';

const previewUrl = process.argv[2] || 'http://127.0.0.1:9340/__preview';
const debuggingUrl = process.argv[3] || 'http://127.0.0.1:9237';
const outputDir = process.argv[4];
const tabs = await (await fetch(debuggingUrl + '/json')).json();
const tab = tabs.find(value => value.type === 'page');
assert.ok(tab, 'A Chromium page must be available');
const socket = new WebSocket(tab.webSocketDebuggerUrl);
await new Promise((resolve, reject) => { socket.onopen = resolve; socket.onerror = reject; });
const pending = new Map();
let serial = 0;
const errors = [];
socket.onmessage = event => {
  const message = JSON.parse(event.data);
  if (message.method === 'Runtime.exceptionThrown') errors.push(message.params.exceptionDetails.text);
  if (message.id && pending.has(message.id)) {
    const task = pending.get(message.id);
    pending.delete(message.id);
    message.error ? task.reject(Error(JSON.stringify(message.error))) : task.resolve(message.result);
  }
};
const send = (method, params = {}) => new Promise((resolve, reject) => {
  const id = ++serial;
  pending.set(id, {resolve, reject});
  socket.send(JSON.stringify({id, method, params}));
});
const evaluate = async expression => {
  const response = await send('Runtime.evaluate', {expression, returnByValue:true, awaitPromise:true});
  assert.equal(response.exceptionDetails, undefined, JSON.stringify(response.exceptionDetails));
  return response.result.value;
};
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));
try {
  await send('Page.enable');
  await send('Runtime.enable');
  await send('Page.navigate', {url:previewUrl});
  let ready = false;
  for (let index = 0; index < 100; index++) {
    ready = await evaluate("document.readyState === 'complete' && document.querySelector('[data-demo-status]')?.textContent.includes('sẵn sàng')");
    if (ready) break;
    await delay(100);
  }
  assert.ok(ready, 'Preview and API-01 fixture must load');
  assert.ok(await evaluate("getComputedStyle(document.body).getPropertyValue('--bs-body-font-family').trim().length > 0"), 'Bootstrap must load');
  for (const width of [1440, 390]) {
    await send('Emulation.setDeviceMetricsOverride', {width, height:1000, deviceScaleFactor:1, mobile:false});
    assert.ok(await evaluate('document.documentElement.scrollWidth <= innerWidth'), `No overflow at ${width}px`);
    if (outputDir) {
      fs.mkdirSync(outputDir, {recursive:true});
      const screenshot = await send('Page.captureScreenshot', {format:'png',captureBeyondViewport:true});
      fs.writeFileSync(path.join(outputDir, `ui-01-${width}.png`), Buffer.from(screenshot.data,'base64'));
    }
  }
  assert.equal(await evaluate("getComputedStyle(document.querySelector('#site-navigation')).display"), 'none');
  await evaluate("document.querySelector('[data-menu-toggle]').click()");
  assert.equal(await evaluate("document.querySelector('[data-menu-toggle]').getAttribute('aria-expanded')"), 'true');
  assert.notEqual(await evaluate("getComputedStyle(document.querySelector('#site-navigation')).display"), 'none');
  await send('Input.dispatchKeyEvent', {type:'keyDown',key:'Escape',code:'Escape',windowsVirtualKeyCode:27});
  assert.equal(await evaluate("document.querySelector('[data-menu-toggle]').getAttribute('aria-expanded')"), 'false');
  assert.ok(await evaluate("document.activeElement.matches('[data-menu-toggle]')"), 'Escape restores focus');
  assert.ok(await evaluate("document.activeElement.matches(':focus-visible') && getComputedStyle(document.activeElement).outlineStyle !== 'none'"), 'Keyboard focus remains visible');
  await evaluate("UTEGear.Toast.show('<img src=x onerror=alert(1)>', 'error', 1)");
  await delay(30);
  assert.equal(await evaluate("document.querySelector('.toast-msg img')"), null, 'Toast cannot inject HTML');
  assert.equal(await evaluate("document.querySelector('.toast-msg').getAttribute('role')"), 'alert');
  assert.ok(await evaluate("document.querySelector('.toast-message').textContent.includes('<img')"), 'Message remains literal text');
  await evaluate("document.querySelector('.toast-dismiss').click()");
  assert.equal(await evaluate("document.querySelector('.toast-msg')"), null, 'Dismiss removes the toast');
  assert.equal(await evaluate("document.querySelector('[data-pagination]').hidden"), true, 'Single-page fixture hides pagination');
  await evaluate(`(() => {
    globalThis.pageChanges=[];
    const page={...UTEGear.MockData.get('productsPage'),page:98,totalPages:100,first:false,last:false};
    UTEGear.Pagination.render(document.querySelector('[data-pagination]'),page,index=>pageChanges.push(index));
  })()`);
  assert.equal(await evaluate("document.querySelectorAll('[data-pagination] button').length"), 7, 'Pagination stays bounded');
  assert.equal(await evaluate("document.querySelector('[aria-current=page]').textContent"), '99');
  await evaluate("document.querySelector('[data-pagination] li:last-child button').click()");
  assert.deepEqual(await evaluate('pageChanges'), [99], 'Next sends zero-based page');
  await evaluate(`UTEGear.Pagination.render(document.querySelector('[data-pagination]'),{...UTEGear.MockData.get('productsPage'),page:99,totalPages:100,first:false,last:true},()=>{})`);
  assert.equal(await evaluate("document.querySelector('[data-pagination] li:last-child button').disabled"), true);
  await evaluate("document.querySelector('[data-demo=empty]').click()");
  assert.equal(await evaluate("document.querySelector('[data-demo-empty]').hidden"), false);
  await send('Emulation.setEmulatedMedia', {features:[{name:'prefers-reduced-motion',value:'reduce'}]});
  await evaluate("const loader=document.createElement('div');loader.className='skeleton';document.body.append(loader)");
  assert.ok(parseFloat(await evaluate("getComputedStyle(document.querySelector('.skeleton')).animationDuration")) <= 0.001);
  assert.deepEqual(errors, [], 'No uncaught browser errors');
  console.log('PASS: Bootstrap layout at 1440/390px, menu/Escape/focus, safe persistent toast, pagination boundaries, mock empty state and reduced motion');
} finally {
  socket.close();
}
