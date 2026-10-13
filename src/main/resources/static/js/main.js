// UTEGear Main JavaScript

// ===== TOAST NOTIFICATIONS =====
function showToast(message, type = 'success') {
    let container = document.querySelector('.toast-area');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-area';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast-msg ${type}`;
    const icons = { success: 'bi-check-circle', error: 'bi-x-circle', info: 'bi-info-circle' };
    toast.innerHTML = `<i class="bi ${icons[type] || icons.info}"></i><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all .3s';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ===== QUANTITY =====
function changeQty(delta) {
    const input = document.getElementById('quantity');
    if (!input) return;
    let val = parseInt(input.value) || 1;
    val += delta;
    if (val < 1) val = 1;
    if (val > 99) val = 99;
    input.value = val;
}

// ===== AJAX CART QUANTITY & REMOVE =====
async function updateCartQty(itemId, newQty) {
    const qty = parseInt(newQty) || 1;
    if (qty < 1) {
        removeCartItem(itemId);
        return;
    }
    try {
        const res = await fetch(`/api/cart/${itemId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantity: qty })
        });
        if (!res.ok) throw new Error('Không thể cập nhật số lượng');
        
        // Refresh cart page data seamlessly
        location.reload();
    } catch (e) {
        showToast(e.message || 'Lỗi cập nhật giỏ hàng', 'error');
    }
}

async function cartPlus(itemId) {
    const input = document.getElementById(`qty-input-${itemId}`);
    const currentVal = input ? parseInt(input.value) || 1 : 1;
    const newVal = currentVal + 1;
    if (input) input.value = newVal;
    await updateCartQty(itemId, newVal);
}

async function cartMinus(itemId) {
    const input = document.getElementById(`qty-input-${itemId}`);
    const currentVal = input ? parseInt(input.value) || 1 : 1;
    if (currentVal <= 1) {
        await removeCartItem(itemId);
        return;
    }
    const newVal = currentVal - 1;
    if (input) input.value = newVal;
    await updateCartQty(itemId, newVal);
}

async function removeCartItem(itemId) {
    try {
        const res = await fetch(`/api/cart/${itemId}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Không thể xóa sản phẩm');
        showToast('Đã xóa sản phẩm khỏi giỏ hàng', 'info');
        
        const itemRow = document.getElementById(`cart-item-${itemId}`);
        if (itemRow) {
            itemRow.style.opacity = '0';
            itemRow.style.transform = 'scale(0.95)';
            itemRow.style.transition = 'all 0.3s';
            setTimeout(() => location.reload(), 300);
        } else {
            location.reload();
        }
    } catch (e) {
        showToast(e.message || 'Có lỗi xảy ra', 'error');
    }
}

async function clearCartAjax() {
    try {
        const res = await fetch('/api/cart', { method: 'DELETE' });
        if (!res.ok) throw new Error('Không thể xóa giỏ hàng');
        showToast('Đã xóa toàn bộ giỏ hàng', 'info');
        setTimeout(() => location.reload(), 400);
    } catch (e) {
        showToast(e.message || 'Có lỗi xảy ra', 'error');
    }
}

// ===== PRODUCT IMAGE GALLERY =====
function changeImage(src) {
    const main = document.getElementById('mainProductImage');
    if (main) main.src = src;
}

// ===== VARIANTS =====
let selectedVariantId = null;
function selectVariant(btn) {
    if (btn.classList.contains('disabled')) return;
    document.querySelectorAll('.variant-option').forEach(b => b.classList.remove('selected'));
    btn.classList.add('selected');
    selectedVariantId = btn.getAttribute('data-variant-id');
}

// ===== ADD TO CART (UNIFIED) =====
async function addToCart(productId, variantId = null, quantity = 1) {
    try {
        const qty = parseInt(quantity) || 1;
        const res = await fetch('/api/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: productId, variantId: variantId, quantity: qty })
        });
        const data = await res.json();
        if (!res.ok) {
            if (res.status === 401) {
                showToast(data.message || 'Vui lòng đăng nhập để thêm vào giỏ hàng', 'error');
                setTimeout(() => { window.location.href = '/login'; }, 1200);
                return;
            }
            throw new Error(data.message || 'Không thể thêm vào giỏ hàng');
        }
        showToast('Đã thêm sản phẩm vào giỏ hàng!', 'success');
        
        // Update Cart Count Badges with Pulse animation
        const countElements = document.querySelectorAll('#cartCount, #mobileCartCount');
        countElements.forEach(el => {
            if (data.cartCount !== undefined) {
                el.textContent = data.cartCount;
                el.classList.add('pulse');
                setTimeout(() => el.classList.remove('pulse'), 400);
            }
        });
    } catch (e) {
        showToast(e.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng', 'error');
    }
}

function addToCartDetail(productId) {
    const btn = document.getElementById('addToCartBtn');
    if (btn && btn.disabled) return;
    const qtyInput = document.getElementById('quantity');
    const qty = qtyInput ? parseInt(qtyInput.value) || 1 : 1;
    const firstVariant = document.querySelector('.variant-option:not(.disabled)');
    let variantId = selectedVariantId || (firstVariant ? firstVariant.getAttribute('data-variant-id') : null);
    addToCart(productId, variantId, qty);
}

// Quick add from card
function quickAddToCart(productId) {
    addToCart(productId, null, 1);
}

// ===== WISHLIST =====
async function toggleWishlist(productId, btnElement = null) {
    try {
        const res = await fetch('/api/wishlist/toggle', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId })
        });
        const data = await res.json();
        if (!res.ok) {
            if (res.status === 401) {
                showToast(data.message || 'Vui lòng đăng nhập để thực hiện', 'error');
                setTimeout(() => { window.location.href = '/login'; }, 1200);
                return;
            }
            throw new Error(data.message || 'Lỗi');
        }
        const isAdded = data.added !== undefined ? data.added : (data.inWishlist !== undefined ? data.inWishlist : false);
        const toastMessage = data.message || (isAdded ? 'Đã thêm vào danh sách yêu thích' : 'Đã bỏ khỏi danh sách yêu thích');
        showToast(toastMessage, isAdded ? 'success' : 'info');

        // Toggle heart icon on buttons matching this productId
        const targetBtns = document.querySelectorAll(`button[onclick*="toggleWishlist(${productId})"], button[data-product-id="${productId}"].wishlist-btn`);
        targetBtns.forEach(btn => {
            const icon = btn.querySelector('i');
            if (icon) {
                if (isAdded) {
                    icon.className = 'bi bi-heart-fill text-danger';
                    btn.classList.add('active');
                } else {
                    icon.className = 'bi bi-heart';
                    btn.classList.remove('active');
                }
            }
        });
    } catch (e) {
        showToast(e.message || 'Lỗi', 'error');
    }
}

// ===== QUICK VIEW =====
async function quickView(productId) {
    try {
        const res = await fetch(`/api/products/${productId}`);
        const product = await res.json();
        const modalBody = document.getElementById('quickViewContent');
        if (!modalBody) return;
        const variantText = product.variants && product.variants.length ? product.variants[0].attributeName + ': ' + product.variants[0].attributeValue : '';
        modalBody.innerHTML = `
            <div class="modal-header"><h5 class="modal-title">${product.name}</h5>
            <button class="btn-close" data-bs-dismiss="modal"></button></div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-md-6"><img src="${product.imageUrl || '/images/placeholder.png'}" class="img-fluid rounded" alt="${product.name}"></div>
                    <div class="col-md-6">
                        <div class="product-detail-price"><span class="price current">${formatPrice(product.salePrice || product.price)}</span></div>
                        <p class="text-muted">${escapeHtml(product.description || '')}</p>
                        ${variantText ? `<p><strong>${escapeHtml(variantText)}</strong></p>` : ''}
                        <button class="btn btn-utama" onclick="quickAddToCart(${product.id})"><i class="bi bi-cart-plus"></i> Thêm vào giỏ</button>
                        <a href="/products/${product.id}" class="btn btn-outline-primary mt-2 w-100">Xem chi tiết</a>
                    </div>
                </div>
            </div>`;
        const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('quickViewModal'));
        modal.show();
    } catch (e) {
        showToast('Không thể tải sản phẩm', 'error');
    }
}

function formatPrice(v) {
    if (v === null || v === undefined) return '0 ₫';
    return Number(v).toLocaleString('vi-VN') + ' ₫';
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str || '';
    return div.innerHTML;
}

// ===== CHECKOUT STORE PICKUP TOGGLE =====
document.addEventListener('DOMContentLoaded', function () {
    const radioPickup = document.querySelector('input[name="shippingMethod"][value="STORE_PICKUP"]');
    const deliverySection = document.getElementById('storePickupSection');
    if (radioPickup && deliverySection) {
        radioPickup.addEventListener('change', function () {
            deliverySection.style.display = this.checked ? 'block' : 'none';
        });
        const radioHome = document.querySelector('input[name="shippingMethod"][value="HOME_DELIVERY"]');
        if (radioHome) {
            radioHome.addEventListener('change', function () {
                deliverySection.style.display = 'none';
            });
        }
    }
});
/* =========================================================
   UTEGEAR PRICE RANGE FILTER
   ========================================================= */

document.addEventListener("DOMContentLoaded", function () {

    const minRange = document.getElementById("minPriceRange");
    const maxRange = document.getElementById("maxPriceRange");

    const minInput = document.getElementById("minPriceInput");
    const maxInput = document.getElementById("maxPriceInput");


    if (!minRange || !maxRange || !minInput || !maxInput) {
        return;
    }


    function updateMinRange() {

        let min = Number(minRange.value);

        let max = Number(maxRange.value);


        if (min > max) {

            min = max;

            minRange.value = min;
        }


        minInput.value = min;
    }


    function updateMaxRange() {

        let min = Number(minRange.value);

        let max = Number(maxRange.value);


        if (max < min) {

            max = min;

            maxRange.value = max;
        }


        maxInput.value = max;
    }


    minRange.addEventListener(
        "input",
        updateMinRange
    );


    maxRange.addEventListener(
        "input",
        updateMaxRange
    );


    minInput.addEventListener(
        "input",
        function () {

            let value = Number(this.value);

            if (value < 0) {
                value = 0;
            }


            if (value > Number(maxRange.value)) {

                value = Number(maxRange.value);

            }


            minRange.value = value;
        }
    );


    maxInput.addEventListener(
        "input",
        function () {

            let value = Number(this.value);

            if (value < 0) {
                value = 0;
            }


            if (value < Number(minRange.value)) {

                value = Number(minRange.value);

            }


            maxRange.value = value;
        }
    );

});

// ===== MOBILE FILTER TOGGLE =====
function toggleMobileFilter() {
    const sidebar = document.getElementById("filterSidebar");
    const icon = document.getElementById("filterToggleIcon");
    if (!sidebar) return;
    sidebar.classList.toggle("show");
    if (icon) {
        if (sidebar.classList.contains("show")) {
            icon.className = "bi bi-chevron-up ms-auto";
        } else {
            icon.className = "bi bi-chevron-down ms-auto";
        }
    }
}

// ===== KEYBOARD ACCELERATORS (GLOBAL SHORTCUTS) =====
document.addEventListener('keydown', function (e) {
    // Ignore if typing inside form input fields or contenteditable
    const activeEl = document.activeElement;
    const isEditing = activeEl && (
        activeEl.tagName === 'INPUT' ||
        activeEl.tagName === 'TEXTAREA' ||
        activeEl.tagName === 'SELECT' ||
        activeEl.isContentEditable
    );

    // '/' key -> Focus Search Bar
    if (e.key === '/' && !isEditing) {
        const searchInput = document.querySelector('input[name="q"], #searchInput, .header-search input');
        if (searchInput) {
            e.preventDefault();
            searchInput.focus();
            searchInput.select();
        }
    }

    // 'Escape' key -> Close Active Modals, Offcanvases & Blur Inputs
    if (e.key === 'Escape') {
        if (isEditing) {
            activeEl.blur();
        }
        // Close Bootstrap Modals
        const openModal = document.querySelector('.modal.show');
        if (openModal && window.bootstrap && window.bootstrap.Modal) {
            const instance = window.bootstrap.Modal.getInstance(openModal);
            if (instance) instance.hide();
        }
        // Close Offcanvas Drawers
        const openOffcanvas = document.querySelector('.offcanvas.show');
        if (openOffcanvas && window.bootstrap && window.bootstrap.Offcanvas) {
            const instance = window.bootstrap.Offcanvas.getInstance(openOffcanvas);
            if (instance) instance.hide();
        }
    }
});