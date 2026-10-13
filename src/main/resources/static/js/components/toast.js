/**
 * UTEGear Toast Notification Component
 */
window.UTEGear = window.UTEGear || {};

UTEGear.Toast = {
    show(message, type = 'success', duration = 3000) {
        let container = document.querySelector('.toast-area') || document.getElementById('toastContainer');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-area';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast-msg ${type}`;
        const icons = {
            success: 'bi-check-circle-fill',
            error: 'bi-x-circle-fill',
            info: 'bi-info-circle-fill'
        };

        toast.innerHTML = `<i class="bi ${icons[type] || icons.info}"></i><span>${UTEGear.Utils.escapeHtml(message)}</span>`;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'all 0.3s cubic-bezier(0.16, 1, 0.3, 1)';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    }
};

// Global backward compatibility bridge
window.showToast = function (msg, type) {
    UTEGear.Toast.show(msg, type);
};
