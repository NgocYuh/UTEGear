window.UTEGear = window.UTEGear || {};
UTEGear.Account = {
    async addresses(root) {
        const items = await UTEGear.API.get('/api/addresses');
        if (!UTEGear.Session.get()) return;
        const host = root.querySelector('[data-address-items]'); host.replaceChildren();
        const form = root.querySelector('[data-address-form]');
        const V = UTEGear.Views;
        for (const item of items) {
            const row = V.node('article', null, 'surface-card p-3 mb-3');
            const addressParts = [item.street, item.ward, item.province].filter(Boolean).join(', ');
            row.append(V.node('h3', item.fullName, 'h6'), V.node('p', `${addressParts} · ${item.phone}`));
            if (item.isDefault) row.append(V.node('strong', 'Địa chỉ mặc định'));
            row.append(V.button('Sửa', async () => {
                form.dataset.addressId = UTEGear.Cart.id(item.id);
                for (const el of form.elements) if (el.name) el.value = item[el.name] || '';
                form.scrollIntoView({ block: 'center' }); form.elements.fullName.focus();
            }, root));
            row.append(V.button('Xóa', async () => {
                await UTEGear.API.delete('/api/addresses/' + UTEGear.Cart.id(item.id));
                if (Number(form.dataset.addressId) === item.id) { form.reset(); delete form.dataset.addressId; }
                await this.addresses(root);
            }, root));
            if (!item.isDefault) row.append(V.button('Đặt mặc định', async () => {
                await UTEGear.API.put('/api/addresses/' + UTEGear.Cart.id(item.id) + '/default'); await this.addresses(root);
            }, root));
            host.append(row);
        }
        if (!items.length) host.append(V.node('p', 'Chưa có địa chỉ giao hàng.'));
    },
    bindProfile(root, profile) {
        const management = root.querySelector('[data-profile-management]'); management.hidden = false;
        const edit = root.querySelector('[data-profile-form]');
        edit.elements.fullName.value = profile.fullName; edit.elements.phone.value = profile.phone || '';
        if (root.dataset.accountBound) return;
        root.dataset.accountBound = 'true';
        const bind = (selector, work) => {
            const form = root.querySelector(selector);
            form.addEventListener('submit', event => {
                event.preventDefault();
                if (!form.reportValidity()) return;
                UTEGear.Views.action(form.querySelector('[type=submit]'), () => work(form, Object.fromEntries(new FormData(form))), root);
            });
        };
        bind('[data-profile-form]', async (_form, data) => {
            const updated = await UTEGear.API.patch('/api/profile', { fullName: data.fullName, phone: data.phone || null });
            const session = UTEGear.Session.get();
            if (session) UTEGear.Session.save({ ...session, user: updated });
            await UTEGear.Views.profile(root); UTEGear.Views.session(); UTEGear.Views.status(root, 'Đã cập nhật thông tin.');
        });
        bind('[data-password-form]', async (form, data) => {
            if (data.newPassword !== data.confirmPassword) throw Error('Mật khẩu xác nhận không khớp');
            await UTEGear.API.put('/api/profile/password', data);
            form.reset(); UTEGear.Session.clear(); location.href = '/login';
        });
        bind('[data-address-form]', async (form, data) => {
            if (form.dataset.addressId) await UTEGear.API.put('/api/addresses/' + UTEGear.Cart.id(form.dataset.addressId), data);
            else await UTEGear.API.post('/api/addresses', data);
            form.reset(); delete form.dataset.addressId;
            await this.addresses(root); UTEGear.Views.status(root, 'Đã lưu địa chỉ.');
        });
        root.querySelector('[data-address-reset]').addEventListener('click', () => {
            const form = root.querySelector('[data-address-form]'); form.reset(); delete form.dataset.addressId;
        });
        this.addresses(root).catch(error => UTEGear.Views.status(root, error.message, true));
    }
};
