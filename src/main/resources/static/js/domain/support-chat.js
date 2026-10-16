(() => {
    const q = selector => document.querySelector(selector);
    const panel = q('#support-chat-panel'), trigger = q('.support-chat__trigger');
    if (!panel || !trigger) return;
    const close = q('.support-chat__close'), store = q('#support-store');
    const messages = q('#support-chat-messages'), input = q('#support-chat-input');
    const form = q('#support-chat-form'), state = q('#support-chat-state'), access = q('#support-chat-access');
    const submit = form.querySelector('button');
    let conversation = null, history = [], generation = 0, refreshVersion = 0, connection, storeLoad, sending = false;
    const controls = () => {
        const enabled = conversation?.status === 'OPEN' && !sending;
        input.disabled = !enabled;
        submit.disabled = !enabled;
    };
    const render = items => {
        history = [...new Map(items.map(message => [message.id, message])).values()];
        const ownId = UTEGear.Session.get()?.user?.id;
        messages.replaceChildren(...history.map(message => {
            const item = document.createElement('p');
            item.className = 'support-chat__message' + (message.senderId === ownId ? ' support-chat__message--mine' : '');
            item.textContent = `${message.senderName}: ${message.content}`;
            return item;
        }));
        if (!history.length) {
            const empty = document.createElement('p');
            empty.className = 'support-chat__empty';
            empty.textContent = 'Bạn cần tư vấn gì? Gửi tin nhắn cho nhân viên chi nhánh.';
            messages.append(empty);
        }
        messages.scrollTop = messages.scrollHeight;
    };
    const refresh = async () => {
        if (!conversation) return;
        const current = generation, id = conversation.id, version = ++refreshVersion;
        const detail = await UTEGear.API.get('/api/support/conversations/' + id);
        if (current !== generation || conversation?.id !== id || version !== refreshVersion) return;
        conversation = detail.conversation;
        render(detail.messages);
        state.textContent = conversation.status === 'CLOSED' ? 'Hội thoại đã đóng. Chọn lại chi nhánh để mở hội thoại mới.'
            : conversation.employeeId ? `CN-${conversation.storeId} · NV-${conversation.employeeId} · ${conversation.employeeName}`
                : `CN-${conversation.storeId} · Đang chờ nhân viên tiếp nhận`;
        controls();
    };
    const reportError = error => { state.textContent = error.message || 'Chưa tải được hội thoại. Vui lòng thử lại.'; };
    const loadStores = () => {
        if (storeLoad) return storeLoad;
        storeLoad = UTEGear.API.get('/api/stores', { public: true }).then(data => {
            store.replaceChildren(new Option('Chọn chi nhánh', ''));
            for (const branch of (data.content || data || [])) store.add(new Option(`CN-${branch.id} · ${branch.name}`, branch.id));
            if (conversation) store.value = String(conversation.storeId);
        }).catch(error => { storeLoad = null; reportError(error); });
        return storeLoad;
    };
    const open = () => {
        panel.hidden = false;
        trigger.setAttribute('aria-expanded', 'true');
        close.focus();
        const session = UTEGear.Session.get();
        const customer = session?.user?.roles?.includes('CUSTOMER');
        access.hidden = Boolean(customer);
        access.href = session ? '/admin/support' : '/login?redirect=' + encodeURIComponent(location.pathname + location.search);
        access.textContent = session ? 'Mở hộp thư nhân viên' : 'Đăng nhập để chat với nhân viên';
        store.disabled = !customer;
        if (!customer) state.textContent = session ? 'Dùng hộp thư nhân viên để hỗ trợ khách.' : 'Vui lòng đăng nhập để nhắn tin';
        loadStores();
        refresh().catch(reportError);
    };
    const shut = () => { panel.hidden = true; trigger.setAttribute('aria-expanded', 'false'); trigger.focus(); };
    trigger.addEventListener('click', () => panel.hidden ? open() : shut());
    close.addEventListener('click', shut);
    document.addEventListener('keydown', event => { if (event.key === 'Escape' && !panel.hidden) shut(); });
    store.addEventListener('change', async () => {
        const current = ++generation;
        connection?.stop(); connection = null;
        conversation = null; sending = false; input.value = ''; controls(); render([]);
        if (!UTEGear.Session.get()?.user?.roles?.includes('CUSTOMER') || !store.value) return;
        state.textContent = 'Đang kết nối với chi nhánh…';
        try {
            const selected = await UTEGear.API.post('/api/support/conversations', { storeId: Number(store.value) });
            if (current !== generation) return;
            conversation = selected;
            await refresh();
            if (current !== generation) return;
            connection = UTEGear.Realtime.start({ support: true, resync: refresh,
                onEvent: event => {
                    const id = event.data.conversationId ?? event.data.id;
                    if (id === conversation?.id) refresh().catch(reportError);
                }
            });
        } catch (error) { if (current === generation) reportError(error); }
    });
    form.addEventListener('submit', async event => {
        event.preventDefault();
        const content = input.value.trim(), current = generation, id = conversation?.id;
        if (!content || content.length > 2000 || !id || input.disabled || sending) return;
        sending = true; controls();
        try {
            const message = await UTEGear.API.post(`/api/support/conversations/${id}/messages`, { content });
            if (current !== generation) return;
            refreshVersion++;
            input.value = ''; render(history.concat(message));
        } catch (error) { if (current === generation) reportError(error); }
        finally { if (current === generation) { sending = false; controls(); } }
    });
    const poll = setInterval(() => { if (!panel.hidden && conversation) refresh().catch(reportError); }, 8000);
    window.addEventListener('utegear:logout', () => {
        generation++; connection?.stop(); conversation = null; sending = false; controls();
        messages.replaceChildren(); state.textContent = 'Vui lòng đăng nhập để nhắn tin'; shut();
    });
    window.addEventListener('pagehide', () => { clearInterval(poll); connection?.stop(); });
})();
