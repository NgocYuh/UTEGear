/**
 * UTEGear Core Utilities
 */
window.UTEGear = window.UTEGear || {};

UTEGear.Utils = {
    formatPrice: function (amount) {
        if (amount === null || amount === undefined) return '0 ₫';
        return Number(amount).toLocaleString('vi-VN') + ' ₫';
    },

    escapeHtml: function (str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    },

    debounce: function (func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    // Storage utilities for Recently Viewed Products
    getRecentlyViewed: function () {
        try {
            return JSON.parse(localStorage.getItem('ute_recently_viewed') || '[]');
        } catch (e) {
            return [];
        }
    },

    addRecentlyViewed: function (productId) {
        if (!productId) return;
        let list = this.getRecentlyViewed();
        list = list.filter(id => id !== productId);
        list.unshift(productId);
        if (list.length > 10) list = list.slice(0, 10);
        localStorage.setItem('ute_recently_viewed', JSON.stringify(list));
    }
};
