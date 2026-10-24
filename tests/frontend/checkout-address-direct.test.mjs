import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const viewsSource = fs.readFileSync(new URL('../../src/main/resources/static/js/domain/views.js', import.meta.url), 'utf8');
const checkoutHtml = fs.readFileSync(new URL('../../src/main/resources/templates/checkout.html', import.meta.url), 'utf8');
const profileHtml = fs.readFileSync(new URL('../../src/main/resources/templates/profile/profile.html', import.meta.url), 'utf8');

test('profile template does not contain district input or label', () => {
    assert.strictEqual(profileHtml.includes('address-district'), false);
    assert.strictEqual(profileHtml.includes('Quận/huyện'), false);
    assert.strictEqual(profileHtml.includes('Quận'), false);
});

test('checkout template contains direct address form without district', () => {
    assert.strictEqual(checkoutHtml.includes('id="checkout-new-address-box"'), true);
    assert.strictEqual(checkoutHtml.includes('id="btn-toggle-new-address"'), true);
    assert.strictEqual(checkoutHtml.includes('id="new-address-name"'), true);
    assert.strictEqual(checkoutHtml.includes('id="new-address-phone"'), true);
    assert.strictEqual(checkoutHtml.includes('id="new-address-street"'), true);
    assert.strictEqual(checkoutHtml.includes('id="new-address-ward"'), true);
    assert.strictEqual(checkoutHtml.includes('id="new-address-province"'), true);
    assert.strictEqual(checkoutHtml.includes('district'), false);
});

async function setupDirectAddressTest(initialAddresses = []) {
    const node = () => ({
        value: '', disabled: false, textContent: '', events: {}, options: [], hidden: false,
        classList: { toggle() {} },
        addEventListener(name, handler) { this.events[name] = handler; },
        replaceChildren(...items) { this.options = items; },
        add(item) { this.options.push(item); },
        querySelector(sel) { return elements[sel]; },
        focus() {}
    });

    const fields = Object.fromEntries(['addressId', 'fulfillmentStoreId', 'paymentMethod', 'couponCode', 'note'].map(name => [name, node()]));
    fields.paymentMethod.value = 'COD';
    const quoteButton = node(); const placeButton = node(); const status = node(); const total = node(); const summary = node();
    quoteButton.disabled = false;

    const elements = {
        '#checkout-new-address-box': node(),
        '#btn-toggle-new-address': node(),
        '#btn-cancel-new-address': node(),
        '#btn-save-new-address': node(),
        '#new-address-name': node(),
        '#new-address-phone': node(),
        '#new-address-street': node(),
        '#new-address-ward': node(),
        '#new-address-province': node(),
        '[data-quote]': quoteButton,
        '[data-place]': placeButton
    };

    const form = {
        elements: { namedItem: name => fields[name] },
        events: {},
        reportValidity: () => Boolean(fields.addressId.value && fields.fulfillmentStoreId.value),
        addEventListener(name, handler) { this.events[name] = handler; },
        querySelector: sel => elements[sel] || (sel === '[data-quote]' ? quoteButton : placeButton)
    };

    const nodes = {
        'form': form,
        '[data-view-items]': node(),
        '[data-view-status]': status,
        '[data-view-total]': total,
        '[data-checkout-store-summary]': summary,
        '#checkout-new-address-box': elements['#checkout-new-address-box'],
        '#btn-toggle-new-address': elements['#btn-toggle-new-address'],
        '#btn-cancel-new-address': elements['#btn-cancel-new-address'],
        '#btn-save-new-address': elements['#btn-save-new-address']
    };

    const root = {
        querySelector: sel => nodes[sel] || elements[sel],
        setAttribute() {},
        append() {}
    };

    const postedAddresses = [];
    const quoted = [];

    const context = {
        document: { addEventListener() {}, createElement: node },
        window: {},
        Option: class {
            constructor(text, value) {
                this.text = text;
                this.value = value;
            }
        },
        FormData: class {
            *[Symbol.iterator]() {
                for (const [key, field] of Object.entries(fields)) yield [key, field.value];
            }
        },
        UTEGear: {
            Session: { get: () => ({ user: { id: 1, fullName: 'Test' } }) },
            Utils: { formatPrice: value => String(value) },
            Cart: { get: async () => ({ items: [{ variantId: 10, quantity: 1 }], itemCount: 1, subtotal: 100 }), id: Number, badges() {} },
            Stores: { list: async () => [{ id: 1, active: true, name: 'Chi nhánh 1', address: 'Địa chỉ 1' }] },
            API: {
                get: async () => [...initialAddresses],
                post: async (path, body) => {
                    if (path === '/api/addresses') {
                        postedAddresses.push(body);
                        return { id: 99, ...body };
                    }
                    throw new Error('Unknown post path: ' + path);
                }
            },
            Transactions: {
                quote: async request => {
                    quoted.push(request);
                    return { quoteToken: 'token', total: 100, fulfillmentStore: { id: 1, name: 'Chi nhánh 1' } };
                },
                place: async () => ({ id: 1, orderCode: 'ORD-1' })
            }
        }
    };

    vm.runInNewContext(viewsSource, context);
    await context.UTEGear.Views.checkout(root);

    return { root, form, fields, elements, postedAddresses, quoted, status };
}

test('when user has no saved addresses, new address box opens directly at checkout', async () => {
    const { elements, status } = await setupDirectAddressTest([]);
    assert.strictEqual(elements['#checkout-new-address-box'].hidden, false);
    assert.strictEqual(status.textContent.includes('Vui lòng nhập địa chỉ nhận hàng'), true);
});

test('saving direct address posts address without district, adds option and selects it', async () => {
    const { fields, elements, postedAddresses, status } = await setupDirectAddressTest([]);
    elements['#new-address-name'].value = 'Nguyễn Văn A';
    elements['#new-address-phone'].value = '0901234567';
    elements['#new-address-street'].value = '123 Võ Văn Ngân';
    elements['#new-address-ward'].value = 'Linh Chiểu';
    elements['#new-address-province'].value = 'TP. Hồ Chí Minh';

    // Click save direct address
    await elements['#btn-save-new-address'].events.click();

    assert.strictEqual(postedAddresses.length, 1);
    assert.strictEqual(postedAddresses[0].fullName, 'Nguyễn Văn A');
    assert.strictEqual(postedAddresses[0].phone, '0901234567');
    assert.strictEqual(postedAddresses[0].street, '123 Võ Văn Ngân');
    assert.strictEqual(postedAddresses[0].ward, 'Linh Chiểu');
    assert.strictEqual(postedAddresses[0].province, 'TP. Hồ Chí Minh');
    assert.strictEqual('district' in postedAddresses[0], false);

    assert.strictEqual(fields.addressId.value, 99);
    assert.strictEqual(elements['#checkout-new-address-box'].hidden, true);
    assert.strictEqual(status.textContent.includes('Đã lưu và chọn địa chỉ giao hàng'), true);
});

test('clicking quote button when new address box is filled automatically saves address and quotes', async () => {
    const { fields, elements, postedAddresses, quoted } = await setupDirectAddressTest([]);
    fields.fulfillmentStoreId.value = '1';
    elements['#new-address-name'].value = 'Trần Thị B';
    elements['#new-address-phone'].value = '0987654321';
    elements['#new-address-street'].value = '456 Lê Văn Việt';
    elements['#new-address-province'].value = 'TP. Thủ Đức';

    // Click quote directly without manual save button click
    await elements['[data-quote]'].events.click();

    assert.strictEqual(postedAddresses.length, 1);
    assert.strictEqual(postedAddresses[0].fullName, 'Trần Thị B');
    assert.strictEqual(fields.addressId.value, 99);
    assert.strictEqual(quoted.length, 1);
    assert.strictEqual(quoted[0].addressId, 99);
});
