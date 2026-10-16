import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const source = fs.readFileSync(new URL('../../src/main/resources/static/js/domain/realtime.js', import.meta.url), 'utf8');
function setup(options = {}) {
    let destination, deliver, connected;
    const events = [], client = { disconnect() {}, ws: { close() {} },
        connect(headers, callback) { assert.ok(headers.Authorization); connected = callback; },
        subscribe(target, callback) { destination = target; deliver = callback; } };
    const session = { accessToken: 'test-only', expiresAt: new Date(Date.now() + 60000).toISOString(), user: { roles: ['CUSTOMER'] } };
    const window = { addEventListener() {}, removeEventListener() {}, Stomp: {} };
    const context = { window, UTEGear: { Session: { get: () => session } }, Stomp: { over: () => client }, WebSocket: class {},
        URL, location: { origin: 'http://localhost:8080' }, setTimeout: () => 1, clearTimeout() {}, Date };
    vm.runInNewContext(source, context);
    const handle = context.UTEGear.Realtime.start({ support: true, resync: async () => {}, onEvent: event => events.push(event), ...options });
    return { connected: () => connected(), destination: () => destination, events, handle,
        frame: (type, data, id = 'evt-1') => deliver({ body: JSON.stringify({ eventId: id, occurredAt: new Date().toISOString(), type, data }) }) };
}

test('support subscribes to private queue, filters unrelated events and duplicate delivery', async () => {
    const ui = setup(); await ui.connected();
    assert.equal(ui.destination(), '/user/queue/support');
    ui.frame('SUPPORT_MESSAGE_CREATED', { id: 1, conversationId: 41 });
    ui.frame('SUPPORT_MESSAGE_CREATED', { id: 1, conversationId: 41 });
    ui.frame('ORDER_CREATED', { orderId: 99 }, 'evt-2');
    ui.frame('SUPPORT_MESSAGE_CREATED', { id: 2 }, 'evt-3');
    ui.frame('SUPPORT_CONVERSATION_ASSIGNED', { id: 41 }, 'evt-4');
    assert.equal(ui.events.length, 2);
});

test('events received while REST resync is pending are delivered after it finishes', async () => {
    let finish;
    const ui = setup({ resync: () => new Promise(resolve => { finish = resolve; }) });
    const pending = ui.connected();
    ui.frame('SUPPORT_MESSAGE_CREATED', { id: 1, conversationId: 41 });
    assert.equal(ui.events.length, 0);
    finish(); await pending;
    assert.equal(ui.events.length, 1);
    ui.handle.stop();
    ui.frame('SUPPORT_MESSAGE_CREATED', { id: 2, conversationId: 41 }, 'evt-2');
    assert.equal(ui.events.length, 1);
});
