import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const source = fs.readFileSync(new URL('../../src/main/resources/static/js/domain/gear-consultant.js', import.meta.url), 'utf8');
const tick = () => new Promise(resolve => setImmediate(resolve));

function setup({ postResponse, storesResponse, promptsResponse } = {}) {
    const elements = new Map();
    const node = () => ({
        hidden: false,
        disabled: false,
        value: '',
        children: [],
        events: {},
        innerHTML: '',
        textContent: '',
        scrollHeight: 100,
        scrollTop: 0,
        addEventListener(name, handler) { this.events[name] = handler; },
        setAttribute(name, val) { this[name] = val; },
        focus() {},
        appendChild(item) { this.children.push(item); },
        remove() {},
        querySelectorAll() { return []; }
    });

    const trigger = node();
    const panel = node(); panel.hidden = true;
    const closeBtn = node();
    const messagesEl = node();
    const form = node();
    const input = node();
    const storeSelect = node();
    const promptsContainer = node();

    elements.set('.gear-consultant__trigger', trigger);
    elements.set('#gear-consultant-panel', panel);
    elements.set('.gear-consultant__close', closeBtn);
    elements.set('#gear-consultant-messages', messagesEl);
    elements.set('#gear-consultant-form', form);
    elements.set('#gear-consultant-input', input);
    elements.set('#consultant-store-select', storeSelect);
    elements.set('#gear-consultant-quick-prompts', promptsContainer);

    const requests = [];

    const mockFetch = async (url, opts = {}) => {
        requests.push({ url, opts });
        if (url === '/api/stores') {
            return {
                ok: true,
                json: async () => storesResponse || [{ id: 1, name: 'CN TP.HCM', province: 'TP.HCM' }, { id: 3, name: 'CN Đà Nẵng', province: 'Đà Nẵng' }]
            };
        }
        if (url === '/api/consultant/quick-prompts') {
            return {
                ok: true,
                json: async () => promptsResponse || [{ id: 'p1', title: 'Combo FPS', prompt: 'Tư vấn combo FPS', icon: 'bi-crosshair' }]
            };
        }
        if (url === '/api/consultant/chat') {
            return {
                ok: true,
                json: async () => postResponse || {
                    reply: 'Đề xuất **Logitech G102** cho bạn',
                    detectedIntent: 'FPS Gaming',
                    store: { id: 3, name: 'CN Đà Nẵng' },
                    budget: 1500000,
                    totalEstimate: 1298000,
                    products: [{
                        id: 101,
                        name: 'Chuột Logitech G102',
                        slug: 'chuot-logitech-g102',
                        effectivePrice: 399000,
                        availableQuantity: 10,
                        matchReason: 'Form nhỏ gọn'
                    }],
                    nextSuggestions: ['Gợi ý tiếp theo']
                }
            };
        }
        return { ok: false, status: 404 };
    };

    const context = {
        document: {
            querySelector: key => elements.get(key),
            getElementById: id => elements.get('#' + id),
            createElement: node,
            addEventListener() {},
            readyState: 'complete'
        },
        fetch: mockFetch,
        Intl: { NumberFormat: class { format(v) { return v.toLocaleString(); } } },
        console
    };

    vm.runInNewContext(source, context);

    return {
        elements,
        requests,
        trigger,
        panel,
        closeBtn,
        input,
        form,
        messagesEl,
        storeSelect,
        promptsContainer
    };
}

test('gear consultant trigger opens and closes panel', async () => {
    const ui = setup();
    assert.equal(ui.panel.hidden, true);

    ui.trigger.events.click();
    assert.equal(ui.panel.hidden, false);

    ui.closeBtn.events.click();
    assert.equal(ui.panel.hidden, true);
});

test('gear consultant submits user message and renders bot recommendations', async () => {
    const ui = setup();
    await tick();

    ui.input.value = 'Tư vấn chuột FPS ở Đà Nẵng';
    await ui.form.events.submit({ preventDefault() {} });
    await tick();

    const chatReq = ui.requests.find(r => r.url === '/api/consultant/chat');
    assert.ok(chatReq);
    assert.equal(JSON.parse(chatReq.opts.body).message, 'Tư vấn chuột FPS ở Đà Nẵng');

    // Messages should contain user bubble and bot bubble
    assert.ok(ui.messagesEl.children.length >= 2);
    const userMsg = ui.messagesEl.children[0];
    assert.equal(userMsg.textContent, 'Tư vấn chuột FPS ở Đà Nẵng');

    const botMsg = ui.messagesEl.children[ui.messagesEl.children.length - 1];
    assert.ok(botMsg.innerHTML.includes('Logitech G102'));
    assert.ok(botMsg.innerHTML.includes('FPS Gaming'));
});

