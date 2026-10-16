(() => {
    'use strict';
    document.querySelectorAll('[data-flash-sale]').forEach(banner => {
        const timer = banner.querySelector('[role="timer"]');
        const status = banner.querySelector('[data-countdown-status]');
        const value = banner.dataset.endsAt;
        // An explicit timezone keeps the deadline identical on every visitor's device.
        const deadline = value && /(Z|[+-]\d{2}:\d{2})$/.test(value) ? Date.parse(value) : NaN;
        if (!Number.isFinite(deadline)) return;
        const fields = ['hours', 'minutes', 'seconds'].map(unit => banner.querySelector('[data-countdown-' + unit + ']'));
        let interval;
        function update() {
            const remaining = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
            const values = [Math.floor(remaining / 3600), Math.floor(remaining / 60) % 60, remaining % 60];
            fields.forEach((field, index) => { field.textContent = String(values[index]).padStart(2, '0'); });
            timer.hidden = false;
            if (remaining === 0) {
                status.textContent = 'Flash Sale đã kết thúc';
                banner.querySelector('.flash-sale-cta').textContent = 'XEM SẢN PHẨM →';
                if (interval !== undefined) clearInterval(interval);
            }
            return remaining;
        }
        if (update() > 0) interval = setInterval(update, 1000);
    });
})();
