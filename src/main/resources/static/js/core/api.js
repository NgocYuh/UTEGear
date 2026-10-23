window.UTEGear = window.UTEGear || {};

// Session management with JWT Access Token + Refresh Token and server revocation support.
UTEGear.Session = {
    key: 'utegear.auth',
    get() {
        try {
            const value = JSON.parse(sessionStorage.getItem(this.key));
            if (!value) return null;
            if (!value?.accessToken) { this.clear(); return null; }
            if (Number.isFinite(Date.parse(value.expiresAt)) && Date.parse(value.expiresAt) <= Date.now()) {
                if (!value.refreshToken) {
                    this.clear();
                    return null;
                }
            }
            return value;
        } catch { return null; }
    },
    save(value) {
        if (!value?.accessToken || value.tokenType !== 'Bearer'
            || !Number.isFinite(Date.parse(value.expiresAt))) {
            throw new Error('Phiên đăng nhập không hợp lệ');
        }
        sessionStorage.setItem(this.key, JSON.stringify(value));
    },
    clear() {
        sessionStorage.removeItem(this.key);
        window.dispatchEvent(new Event('utegear:logout'));
    },
    redirectTarget(value) {
        try {
            const target = new URL(value || '/', location.origin);
            return target.origin === location.origin ? target.pathname + target.search + target.hash : '/';
        } catch { return '/'; }
    }
};

UTEGear.API = {
    async request(url, options = {}) {
        const target = new URL(url, location.origin);
        if (target.origin !== location.origin || !target.pathname.startsWith('/api/')) {
            throw new Error('Chỉ được gọi API cùng máy chủ');
        }
        const { public: isPublic = false, _retry = false, ...fetchOptions } = options;
        const headers = new Headers(fetchOptions.headers);
        headers.set('Accept', 'application/json');
        if (fetchOptions.body != null && !(fetchOptions.body instanceof FormData)) headers.set('Content-Type', 'application/json');
        const session = isPublic ? null : UTEGear.Session.get();
        if (session) headers.set('Authorization', `Bearer ${session.accessToken}`);
        const response = await fetch(target.pathname + target.search, {
            ...fetchOptions, headers, credentials: 'same-origin', redirect: 'error'
        });
        let data = null;
        if (response.status !== 204) {
            const body = await response.text();
            if (body) {
                try { data = JSON.parse(body); }
                catch { if (response.ok) throw new Error('Máy chủ trả dữ liệu không hợp lệ'); }
            }
        }
        if (!response.ok) {
            const error = new Error(data?.message || `Yêu cầu thất bại (${response.status})`);
            error.status = response.status;
            error.code = data?.code;
            error.details = data?.fieldErrors || data?.details;
            if (response.status === 401 && !isPublic) {
                if (session?.refreshToken && !_retry && !target.pathname.startsWith('/api/auth/')) {
                    try {
                        const newSession = await this.post('/api/auth/refresh', { refreshToken: session.refreshToken }, { public: true });
                        if (newSession?.accessToken) {
                            UTEGear.Session.save(newSession);
                            return await this.request(url, { ...options, _retry: true });
                        }
                    } catch (refreshErr) {
                        // refresh failed, proceed to clear session
                    }
                }
                UTEGear.Session.clear();
                window.dispatchEvent(new CustomEvent('utegear:unauthorized', { detail: error }));
            }
            throw error;
        }
        return data;
    },
    get(url, options) { return this.request(url, { ...options, method: 'GET' }); },
    post(url, body, options) { return this.request(url, { ...options, method: 'POST', body: JSON.stringify(body) }); },
    put(url, body, options) { return this.request(url, { ...options, method: 'PUT', body: JSON.stringify(body) }); },
    patch(url, body, options) { return this.request(url, { ...options, method: 'PATCH', body: JSON.stringify(body) }); },
    delete(url, options) { return this.request(url, { ...options, method: 'DELETE' }); }
};
