import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const source = fs.readFileSync(new URL('../../src/main/resources/static/js/components/account-menu.js', import.meta.url), 'utf8');
function setup() {
    const events = {}, global = {}, attributes = {};
    const trigger = { setAttribute: (key, value) => { attributes[key] = value; }, focus: () => { trigger.focused = true; } };
    const inside = {};
    const menu = { open: false, querySelector: () => trigger,
        contains: node => node === inside || node === trigger,
        addEventListener: (name, listener) => { events[name] = listener; } };
    const document = { activeElement: null, querySelectorAll: () => [menu],
        addEventListener: (name, listener) => { global[name] = listener; } };
    vm.runInNewContext(source, { document, window: { addEventListener: (name, listener) => { global[name] = listener; } } });
    return { menu, events, global, attributes, trigger, document, inside };
}
test('mouse hover opens the account panel and leaving closes it', () => {
    const ui = setup();
    ui.events.pointerenter({ pointerType: 'mouse' });
    assert.equal(ui.menu.open, true);
    assert.equal(ui.attributes['aria-expanded'], 'true');
    ui.events.pointerleave({ pointerType: 'mouse' });
    assert.equal(ui.menu.open, false);
    assert.equal(ui.attributes['aria-expanded'], 'false');
});
test('touch leaves native disclosure activation intact and toggle updates ARIA', () => {
    const ui = setup();
    ui.events.pointerenter({ pointerType: 'touch' });
    assert.equal(ui.menu.open, false);
    ui.menu.open = true;
    ui.events.toggle();
    assert.equal(ui.attributes['aria-expanded'], 'true');
});
test('focus remains usable inside the panel and Escape returns focus to the trigger', () => {
    const ui = setup();
    ui.menu.open = true;
    ui.document.activeElement = ui.inside;
    ui.events.pointerleave({ pointerType: 'mouse' });
    ui.events.focusout({ relatedTarget: ui.inside });
    assert.equal(ui.menu.open, true);
    let prevented = false;
    ui.events.keydown({ key: 'Escape', preventDefault: () => { prevented = true; } });
    assert.equal(ui.menu.open, false);
    assert.equal(ui.trigger.focused, true);
    assert.equal(prevented, true);
});
test('outside interaction, focus leaving, and logout close the panel', () => {
    const ui = setup();
    ui.menu.open = true;
    ui.global.pointerdown({ target: ui.inside });
    assert.equal(ui.menu.open, true);
    ui.global.pointerdown({ target: {} });
    assert.equal(ui.menu.open, false);
    ui.menu.open = true;
    ui.events.focusout({ relatedTarget: null });
    assert.equal(ui.menu.open, false);
    ui.menu.open = true;
    ui.global['utegear:logout']();
    assert.equal(ui.menu.open, false);
});
