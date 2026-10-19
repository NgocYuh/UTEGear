import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const script = fs.readFileSync(new URL('../../src/main/resources/static/js/domain/switch-tester.js', import.meta.url), 'utf8');
const mockData = JSON.parse(fs.readFileSync(new URL('../../docs/api/mock/switch-sound.json', import.meta.url), 'utf8'));

function createMockElement(tag = 'div', attributes = {}) {
    const el = {
        tagName: tag.toUpperCase(),
        dataset: { ...attributes.dataset },
        className: attributes.className || '',
        classList: {
            classes: new Set((attributes.className || '').split(' ').filter(Boolean)),
            add(...c) { c.forEach(x => this.classes.add(x)); el.className = Array.from(this.classes).join(' '); },
            remove(...c) { c.forEach(x => this.classes.delete(x)); el.className = Array.from(this.classes).join(' '); },
            toggle(c, force) {
                if (force === undefined) {
                    if (this.classes.has(c)) this.classes.delete(c); else this.classes.add(c);
                } else if (force) this.classes.add(c); else this.classes.delete(c);
                el.className = Array.from(this.classes).join(' ');
            },
            contains(c) { return this.classes.has(c); },
            replace(oldC, newC) { this.remove(oldC); this.add(newC); }
        },
        style: {
            properties: {},
            setProperty(prop, val) { this.properties[prop] = val; },
            getPropertyValue(prop) { return this.properties[prop] || ''; }
        },
        textContent: '',
        hidden: false,
        disabled: false,
        children: [],
        events: {},
        options: [],
        add(opt) { this.options.push(opt); },
        value: '',
        addEventListener(event, handler) {
            this.events[event] = this.events[event] || [];
            this.events[event].push(handler);
        },
        dispatchEvent(event) {
            const handlers = this.events[event.type] || [];
            handlers.forEach(h => h(event));
        },
        append(...nodes) {
            nodes.forEach(n => this.children.push(n));
        },
        replaceChildren(...nodes) {
            this.children = [...nodes];
        },
        setAttribute(name, val) { attributes[name] = String(val); },
        getAttribute(name) { return attributes[name] !== undefined ? String(attributes[name]) : null; },
        removeAttribute(name) { delete attributes[name]; },
        querySelector(selector) {
            return this.querySelectorAll(selector)[0] || null;
        },
        querySelectorAll(selector) {
            const results = [];
            const search = (node) => {
                if (!node || typeof node !== 'object') return;
                let matches = false;
                if (selector.startsWith('.')) {
                    matches = node.classList?.contains(selector.slice(1));
                } else if (selector.startsWith('#')) {
                    matches = node.id === selector.slice(1);
                } else if (selector.startsWith('[') && selector.endsWith(']')) {
                    const attr = selector.slice(1, -1);
                    if (attr.includes('=')) {
                        const [k, v] = attr.split('=').map(s => s.replace(/['"]/g, ''));
                        if (k.startsWith('data-')) {
                            const dataKey = k.slice(5).replace(/-([a-z])/g, (_, c) => c.toUpperCase());
                            matches = node.dataset?.[dataKey] === v;
                        } else {
                            matches = node.getAttribute?.(k) === v;
                        }
                    } else if (attr.startsWith('data-')) {
                        const dataKey = attr.slice(5).replace(/-([a-z])/g, (_, c) => c.toUpperCase());
                        matches = node.dataset?.[dataKey] !== undefined;
                    }
                }
                if (matches) results.push(node);
                (node.children || []).forEach(search);
            };
            search(this);
            return results;
        }
    };
    return el;
}

function setupContext(apiResponse) {
    const root = createMockElement('div');
    const container = createMockElement('section', { dataset: { switchTester: '', productId: '145' } });
    root.append(container);

    const pills = createMockElement('div', { dataset: { switchPills: '' } });
    const desc = createMockElement('div', { dataset: { activeSwitchDesc: '' } });
    const chassis = createMockElement('div', { dataset: { keyboardChassis: '' } });
    const disclaimer = createMockElement('div', { dataset: { switchDisclaimer: '' } });
    const muteBtn = createMockElement('button', { dataset: { testerMute: '' } });
    const muteIcon = createMockElement('i', { dataset: { testerMuteIcon: '' } });
    const muteText = createMockElement('span', { dataset: { testerMuteText: '' } });
    muteBtn.append(muteIcon, muteText);

    const compareToggle = createMockElement('button', { dataset: { testerToggleCompare: '' } });
    const comparePanel = createMockElement('div', { dataset: { comparePanel: '' } });
    comparePanel.hidden = true;
    const selectA = createMockElement('select', { dataset: { compareSelect: 'A' } });
    const selectB = createMockElement('select', { dataset: { compareSelect: 'B' } });
    const titleA = createMockElement('th', { dataset: { tableSwitchTitle: 'A' } });
    const titleB = createMockElement('th', { dataset: { tableSwitchTitle: 'B' } });
    const tbody = createMockElement('tbody', { dataset: { compareTableBody: '' } });
    comparePanel.append(selectA, selectB, titleA, titleB, tbody);

    const key1 = createMockElement('button', { className: 'vkey', dataset: { key: 'KeyA' } });
    key1.setAttribute('aria-label', 'Phím A');
    const keySpace = createMockElement('button', { className: 'vkey vkey-space', dataset: { key: 'Space', stab: 'true' } });
    keySpace.setAttribute('aria-label', 'Spacebar');
    chassis.append(key1, keySpace);

    const layoutBadge = createMockElement('span', { dataset: { layoutBadge: '' } });
    const layoutBtnTkl = createMockElement('button', { dataset: { layoutBtn: 'TKL' } });
    const layoutBtnFull = createMockElement('button', { dataset: { layoutBtn: 'FULLSIZE' } });
    const layoutBtn65 = createMockElement('button', { dataset: { layoutBtn: 'COMPACT_65' } });

    const toggle3d = createMockElement('button', { dataset: { '3dToggle': '' } });
    const reset3d = createMockElement('button', { dataset: { '3dReset': '' } });
    const icon3d = createMockElement('i', { dataset: { '3dIcon': '' } });
    const text3d = createMockElement('span', { dataset: { '3dText': '' } });
    toggle3d.append(icon3d, text3d);

    const fullscreenBtn = createMockElement('button', { dataset: { testerFullscreen: '' } });
    const fsIcon = createMockElement('i', { dataset: { fullscreenIcon: '' } });
    const fsText = createMockElement('span', { dataset: { fullscreenText: '' } });
    fullscreenBtn.append(fsIcon, fsText);

    const inlineTheaterBtn = createMockElement('button', { dataset: { theaterTriggerInline: '' } });
    const theaterCloseBtn = createMockElement('button', { dataset: { theaterClose: '' } });
    const theaterBackdrop = createMockElement('div', { dataset: { theaterBackdrop: '' } });

    const rgbSelect = createMockElement('select', { dataset: { rgbMode: '' } });
    rgbSelect.value = 'rgb-wave';
    const rgbBadge = createMockElement('span', { dataset: { rgbStatusBadge: '' } });

    const interactiveWrapper = createMockElement('div', { dataset: { keyboardInteractive: '' } });
    interactiveWrapper.append(chassis);

    container.append(
        pills, desc, interactiveWrapper, disclaimer, muteBtn, compareToggle, comparePanel,
        layoutBadge, layoutBtnTkl, layoutBtnFull, layoutBtn65, toggle3d, reset3d,
        fullscreenBtn, inlineTheaterBtn, theaterCloseBtn, theaterBackdrop,
        rgbSelect, rgbBadge
    );

    const purchaseBox = createMockElement('div', { dataset: { purchaseSwitchBox: '' } });
    purchaseBox.hidden = true;
    const purchasePills = createMockElement('div', { dataset: { purchaseSwitchPills: '' } });
    const selectedSwitchBadge = createMockElement('span', { dataset: { selectedSwitchBadge: '' } });
    purchaseBox.append(purchasePills, selectedSwitchBadge);

    const buyNowBtn = createMockElement('button', { dataset: { detailBuyNow: '' } });
    let buyNowExecuted = false;
    buyNowBtn.click = () => {
        const evt = {
            type: 'click',
            defaultPrevented: false,
            stopped: false,
            preventDefault() { this.defaultPrevented = true; },
            stopImmediatePropagation() { this.stopped = true; }
        };
        const handlers = buyNowBtn.events['click'] || [];
        for (const h of handlers) {
            h(evt);
            if (evt.stopped) return;
        }
        buyNowExecuted = true;
    };

    const variantSelect = createMockElement('select', { dataset: { detailVariant: '' } });
    variantSelect.options = [
        { value: '201', text: 'Red Switch' },
        { value: '202', text: 'Blue Switch' }
    ];
    variantSelect.value = '201';
    root.append(variantSelect, purchaseBox, buyNowBtn);

    const context = {
        window: {
            addEventListener() {},
            AudioContext: class {
                constructor() {
                    this.state = 'running';
                    this.currentTime = 0;
                    this.sampleRate = 44100;
                    this.destination = {};
                }
                resume() {}
                createOscillator() {
                    return {
                        frequency: { setValueAtTime() {}, exponentialRampToValueAtTime() {} },
                        connect() {},
                        start() {},
                        stop() {}
                    };
                }
                createGain() {
                    return {
                        gain: { setValueAtTime() {}, exponentialRampToValueAtTime() {} },
                        connect() {}
                    };
                }
                createBuffer() {
                    return { getChannelData() { return new Float32Array(100); } };
                }
                createBufferSource() {
                    return { connect() {}, start() {} };
                }
                createBiquadFilter() {
                    return {
                        frequency: { setValueAtTime() {} },
                        Q: { setValueAtTime() {} },
                        connect() {}
                    };
                }
            }
        },
        document: {
            querySelector(sel) { return root.querySelector(sel); },
            querySelectorAll(sel) { return root.querySelectorAll(sel); },
            createElement(tag) { return createMockElement(tag); },
            addEventListener() {}
        },
        Event: class { constructor(type) { this.type = type; } },
        Option: class { constructor(text, val, def, sel) { this.text = text; this.value = val; } },
        setTimeout(fn) { fn(); },
        UTEGear: {
            API: {
                get: async () => structuredClone(apiResponse)
            }
        }
    };

    vm.runInNewContext(script, context);
    return {
        context, root, container, pills, desc, chassis, muteBtn, compareToggle, comparePanel,
        selectA, selectB, tbody, variantSelect, rgbSelect, rgbBadge,
        purchaseBox, purchasePills, selectedSwitchBadge, buyNowBtn,
        isBuyNowExecuted: () => buyNowExecuted
    };
}

test('switch tester initializes and renders pills when product is supported keyboard', async () => {
    const { context, container, pills, desc } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    assert.equal(container.hidden, false);
    assert.equal(pills.children.length, 3);
    assert.equal(context.UTEGear.SwitchTester.activeSwitch.code, 'RED_LINEAR');
    assert.ok(desc.textContent.includes('Red Switch'));
});

test('switch tester hides container when product is not supported', async () => {
    const { context, container } = setupContext(mockData.productUnsupportedMouse);
    await context.UTEGear.SwitchTester.init();

    assert.equal(container.hidden, true);
});

test('selecting another switch updates active status and description', async () => {
    const { context, container, pills, desc } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    const bluePill = pills.children[1];
    bluePill.dispatchEvent({ type: 'click' });

    assert.equal(context.UTEGear.SwitchTester.activeSwitch.code, 'BLUE_CLICKY');
    assert.ok(desc.textContent.includes('Blue Switch'));
});

test('mute button toggles sound mute state', async () => {
    const { context, muteBtn } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    assert.equal(context.UTEGear.SwitchTester.isMuted, false);
    muteBtn.dispatchEvent({ type: 'click' });
    assert.equal(context.UTEGear.SwitchTester.isMuted, true);
    assert.equal(muteBtn.getAttribute('aria-pressed'), 'true');
});

test('compare panel toggles and renders A/B comparison table', async () => {
    const { context, compareToggle, comparePanel, tbody } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    assert.equal(comparePanel.hidden, true);
    compareToggle.dispatchEvent({ type: 'click' });
    assert.equal(comparePanel.hidden, false);
    assert.ok(tbody.children.length >= 5);
});

test('layout adapts to keyboard format and supports switching', async () => {
    const { context, chassis, container } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    assert.ok(chassis.classList.contains('layout-tkl'));
    const fullsizeBtn = container.querySelector('[data-layout-btn="FULLSIZE"]');
    fullsizeBtn.dispatchEvent({ type: 'click' });

    assert.ok(chassis.classList.contains('layout-fullsize'));
    assert.equal(context.UTEGear.SwitchTester.currentLayout, 'FULLSIZE');
});

test('3D mode toggles, rotates and resets view on test', async () => {
    const { context, container } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    const toggle3d = container.querySelector('[data-3d-toggle]');
    const text3d = container.querySelector('[data-3d-text]');

    assert.equal(typeof context.UTEGear.SwitchTester.resetView, 'function');
    toggle3d.dispatchEvent({ type: 'click' });
    assert.equal(toggle3d.getAttribute('aria-pressed'), 'true');
    assert.equal(text3d.textContent, 'Chế độ: 3D Xoay 360°');

    context.UTEGear.SwitchTester.resetView();
    assert.ok(true);
});

test('theater fullscreen mode toggles and closes properly', async () => {
    const { context, container } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    const fullscreenBtn = container.querySelector('[data-tester-fullscreen]');
    const theaterCloseBtn = container.querySelector('[data-theater-close]');
    const wrapper = container.querySelector('[data-keyboard-interactive]');
    const backdrop = container.querySelector('[data-theater-backdrop]');

    assert.equal(context.UTEGear.SwitchTester.isTheaterActive(), false);

    fullscreenBtn.dispatchEvent({ type: 'click' });
    assert.equal(context.UTEGear.SwitchTester.isTheaterActive(), true);
    assert.ok(wrapper.classList.contains('theater-mode'));
    assert.ok(backdrop.classList.contains('active'));

    theaterCloseBtn.dispatchEvent({ type: 'click' });
    assert.equal(context.UTEGear.SwitchTester.isTheaterActive(), false);
    assert.equal(wrapper.classList.contains('theater-mode'), false);
});

test('build3DKeycaps creates 5-face 3D pyramid frustum and stem inside every keycap', async () => {
    const { context, container } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    const keyA = container.querySelector('[data-key="KeyA"]');
    const body3d = keyA.querySelector('.vkey-3d-body');
    assert.ok(body3d, 'Every keycap should contain .vkey-3d-body');
    assert.equal(body3d.children.length, 5, 'Should have 4 beveled skirts + 1 switch stem');
});

test('RGB LED mode selector updates chassis lighting classes and status badge', async () => {
    const { context, chassis, rgbSelect, rgbBadge } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    assert.ok(chassis.classList.contains('rgb-wave'));
    assert.ok(rgbBadge.textContent.includes('Sóng Cầu Vồng'));

    rgbSelect.value = 'reactive-glow';
    rgbSelect.dispatchEvent({ type: 'change' });
    assert.ok(chassis.classList.contains('reactive-glow'));
    assert.ok(rgbBadge.textContent.includes('Reactive'));

    rgbSelect.value = 'cyber-cyan';
    rgbSelect.dispatchEvent({ type: 'change' });
    assert.ok(chassis.classList.contains('cyber-cyan'));
    assert.ok(rgbBadge.textContent.includes('Cyberpunk'));

    rgbSelect.value = 'per-key';
    rgbSelect.dispatchEvent({ type: 'change' });
    assert.ok(chassis.classList.contains('per-key'));
    assert.ok(rgbBadge.textContent.includes('Per-Key'));

    rgbSelect.value = 'rgb-off';
    rgbSelect.dispatchEvent({ type: 'change' });
    assert.ok(chassis.classList.contains('rgb-off'));
    assert.ok(rgbBadge.textContent.includes('Đã Tắt'));
});

test('clicking Buy Now without selecting switch blocks action and warns user to choose switch', async () => {
    const {
        context, purchaseBox, purchasePills, selectedSwitchBadge,
        buyNowBtn, isBuyNowExecuted
    } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    assert.equal(purchaseBox.hidden, false);
    assert.equal(purchasePills.children.length, 3);
    assert.equal(context.UTEGear.SwitchTester.purchaseSwitchConfirmed, undefined);

    // 1. User clicks Buy Now without selecting a switch -> blocked!
    buyNowBtn.click();
    assert.equal(isBuyNowExecuted(), false);
    assert.ok(selectedSwitchBadge.textContent.includes('Vui lòng chọn Switch'));
    assert.ok(purchaseBox.classList.contains('shake-highlight'));
});

test('clicking switch pill confirms switch and subsequent Buy Now executes immediately without modal', async () => {
    const {
        context, purchaseBox, purchasePills, selectedSwitchBadge, buyNowBtn, isBuyNowExecuted
    } = setupContext(mockData.productSupportedKeyboard);
    await context.UTEGear.SwitchTester.init();

    // User selects Blue Switch via pill
    purchasePills.children[1].dispatchEvent({ type: 'click' });
    assert.equal(context.UTEGear.SwitchTester.purchaseSwitchConfirmed, true);
    assert.equal(context.UTEGear.SwitchTester.activeSwitch.code, 'BLUE_CLICKY');
    assert.ok(selectedSwitchBadge.textContent.includes('Blue Switch'));
    assert.equal(purchaseBox.classList.contains('shake-highlight'), false);

    // Clicking Buy Now directly executes
    buyNowBtn.click();
    assert.equal(isBuyNowExecuted(), true);
});

test('Logitech G515 keyboard sets Lilac Purple arrow cluster and stealth keycaps', async () => {
    const g515Data = {
        ...mockData.productSupportedKeyboard,
        productName: 'Bàn phím cơ không dây Logitech G515 TKL Lightspeed',
        appearance: {
            layoutType: 'TKL',
            layoutDisplayName: 'Logitech G515 TKL Lightspeed',
            caseColor: '#121216',
            alphaColor: '#18181c',
            alphaTextColor: '#ffffff',
            modColor: '#18181c',
            modTextColor: '#ffffff',
            accentColor: '#8b5cf6',
            accentTextColor: '#ffffff'
        }
    };
    const { context, chassis } = setupContext(g515Data);
    await context.UTEGear.SwitchTester.init();

    assert.equal(chassis.style.getPropertyValue('--kb-arrow-color'), '#8b5cf6');
    assert.equal(chassis.style.getPropertyValue('--kb-space-color'), '#18181c');
    assert.equal(chassis.style.getPropertyValue('--kb-enter-color'), '#18181c');
    assert.equal(chassis.style.getPropertyValue('--kb-esc-color'), '#8b5cf6');
});

test('Logitech Compact 96% keyboard sets LAYOUT_96 and Lilac Purple arrow cluster', async () => {
    const logitech96Data = {
        ...mockData.productSupportedKeyboard,
        productName: 'Bàn phím cơ không dây Logitech Gaming Compact 96%',
        appearance: {
            layoutType: 'LAYOUT_96',
            layoutDisplayName: 'Logitech G Compact 96% (Numpad + Dual Knobs)',
            caseColor: '#121216',
            alphaColor: '#18181c',
            alphaTextColor: '#ffffff',
            modColor: '#18181c',
            modTextColor: '#ffffff',
            accentColor: '#8b5cf6',
            accentTextColor: '#ffffff'
        }
    };
    const { context, chassis } = setupContext(logitech96Data);
    await context.UTEGear.SwitchTester.init();

    assert.ok(chassis.classList.contains('layout-96'));
    assert.ok(chassis.classList.contains('brand-logitech'));
    assert.equal(chassis.style.getPropertyValue('--kb-arrow-color'), '#8b5cf6');
    assert.equal(chassis.style.getPropertyValue('--kb-esc-color'), '#8b5cf6');
    assert.equal(chassis.style.getPropertyValue('--kb-space-color'), '#18181c');
});
test('AULA F75 keyboard sets LAYOUT_75 and authentic amber orange keycap accents', async () => {
    const f75Data = {
        ...mockData.productSupportedKeyboard,
        productName: 'Bàn phím cơ không dây AULA F75 Glacier Navy Orange',
        appearance: {
            layoutType: 'LAYOUT_75',
            layoutDisplayName: 'AULA F75 (75% Compact - 81 Phím & Knob Bạc)',
            caseColor: '#121316',
            alphaColor: '#1e222d',
            alphaTextColor: '#fbbf24',
            modColor: '#161922',
            modTextColor: '#fbbf24',
            accentColor: '#f59e0b',
            accentTextColor: '#18181b'
        }
    };
    const { context, chassis } = setupContext(f75Data);
    await context.UTEGear.SwitchTester.init();

    assert.ok(chassis.classList.contains('layout-75'));
    assert.equal(chassis.classList.contains('brand-logitech'), false);
    assert.equal(chassis.style.getPropertyValue('--kb-arrow-color'), '#f59e0b');
    assert.equal(chassis.style.getPropertyValue('--kb-esc-color'), '#f59e0b');
    assert.equal(chassis.style.getPropertyValue('--kb-enter-color'), '#f59e0b');
    assert.equal(chassis.style.getPropertyValue('--kb-space-color'), '#f59e0b');
    assert.equal(chassis.style.getPropertyValue('--kb-alpha-text'), '#fbbf24');
});
