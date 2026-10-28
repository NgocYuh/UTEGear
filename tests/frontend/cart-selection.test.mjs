import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const source = fs.readFileSync(new URL('../../src/main/resources/static/js/domain/views.js', import.meta.url), 'utf8');

function createMockDom() {
    const storage = new Map();
    const sessionStorageMock = {
        getItem: (k) => storage.get(k) || null,
        setItem: (k, v) => storage.set(k, String(v)),
        removeItem: (k) => storage.delete(k),
        clear: () => storage.clear()
    };

    const makeNode = (tag = 'div', text = '') => {
        const events = {};
        const children = [];
        const classes = new Set();
        const dataset = {};
        const element = {
            tagName: tag.toUpperCase(),
            textContent: text,
            value: '',
            checked: false,
            disabled: false,
            hidden: false,
            id: '',
            type: '',
            style: {},
            dataset,
            events,
            children,
            get className() { return Array.from(classes).join(' '); },
            set className(val) {
                classes.clear();
                if (val) val.split(/\s+/).filter(Boolean).forEach(c => classes.add(c));
            },
            classList: {
                add: (c) => classes.add(c),
                remove: (c) => classes.delete(c),
                toggle: (c, force) => force ? classes.add(c) : classes.delete(c),
                contains: (c) => classes.has(c)
            },
            append: (...nodes) => children.push(...nodes),
            prepend: (...nodes) => children.unshift(...nodes),
            replaceChildren: (...nodes) => { children.length = 0; children.push(...nodes); },
            addEventListener: (ev, handler) => { events[ev] = handler; },
            dispatchEvent: (ev) => { if (events[ev.type]) events[ev.type](ev); },
            querySelector: (sel) => {
                if (sel === '#cart-select-all') return children.find(c => c.id === 'cart-select-all') || null;
                if (sel === '[data-view-total]') return totalNode;
                if (sel === '[data-view-items]') return itemsContainer;
                if (sel === '[data-view-actions]') return actionsContainer;
                if (sel === '[data-view-status]') return statusNode;
                return null;
            },
            querySelectorAll: (sel) => {
                const results = [];
                const search = (node) => {
                    if (!node) return;
                    if (sel === '.cart-item-check' && node.classList?.contains('cart-item-check')) results.push(node);
                    if (sel === '[data-view-items]' && node === itemsContainer) results.push(node);
                    if (node.children) node.children.forEach(search);
                };
                search(element);
                return results;
            },
            reportValidity: () => true,
            setAttribute: () => {},
            removeAttribute: () => {}
        };
        return element;
    };

    const totalNode = makeNode('strong', '—');
    const statusNode = makeNode('p', '');
    const itemsContainer = makeNode('div', '');
    const actionsContainer = makeNode('div', '');
    const root = makeNode('section');
    root.append(itemsContainer, totalNode, actionsContainer, statusNode);

    const context = {
        document: {
            addEventListener: () => {},
            createElement: (tag) => makeNode(tag),
            querySelectorAll: () => []
        },
        window: {
            addEventListener: () => {},
            setTimeout: (fn) => setTimeout(fn, 0),
            clearTimeout: () => {}
        },
        sessionStorage: sessionStorageMock,
        Option: class { constructor(t, v) { this.text = t; this.value = v; } },
        UTEGear: {
            Session: { get: () => ({ user: { fullName: 'Test User' } }) },
            Utils: { formatPrice: (v) => `${v} ₫` },
            Cart: {
                id: (v) => Number(v),
                badges: () => {},
                get: async () => ({
                    subtotal: 1500000,
                    itemCount: 2,
                    items: [
                        { id: 101, variantId: 501, quantity: 1, lineTotal: 1000000, unitPrice: 1000000, available: true, availableStock: 10, product: { name: 'Sản phẩm 1' }, variant: { optionValues: {} } },
                        { id: 102, variantId: 502, quantity: 1, lineTotal: 500000, unitPrice: 500000, available: true, availableStock: 5, product: { name: 'Sản phẩm 2' }, variant: { optionValues: {} } }
                    ]
                }),
                update: async () => {},
                remove: async () => {},
                clear: async () => {}
            }
        }
    };

    vm.runInNewContext(source, context);
    return { context, root, totalNode, itemsContainer, actionsContainer, sessionStorageMock };
}

test('cart page renders item selection checkboxes and updates total dynamically', async () => {
    const { context, root, totalNode, itemsContainer, actionsContainer, sessionStorageMock } = createMockDom();

    await context.UTEGear.Views.cart(root);

    const checkboxes = root.querySelectorAll('.cart-item-check');
    assert.equal(checkboxes.length, 2, 'Should have 2 item checkboxes');
    assert.equal(checkboxes[0].checked, true, 'First item checked by default');
    assert.equal(checkboxes[1].checked, true, 'Second item checked by default');

    // Both items checked: 1,000,000 + 500,000 = 1,500,000
    assert.ok(totalNode.textContent.includes('1500000'), 'Initial total should be sum of both items');

    const checkoutBtn = actionsContainer.children.find(c => c.textContent.includes('Thanh toán'));
    assert.ok(checkoutBtn, 'Checkout button should exist');
    assert.equal(checkoutBtn.textContent, 'Thanh toán (2)');

    // Uncheck second item (500k)
    checkboxes[1].checked = false;
    checkboxes[1].events.change();

    assert.ok(totalNode.textContent.includes('1000000'), 'Total should update to 1,000,000 after unchecking item 2');
    assert.equal(checkoutBtn.textContent, 'Thanh toán (1)');

    const saved = JSON.parse(sessionStorageMock.getItem('utegear_checkout_selected_variants'));
    assert.deepEqual(saved, [501], 'sessionStorage should store only selected variantId 501');

    // Uncheck first item as well
    checkboxes[0].checked = false;
    checkboxes[0].events.change();

    assert.equal(checkoutBtn.textContent, 'Thanh toán (0)');
    assert.ok(checkoutBtn.classList.contains('disabled'), 'Checkout button should be disabled when 0 items selected');
});

