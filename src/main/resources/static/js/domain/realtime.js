window.UTEGear = window.UTEGear || {};

// Stomp is supplied by the page. BE /ws is native WebSocket, not SockJS.
UTEGear.Realtime = {
    start({ admin = false, orderId = null, resync, onEvent, onState = () => {} }) {
        if (typeof resync !== 'function') throw new Error('Cần tải lại REST state trước khi nhận realtime');
        let client, retry, expiry, stopped = false, attempt = 0, generation = 0;
        const seen = new Set();
        const accepts = event => {
            if (!event || typeof event.eventId !== 'string' || !event.eventId
                || !Number.isFinite(Date.parse(event.occurredAt)) || !event.data) return false;
            if (admin) return event.type === 'ORDER_CREATED' && Number.isSafeInteger(event.data.orderId);
            return event.type === 'ORDER_STATUS_CHANGED' && Number(event.data.orderId) === Number(orderId)
                && ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'COMPLETED', 'CANCELLED'].includes(event.data.status);
        };
        const stop = () => {
            stopped = true;
            generation++;
            clearTimeout(retry);
            clearTimeout(expiry);
            if (client) {
                client.disconnect();
                client.ws?.close();
            }
            window.removeEventListener('utegear:logout', stop);
            window.removeEventListener('pagehide', stop);
            onState('disconnected');
        };
        const connect = () => {
            if (stopped) return;
            const session = UTEGear.Session.get();
            if (!session || (admin && !session.user?.roles?.includes('ADMIN'))) { stop(); return; }
            if (!window.Stomp) { onState('unavailable'); return; }
            clearTimeout(expiry);
            expiry = setTimeout(stop, Math.min(Date.parse(session.expiresAt) - Date.now(), 2147483647));
            const current = ++generation;
            const url = new URL('/ws', location.origin);
            url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
            client = Stomp.over(new WebSocket(url.href, ['v12.stomp', 'v11.stomp', 'v10.stomp']));
            client.debug = () => {}; // Never log CONNECT frames containing the JWT.
            const failed = reason => {
                if (stopped || current !== generation) return;
                if (reason?.command === 'ERROR') { stop(); onState('authentication-required'); return; }
                generation++;
                client.ws?.close();
                onState('disconnected');
                retry = setTimeout(connect, Math.min(1000 * 2 ** attempt++, 30000));
            };
            onState('connecting');
            client.connect({ Authorization: `Bearer ${session.accessToken}` }, async () => {
                try {
                    // Subscribe before loading REST to avoid dropping events during resync.
                    let ready = false;
                    const queued = [];
                    const deliver = event => {
                        if (!accepts(event) || seen.has(event.eventId)) return;
                        seen.add(event.eventId);
                        if (seen.size > 500) seen.delete(seen.values().next().value);
                        onEvent(event);
                    };
                    client.subscribe(admin ? '/topic/admin/orders' : '/user/queue/orders', frame => {
                        if (stopped || current !== generation) return;
                        try {
                            const event = JSON.parse(frame.body);
                            if (ready) deliver(event);
                            else if (queued.length < 500) queued.push(event);
                            else failed();
                        } catch { /* Reject malformed events. */ }
                    });
                    await resync();
                    if (stopped || current !== generation) return;
                    ready = true;
                    queued.forEach(deliver);
                    attempt = 0;
                    onState('connected');
                } catch { failed(); }
            }, failed);
        };
        window.addEventListener('utegear:logout', stop);
        window.addEventListener('pagehide', stop);
        connect();
        return { stop };
    }
};
