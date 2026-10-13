/**
 * UTEGear API Client Wrapper
 */
window.UTEGear = window.UTEGear || {};

UTEGear.API = {
    async request(url, options = {}) {
        const config = {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                if (response.status === 401) {
                    UTEGear.Toast.show(data.message || 'Vui lòng đăng nhập để tiếp tục', 'error');
                    setTimeout(() => {
                        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
                    }, 1200);
                    throw new Error('UNAUTHORIZED');
                }
                throw new Error(data.message || 'Đã xảy ra lỗi hệ thống');
            }

            return data;
        } catch (error) {
            if (error.message !== 'UNAUTHORIZED') {
                console.error('[API Error]', error);
            }
            throw error;
        }
    },

    get(url) {
        return this.request(url, { method: 'GET' });
    },

    post(url, body) {
        return this.request(url, { method: 'POST', body: JSON.stringify(body) });
    },

    delete(url) {
        return this.request(url, { method: 'DELETE' });
    }
};
