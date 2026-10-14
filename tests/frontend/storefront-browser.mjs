// Real browser/API tests. Only run against the disposable H2 QA launcher/fixture.
import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
const base = process.argv[2] || 'http://127.0.0.1:18081', cdp = process.argv[3] || 'http://127.0.0.1:9238';
const output = path.resolve(process.argv[4] || 'target/frontend-browser');
for (const origin of [base, cdp]) { const url = new URL(origin); assert.ok(['127.0.0.1','localhost'].includes(url.hostname) && !url.username && !url.password, 'Local test origins only'); }
const checks = [], browsers = [];
function check(value, name) { assert.ok(value, name); checks.push({ name, status: 'PASS' }); }
function fail(name, detail) { checks.push({ name, status: 'FAIL', detail }); }
async function api(route, { method = 'GET', token, body } = {}) {
    const response = await fetch(base + route, { method, headers: { ...(body ? { 'Content-Type':'application/json' } : {}), ...(token ? { Authorization: 'Bearer ' + token } : {}) }, body: body ? JSON.stringify(body) : undefined });
    let data = null; if (response.status !== 204) data = await response.json();
    return { response, data };
}
async function createBrowser() {
    const target = await (await fetch(cdp + '/json/new?about:blank', { method: 'PUT' })).json();
    const socket = new WebSocket(target.webSocketDebuggerUrl), pending = new Map(); let serial = 0;
    await new Promise((resolve,reject) => { socket.onopen=resolve; socket.onerror=reject; });
    const errors = [], stomp = [];
    socket.onmessage = event => {
        const value = JSON.parse(event.data); if (value.method === 'Runtime.exceptionThrown') errors.push(value.params.exceptionDetails.text);
        if (value.method === 'Network.webSocketFrameSent' || value.method === 'Network.webSocketFrameReceived') {
            const frame = value.params.response.payloadData;
            // Retain only protocol evidence. Never retain raw frames or JWT header values.
            const command = frame.split('\n')[0].trim();
            if (['CONNECT', 'CONNECTED', 'SUBSCRIBE', 'ERROR'].includes(command)) stomp.push({ command, hasAuthorization: command === 'CONNECT' && /\nAuthorization:Bearer /i.test(frame) });
        }
        const wait = pending.get(value.id); if (wait) { clearTimeout(wait.timer); pending.delete(value.id); value.error ? wait.reject(Error(value.error.message)) : wait.resolve(value.result); }
    };
    const send = (method, params = {}) => new Promise((resolve,reject) => {
        const id=++serial, timer=setTimeout(()=>{pending.delete(id);reject(Error('CDP timeout'));},15000);
        pending.set(id,{resolve,reject,timer}); socket.send(JSON.stringify({id,method,params}));
    });
    const evaluate = async expression => {
        const response=await send('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});
        if(response.exceptionDetails) throw Error('Browser evaluation failed'); return response.result.value;
    };
    const wait = async expression => {
        for(let i=0;i<100;i++){try { if(await evaluate(expression))return; } catch {} await new Promise(resolve=>setTimeout(resolve,100));}
        throw Error('UI state timeout: ' + expression.slice(0,100));
    };
    const navigate = async (route, expected = new URL(route,base).pathname) => {
        await send('Page.navigate',{url:base+route});
        await wait(`location.pathname === ${JSON.stringify(expected)} && document.readyState === 'complete' && !!window.UTEGear?.Views`);
    };
    const fill = (selector, values) => evaluate(`(()=>{const form=document.querySelector(${JSON.stringify(selector)});for(const [name,value] of Object.entries(${JSON.stringify(values)}))form.elements.namedItem(name).value=value;form.requestSubmit();})()`);
    const login = async (email,password) => { await navigate('/login'); await fill('[data-auth=login]',{email,password}); await wait(`location.pathname === '/' && !!window.UTEGear?.Session.get() && !document.querySelector('[data-session-controls]').hidden`); };
    await send('Page.enable'); await send('Runtime.enable'); await send('Network.enable');
    const browser={send,evaluate,wait,navigate,fill,login,errors,stomp,close:async()=>{for(const task of pending.values())clearTimeout(task.timer);socket.close();await fetch(cdp+'/json/close/'+target.id).catch(()=>{});}};
    browsers.push(browser); return browser;
}
fs.mkdirSync(output,{recursive:true});
try {
    const seed=await api('/api/products/100'); check(seed.data?.slug==='qa-keyboard-100','Isolated H2 seed present');
    const email='qa-'+randomUUID()+'@example.test', password=randomUUID()+'Ab1!';
    const registered=await api('/api/auth/register',{method:'POST',body:{fullName:'QA Browser',email,password,phone:null}}); check(registered.response.status===201,'Real CUSTOMER registration');
    const A=await createBrowser(); await A.login(email,password); check(true,'Real login form and session/header');
    await A.navigate('/products'); check(await A.evaluate(`document.querySelector('.product-card').textContent.includes('QA Keyboard <safe>')`),'Catalog DTO and text escaping');
    await A.navigate('/products/100'); await A.wait(`document.querySelector('[data-product-availability]').textContent.includes('QA Store')`);
    check(await A.evaluate(`document.querySelector('main').textContent.includes('<script>literal</script>') && !document.querySelector('main script')`),'Description is escaped text');
    await A.evaluate(`document.getElementById('quantity').value='2';document.querySelector('[data-detail-add]').click()`); await A.wait(`!!document.querySelector('.toast-msg')`);
    await A.navigate('/cart'); await A.wait(`!!document.querySelector('[data-view-items] input')`);
    check(await A.evaluate(`document.querySelector('[data-view-total]').textContent.includes('1.000.000')`),'Cart hydrates real server subtotal');
    await A.evaluate(`document.querySelector('[data-view-items] input').value='3';Array.from(document.querySelectorAll('[data-view-items] button')).find(el=>el.textContent==='Cập nhật').click()`);
    await A.wait(`document.querySelector('[data-view-total]').textContent.includes('1.500.000') || document.querySelector('[data-view-status]').classList.contains('text-danger')`);
    if(await A.evaluate(`document.querySelector('[data-view-total]').textContent.includes('1.500.000')`))check(true,'Cart PUT success');
    else { fail('BE cart PUT','PUT cart returned an error; FE restores server quantity, no fake success');check(await A.evaluate(`document.querySelector('[data-view-items] input').value==='2'`),'Cart input rollback on failed PUT'); }
    await A.navigate('/products');await A.evaluate(`document.querySelector('[data-action=toggle-wishlist]').click()`);await A.wait(`document.querySelector('[data-action=toggle-wishlist]').getAttribute('aria-pressed')==='true'`);
    await A.navigate('/wishlist');await A.wait(`document.querySelector('[data-view-items]').textContent.includes('QA Keyboard')`);check(true,'Wishlist API hydration');
    await A.evaluate(`Array.from(document.querySelectorAll('[data-view-items] button')).find(el=>el.textContent==='Bỏ yêu thích').click()`);await A.wait(`document.querySelector('[data-view-status]').textContent.includes('đang trống')`);check(true,'Wishlist delete');
    await A.navigate('/profile');await A.wait(`!document.querySelector('[data-profile-management]').hidden`);
    await A.fill('[data-profile-form]',{fullName:'QA Updated',phone:'0900000198'});await A.wait(`document.querySelector('[data-session-name]').textContent==='QA Updated'`);check(true,'Profile PATCH updates profile/session/header');
    const address={fullName:'QA Receiver',phone:'0900000101',street:'QA Street',ward:'QA Ward',district:'QA District',province:'QA Province',label:'QA Home'};
    await A.fill('[data-address-form]',address);await A.wait(`document.querySelector('[data-address-items]').textContent.includes('QA Street')`);check(true,'Create address through real form');
    await A.evaluate(`document.querySelector('[data-address-reset]').click()`);await A.fill('[data-address-form]',{...address,street:'QA Second Street'});await A.wait(`document.querySelectorAll('[data-address-items] article').length===2`);
    await A.evaluate(`Array.from(document.querySelectorAll('[data-address-items] button')).find(el=>el.textContent==='Đặt mặc định').click()`);await A.wait(`document.querySelector('[data-address-items]').textContent.includes('mặc định')`);check(true,'Address default API');
    await A.evaluate(`document.querySelector('[data-address-items] article button').click()`);await A.fill('[data-address-form]',{...address,street:'QA Updated Street'});await A.wait(`document.querySelector('[data-address-items]').textContent.includes('QA Updated Street')`);check(true,'Address PUT edit');
    await A.evaluate(`Array.from(document.querySelectorAll('[data-address-items] article'))[1].querySelectorAll('button')[1].click()`);await A.wait(`document.querySelectorAll('[data-address-items] article').length===1`);check(true,'Address DELETE');
    await A.navigate('/admin/orders');await A.wait(`document.querySelector('[data-view-status]').classList.contains('text-danger')`);check(await A.evaluate(`!!UTEGear.Session.get()`),'Customer ADMIN denial preserves session (403)');
    await A.navigate('/checkout');await A.wait(`!document.querySelector('[data-quote]').disabled`);
    await A.evaluate(`document.querySelector('[name=addressId]').selectedIndex=1;document.querySelector('[name=fulfillmentStoreId]').value='100';document.querySelector('[data-quote]').click()`);await A.wait(`!document.querySelector('[data-place]').disabled`);check(true,'Real checkout quote');
    await A.evaluate(`document.querySelector('[name=note]').value='QA changed';document.querySelector('[name=note]').dispatchEvent(new Event('input',{bubbles:true}))`);check(await A.evaluate(`document.querySelector('[data-place]').disabled`),'Changing checkout invalidates quote');
    await A.evaluate(`document.querySelector('[data-quote]').click()`);await A.wait(`!document.querySelector('[data-place]').disabled`);await A.evaluate(`document.querySelector('[data-place]').click()`);await A.wait(`document.querySelector('[data-view-status]').textContent.includes('Đã đặt đơn')`);check(true,'Real order placement');
    const orderId=await A.evaluate(`Number(new URL(Array.from(document.querySelectorAll('a')).find(el=>el.textContent==='Xem đơn hàng').href).searchParams.get('focus'))`);
    await A.navigate('/orders/'+orderId,'/orders');await A.wait(`document.querySelector('[data-order-detail]').textContent.includes('QA Keyboard')`);check(true,'Redirect focus and authenticated order detail');
    await A.wait(`document.querySelector('[data-realtime-status]').textContent.includes('Đã kết nối') || document.querySelector('[data-realtime-status]').textContent.includes('bị từ chối') || document.querySelector('[data-realtime-status]').textContent.includes('Chưa tải')`);
    const connected=await A.evaluate(`document.querySelector('[data-realtime-status]').textContent.includes('Đã kết nối')`);
    check(A.stomp.some(frame=>frame.command==='CONNECT' && frame.hasAuthorization),'Customer JWT sent only in STOMP CONNECT');
    if(connected)check(true,'Real customer STOMP CONNECT/subscription/resync');else fail('Customer STOMP',await A.evaluate(`document.querySelector('[data-realtime-status]').textContent`));
    const otherEmail='qa-'+randomUUID()+'@example.test',otherPassword=randomUUID()+'Ab1!';await api('/api/auth/register',{method:'POST',body:{fullName:'QA Other',email:otherEmail,password:otherPassword}});
    const other=await api('/api/auth/login',{method:'POST',body:{email:otherEmail,password:otherPassword}});
    const denied=await api('/api/orders/'+orderId,{token:other.data.accessToken});check(denied.response.status===404,'User B cannot read User A order');
    await A.evaluate(`document.getElementById('order-cancel-reason').value='QA cancel';Array.from(document.querySelectorAll('[data-order-detail] button')).find(el=>el.textContent==='Hủy đơn hàng').click()`);await A.wait(`document.querySelector('[data-order-detail]').textContent.includes('CANCELLED')`);check(true,'Real customer cancellation');
    if(process.env.QA_ADMIN_PASSWORD){
        await A.navigate('/products/100');await A.evaluate(`document.querySelector('[data-detail-add]').click()`);await A.wait(`!!document.querySelector('.toast-msg')`);
        await A.navigate('/checkout');await A.wait(`!document.querySelector('[data-quote]').disabled`);
        await A.evaluate(`document.querySelector('[name=addressId]').selectedIndex=1;document.querySelector('[name=fulfillmentStoreId]').value='100';document.querySelector('[data-quote]').click()`);await A.wait(`!document.querySelector('[data-place]').disabled`);await A.evaluate(`document.querySelector('[data-place]').click()`);await A.wait(`document.querySelector('[data-view-status]').textContent.includes('Đã đặt đơn')`);
        const secondId=await A.evaluate(`Number(new URL(Array.from(document.querySelectorAll('a')).find(el=>el.textContent==='Xem đơn hàng').href).searchParams.get('focus'))`);
        const admin=await createBrowser();await admin.login(process.env.QA_ADMIN_EMAIL,process.env.QA_ADMIN_PASSWORD);await admin.navigate('/admin/orders');await admin.wait(`document.querySelector('[data-view-items]').textContent.includes('UTE-')`);check(true,'Real ADMIN order list');
        await admin.wait(`document.querySelector('[data-realtime-status]').textContent.includes('Đã kết nối') || document.querySelector('[data-realtime-status]').textContent.includes('bị từ chối') || document.querySelector('[data-realtime-status]').textContent.includes('Chưa tải')`);
        check(admin.stomp.some(frame=>frame.command==='CONNECT' && frame.hasAuthorization),'Admin JWT sent only in STOMP CONNECT');
        if(await admin.evaluate(`document.querySelector('[data-realtime-status]').textContent.includes('Đã kết nối')`))check(true,'Real ADMIN STOMP CONNECT/subscription/resync');else fail('Admin STOMP',await admin.evaluate(`document.querySelector('[data-realtime-status]').textContent`));
        await admin.navigate('/admin/orders/'+orderId,'/admin/orders');await admin.wait(`document.querySelector('[data-order-detail]').textContent.includes('CANCELLED')`);check(true,'Admin real order detail');
        await admin.navigate('/admin/orders/'+secondId,'/admin/orders');await admin.wait(`!!document.getElementById('order-next-status')`);
        await admin.evaluate(`document.getElementById('order-next-status').value='CONFIRMED';Array.from(document.querySelectorAll('[data-order-detail] button')).find(el=>el.textContent==='Cập nhật trạng thái').click()`);await admin.wait(`document.querySelector('[data-order-detail]').textContent.includes('Trạng thái: CONFIRMED')`);check(true,'Real ADMIN status update');
        await A.navigate('/orders/'+secondId,'/orders');await A.wait(`document.querySelector('[data-order-detail]').textContent.includes('Trạng thái: CONFIRMED')`);check(true,'Customer REST resync shows ADMIN status');
    }
    await A.navigate('/profile');await A.wait(`!document.querySelector('[data-profile-management]').hidden`);const nextPassword=randomUUID()+'Ab1!';await A.fill('[data-password-form]',{currentPassword:password,newPassword:nextPassword,confirmPassword:nextPassword});await A.wait(`location.pathname==='/login' && !window.UTEGear?.Session.get()`);check(true,'Real password update clears local session');
    await A.login(email,nextPassword);check(true,'Login with updated password');
    await A.navigate('/cart');await A.wait(`document.querySelector('[data-view-status]').textContent.includes('đang trống')`);
    await A.send('Emulation.setDeviceMetricsOverride',{width:390,height:900,deviceScaleFactor:1,mobile:true});check(await A.evaluate('document.documentElement.scrollWidth<=innerWidth+1'),'Customer view 390px no overflow');
    const png=await A.send('Page.captureScreenshot',{format:'png',captureBeyondViewport:true});fs.writeFileSync(path.join(output,'cart-390.png'),Buffer.from(png.data,'base64'));
    await A.evaluate(`document.querySelector('[data-session-logout]').click()`);await A.wait(`location.pathname==='/login' && !window.UTEGear?.Session.get()`);check(true,'Logout clears session');
    check(browsers.every(browser=>browser.errors.length===0),'No uncaught browser exceptions');
}catch(error){fail('Test execution',error.message);}
finally{for(const browser of browsers)await browser.close();}
const status=checks.some(item=>item.status==='FAIL')?'FAIL':'PASS';
fs.writeFileSync(path.join(output,'report.json'),JSON.stringify({status,checks,limitations:['Temporary H2 test data; no credentials stored','Full QA-04 still requires all manual and integration risk cases']},null,2));
console.log(`${status}: ${checks.filter(item=>item.status==='PASS').length} PASS, ${checks.filter(item=>item.status==='FAIL').length} FAIL; report: ${output}`);if(status==='FAIL')process.exitCode=1;
