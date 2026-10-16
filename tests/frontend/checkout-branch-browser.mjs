// Writes only a dedicated development QA customer's cart/orders. Cancels its test orders.
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { randomUUID } from 'node:crypto';
import { createBrowser } from './browser-harness.mjs';

const [base, cdp, fixtureFile, outputDir] = process.argv.slice(2);
for (const value of [base, cdp]) assert.ok(['localhost','127.0.0.1'].includes(new URL(value).hostname));
const fixture=JSON.parse(fs.readFileSync(fixtureFile,'utf8'));
assert.match(fixture.email,/^chat-qa-.*@demo\.utegear\.test$/);
const run='checkout-branch-qa-'+randomUUID(), output=path.resolve(outputDir||'target/checkout-branch');
fs.mkdirSync(output,{recursive:true});
let session, browser;
async function api(route,method='GET',body,expected=200){
    const response=await fetch(base+route,{method,headers:{'Content-Type':'application/json',...(session?{Authorization:'Bearer '+session.accessToken}:{})},body:body?JSON.stringify(body):undefined});
    const text=await response.text(),data=text?JSON.parse(text):null;
    assert.equal(response.status,expected,`${method} ${route}: ${data?.code||''}`);return data;
}
session=await api('/api/auth/login','POST',{email:fixture.email,password:fixture.password});
const beforeOrders=await api('/api/orders?size=100');
const beforeIds=new Set(beforeOrders.content.map(order=>order.id));
const stores=await api('/api/stores');assert.equal(stores.length,3);
const catalog=await api('/api/products?size=1');
const productId=catalog.content[0].id;
const availability=await api(`/api/products/${productId}/availability`);
const variant=availability.variants.find(variant=>[1,2,3].every(id=>variant.availableStores.some(store=>store.storeId===id&&store.quantity>0)));
assert.ok(variant,'A variant stocked in all three demo stores is required');
const variantId=variant.variantId;
const stock=async()=>Promise.all([1,2,3].map(id=>api(`/api/stores/${id}/availability?variantId=${variantId}`)));
const before=(await stock()).map(item=>item.quantity);
let addresses=await api('/api/addresses');
if(!addresses.length)addresses=[await api('/api/addresses','POST',{fullName:'Khách QA chi nhánh',phone:'0900000099',street:'Địa chỉ demo kiểm thử',ward:'',district:'',province:'Hà Nội',label:'Development QA'},201)];
const addressId=addresses[0].id;
const payload={addressId,fulfillmentStoreId:2,paymentMethod:'COD',items:[{variantId,quantity:1}],note:run};
try{
    const quote=await api('/api/checkout/quote','POST',payload);
    assert.equal(quote.fulfillmentStore.id,2);
    const changed=await api('/api/orders','POST',{...payload,fulfillmentStoreId:3,quoteToken:quote.quoteToken},409);
    assert.equal(changed.code,'QUOTE_CHANGED');
    const insufficient=await api('/api/checkout/quote','POST',{...payload,fulfillmentStoreId:3,items:[{variantId,quantity:before[2]+1}]},409);
    assert.equal(insufficient.code,'INSUFFICIENT_STOCK');
    browser=await createBrowser({base,cdp});await browser.login(fixture.email,fixture.password);
    for(const branchId of [2,3]){
        await api('/api/cart/items','DELETE',null,204);
        await api('/api/cart/items','POST',{variantId,quantity:1},201);
        await browser.navigate('/checkout');
        await browser.wait(`document.querySelector('#checkout-store').options.length===4 && !document.querySelector('[data-quote]').disabled`);
        assert.ok(await browser.evaluate(`Array.from(document.querySelector('#checkout-store').options).some(option=>option.textContent.includes('CN-3'))`));
        await browser.evaluate(`document.querySelector('#checkout-address').value=${JSON.stringify(String(addressId))};document.querySelector('#checkout-store').value='2';document.querySelector('#checkout-note').value=${JSON.stringify(run)};document.querySelector('#checkout-store').dispatchEvent(new Event('change',{bubbles:true}));document.querySelector('[data-quote]').click()`);
        await browser.wait(`!document.querySelector('[data-place]').disabled`);
        if(branchId===3){
            await browser.evaluate(`document.querySelector('#checkout-store').value='3';document.querySelector('#checkout-store').dispatchEvent(new Event('change',{bubbles:true}))`);
            assert.equal(await browser.evaluate(`document.querySelector('[data-place]').disabled`),true);
            assert.equal(await browser.evaluate(`document.querySelector('[data-view-total]').textContent`),'—');
            await browser.evaluate(`document.querySelector('[data-quote]').click()`);
            await browser.wait(`!document.querySelector('[data-place]').disabled`);
        }
        assert.ok((await browser.evaluate(`document.querySelector('[data-checkout-store-summary]').textContent`)).includes('CN-'+branchId));
        await browser.send('Emulation.setDeviceMetricsOverride',{width:branchId===2?1365:390,height:900,deviceScaleFactor:1,mobile:branchId===3});
        assert.ok(await browser.evaluate(`document.documentElement.scrollWidth<=innerWidth`));
        const screenshot=await browser.send('Page.captureScreenshot',{format:'png'});
        fs.writeFileSync(path.join(output,`checkout-cn${branchId}.png`),Buffer.from(screenshot.data,'base64'));
        await browser.evaluate(`document.querySelector('#checkoutForm').requestSubmit()`);
        await browser.wait(`document.querySelector('[data-view-status]').textContent.includes('Đã đặt đơn')`);
        const items=(await api('/api/orders?size=100')).content;
        const order=items.find(order=>!beforeIds.has(order.id)&&order.status==='PENDING');assert.ok(order);
        const detail=await api('/api/orders/'+order.id);assert.equal(detail.fulfillmentStore.id,branchId);
        const after=(await stock()).map(item=>item.quantity);
        assert.deepEqual(after,before.map((quantity,index)=>quantity-(index===branchId-1?1:0)));
        await api('/api/orders/'+order.id+'/cancel','POST',{reason:run});
        assert.deepEqual((await stock()).map(item=>item.quantity),before);
    }
    assert.deepEqual(browser.errors,[]);
    console.log('PASS: 3 branch choices, CN-2 and CN-3 orders, branch-specific decrement/restore, stale quote rejection, insufficient stock, change invalidation, desktop/mobile.');
}finally{
    try{
        for(const order of (await api('/api/orders?size=100')).content.filter(order=>!beforeIds.has(order.id)&&order.status==='PENDING')){
            const detail=await api('/api/orders/'+order.id);
            if(detail.note===run)await api('/api/orders/'+order.id+'/cancel','POST',{reason:run});
        }
        await api('/api/cart/items','DELETE',null,204);
    }finally{if(browser)await browser.close();}
}
