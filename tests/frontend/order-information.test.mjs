import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

function setup() {
    const node = (tag, text, className) => ({ tag, text: text == null ? '' : String(text), className, children: [],
        append(...children) { this.children.push(...children); }, classList: { add() {} } });
    const context = vm.createContext({ Intl, Date, URL, document: { addEventListener() {} }, UTEGear: { Views: {
        node, image: (url, name) => Object.assign(node('img'), { src: url, alt: name }), money: value => `${Number(value || 0).toLocaleString('vi-VN')} ₫`
    } } });
    context.window = context;
    vm.runInContext(fs.readFileSync(new URL('../../src/main/resources/static/js/domain/orders.js', import.meta.url), 'utf8'), context);
    return { orders: context.UTEGear.Orders, host: node('section') };
}
const text = node => [node.text, ...node.children.map(text)].join('\n');
const detail = () => ({ orderCode: 'UTE-QA', status: 'PENDING', createdAt: '2026-10-15T17:30:00Z',
    customer: { name: 'Khách thử', phone: '0900000000', email: 'qa@example.test' },
    shipping: { street: 'Đường thử', ward: 'Phường thử', district: 'Quận thử', province: 'TP. Hồ Chí Minh', method: 'STANDARD' },
    fulfillmentStore: { name: 'Demo' }, payment: { method: 'COD', status: 'PENDING' },
    subtotal: 750000, shippingFee: 30000, discount: 0, total: 780000,
    items: [{ productName: 'Lexar H31', sku: 'LEX-H31', imageUrl: '/images/test.png', quantity: 1, unitPrice: 750000, subtotal: 750000, optionValues: {} }]
});
test('Customer detail explains recipient, COD, line item and server-calculated total breakdown', () => {
    const { orders, host } = setup(); orders.customerDetail(host, detail()); const content = text(host);
    for (const value of ['Chờ xác nhận', 'Khách thử', '0900000000', 'Đường thử, Phường thử, Quận thử, TP. Hồ Chí Minh',
        'Thanh toán khi nhận hàng (COD)', 'Chưa thanh toán', 'Lexar H31', 'Mã sản phẩm: LEX-H31', 'Số lượng: 1 × 750.000 ₫',
        'Tiền sản phẩm', 'Phí vận chuyển', '30.000 ₫', 'Giảm giá', '0 ₫', 'Tổng thanh toán', '780.000 ₫']) assert.ok(content.includes(value), value);
    assert.equal(host.hidden, false);
});
test('Coupon, variant choices and a missing payment remain explicit without fabricating values', () => {
    const { orders, host } = setup(); const order = detail();
    order.couponCode = 'QA10'; order.discount = 10000; order.total = 770000; order.payment = null;
    order.items[0].optionValues = { Màu: 'Bạc' };
    orders.customerDetail(host, order); const content = text(host);
    for (const value of ['Màu: Bạc', 'Giảm giá (QA10)', '−10.000 ₫', '77', 'Phương thức chưa cập nhật']) assert.ok(content.includes(value), value);
});
test('Untrusted product and note content is assigned as text and never parsed as HTML', () => {
    const { orders, host } = setup(); const order = detail();
    order.items[0].productName = '<img src=x onerror=alert(1)>'; order.note = '<script>alert(1)</script>';
    orders.customerDetail(host, order);
    assert.ok(text(host).includes(order.note));
    assert.ok(text(host).includes(order.items[0].productName));
    const nodes = node => [node, ...node.children.flatMap(nodes)];
    assert.equal(nodes(host).some(node => node.innerHTML != null || node.tag === 'script'), false);
});
test('Status labels and order dates have readable fallbacks and use Vietnam time', () => {
    const { orders } = setup();
    assert.equal(orders.badge('CANCELLED').text, 'Đã hủy');
    assert.equal(orders.badge('NEW_UNKNOWN').text, 'Đang cập nhật');
    assert.equal(orders.date(null), 'Chưa cập nhật');
    assert.equal(orders.date('bad date'), 'Chưa cập nhật');
    assert.match(orders.date('2026-10-15T17:30:00Z'), /16\/10\/2026/);
});
