window.UTEGear = window.UTEGear || {};
UTEGear.MediaManager = {
    init(root) {
        const V = UTEGear.Views, controls = root.querySelector('[data-media-controls]');
        const select = root.querySelector('[data-media-product]'), upload = root.querySelector('[data-media-upload]');
        const host = root.querySelector('[data-media-images]'), title = root.querySelector('[data-media-product-name]');
        let busy = false, productId = null, revision = 0;
        const authorized = () => UTEGear.Session.get()?.user?.roles?.includes('ADMIN');
        const status = (message, error = false) => {
            const el = root.querySelector('[data-media-status]'); el.textContent = message;
            el.classList.toggle('text-danger', error); root.setAttribute('aria-busy', String(busy));
        };
        const clear = () => {
            revision++; productId = null; select.replaceChildren(new Option('Chọn sản phẩm', ''));
            title.textContent = ''; host.replaceChildren(); upload.reset(); upload.hidden = true;
            controls.disabled = true; this.lastReceipt = null;
            status('Chỉ quản trị viên đã đăng nhập được quản lý ảnh.', true);
        };
        const validFile = (file, required = false) => {
            if (!file) { if (required) throw Error('Vui lòng chọn ảnh.'); return; }
            if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || file.size > 5242880 || !file.size) {
                throw Error('Chọn ảnh JPEG, PNG hoặc WebP có dung lượng từ 1 byte đến 5 MiB.');
            }
        };
        const mutate = async (work, success) => {
            if (busy) return;
            if (!authorized()) { clear(); return; }
            busy = true; controls.disabled = true; status('Đang lưu ảnh…');
            try {
                const receipt = await work();
                if (receipt && (!receipt.secureUrl || !receipt.publicId)) throw Error('Máy chủ trả thông tin ảnh chưa đầy đủ.');
                if (!authorized()) { clear(); return; }
                this.lastReceipt = receipt || null;
                await loadProduct(productId); status(success);
            } catch (error) {
                if (authorized() && productId) { try { await loadProduct(productId); } catch { /* Keep current gallery while offline. */ } }
                status(error.message, true);
            }
            finally { busy = false; controls.disabled = !authorized(); root.setAttribute('aria-busy', 'false'); }
        };
        const row = image => {
            const article = V.node('article', null, 'border rounded p-3 mb-3'); article.dataset.mediaImageId = image.id;
            article.append(V.image(image.secureUrl, image.altText || title.textContent));
            const form = V.node('form', null, 'mt-2');
            const field = (labelText, name, type) => {
                const label = V.node('label', labelText, 'form-label'), input = V.node('input');
                input.type = type; input.name = name; input.id = `media-${image.id}-${name}`;
                input.className = type === 'checkbox' ? 'form-check-input mx-2' : 'form-control mb-2';
                label.htmlFor = input.id; form.append(label, input); return input;
            };
            const alt = field('Mô tả ảnh', 'altText', 'text'); alt.maxLength = 255; alt.value = image.altText || '';
            const primary = field('Ảnh chính', 'primary', 'checkbox'); primary.checked = image.primary;
            const replacement = field('Thay ảnh (không chọn file để chỉ sửa mô tả)', 'file', 'file');
            replacement.accept = 'image/jpeg,image/png,image/webp';
            const save = V.node('button', 'Lưu thay đổi', 'btn btn-outline-primary m-1'); save.type = 'submit';
            const remove = V.node('button', 'Xóa ảnh', 'btn btn-outline-danger m-1'); remove.type = 'button';
            const confirmation = V.node('div', null, 'my-2'); confirmation.hidden = true;
            const confirm = V.node('button', 'Xác nhận xóa ảnh', 'btn btn-danger m-1'); confirm.type = 'button';
            const cancel = V.node('button', 'Giữ ảnh', 'btn btn-outline-secondary m-1'); cancel.type = 'button';
            confirmation.append(V.node('p', 'Ảnh sẽ được xóa khỏi sản phẩm. Bạn có muốn tiếp tục?'), confirm, cancel);
            remove.addEventListener('click', () => { confirmation.hidden = false; confirm.focus(); });
            cancel.addEventListener('click', () => { confirmation.hidden = true; remove.focus(); });
            confirm.addEventListener('click', () => mutate(() => UTEGear.API.delete(`/api/admin/products/${productId}/images/${UTEGear.Admin.id(image.id)}`), 'Đã xóa ảnh.'));
            form.addEventListener('submit', event => {
                event.preventDefault(); if (!form.reportValidity() || busy) return;
                const file = replacement.files[0];
                try { validFile(file); } catch (error) { status(error.message, true); return; }
                const body = new FormData(); body.set('altText', alt.value); body.set('primary', String(primary.checked));
                if (file) body.set('file', file);
                mutate(() => UTEGear.API.request(`/api/admin/products/${productId}/images/${UTEGear.Admin.id(image.id)}`, { method: 'PUT', body }), file ? 'Đã thay ảnh.' : 'Đã cập nhật mô tả ảnh.');
            });
            form.append(save, remove, confirmation); article.append(form); return article;
        };
        const loadProduct = async id => {
            const current = ++revision;
            if (!id) { productId = null; title.textContent = ''; host.replaceChildren(); upload.hidden = true; return; }
            const product = await UTEGear.API.get('/api/products/' + UTEGear.Admin.id(id), { public: true });
            if (current !== revision || !authorized()) return;
            productId = product.id; title.textContent = product.name; host.replaceChildren();
            product.images.forEach(image => host.append(row(image)));
            if (!product.images.length) host.append(V.node('p', 'Sản phẩm chưa có ảnh.'));
            upload.hidden = false;
        };
        const search = async query => {
            if (busy || !authorized()) return;
            busy = true; controls.disabled = true;
            const current = ++revision; status('Đang tìm sản phẩm…');
            try {
                const page = await UTEGear.API.get('/api/products?' + new URLSearchParams({ q: query, size: 20, page: 0 }), { public: true });
                if (current !== revision || !authorized()) return;
                select.replaceChildren(new Option('Chọn sản phẩm', ''));
                page.content.forEach(product => select.add(new Option(product.name, product.id)));
                upload.reset(); await loadProduct(null); this.lastReceipt = null;
                status(page.content.length ? 'Chọn sản phẩm để quản lý ảnh.' : 'Không tìm thấy sản phẩm.');
            } finally { busy = false; controls.disabled = !authorized(); root.setAttribute('aria-busy', 'false'); }
        };
        root.querySelector('[data-media-search]').addEventListener('submit', event => {
            event.preventDefault(); if (!busy) search(event.target.elements.q.value).catch(error => status(error.message, true));
        });
        select.addEventListener('change', async () => {
            if (busy || !authorized()) return;
            busy = true; controls.disabled = true; this.lastReceipt = null;
            productId = null; upload.reset(); upload.hidden = true; host.replaceChildren(); title.textContent = '';
            status('Đang tải ảnh…');
            try { await loadProduct(select.value); status(''); }
            catch (error) { status(error.message, true); }
            finally { busy = false; controls.disabled = !authorized(); root.setAttribute('aria-busy', 'false'); }
        });
        upload.addEventListener('submit', event => {
            event.preventDefault(); if (busy || !productId || !upload.reportValidity()) return;
            const file = upload.elements.file.files[0];
            try { validFile(file, true); } catch (error) { status(error.message, true); return; }
            mutate(async () => {
                const result = await UTEGear.Admin.upload(productId, file, { altText: upload.elements.altText.value, primary: upload.elements.primary.checked });
                upload.reset(); return result;
            }, 'Đã tải ảnh lên.');
        });
        window.addEventListener('utegear:logout', clear);
        if (location.hash === '#product-media') root.open = true;
        if (!authorized()) { clear(); return; }
        controls.disabled = false; search('').catch(error => status(error.message, true));
    }
};
document.addEventListener('DOMContentLoaded', () => document.querySelectorAll('[data-media-manager]').forEach(root => UTEGear.MediaManager.init(root)));
