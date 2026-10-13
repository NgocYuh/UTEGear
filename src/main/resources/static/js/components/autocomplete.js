/**
 * UTEGear Search Autocomplete Widget
 */
window.UTEGear = window.UTEGear || {};

UTEGear.Autocomplete = {
    init() {
        const searchInputs = document.querySelectorAll('.search-input');
        searchInputs.forEach(input => {
            const wrapper = input.closest('.search-form') || input.parentElement;
            if (!wrapper) return;

            let dropdown = wrapper.querySelector('.search-autocomplete-dropdown');
            if (!dropdown) {
                dropdown = document.createElement('div');
                dropdown.className = 'search-autocomplete-dropdown';
                wrapper.appendChild(dropdown);
            }

            input.addEventListener('input', UTEGear.Utils.debounce(async (e) => {
                const query = e.target.value.trim();
                if (query.length < 2) {
                    dropdown.classList.remove('show');
                    dropdown.innerHTML = '';
                    return;
                }

                try {
                    const data = await UTEGear.API.get(`/api/products?keyword=${encodeURIComponent(query)}&size=5`);
                    const products = data.products || data.content || (Array.isArray(data) ? data : []);
                    this.render(dropdown, products, query);
                } catch (err) {
                    dropdown.classList.remove('show');
                }
            }, 250));

            // Keyboard Accessibility Navigation
            input.addEventListener('keydown', (e) => {
                if (!dropdown.classList.contains('show')) return;
                const items = Array.from(dropdown.querySelectorAll('.autocomplete-item'));
                const currentIndex = items.findIndex(item => item.classList.contains('selected'));

                if (e.key === 'ArrowDown') {
                    e.preventDefault();
                    const nextIndex = currentIndex < items.length - 1 ? currentIndex + 1 : 0;
                    items.forEach(i => i.classList.remove('selected'));
                    if (items[nextIndex]) items[nextIndex].classList.add('selected');
                } else if (e.key === 'ArrowUp') {
                    e.preventDefault();
                    const prevIndex = currentIndex > 0 ? currentIndex - 1 : items.length - 1;
                    items.forEach(i => i.classList.remove('selected'));
                    if (items[prevIndex]) items[prevIndex].classList.add('selected');
                } else if (e.key === 'Enter' && currentIndex >= 0) {
                    e.preventDefault();
                    items[currentIndex].click();
                } else if (e.key === 'Escape') {
                    dropdown.classList.remove('show');
                }
            });

            document.addEventListener('click', (e) => {
                if (!wrapper.contains(e.target)) {
                    dropdown.classList.remove('show');
                }
            });
        });
    },

    render(dropdown, products, query) {
        if (!products || products.length === 0) {
            dropdown.innerHTML = `<div class="p-3 text-center text-muted font-medium" style="font-size: 0.85rem;">Không tìm thấy sản phẩm cho "${UTEGear.Utils.escapeHtml(query)}"</div>`;
            dropdown.classList.add('show');
            return;
        }

        let html = `<div class="autocomplete-section-title">Gợi ý sản phẩm (${products.length})</div>`;
        products.forEach(item => {
            const priceHtml = item.salePrice ? UTEGear.Utils.formatPrice(item.salePrice) : UTEGear.Utils.formatPrice(item.price);
            html += `
                <a href="/products/${item.id}" class="autocomplete-item">
                    <img src="${item.imageUrl || '/images/placeholder.png'}" class="autocomplete-item-img" alt="${UTEGear.Utils.escapeHtml(item.name)}" onerror="this.src='/images/placeholder.png'">
                    <div>
                        <div class="autocomplete-item-name">${UTEGear.Utils.escapeHtml(item.name)}</div>
                        <div class="text-muted" style="font-size: 0.75rem;">${item.brand ? UTEGear.Utils.escapeHtml(item.brand.name) : 'UTEGear'}</div>
                    </div>
                    <div class="autocomplete-item-price">${priceHtml}</div>
                </a>
            `;
        });

        dropdown.innerHTML = html;
        dropdown.classList.add('show');
    }
};
