// FE risk/edge-case checks against disposable H2 and real Chromium/API only.
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { randomUUID } from 'node:crypto';
import { createBrowser } from './browser-harness.mjs';
const base=process.argv[2],cdp=process.argv[3],output=path.resolve(process.argv[4]);
for(const origin of [base,cdp])assert.ok(['localhost','127.0.0.1'].includes(new URL(origin).hostname));
const checks=[],browsers=[],qa05MediaRoutes=[];
const check=(value,name)=>{assert.ok(value,name);checks.push({name,status:'PASS'});};
const count=(A,route,method='POST')=>A.requests.filter(r=>r.route===route&&r.method===method).length;
async function api(route,body,token,method=body?'POST':'GET'){
 const response=await fetch(base+route,{method,headers:{...(body?{'Content-Type':'application/json'}:{}),...(token?{Authorization:'Bearer '+token}:{})},body:body?JSON.stringify(body):undefined});
 return {status:response.status,data:response.status===204?null:await response.json()};
}
fs.mkdirSync(output,{recursive:true});
try{
 for(const route of ['/admin/products','/admin/products/200/edit']){const response=await fetch(base+route);qa05MediaRoutes.push({route,httpStatus:response.status});}
 check((await api('/api/products/200')).data.slug==='qa-multi-200','QA multi-variant fixture marker');
 const email='qa-risk-'+randomUUID()+'@example.test',password=randomUUID()+'Ab1!';
 const registered=await api('/api/auth/register',{fullName:'QA Risk',email,password});check(registered.status===201,'Dedicated risk CUSTOMER registered');
 const A=await createBrowser({base,cdp,browsers});
 const formEmail='qa-form-'+randomUUID()+'@example.test';await A.navigate('/register');await A.fill('[data-auth=register]',{fullName:'QA Form Registration',email:formEmail,password,confirmPassword:password});await A.wait(`location.pathname==='/login'`);check((await api('/api/auth/login',{email:formEmail,password})).status===200,'Valid registration form creates real login-capable customer');
 await A.navigate('/register');await A.fill('[data-auth=register]',{fullName:'QA Duplicate',email,password,confirmPassword:password});await A.wait(`!!document.querySelector('[data-auth-status]').textContent && !document.querySelector('form[data-auth]').dataset.busy`);
 check(await A.evaluate(`location.pathname==='/register' && !UTEGear.Session.get() && !document.querySelector('form[data-auth] button[type=submit]').disabled`),'Duplicate registration error unlocks form, no false success');
 await A.navigate('/login');await A.fill('[data-auth=login]',{email,password:'WrongPassword1!'});await A.wait(`!!document.querySelector('[data-auth-status]').textContent && !document.querySelector('form[data-auth]').dataset.busy`);
 check(await A.evaluate(`location.pathname==='/login'&&!UTEGear.Session.get()&&!document.querySelector('button[type=submit]').disabled && document.querySelector('[data-auth-status]').getAttribute('aria-live')==='polite'`),'Real wrong-password API error, unlocked submit and live status');
 await A.login(email,password);
 await A.navigate('/products');await A.evaluate(`document.getElementById('catalog-q').value='QA Multi';document.querySelector('form[action="/products"]').requestSubmit()`);await A.wait(`location.search.includes('QA')&&document.readyState==='complete'&&document.querySelectorAll('.product-card').length===1`);
 check(await A.evaluate(`document.querySelector('.product-card').textContent.includes('QA Multi')`),'SSR search/filter uses real catalog');
 await A.navigate('/products/200');await A.wait(`document.querySelector('[data-product-availability]').textContent.includes('QA Store')`);
 check(await A.evaluate(`document.querySelector('[data-detail-variant]').options.length===3`),'All three variants rendered');
 await A.evaluate(`(()=>{const select=document.querySelector('[data-detail-variant]');select.value='201';select.dispatchEvent(new Event('change'));})()`);await A.wait(`document.querySelector('[data-variant-price]').textContent.includes('650.000')&&document.querySelector('[data-product-availability]').textContent.includes('QA Store B')`);
 check(await A.evaluate(`document.querySelector('[data-detail-add]').dataset.variantId==='201'&&!document.querySelector('[data-product-availability]').textContent.includes('QA Inactive')`),'Blue variant price/store availability; inactive store excluded');
 await A.evaluate(`document.querySelectorAll('[data-gallery-url]')[1].click()`);check(await A.evaluate(`new URL(document.querySelector('#mainProductImage').src).pathname==='/favicon.svg'`),'Gallery thumbnail changes real image');
 await A.evaluate(`(()=>{const select=document.querySelector('[data-detail-variant]');select.value='202';select.dispatchEvent(new Event('change'));})()`);await A.wait(`document.querySelector('[data-product-availability]').textContent.includes('Chưa có hàng')`);
 check(await A.evaluate(`document.querySelector('[data-detail-add]').disabled`),'Out-of-stock variant cannot be added despite inactive-store inventory');
 const inactive=await api('/api/stores/102/availability?variantId=201');check(inactive.status===409&&inactive.data.code==='STORE_INACTIVE','Inactive store rejected by real availability API');
 await A.evaluate(`(()=>{const select=document.querySelector('[data-detail-variant]');select.value='201';select.dispatchEvent(new Event('change'));document.getElementById('quantity').value='1';document.querySelector('[data-detail-add]').click();})()`);await A.wait(`!!document.querySelector('.toast-msg')`);
 await A.navigate('/cart');await A.wait(`!!document.querySelector('[data-view-items] input')`);
 check(await A.evaluate(`document.querySelector('[data-view-items]').textContent.includes('Blue')&&document.querySelector('[data-view-total]').textContent.includes('650.000')`),'Cart persists selected blue variant and server price');
 await A.evaluate(`document.querySelector('[data-view-items] input').value='0';Array.from(document.querySelectorAll('[data-view-items] button')).find(el=>el.textContent==='Cập nhật').click()`);
 check(await A.evaluate(`!document.querySelector('[data-view-items] input').validity.valid`),'Cart quantity zero rejected by browser validation');
 await A.navigate('/profile');await A.wait(`!document.querySelector('[data-profile-management]').hidden`);
 await A.fill('[data-address-form]',{fullName:'QA Risk Receiver',phone:'0900000102',street:'QA Risk Street',ward:'QA Ward',district:'QA District',province:'QA Province',label:'QA'});await A.wait(`document.querySelector('[data-address-items]').textContent.includes('QA Risk Street')`);
 const checkout=async()=>{await A.navigate('/checkout');await A.wait(`!document.querySelector('[data-quote]').disabled`);await A.evaluate(`document.querySelector('[name=addressId]').selectedIndex=1;document.querySelector('[name=fulfillmentStoreId]').value='100'`);};
 await checkout();check(await A.evaluate(`!Array.from(document.querySelector('[name=fulfillmentStoreId]').options).some(option=>option.value==='102')`),'Checkout excludes inactive store');
 for(const coupon of ['QAEXPIRED','QAMIN']){
  await A.evaluate(`document.querySelector('[name=couponCode]').value=${JSON.stringify(coupon)};document.querySelector('[name=couponCode]').dispatchEvent(new Event('input',{bubbles:true}));document.querySelector('[data-quote]').click()`);await A.wait(`document.querySelector('[data-view-status]').classList.contains('text-danger')&&!document.querySelector('[data-quote]').disabled`);
  check(await A.evaluate(`document.querySelector('[data-place]').disabled && document.querySelector('[data-view-status]').getAttribute('aria-live')==='polite'`),coupon+' real API error announced, placement disabled');
 }
 await A.evaluate(`document.querySelector('[name=couponCode]').value='QAVALID';document.querySelector('[name=couponCode]').dispatchEvent(new Event('input',{bubbles:true}));document.querySelector('[data-quote]').click()`);await A.wait(`!document.querySelector('[data-place]').disabled`);
 check(await A.evaluate(`document.querySelector('[data-view-total]').textContent.includes('630.000')`),'Valid coupon reduces real checkout total');
 const postBeforeExpiry=count(A,'/api/orders');
 await A.evaluate(`window.__qaNow=Date.now;Date.now=()=>window.__qaNow()+16*60*1000;document.querySelector('[data-place]').click()`);await A.wait(`document.querySelector('[data-view-status]').textContent.includes('hết hạn')`);
 await A.evaluate(`Date.now=window.__qaNow;delete window.__qaNow`);
 check(count(A,'/api/orders')===postBeforeExpiry&&await A.evaluate(`document.querySelector('[data-place]').disabled&&!document.querySelector('#checkoutForm').hidden`),'Client quote expiry with advanced browser clock sends no order POST');
 await A.evaluate(`document.querySelector('[data-quote]').click()`);await A.wait(`!document.querySelector('[data-place]').disabled`);
 const beforeRace=count(A,'/api/checkout/quote');
 await A.evaluate(`document.querySelector('[data-quote]').click();document.querySelector('[name=note]').value='QA changed while quoting';document.querySelector('[name=note]').dispatchEvent(new Event('input',{bubbles:true}))`);await A.wait(`!document.querySelector('[data-quote]').disabled&&document.querySelector('[data-view-status]').textContent.includes('Thông tin đã thay đổi')`);
 check(count(A,'/api/checkout/quote')===beforeRace+1&&await A.evaluate(`document.querySelector('[data-place]').disabled`),'Changing form while real quote is pending discards stale response');
 await A.evaluate(`document.querySelector('[data-quote]').click()`);await A.wait(`!document.querySelector('[data-place]').disabled`);
 const beforePlace=count(A,'/api/orders');await A.evaluate(`document.querySelector('[data-place]').click();document.querySelector('[data-place]').click();document.querySelector('#checkoutForm').requestSubmit()`);await A.wait(`document.querySelector('[data-view-status]').textContent.includes('Đã đặt đơn')`);
 check(count(A,'/api/orders')===beforePlace+1,'Rapid double-click/programmatic submit sends exactly one real order POST');
 const history=await A.evaluate(`UTEGear.API.get('/api/orders').then(page=>page.totalElements)`);check(history===1,'Exactly one customer order exists after double-submit');
 // Add excess quantity; checkout quote must show real stock error, not create an order.
 await A.evaluate(`UTEGear.Cart.add({variantId:201,quantity:4})`);await checkout();await A.evaluate(`document.querySelector('[data-quote]').click()`);await A.wait(`document.querySelector('[data-view-status]').classList.contains('text-danger')&&!document.querySelector('[data-quote]').disabled`);
 check(await A.evaluate(`document.querySelector('[data-place]').disabled`),'Insufficient inventory quote rejected and placement disabled');
 await A.navigate('/cart');await A.wait(`!!document.querySelector('[data-view-items] input')`);await A.evaluate(`Array.from(document.querySelectorAll('[data-view-items] button')).find(el=>el.textContent==='Xóa').click()`);await A.wait(`document.querySelector('[data-view-status]').textContent.includes('đang trống')`);check(true,'Cart remove against real API');
 await A.evaluate(`UTEGear.Cart.add({variantId:200,quantity:1})`);await A.navigate('/cart');await A.wait(`!!document.querySelector('[data-view-items] input')`);await A.evaluate(`Array.from(document.querySelectorAll('[data-view-actions] button')).find(el=>el.textContent==='Xóa toàn bộ giỏ').click()`);await A.wait(`document.querySelector('[data-view-status]').textContent.includes('đang trống')`);check(true,'Cart clear against real API');
 await A.navigate('/products');await A.evaluate(`document.querySelector('[data-action=quick-view][data-product-id="200"]').focus();document.querySelector('[data-action=quick-view][data-product-id="200"]').click()`);await A.wait(`document.querySelector('#quickViewModal').classList.contains('show')&&document.querySelector('#quickViewContent').textContent.includes('QA Multi')`);
 await A.wait(`document.querySelector('#quickViewModal').contains(document.activeElement)`);
 await A.send('Input.dispatchKeyEvent',{type:'keyDown',key:'Tab',code:'Tab',windowsVirtualKeyCode:9});await A.send('Input.dispatchKeyEvent',{type:'keyUp',key:'Tab',code:'Tab',windowsVirtualKeyCode:9});check(await A.evaluate(`document.querySelector('#quickViewModal').contains(document.activeElement)`),'Quick-view Tab focus remains inside modal');
 await A.send('Input.dispatchKeyEvent',{type:'keyDown',key:'Tab',code:'Tab',windowsVirtualKeyCode:9,modifiers:8});await A.send('Input.dispatchKeyEvent',{type:'keyUp',key:'Tab',code:'Tab',windowsVirtualKeyCode:9,modifiers:8});check(await A.evaluate(`document.querySelector('#quickViewModal').contains(document.activeElement)`),'Quick-view Shift+Tab focus remains inside modal');
 const ax=await A.send('Accessibility.getFullAXTree');check(ax.nodes.filter(node=>!node.ignored&&node.role?.value==='button').every(node=>node.name?.value?.trim()),'Visible buttons have accessible names in Chromium accessibility tree');
 check(ax.nodes.some(node=>!node.ignored&&node.role?.value==='dialog'&&node.name?.value==='QA Multi'),'Quick-view dialog has an accessible product title');
 await A.send('Input.dispatchKeyEvent',{type:'keyDown',key:'Escape',code:'Escape',windowsVirtualKeyCode:27});await A.send('Input.dispatchKeyEvent',{type:'keyUp',key:'Escape',code:'Escape',windowsVirtualKeyCode:27});await A.wait(`!document.querySelector('#quickViewModal').classList.contains('show')&&document.activeElement.matches('[data-action=quick-view][data-product-id="200"]')`);
 check(await A.evaluate(`document.activeElement.matches('[data-action=quick-view][data-product-id="200"]')`),'Quick-view Escape closes and restores trigger focus');
 for(const route of ['/profile','/cart','/checkout','/products/200','/orders']){
  await A.navigate(route);await A.wait(`document.readyState==='complete'`);
  if(route==='/profile')await A.wait(`!document.querySelector('[data-profile-management]').hidden`);
  for(const width of [1440,768,390]){await A.send('Emulation.setDeviceMetricsOverride',{width,height:900,deviceScaleFactor:1,mobile:width===390});check(await A.evaluate(`document.documentElement.scrollWidth<=innerWidth+1`),'Authenticated responsive '+route+' '+width);}
  check(await A.evaluate(`Array.from(document.querySelectorAll('input:not([type=hidden]),select,textarea')).filter(el=>el.getClientRects().length&&!el.labels?.length&&!el.getAttribute('aria-label')&&!el.getAttribute('aria-labelledby')).length===0`),'Authenticated form labels '+route);
 }
 await A.evaluate(`UTEGear.Cart.add({variantId:200,quantity:1})`);await A.navigate('/cart');await A.wait(`!!document.querySelector('[data-view-items] input')`);
 await A.send('Network.emulateNetworkConditions',{offline:true,latency:0,downloadThroughput:-1,uploadThroughput:-1});
 await A.evaluate(`Array.from(document.querySelectorAll('[data-view-actions] button')).find(el=>el.textContent==='Xóa toàn bộ giỏ').click()`);await A.wait(`document.querySelector('[data-view-status]').classList.contains('text-danger')&&!document.querySelector('[data-view-actions] button').disabled`);
 check(await A.evaluate(`!!document.querySelector('[data-view-items] input')&&!!UTEGear.Session.get()`),'Real offline API error preserves cart/session and unlocks retry');
 await A.send('Network.emulateNetworkConditions',{offline:false,latency:0,downloadThroughput:-1,uploadThroughput:-1});await A.evaluate(`document.querySelector('[data-view-actions] button').click()`);await A.wait(`document.querySelector('[data-view-status]').textContent.includes('đang trống')`);check(true,'Real cart retry succeeds after network recovery');
 await A.navigate('/profile');await A.wait(`!document.querySelector('[data-profile-management]').hidden`);
 await A.evaluate(`UTEGear.Session.save({...UTEGear.Session.get(),accessToken:'invalid-qa-token'});UTEGear.API.get('/api/profile').catch(()=>{})`);await A.wait(`location.pathname==='/login'&&!UTEGear.Session.get()`);check(true,'Real protected 401 clears session and redirects safely');
 await A.login(email,password);await A.navigate('/profile');await A.wait(`!document.querySelector('[data-profile-management]').hidden`);
 await A.evaluate(`window.__qaNow=Date.now;Date.now=()=>window.__qaNow()+61*60*1000;UTEGear.Session.get();Date.now=window.__qaNow;delete window.__qaNow`);
 check(await A.evaluate(`!UTEGear.Session.get()&&document.querySelector('[data-profile-management]').hidden&&document.querySelector('[data-address-items]').childNodes.length===0&&document.querySelector('[data-session-controls]').hidden`),'Client session expiry clears private profile/address UI and header');
 check(browsers.every(browser=>browser.errors.length===0),'No uncaught browser exceptions in risk cases');
}catch(error){checks.push({name:'Risk suite execution',status:'FAIL',detail:error.message});}
finally{for(const browser of browsers)await browser.close();}
const status=checks.some(check=>check.status==='FAIL')?'FAIL':'PASS';
fs.writeFileSync(path.join(output,'report.json'),JSON.stringify({status,checks,qa05MediaRoutes,limitations:['Disposable H2; no PostgreSQL claim','Expiry boundaries use advanced client clock, not server time','Gallery uses local fixture images, not Cloudinary upload']},null,2));
console.log(`Risk suite ${status}: ${checks.filter(c=>c.status==='PASS').length} PASS, ${checks.filter(c=>c.status==='FAIL').length} FAIL; ${output}`);process.exitCode=status==='PASS'?0:1;
