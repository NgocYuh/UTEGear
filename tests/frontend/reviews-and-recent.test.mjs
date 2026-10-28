import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const recentlyViewedJs = fs.readFileSync(new URL('../../src/main/resources/static/js/domain/recently-viewed.js', import.meta.url), 'utf8');
const reviewsJs = fs.readFileSync(new URL('../../src/main/resources/static/js/domain/reviews.js', import.meta.url), 'utf8');

test('RecentlyViewed records items and respects MAX_ITEMS', () => {
    const storage = new Map();
    const context = vm.createContext({
        localStorage: {
            getItem: k => storage.get(k) || null,
            setItem: (k, v) => storage.set(k, String(v)),
            removeItem: k => storage.delete(k)
        },
        document: { addEventListener() {} },
        UTEGear: {}
    });
    context.window = context;

    vm.runInContext(recentlyViewedJs, context);
    const RV = context.UTEGear.RecentlyViewed;

    assert.ok(RV, 'UTEGear.RecentlyViewed should be defined');
    RV.clear();

    for (let i = 1; i <= 12; i++) {
        RV.record({ id: String(i), name: `Product ${i}`, price: `${i * 100000} ₫`, category: 'Phím' });
    }

    const items = RV.getItems();
    assert.equal(items.length, 8, 'Should limit to MAX_ITEMS = 8');
    assert.equal(items[0].id, '12', 'Most recently recorded item should be at the front');
});

test('Reviews character counter validates minimum 50 characters', () => {
    let inputListener = null;
    const textarea = {
        value: '',
        addEventListener(event, fn) { if (event === 'input') inputListener = fn; }
    };
    const counter = { textContent: '', className: '' };
    const submitBtn = { disabled: true };

    const root = {
        querySelector(sel) {
            if (sel === '#reviewComment') return textarea;
            if (sel === '#reviewCharCount') return counter;
            if (sel === '#btnSubmitReview') return submitBtn;
            return null;
        }
    };

    const context = vm.createContext({
        document: { addEventListener() {} },
        UTEGear: {}
    });
    context.window = context;

    vm.runInContext(reviewsJs, context);
    const R = context.UTEGear.Reviews;
    R.setupCharCounter(root);

    // Test under 50 chars
    textarea.value = 'Bàn phím gõ rất tốt!';
    inputListener();
    assert.equal(submitBtn.disabled, true, 'Submit button should remain disabled under 50 chars');
    assert.ok(counter.textContent.includes('20 / 50'));

    // Test over 50 chars
    textarea.value = 'Bàn phím cơ gõ rất êm ái, switch nảy và âm thanh rất đầm tay, đóng gói cẩn thận!';
    inputListener();
    assert.equal(submitBtn.disabled, false, 'Submit button should be enabled when length >= 50');
    assert.ok(counter.className.includes('text-success'));
});

test('Reviews star renderer produces correct stars html', () => {
    const context = vm.createContext({
        document: { addEventListener() {} },
        UTEGear: {}
    });
    context.window = context;
    vm.runInContext(reviewsJs, context);
    const R = context.UTEGear.Reviews;

    const full5 = R.renderStarsHtml(5.0);
    assert.equal((full5.match(/bi-star-fill/g) || []).length, 5, '5.0 should render 5 full stars');

    const half45 = R.renderStarsHtml(4.5);
    assert.equal((half45.match(/bi-star-fill/g) || []).length, 4, '4.5 should render 4 full stars');
    assert.equal((half45.match(/bi-star-half/g) || []).length, 1, '4.5 should render 1 half star');
});

