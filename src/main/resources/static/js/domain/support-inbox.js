(() => {
    const root = document.querySelector('[data-support-inbox]');
    if (!root) return;
    const q = selector => root.querySelector(selector), list = q('[data-support-list]');
    const detail = q('[data-support-detail]'), form = q('[data-support-form]'), status = q('[data-support-status]');
    const title = q('[data-support-title]'), close = q('[data-support-close]'), input = q('#employee-support-input');
    const send = form.querySelector('button'), session = UTEGear.Session.get();
    const employee = session?.user?.roles?.includes('EMPLOYEE');
    let active = null, generation = 0, sending = false, connection, poll;
    const error = failure => { status.textContent = failure.message || 'Không tải được hội thoại.'; };
    const controls = () => {
        form.hidden = !employee || !active || active.status !== 'OPEN';
        close.hidden = !active || active.status !== 'OPEN';
        send.disabled = sending || !active || active.status !== 'OPEN';
    };
    const refreshDetail = async () => {
        if (!active) return;
        const current = generation, id = active.id;
        const result = await UTEGear.API.get(`/api/support/conversations/${id}`);
        if (current !== generation || active?.id !== id) return;
        active = result.conversation;
        title.textContent = `#${active.id} · ${active.customerName} · CN-${active.storeId} · ${active.storeName} · ${active.employeeId ? 'NV-' + active.employeeId : 'Chờ tiếp nhận'} · ${active.status === 'OPEN' ? 'Đang mở' : 'Đã đóng'}`;
        detail.replaceChildren(...result.messages.map(message => {
            const item = document.createElement('p');
            item.className = 'support-chat__message' + (message.senderId === session.user.id ? ' support-chat__message--mine' : '');
            item.textContent = `${message.senderName}: ${message.content}`;
            return item;
        }));
        detail.scrollTop = detail.scrollHeight;
        controls();
    };
    const open = async item => {
        const current = ++generation;
        active = null; controls(); input.value = '';
        try {
            if (!item.employeeId && employee && item.status === 'OPEN') {
                item = await UTEGear.API.put(`/api/support/employee/conversations/${item.id}/claim`, {});
            }
            if (current !== generation) return;
            active = item;
            await refreshDetail();
        } catch (failure) { if (current === generation) error(failure); }
    };
    const load = async () => {
        const items = await UTEGear.API.get('/api/support/employee/conversations');
        list.replaceChildren(...items.map(item => {
            const button = document.createElement('button');
            button.type = 'button'; button.className = 'support-inbox__item';
            button.textContent = `#${item.id} · ${item.customerName} · CN-${item.storeId} · ${item.employeeId ? 'NV-' + item.employeeId : 'Chờ nhận'} · ${item.status === 'OPEN' ? 'Đang mở' : 'Đã đóng'}`;
            button.setAttribute('aria-pressed', String(item.id === active?.id));
            button.addEventListener('click', () => open(item));
            return button;
        }));
        status.textContent = `${items.filter(item => item.status === 'OPEN').length} hội thoại đang mở / ${items.length} hội thoại.`;
    };
    const resync = async () => { await load(); await refreshDetail(); };
    form.addEventListener('submit', async event => {
        event.preventDefault();
        const content = input.value.trim(), id = active?.id, current = generation;
        if (!content || content.length > 2000 || !id || sending || form.hidden) return;
        sending = true; controls();
        try {
            await UTEGear.API.post(`/api/support/conversations/${id}/messages`, { content });
            if (current === generation) { input.value = ''; await refreshDetail(); }
        } catch (failure) { if (current === generation) error(failure); }
        finally { sending = false; controls(); }
    });
    close.addEventListener('click', async () => {
        if (!active || close.disabled) return;
        const id = active.id, current = generation;
        close.disabled = true;
        try {
            await UTEGear.API.put(`/api/support/conversations/${id}/close`, {});
            if (current === generation) await resync();
        } catch (failure) { error(failure); }
        finally { close.disabled = false; }
    });
    q('[data-support-refresh]').addEventListener('click', () => resync().catch(error));
    const stop = () => { generation++; clearInterval(poll); connection?.stop(); active = null; controls(); detail.replaceChildren(); list.replaceChildren(); };
    window.addEventListener('utegear:logout', stop);
    window.addEventListener('pagehide', stop);
    document.addEventListener('DOMContentLoaded', () => {
        if (!session || !(employee || session.user?.roles?.includes('ADMIN'))) return;
        controls();
        connection = UTEGear.Realtime.start({ support: true, resync, onEvent: () => resync().catch(error) });
        resync().catch(error);
        poll = setInterval(() => resync().catch(error), 8000);
    });
})();
