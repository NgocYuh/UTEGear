/**
 * UTEGear Variant Selection & Gallery Module
 */
window.UTEGear = window.UTEGear || {};

UTEGear.Variant = {
    selectedVariantId: null,

    select(btn) {
        if (!btn || btn.classList.contains('disabled')) return;

        const group = btn.closest('.variant-group');
        if (group) {
            group.querySelectorAll('.variant-option').forEach(b => b.classList.remove('selected'));
        }
        btn.classList.add('selected');

        this.selectedVariantId = btn.getAttribute('data-variant-id');
        const stock = parseInt(btn.getAttribute('data-stock') || '999');

        // Dynamic Stock Badge Update
        const stockContainer = document.querySelector('.stock-info');
        if (stockContainer) {
            if (stock <= 0) {
                stockContainer.innerHTML = `<span class="badge-status badge-status-out-stock"><i class="bi bi-x-circle"></i> Hết hàng</span>`;
            } else {
                stockContainer.innerHTML = `<span class="badge-status badge-status-in-stock"><i class="bi bi-check-circle"></i> Còn hàng (${stock} sản phẩm)</span>`;
            }
        }
    }
};

window.selectVariant = function (btn) {
    UTEGear.Variant.select(btn);
};

window.changeImage = function (src) {
    const mainImg = document.getElementById('mainProductImage');
    if (mainImg) mainImg.src = src;
};

window.changeQty = function (delta) {
    const input = document.getElementById('quantity');
    if (!input) return;
    let val = (parseInt(input.value) || 1) + delta;
    if (val < 1) val = 1;
    if (val > 99) val = 99;
    input.value = val;
};
