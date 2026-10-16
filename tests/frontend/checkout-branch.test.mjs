import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const source = fs.readFileSync(new URL('../../src/main/resources/static/js/domain/views.js', import.meta.url), 'utf8');
async function setup(quoteHandler) {
    const node = () => ({ value: '', disabled: true, textContent: '', events: {}, options: [],
        classList: { toggle() {} }, addEventListener(name,handler){this.events[name]=handler;},
        replaceChildren(...items){this.options=items;},add(item){this.options.push(item);} });
    const fields=Object.fromEntries(['addressId','fulfillmentStoreId','paymentMethod','couponCode','note'].map(name=>[name,node()]));
    fields.paymentMethod.value='COD';
    const quoteButton=node(),placeButton=node(),status=node(),total=node(),summary=node();
    total.textContent='—';
    const form={elements:{namedItem:name=>fields[name]},events:{},reportValidity:()=>Boolean(fields.addressId.value&&fields.fulfillmentStoreId.value),
        addEventListener(name,handler){this.events[name]=handler;},querySelector:selector=>selector==='[data-quote]'?quoteButton:placeButton};
    const nodes={'form':form,'[data-view-items]':node(),'[data-view-status]':status,'[data-view-total]':total,'[data-checkout-store-summary]':summary};
    const root={querySelector:selector=>nodes[selector],setAttribute(){},append(){}};
    const quoted=[],placed=[];
    const context={document:{addEventListener(){},createElement:node},window:{},Option:class{constructor(text,value){this.text=text;this.value=value;}},
        FormData:class{*[Symbol.iterator](){for(const [key,field]of Object.entries(fields))yield[key,field.value];}},
        UTEGear:{Session:{get:()=>({})},Utils:{formatPrice:value=>String(value)},
            Cart:{get:async()=>({items:[{variantId:218,quantity:1}],itemCount:1,subtotal:100}),id:Number,badges(){}},
            Stores:{list:async()=>[1,2,3].map(id=>({id,active:true,name:'Chi nhánh '+id,address:'Địa chỉ demo'}))},
            API:{get:async()=>[{id:1,fullName:'Khách QA',street:'Demo',province:'Hà Nội'}]},
            Transactions:{quote:async request=>{quoted.push(request);return quoteHandler?quoteHandler(request):{quoteToken:'test-only',total:130,fulfillmentStore:{id:Number(request.fulfillmentStoreId),name:'Chi nhánh '+request.fulfillmentStoreId}};},
                place:async(request,quote)=>{placed.push({request,quote});return{id:1,orderCode:'QA-1'};}}}};
    vm.runInNewContext(source,context);
    await context.UTEGear.Views.checkout(root);
    fields.addressId.value='1';
    const choose=id=>{fields.fulfillmentStoreId.value=String(id);form.events.change();};
    return{fields,quoteButton,placeButton,total,summary,status,quoted,placed,choose,
        quote:()=>quoteButton.events.click(),submit:async()=>{form.events.submit({preventDefault(){}});await new Promise(resolve=>setImmediate(resolve));}};
}

test('all three branch codes are selectable without automatically choosing a branch',async()=>{
    const ui=await setup();assert.equal(ui.fields.fulfillmentStoreId.value,'');
    assert.equal(ui.fields.fulfillmentStoreId.options.length,4);
    assert.ok(ui.fields.fulfillmentStoreId.options[2].text.includes('CN-2'));
    await ui.submit();assert.equal(ui.placed.length,0);
});

test('changing a quoted branch clears total and prevents placing with the previous quote',async()=>{
    const ui=await setup();ui.choose(2);await ui.quote();assert.equal(ui.placeButton.disabled,false);
    ui.choose(3);assert.equal(ui.placeButton.disabled,true);assert.equal(ui.total.textContent,'—');
    await ui.submit();assert.equal(ui.placed.length,0);
});

test('late quote for the previous branch cannot enable order submission',async()=>{
    let finish;const ui=await setup(()=>new Promise(resolve=>{finish=resolve;}));
    ui.choose(2);const pending=ui.quote();ui.choose(3);
    finish({total:130,fulfillmentStore:{id:2,name:'Hà Nội'}});await pending;
    assert.equal(ui.placeButton.disabled,true);assert.equal(ui.total.textContent,'—');
});

test('a mismatched quote store is rejected before confirmation',async()=>{
    const ui=await setup(async()=>({total:130,fulfillmentStore:{id:1,name:'Other'}}));
    ui.choose(2);await ui.quote();assert.equal(ui.placeButton.disabled,true);
    assert.ok(ui.status.textContent.includes('không khớp'));await ui.submit();assert.equal(ui.placed.length,0);
});

test('the selected branch is sent in the quote and final order and remains visible',async()=>{
    const ui=await setup();ui.choose(3);await ui.quote();await ui.submit();
    assert.equal(ui.quoted[0].fulfillmentStoreId,'3');assert.equal(ui.placed[0].request.fulfillmentStoreId,'3');
    assert.ok(ui.summary.textContent.includes('CN-3'));assert.ok(ui.status.textContent.includes('CN-3'));
});
