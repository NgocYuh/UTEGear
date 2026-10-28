window.UTEGear = window.UTEGear || {};
UTEGear.RecentlyViewed = {
    STORAGE_KEY: 'utegear_recently_viewed',
    MAX_ITEMS: 8,

    getItems() {
        try {
            const raw = localStorage.getItem(this.STORAGE_KEY);
            return raw ? JSON.parse(raw) : [];
        } catch {
            return [];
        }
    },

    saveItems(items) {
        try {
            localStorage.setItem(this.STORAGE_KEY, JSON.stringify(items.slice(0, this.MAX_ITEMS)));
        } catch {
            // Ignore quota errors
        }
    },

    record(product) {
        if (!product || !product.id) return;
        let items = this.getItems().filter(item => String(item.id) !== String(product.id));
        items.unshift({
            id: String(product.id),
            name: product.name || 'Sản phẩm UTEGear',
            price: product.price || '',
            imageUrl: product.imageUrl || '/images/placeholder.svg',
            category: product.category || 'Gear',
            url: product.url || `/products/${product.id}`
        });
        this.saveItems(items);
    },

    clear() {
        try {
            localStorage.removeItem(this.STORAGE_KEY);
        } catch {}
    },

    init() {
        const detailRoot = document.querySelector('[data-page="product-detail"]');
        if (detailRoot) {
            const productId = detailRoot.dataset.productId;
            const name = document.querySelector('.product-title')?.textContent?.trim();
            const imageUrl = document.getElementById('mainProductImage')?.src;
            const price = document.querySelector('[data-variant-price]')?.textContent?.trim();
            const category = document.querySelector('.product-category-badge')?.textContent?.trim();

            if (productId && name) {
                this.record({
                    id: productId,
                    name,
                    imageUrl,
                    price,
                    category,
                    url: window.location.pathname
                });
            }
            this.render(productId);
        } else {
            // Can also render on home or catalog pages if container is present
            this.render();
        }

        const clearBtn = document.querySelector('[data-clear-recent]');
        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                this.clear();
                const container = document.querySelector('[data-recently-viewed-container]');
                if (container) container.hidden = true;
            });
        }
    },

    render(excludeId = null) {
        const container = document.querySelector('[data-recently-viewed-container]');
        if (!container) return;

        const grid = container.querySelector('[data-recent-products-grid]');
        if (!grid) return;

        let items = this.getItems();
        if (excludeId) {
            items = items.filter(item => String(item.id) !== String(excludeId));
        }

        if (!items.length) {
            container.hidden = true;
            return;
        }

        container.hidden = false;
        grid.replaceChildren();

        for (const item of items) {
            const col = document.createElement('div');
            col.className = 'col';

            const card = document.createElement('div');
            card.className = 'card h-100 border shadow-sm rounded-3 overflow-hidden text-decoration-none text-dark';
            card.style.transition = 'transform 0.2s, box-shadow 0.2s';

            card.innerHTML = `
                <div class="position-relative bg-light text-center p-3" style="min-height: 160px; display: flex; align-items: center; justify-content: center;">
                    <img src="${item.imageUrl}" alt="${item.name}" class="img-fluid" style="max-height: 140px; object-fit: contain;" onerror="this.onerror=null;this.src='/images/placeholder.svg'">
                    <span class="badge bg-secondary position-absolute top-0 start-0 m-2 small opacity-75">${item.category}</span>
                </div>
                <div class="card-body p-3 d-flex flex-column">
                    <h4 class="card-title h6 fw-semibold mb-2 line-clamp-2" style="font-size: 0.9rem; min-height: 2.6rem;" title="${item.name}">${item.name}</h4>
                    <div class="mt-auto">
                        <p class="text-danger fw-bold mb-2" style="font-size: 0.95rem;">${item.price || 'Liên hệ'}</p>
                        <a href="${item.url}" class="btn btn-outline-primary btn-sm w-100">Xem lại</a>
                    </div>
                </div>
            `;

            col.appendChild(card);
            grid.appendChild(col);
        }
    }
};

document.addEventListener('DOMContentLoaded', () => UTEGear.RecentlyViewed.init());

