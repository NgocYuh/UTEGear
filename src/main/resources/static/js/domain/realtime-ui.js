window.UTEGear = window.UTEGear || {};
UTEGear.RealtimeUI = {
    orderId: null, connection: null,
    labels: { connecting: 'Đang kết nối cập nhật đơn hàng…', connected: 'Đã kết nối cập nhật đơn hàng.', disconnected: 'Mất kết nối cập nhật đơn hàng.', unavailable: 'Chưa tải được thành phần cập nhật đơn hàng.', 'authentication-required': 'Kết nối bị từ chối. Vui lòng đăng nhập lại.' },
    customer(id, resync, root) {
        if (this.orderId === Number(id) && this.connection) return;
        this.connection?.stop(); this.orderId = Number(id);
        const status = root.querySelector('[data-realtime-status]');
        this.connection = UTEGear.Realtime.start({ orderId: Number(id), resync,
            onState: state => { if (status) status.textContent = this.labels[state] || state; },
            onEvent: () => resync().catch(error => UTEGear.Views.status(root, error.message, true))
        });
    },
    admin() {
        if (!document.body.classList.contains('admin-body') || !UTEGear.Session.get()?.user?.roles?.includes('ADMIN')) return;
        const root = document.querySelector('[data-orders-view=admin]'); if (!root) return;
        const status = root.querySelector('[data-realtime-status]');
        UTEGear.Realtime.start({ admin: true,
            resync: async () => { await UTEGear.Admin.orders(); window.dispatchEvent(new Event('utegear:admin-orders-refresh')); },
            onState: state => { status.textContent = this.labels[state] || state; },
            onEvent: event => {
                UTEGear.Toast.show('Có đơn hàng mới ' + event.data.orderCode, 'info');
                window.dispatchEvent(new Event('utegear:admin-orders-refresh'));
            }
        });
    }
};
document.addEventListener('DOMContentLoaded', () => UTEGear.RealtimeUI.admin());
