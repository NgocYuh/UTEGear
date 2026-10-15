import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const source = fs.readFileSync(new URL('../../src/main/resources/static/js/components/product-tabs.js', import.meta.url), 'utf8');
function setup() {
    const panels = ['featured', 'bestsellers', 'latest'].map((id, index) => ({ id, hidden: index !== 0 }));
    const tabs = panels.map((panel, index) => ({
        attributes: { 'aria-controls': panel.id, 'aria-selected': String(index === 0) }, events: {}, tabIndex: index === 0 ? 0 : -1,
        getAttribute(key) { return this.attributes[key]; }, setAttribute(key, value) { this.attributes[key] = value; },
        addEventListener(key, callback) { this.events[key] = callback; }, focus() { this.focused = true; }
    }));
    const root = { querySelectorAll: selector => selector === '[role="tab"]' ? tabs : panels };
    vm.runInNewContext(source, { document: { querySelectorAll: () => [root] } });
    return { tabs, panels };
}
test('click reveals one product panel, including an empty group, and updates tab order', () => {
    const { tabs, panels } = setup();
    tabs[1].events.click();
    assert.deepEqual(panels.map(panel => panel.hidden), [true, false, true]);
    assert.deepEqual(tabs.map(tab => tab.attributes['aria-selected']), ['false', 'true', 'false']);
    assert.deepEqual(tabs.map(tab => tab.tabIndex), [-1, 0, -1]);
    tabs[2].events.click();
    assert.deepEqual(panels.map(panel => panel.hidden), [true, true, false]);
});
test('arrows wrap around and Home/End move focus with panel selection', () => {
    const { tabs, panels } = setup();
    const key = (index, value) => { let prevented = false; tabs[index].events.keydown({ key: value, preventDefault() { prevented = true; } }); return prevented; };
    assert.equal(key(0, 'ArrowLeft'), true);
    assert.equal(tabs[2].focused, true);
    assert.equal(panels[2].hidden, false);
    key(2, 'ArrowRight'); assert.equal(panels[0].hidden, false);
    key(0, 'End'); assert.equal(panels[2].hidden, false);
    key(2, 'Home'); assert.equal(panels[0].hidden, false);
    assert.equal(key(0, 'Tab'), false);
});
test('a page without product tabs needs no DOM setup', () => {
    assert.doesNotThrow(() => vm.runInNewContext(source, { document: { querySelectorAll: () => [] } }));
});
