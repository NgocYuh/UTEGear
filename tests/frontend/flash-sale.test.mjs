import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const source = fs.readFileSync(new URL('../../src/main/resources/static/js/domain/flash-sale.js', import.meta.url), 'utf8');
const deadline = '2026-12-25T23:59:00+07:00';
function setup(now, end = deadline) {
    const fields = [{}, {}, {}], timer = { hidden: true }, status = {}, cta = {};
    let tick, cleared = false;
    const banner = {
        dataset: { endsAt: end },
        querySelector: selector => ({ '[role="timer"]': timer, '[data-countdown-status]': status,
            '[data-countdown-hours]': fields[0], '[data-countdown-minutes]': fields[1],
            '[data-countdown-seconds]': fields[2], '.flash-sale-cta': cta })[selector]
    };
    vm.runInNewContext(source, {
        document: { querySelectorAll: () => [banner] },
        Date: { parse: Date.parse, now: () => now },
        setInterval: callback => { tick = callback; return 1; }, clearInterval: () => { cleared = true; }
    });
    return { fields, timer, status, cta, get cleared() { return cleared; }, get tick() { return tick; },
        advance: milliseconds => { now += milliseconds; tick(); } };
}
test('deadline is configured with Vietnam timezone in the home template', () => {
    const html = fs.readFileSync(new URL('../../src/main/resources/templates/home.html', import.meta.url), 'utf8');
    assert.ok(html.includes('data-ends-at="' + deadline + '"'));
    assert.ok(html.includes('/js/domain/flash-sale.js'));
    assert.equal(Date.parse(deadline), Date.parse('2026-12-25T16:59:00Z'));
});
test('counts down across minute boundaries and catches up after a suspended tab', () => {
    const ui = setup(Date.parse(deadline) - 3661000);
    const values = () => ui.fields.map(field => field.textContent);
    assert.deepEqual(values(), ['01', '01', '01']);
    assert.equal(ui.timer.hidden, false);
    ui.advance(2000);
    assert.deepEqual(values(), ['01', '00', '59']);
    ui.advance(600000);
    assert.deepEqual(values(), ['00', '50', '59']);
});
test('supports more than 24 hours and preserves the deadline after reload', () => {
    const now = Date.parse(deadline) - 172800000;
    assert.equal(setup(now).fields[0].textContent, '48');
    assert.equal(setup(now + 3600000).fields[0].textContent, '47');
});
test('stops at zero and changes the expired call to action', () => {
    const ui = setup(Date.parse(deadline) - 1000);
    ui.advance(2000);
    assert.deepEqual(ui.fields.map(field => field.textContent), ['00', '00', '00']);
    assert.equal(ui.status.textContent, 'Flash Sale đã kết thúc');
    assert.equal(ui.cta.textContent, 'XEM SẢN PHẨM →');
    assert.equal(ui.cleared, true);
    assert.equal(setup(Date.parse(deadline)).tick, undefined);
});
test('invalid or timezone-free deadlines do not start a timer', () => {
    for (const value of ['', 'invalid', '2026-12-25T23:59:00']) {
        const ui = setup(0, value);
        assert.equal(ui.timer.hidden, true);
        assert.equal(ui.tick, undefined);
    }
});
