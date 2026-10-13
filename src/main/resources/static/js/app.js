/**
 * UTEGear Master Application Entry Point
 * Event Delegation & Module Initializer.
 */
document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Sub-modules
    if (UTEGear.Autocomplete) UTEGear.Autocomplete.init();
    if (UTEGear.Filter) UTEGear.Filter.init();

    // 2. Global Event Delegation Architecture
    document.body.addEventListener('click', (e) => {
        const target = e.target.closest('[data-action]');
        if (!target) return;

        const action = target.getAttribute('data-action');
        const productId = target.getAttribute('data-product-id');
        const variantId = target.getAttribute('data-variant-id');

        switch (action) {
            case 'add-to-cart':
                e.preventDefault();
                UTEGear.Cart.add({ productId, variantId, quantity: 1 });
                break;
            case 'toggle-wishlist':
                e.preventDefault();
                UTEGear.Wishlist.toggle(productId, target);
                break;
            case 'quick-view':
                e.preventDefault();
                if (window.quickView) window.quickView(productId);
                break;
            default:
                break;
        }
    });

    // 3. Track Recently Viewed Product on Detail Pages
    const detailContainer = document.querySelector('[data-page="product-detail"]');
    if (detailContainer) {
        const pId = detailContainer.getAttribute('data-product-id');
        if (pId && UTEGear.Utils) {
            UTEGear.Utils.addRecentlyViewed(Number(pId));
        }
    }
});
