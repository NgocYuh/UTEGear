window.UTEGear = window.UTEGear || {};

UTEGear.AdminPages = {
    state: { categories: [], brands: [], products: [], stores: [] },
    productStatusLabels: {
        ACTIVE: 'Hoạt động',
        INACTIVE: 'Tạm ẩn',
        DELETED: 'Ngừng kinh doanh'
    },
    roleLabels: {
        CUSTOMER: 'Khách hàng',
        EMPLOYEE: 'Nhân viên',
        ADMIN: 'Quản trị viên'
    },

    node(tag, text, className) {
        const element = document.createElement(tag);
        if (text != null) element.textContent = String(text);
        if (className) element.className = className;
        return element;
    },

    status(root, message, error = false) {
        const element = root.querySelector('[data-admin-status]');
        if (!element) return;
        element.textContent = message;
        element.classList.toggle('text-danger', error);
    },

    badge(active, activeText = 'Hoạt động', inactiveText = 'Tạm ẩn') {
        return this.node('span', active ? activeText : inactiveText,
                'badge-status ' + (active ? 'status-active' : 'status-inactive'));
    },

    cell(row, content, className) {
        const cell = this.node('td', null, className);
        if (content instanceof Node) cell.append(content);
        else cell.textContent = content == null || content === '' ? 'Chưa cập nhật' : String(content);
        row.append(cell);
        return cell;
    },

    emptyCell(row, className) {
        const cell = this.node('td', null, className);
        row.append(cell);
        return cell;
    },

    button(text, action, className = 'btn-admin-secondary') {
        const button = this.node('button', text, className);
        button.type = 'button';
        button.addEventListener('click', async () => {
            if (button.disabled) return;
            button.disabled = true;
            try { await action(); }
            catch (error) {
                const root = button.closest('[data-admin-page]');
                if (root) this.status(root, error.message, true);
            }
            finally { button.disabled = false; }
        });
        return button;
    },

    setEmpty(root, empty) {
        const message = root.querySelector('[data-admin-empty]');
        if (message) message.hidden = !empty;
    },

    resetForm(form) {
        form.reset();
        if (form.elements.id) form.elements.id.value = '';
        for (const checkbox of form.querySelectorAll('input[type="checkbox"]')) checkbox.checked = true;
        form.querySelector('[name="sortOrder"]')?.setAttribute('value', '0');
    },

    fillForm(form, item) {
        for (const [name, value] of Object.entries(item)) {
            const field = form.elements.namedItem(name);
            if (!field) continue;
            if (field.type === 'checkbox') field.checked = Boolean(value);
            else field.value = value == null ? '' : value;
        }
        form.scrollIntoView({ behavior: 'smooth', block: 'start' });
        form.querySelector('input:not([type="hidden"]), select, textarea')?.focus();
    },

    bindReset(form) {
        form.querySelector('[data-admin-reset]')?.addEventListener('click', () => {
            setTimeout(() => this.resetForm(form));
        });
    },

    async submit(form, root, work, success) {
        if (form.dataset.busy || !form.reportValidity()) return;
        form.dataset.busy = 'true';
        const button = form.querySelector('[type="submit"]');
        if (button) button.disabled = true;
        try {
            await work();
            this.status(root, success);
            this.resetForm(form);
        } catch (error) {
            this.status(root, error.message, true);
            throw error;
        } finally {
            delete form.dataset.busy;
            if (button) button.disabled = false;
        }
    },

    async dashboard(root) {
        const summary = await UTEGear.API.get('/api/admin/dashboard');
        root.querySelectorAll('[data-stat]').forEach(element => {
            const key = element.dataset.stat;
            if (key === 'totalRevenue') {
                element.textContent = Number(summary[key] || 0).toLocaleString('vi-VN') + ' ₫';
            } else {
                element.textContent = Number(summary[key] || 0).toLocaleString('vi-VN');
            }
        });

        const topBody = root.querySelector('[data-top-products-rows]');
        if (topBody && summary.topProducts) {
            topBody.replaceChildren();
            if (summary.topProducts.length === 0) {
                topBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">Chưa có dữ liệu bán hàng.</td></tr>';
            } else {
                summary.topProducts.forEach((p, idx) => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td class="text-center fw-bold text-muted">#${idx + 1}</td>
                        <td>
                            <div class="d-flex align-items-center gap-2">
                                <img src="${p.imageUrl}" alt="${p.name}" class="rounded border" style="width: 40px; height: 40px; object-fit: contain;" onerror="this.onerror=null;this.src='/images/placeholder.svg'">
                                <span class="fw-medium">${p.name}</span>
                            </div>
                        </td>
                        <td><span class="badge bg-light text-dark border">${p.categoryName}</span></td>
                        <td class="text-end fw-semibold">${Number(p.price || 0).toLocaleString('vi-VN')} ₫</td>
                        <td class="text-center"><span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">${p.soldCount} đã bán</span></td>
                    `;
                    topBody.appendChild(tr);
                });
            }
        }

        root.querySelector('[data-admin-content]').hidden = false;
        this.status(root, 'Số liệu đã được đồng bộ.');
    },

    async references(root, kind) {
        const form = root.querySelector(`[data-admin-form="${kind}"]`);
        const rows = root.querySelector('[data-admin-rows]');
        const load = async () => {
            const items = await UTEGear.API.get(`/api/admin/${kind}`);
            this.state[kind] = items;
            rows.replaceChildren();
            for (const [index, item] of items.entries()) {
                const row = this.node('tr');
                this.cell(row, index + 1, 'admin-index-cell');
                this.cell(row, item.name);
                this.cell(row, item.slug);
                if (kind === 'categories') this.cell(row, item.sortOrder);
                else this.cell(row, item.description);
                this.cell(row, this.badge(item.active));
                const actions = this.emptyCell(row, 'text-end admin-actions-cell');
                actions.append(this.button('Sửa', () => this.fillForm(form, item)));
                rows.append(row);
            }
            this.setEmpty(root, !items.length);
            this.status(root, items.length ? `Hiển thị ${items.length} ${kind === 'categories' ? 'danh mục' : 'thương hiệu'}.` : 'Chưa có dữ liệu.');
        };
        form.addEventListener('submit', event => {
            event.preventDefault();
            const fields = new FormData(form);
            const id = fields.get('id');
            const payload = {
                name: fields.get('name'),
                description: fields.get('description') || null,
                active: form.elements.active.checked
            };
            if (kind === 'categories') payload.sortOrder = Number(fields.get('sortOrder'));
            this.submit(form, root, async () => {
                if (id) await UTEGear.API.put(`/api/admin/${kind}/${UTEGear.Admin.id(id)}`, payload);
                else await UTEGear.API.post(`/api/admin/${kind}`, payload);
                await load();
            }, 'Đã lưu dữ liệu.').catch(() => {});
        });
        this.bindReset(form);
        await load();
    },

    parseObject(value, label) {
        try {
            const parsed = JSON.parse(value || '{}');
            if (!parsed || Array.isArray(parsed) || typeof parsed !== 'object') throw new Error();
            return parsed;
        } catch {
            throw new Error(`${label} chưa đúng định dạng tên và giá trị.`);
        }
    },

    productPayload(form) {
        const fields = new FormData(form);
        return {
            name: fields.get('name'),
            categoryId: UTEGear.Admin.id(fields.get('categoryId')),
            brandId: fields.get('brandId') ? UTEGear.Admin.id(fields.get('brandId')) : null,
            description: fields.get('description') || null,
            basePrice: Number(fields.get('basePrice')),
            salePrice: fields.get('salePrice') ? Number(fields.get('salePrice')) : null,
            status: fields.get('status'),
            defaultSku: fields.get('defaultSku'),
            optionValues: this.parseObject(fields.get('optionValues'), 'Lựa chọn biến thể'),
            specifications: this.parseObject(fields.get('specifications'), 'Thông số sản phẩm')
        };
    },

    async products(root) {
        const form = root.querySelector('[data-admin-form="products"]');
        const rows = root.querySelector('[data-admin-rows]');
        const search = root.querySelector('[data-admin-search="products"]');
        const [categories, brands] = await Promise.all([
            UTEGear.API.get('/api/admin/categories'), UTEGear.API.get('/api/admin/brands')
        ]);
        this.state.categories = categories;
        this.state.brands = brands;
        const categorySelect = form.elements.categoryId;
        const brandSelect = form.elements.brandId;
        categories.forEach(item => categorySelect.add(new Option(item.name + (item.active ? '' : ' (tạm ẩn)'), item.id)));
        brands.forEach(item => brandSelect.add(new Option(item.name + (item.active ? '' : ' (tạm ẩn)'), item.id)));
        const edit = item => this.fillForm(form, {
            ...item,
            optionValues: JSON.stringify(item.optionValues || {}, null, 2),
            specifications: JSON.stringify(item.specifications || {}, null, 2)
        });
        const load = async (query = '') => {
            const page = await UTEGear.API.get('/api/admin/products?' + new URLSearchParams({ q: query, page: 0, size: 100 }));
            this.state.products = page.content;
            rows.replaceChildren();
            for (const [index, item] of page.content.entries()) {
                const row = this.node('tr');
                this.cell(row, page.page * page.size + index + 1, 'admin-index-cell');
                const product = this.node('div', null, 'admin-product-cell');
                const info = this.node('div');
                const link = this.node('a', item.name, 'admin-product-title');
                link.href = `/products/${item.id}`; link.target = '_blank'; link.rel = 'noopener';
                info.append(link, this.node('small', `${item.categoryName}${item.brandName ? ' · ' + item.brandName : ''}`, 'd-block text-muted'));
                product.append(info);
                this.cell(row, product);
                this.cell(row, item.defaultSku);
                this.cell(row, UTEGear.Utils.formatPrice(item.salePrice ?? item.basePrice));
                this.cell(row, item.totalStock);
                const statusLabel = this.productStatusLabels[item.status] || 'Không xác định';
                this.cell(row, this.badge(item.status === 'ACTIVE', statusLabel, statusLabel));
                const actions = this.emptyCell(row, 'text-end admin-actions-cell');
                actions.append(this.button('Sửa', () => edit(item)));
                const nextStatus = item.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
                actions.append(this.button(nextStatus === 'ACTIVE' ? 'Kích hoạt' : 'Tạm ẩn', async () => {
                    await UTEGear.API.patch(`/api/admin/products/${item.id}/status`, { status: nextStatus });
                    await load(search.elements.q.value);
                    this.status(root, 'Đã cập nhật trạng thái sản phẩm.');
                }, 'btn-admin-secondary ms-2'));
                rows.append(row);
            }
            rows.closest('.admin-table-container').scrollLeft = 0;
            this.setEmpty(root, page.empty);
            this.status(root, page.empty ? 'Không tìm thấy sản phẩm phù hợp.' : `Hiển thị ${page.content.length} trên tổng số ${page.totalElements} sản phẩm.`);
        };
        form.addEventListener('submit', event => {
            event.preventDefault();
            const id = form.elements.id.value;
            let payload;
            try { payload = this.productPayload(form); }
            catch (error) { this.status(root, error.message, true); return; }
            this.submit(form, root, async () => {
                if (id) await UTEGear.API.put(`/api/admin/products/${UTEGear.Admin.id(id)}`, payload);
                else await UTEGear.API.post('/api/admin/products', payload);
                await load(search.elements.q.value);
            }, 'Đã lưu sản phẩm.').catch(() => {});
        });
        search.addEventListener('submit', event => { event.preventDefault(); load(search.elements.q.value).catch(error => this.status(root, error.message, true)); });
        this.bindReset(form);
        await load(new URLSearchParams(location.search).get('q') || '');
        const routeId = location.pathname.match(/^\/admin\/products\/(\d+)\/edit$/)?.[1];
        if (routeId) edit(await UTEGear.API.get(`/api/admin/products/${routeId}`));
    },

    storePayload(form) {
        const fields = new FormData(form);
        const nullable = name => fields.get(name) || null;
        return {
            name: fields.get('name'), address: fields.get('address'), province: fields.get('province'),
            district: nullable('district'), phone: nullable('phone'), email: nullable('email'),
            imageUrl: nullable('imageUrl'), openingTime: fields.get('openingTime'), closingTime: fields.get('closingTime'),
            description: nullable('description'), latitude: null, longitude: null, active: form.elements.active.checked
        };
    },

    fillSelect(select, items, label) {
        select.replaceChildren(new Option('Chọn', ''));
        items.forEach(item => select.add(new Option(label(item), item.id)));
    },

    async stores(root) {
        const form = root.querySelector('[data-admin-form="stores"]');
        const rows = root.querySelector('[data-admin-rows]');
        const inventoryStore = root.querySelector('[data-inventory-store]');
        const inventoryVariant = root.querySelector('[data-inventory-variant]');
        const transfer = root.querySelector('[data-transfer-form]');
        const inventoryRows = root.querySelector('[data-inventory-rows]');
        const loadStores = async () => {
            const page = await UTEGear.API.get('/api/admin/stores?page=0&size=100');
            this.state.stores = page.content;
            rows.replaceChildren();
            for (const item of page.content) {
                const row = this.node('tr');
                this.cell(row, item.name); this.cell(row, `${item.address}, ${item.province}`);
                this.cell(row, `${item.openingTime}–${item.closingTime}`); this.cell(row, this.badge(item.active));
                const actions = this.emptyCell(row, 'text-end admin-actions-cell');
                actions.append(this.button('Sửa', () => this.fillForm(form, item)));
                actions.append(this.button('Xem kho', async () => { inventoryStore.value = item.id; await loadInventory(); root.querySelector('[data-inventory-panel]').scrollIntoView({ behavior: 'smooth' }); }, 'btn-admin-secondary ms-2'));
                rows.append(row);
            }
            this.setEmpty(root, page.empty);
            const activeStores = page.content.filter(item => item.active);
            this.fillSelect(inventoryStore, activeStores, item => item.name);
            this.fillSelect(transfer.elements.sourceStoreId, activeStores, item => item.name);
            this.fillSelect(transfer.elements.destinationStoreId, activeStores, item => item.name);
            return page;
        };
        const loadVariants = async () => {
            const page = await UTEGear.API.get('/api/admin/products?page=0&size=100&status=ACTIVE');
            const variants = page.content.flatMap(item => (item.variants || [])
                .filter(variant => variant.active)
                .map(variant => ({ id: variant.id, name: item.name, sku: variant.sku })));
            this.fillSelect(inventoryVariant, variants, item => `${item.name} · ${item.sku}`);
            this.fillSelect(transfer.elements.variantId, variants, item => `${item.name} · ${item.sku}`);
        };
        const loadInventory = async () => {
            const params = inventoryStore.value ? '?' + new URLSearchParams({ storeId: inventoryStore.value }) : '';
            const items = await UTEGear.API.get('/api/admin/inventory' + params);
            inventoryRows.replaceChildren();
            for (const item of items) {
                const row = this.node('tr');
                this.cell(row, item.storeName); this.cell(row, item.productName); this.cell(row, item.sku);
                this.cell(row, item.quantity); this.cell(row, new Date(item.updatedAt).toLocaleString('vi-VN'));
                inventoryRows.append(row);
            }
            root.querySelector('[data-inventory-empty]').hidden = Boolean(items.length);
        };
        form.addEventListener('submit', event => {
            event.preventDefault(); const id = form.elements.id.value; const payload = this.storePayload(form);
            this.submit(form, root, async () => {
                if (id) await UTEGear.API.put(`/api/admin/stores/${UTEGear.Admin.id(id)}`, payload);
                else await UTEGear.API.post('/api/admin/stores', payload);
                await loadStores(); await loadInventory();
            }, 'Đã lưu cửa hàng.').catch(() => {});
        });
        root.querySelector('[data-inventory-save]').addEventListener('click', async () => {
            try {
                await UTEGear.Admin.inventory(inventoryStore.value, inventoryVariant.value, Number(root.querySelector('[data-inventory-quantity]').value));
                await loadInventory(); this.status(root, 'Đã cập nhật tồn kho.');
            } catch (error) { this.status(root, error.message, true); }
        });
        inventoryStore.addEventListener('change', () => loadInventory().catch(error => this.status(root, error.message, true)));
        transfer.addEventListener('submit', event => {
            event.preventDefault(); const fields = new FormData(transfer);
            this.submit(transfer, root, async () => {
                await UTEGear.Admin.transfer(fields.get('sourceStoreId'), fields.get('destinationStoreId'), fields.get('variantId'), Number(fields.get('quantity')));
                await loadInventory();
            }, 'Đã điều chuyển tồn kho.').catch(() => {});
        });
        this.bindReset(form);
        await Promise.all([loadStores(), loadVariants()]);
        const routeId = location.pathname.match(/^\/admin\/stores\/(\d+)$/)?.[1];
        if (routeId) {
            inventoryStore.value = routeId;
            this.fillForm(form, await UTEGear.API.get(`/api/admin/stores/${routeId}`));
        }
        await loadInventory();
        this.status(root, 'Dữ liệu cửa hàng và tồn kho đã được đồng bộ.');
    },

    couponPayload(form) {
        const fields = new FormData(form);
        const instant = name => new Date(fields.get(name)).toISOString();
        return {
            code: fields.get('code'), discountType: fields.get('discountType'),
            discountValue: Number(fields.get('discountValue')),
            minimumOrderValue: Number(fields.get('minimumOrderValue') || 0),
            maximumDiscount: fields.get('maximumDiscount') ? Number(fields.get('maximumDiscount')) : null,
            validFrom: instant('validFrom'), validUntil: instant('validUntil'),
            maxUses: Number(fields.get('maxUses')), active: form.elements.active.checked
        };
    },

    dateInput(value) {
        if (!value) return '';
        const date = new Date(value); const offset = date.getTimezoneOffset() * 60000;
        return new Date(date.getTime() - offset).toISOString().slice(0, 16);
    },

    async coupons(root) {
        const form = root.querySelector('[data-admin-form="coupons"]'); const rows = root.querySelector('[data-admin-rows]');
        const load = async () => {
            const items = await UTEGear.API.get('/api/admin/coupons'); rows.replaceChildren();
            for (const item of items) {
                const row = this.node('tr'); this.cell(row, item.code);
                this.cell(row, item.discountType === 'PERCENTAGE' ? `${item.discountValue}%` : UTEGear.Utils.formatPrice(item.discountValue));
                this.cell(row, `Từ ${UTEGear.Utils.formatPrice(item.minimumOrderValue)}${item.maximumDiscount == null ? '' : ', tối đa ' + UTEGear.Utils.formatPrice(item.maximumDiscount)}`);
                this.cell(row, `${new Date(item.validFrom).toLocaleString('vi-VN')} đến ${new Date(item.validUntil).toLocaleString('vi-VN')}`);
                this.cell(row, `${item.usedCount}/${item.maxUses || '∞'}`); this.cell(row, this.badge(item.active));
                const actions = this.emptyCell(row, 'text-end admin-actions-cell');
                actions.append(this.button('Sửa', () => this.fillForm(form, { ...item, validFrom: this.dateInput(item.validFrom), validUntil: this.dateInput(item.validUntil) })));
                rows.append(row);
            }
            this.setEmpty(root, !items.length); this.status(root, items.length ? `Hiển thị ${items.length} mã giảm giá.` : 'Chưa có mã giảm giá.');
        };
        form.addEventListener('submit', event => {
            event.preventDefault(); const id = form.elements.id.value; let payload;
            try { payload = this.couponPayload(form); } catch { this.status(root, 'Thời gian hiệu lực không hợp lệ.', true); return; }
            this.submit(form, root, async () => {
                if (id) await UTEGear.API.put(`/api/admin/coupons/${UTEGear.Admin.id(id)}`, payload);
                else await UTEGear.API.post('/api/admin/coupons', payload);
                await load();
            }, 'Đã lưu mã giảm giá.').catch(() => {});
        });
        this.bindReset(form); await load();
    },

    async users(root) {
        const rows = root.querySelector('[data-admin-rows]');
        const currentEmail = UTEGear.Session.get()?.user?.email?.toLowerCase();
        const load = async () => {
            const [items, storePage] = await Promise.all([UTEGear.API.get('/api/admin/users?page=0&size=100'), UTEGear.API.get('/api/admin/stores?page=0&size=100')]); rows.replaceChildren();
            for (const item of items) {
                const row = this.node('tr'); this.cell(row, item.fullName);
                this.cell(row, `${item.email}${item.phone ? ' · ' + item.phone : ''}`);
                const roles = this.node('div', null, 'd-flex gap-2 flex-wrap');
                for (const role of ['CUSTOMER', 'EMPLOYEE', 'ADMIN']) {
                    const label = this.node('label', null, 'form-check form-check-inline');
                    const input = this.node('input'); input.type = 'checkbox'; input.className = 'form-check-input'; input.value = role; input.checked = item.roles.includes(role);
                    if (item.email.toLowerCase() === currentEmail && role === 'ADMIN') {
                        input.checked = true;
                        input.disabled = true;
                        input.title = 'Không thể tự gỡ vai trò Quản trị viên của phiên đang dùng';
                    }
                    label.append(input, document.createTextNode(' ' + this.roleLabels[role])); roles.append(label);
                }
                this.cell(row, roles);
                const assignment = document.createElement('select'); assignment.className = 'form-select form-select-sm';
                assignment.add(new Option('Chưa phân công', ''));
                storePage.content.filter(store => store.active).forEach(store => assignment.add(new Option(store.name, store.id)));
                assignment.value = item.assignedStoreId || '';
                assignment.disabled = !item.roles.includes('EMPLOYEE');
                roles.querySelector('input[value="EMPLOYEE"]').addEventListener('change', event => { assignment.disabled = !event.target.checked; if (assignment.disabled) assignment.value = ''; });
                this.cell(row, assignment); this.cell(row, this.badge(item.enabled, 'Đang dùng', 'Đã khóa'));
                this.cell(row, new Date(item.createdAt).toLocaleDateString('vi-VN'));
                const actions = this.emptyCell(row, 'text-end admin-actions-cell');
                const ownAccount = item.email.toLowerCase() === currentEmail;
                const saveRoles = this.button('Lưu quyền', async () => {
                    const selected = [...roles.querySelectorAll('input:checked')].map(input => input.value);
                    await UTEGear.API.put(`/api/admin/users/${item.id}/roles`, { roles: selected });
                    if (selected.includes('EMPLOYEE')) await UTEGear.API.put(`/api/admin/users/${item.id}/assigned-store`, { storeId: assignment.value ? Number(assignment.value) : null });
                    await load();
                    this.status(root, 'Đã cập nhật vai trò. Người dùng cần đăng nhập lại để áp dụng quyền mới.');
                });
                saveRoles.title = ownAccount ? 'Có thể bỏ vai trò Khách hàng nhưng phải giữ vai trò Quản trị viên cho phiên đang dùng' : '';
                const toggle = this.button(item.enabled ? 'Khóa' : 'Mở khóa', async () => {
                    await UTEGear.API.patch(`/api/admin/users/${item.id}/enabled`, { enabled: !item.enabled }); await load();
                    this.status(root, 'Đã cập nhật trạng thái tài khoản.');
                }, 'btn-admin-secondary ms-2');
                toggle.disabled = ownAccount; toggle.title = ownAccount ? 'Không tự khóa phiên đang dùng' : '';
                actions.append(saveRoles, toggle); rows.append(row);
            }
            this.setEmpty(root, !items.length); this.status(root, items.length ? `Hiển thị ${items.length} tài khoản.` : 'Chưa có tài khoản.');
        };
        await load();
    },

    async init() {
        const root = document.querySelector('[data-admin-page]');
        if (!root || !UTEGear.Views.adminAccess()) return;
        const page = root.dataset.adminPage;
        try {
            if (page === 'dashboard') await this.dashboard(root);
            else if (page === 'categories' || page === 'brands') await this.references(root, page);
            else if (typeof this[page] === 'function') await this[page](root);
        } catch (error) {
            this.status(root, error.message, true);
        }
    }
};

document.addEventListener('DOMContentLoaded', () => UTEGear.AdminPages.init());
