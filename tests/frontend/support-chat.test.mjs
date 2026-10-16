import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const source = fs.readFileSync(new URL('../../src/main/resources/static/js/domain/support-chat.js', import.meta.url), 'utf8');
const tick = () => new Promise(resolve => setImmediate(resolve));
function setup({ guest = false, post } = {}) {
    const elements = new Map(), global = {};
    const node = () => ({ hidden: false, disabled: false, value: '', children: [], events: {},
        addEventListener(name, handler) { this.events[name] = handler; },
        setAttribute() {}, focus() {}, append(item) { this.children.push(item); },
        replaceChildren(...items) { this.children = items; }, add(item) { this.children.push(item); } });
    for (const key of ['#support-chat-panel','.support-chat__trigger','.support-chat__close','#support-store','#support-chat-messages','#support-chat-input','#support-chat-form','#support-chat-state','#support-chat-access']) elements.set(key,node());
    elements.get('#support-chat-panel').hidden = true;
    const submit = node(); elements.get('#support-chat-form').querySelector = () => submit;
    const requests = [];
    const conversation = id => ({ id, storeId: id, employeeId: id + 10, employeeName: 'Nhân viên', status: 'OPEN' });
    const message = { id: 99, conversationId: 1, senderId: 1, senderName: 'Khách', content: '<img src=x onerror=alert(1)>' };
    const context = { document: { querySelector: key => elements.get(key), createElement: node, addEventListener() {} },
        window: { addEventListener: (name, callback) => { global[name] = callback; } },
        location: { pathname: '/', search: '' }, Option: class { constructor(text,value){this.text=text;this.value=value;} },
        setInterval: () => 1, clearInterval() {}, encodeURIComponent,
        UTEGear: { Session: { get: () => guest ? null : { user: { id: 1, roles: ['CUSTOMER'] } } },
            Realtime: { start: () => ({ stop() {} }) },
            API: { get: async url => url === '/api/stores' ? [{id:1,name:'A'},{id:2,name:'B'},{id:3,name:'C'}]
                : { conversation: conversation(Number(url.split('/').at(-1))), messages: [message] },
                post: async (url, body) => { requests.push({url,body}); return post ? post(url,body,conversation) : url.endsWith('/messages') ? message : conversation(body.storeId); } } } };
    vm.runInNewContext(source,context);
    const get = key => elements.get(key);
    return {get,requests,global,submit,open:async()=>{get('.support-chat__trigger').events.click();await tick();},
        choose: id => { get('#support-store').value=String(id);return get('#support-store').events.change(); },
        send: () => get('#support-chat-form').events.submit({preventDefault(){}}) };
}

test('guest sees login action and cannot open a customer conversation', async () => {
    const ui=setup({guest:true}); await ui.open();
    assert.equal(ui.get('#support-store').disabled,true);
    assert.equal(ui.get('#support-chat-access').hidden,false);
    await ui.choose(1); assert.equal(ui.requests.length,0);
});

test('reopening does not duplicate branches and untrusted message content remains plain text', async () => {
    const ui=setup();await ui.open();await ui.choose(1);
    ui.get('.support-chat__close').events.click();await ui.open();
    assert.equal(ui.get('#support-store').children.length,4);
    assert.ok(ui.get('#support-chat-messages').children[0].textContent.includes('<img'));
    assert.equal(ui.get('#support-chat-messages').children[0].innerHTML,undefined);
});

test('late response from previous branch cannot reenable sending into the old conversation', async () => {
    let finish;
    const ui=setup({post:async (url,body,conversation)=>body.storeId===1?new Promise(resolve=>{finish=()=>resolve(conversation(1));}):conversation(2)});
    await ui.open();const old=ui.choose(1);await ui.choose(2);finish();await old;
    ui.get('#support-chat-input').value='Cho chi nhánh 2';await ui.send();
    assert.equal(ui.requests.at(-1).url,'/api/support/conversations/2/messages');
});

test('in-flight send blocks double submission and duplicate message ID is rendered once', async () => {
    let finish;
    const ui=setup({post:async(url,body,conversation)=>url.endsWith('/messages')?new Promise(resolve=>{finish=()=>resolve({id:99,senderId:1,senderName:'Khách',content:'Một tin'});}):conversation(body.storeId)});
    await ui.open();await ui.choose(1);ui.get('#support-chat-input').value='Một tin';
    const pending=ui.send();await ui.send();assert.equal(ui.submit.disabled,true);finish();await pending;
    assert.equal(ui.requests.filter(request=>request.url.endsWith('/messages')).length,1);
    assert.equal(ui.get('#support-chat-messages').children.length,1);
    assert.equal(ui.submit.disabled,false);
});
