/**
 * UTEGear Master Application Entry Point
 * Event Delegation & Module Initializer.
 */
document.addEventListener('DOMContentLoaded', () => {
    UTEGear.Auth.init();
    // 1. Initialize Sub-modules
    if (UTEGear.Autocomplete) UTEGear.Autocomplete.init();
    if (UTEGear.Filter) UTEGear.Filter.init();

    // 2. Global Event Delegation Architecture
    document.body.addEventListener('click', (e) => {
        const target = e.target.closest('[data-action]');
        if (!target) return;

        const action = target.getAttribute('data-action');
        const productId = target.getAttribute('data-product-id');
        const variantId = target.getAttribute('data-variant-id');

        switch (action) {
            case 'add-to-cart':
                e.preventDefault();
                window.addToCart(productId, variantId, target.dataset.quantityTarget ? document.getElementById(target.dataset.quantityTarget).value : 1);
                break;
            case 'toggle-wishlist':
                e.preventDefault();
                window.toggleWishlist(productId, target);
                break;
            case 'quick-view':
                e.preventDefault();
                if (window.quickView) window.quickView(productId);
                break;
            default:
                break;
        }
    });

    // 3. Track Recently Viewed Product on Detail Pages
    const detailContainer = document.querySelector('[data-page="product-detail"]');
    if (detailContainer) {
        const pId = detailContainer.getAttribute('data-product-id');
        if (pId && UTEGear.Utils) {
            UTEGear.Utils.addRecentlyViewed(Number(pId));
        }
    }
});

UTEGear.Auth = {
    async login(email, password) {
        const session = await UTEGear.API.post('/api/auth/login', { email, password }, { public: true });
        UTEGear.Session.save(session);
        return session;
    },
    register({ fullName, email, phone, password, confirmPassword }) {
        if (password !== confirmPassword) throw new Error('Mật khẩu xác nhận không khớp');
        return UTEGear.API.post('/api/auth/register', { fullName, email, phone: phone || null, password }, { public: true });
    },
    verifyRegistrationOtp(email, otpCode) {
        return UTEGear.API.post('/api/auth/verify-registration-otp', { email, otpCode }, { public: true });
    },
    resendRegistrationOtp(email) {
        return UTEGear.API.post('/api/auth/resend-registration-otp', { email }, { public: true });
    },
    forgotPassword(email) {
        return UTEGear.API.post('/api/auth/forgot-password', { email }, { public: true });
    },
    resetPassword(email, otpCode, newPassword) {
        return UTEGear.API.post('/api/auth/reset-password', { email, otpCode, newPassword }, { public: true });
    },
    profile() { return UTEGear.API.get('/api/profile'); },
    async logout() {
        try { await UTEGear.API.post('/api/auth/logout'); }
        finally { UTEGear.Session.clear(); }
    },
    init() {
        document.querySelectorAll('form[data-auth]').forEach(form => {
            form.addEventListener('submit', async event => {
                event.preventDefault();
                if (!form.reportValidity() || form.dataset.busy) return;
                form.dataset.busy = 'true';
                const button = form.querySelector('[type="submit"]');
                if (button) button.disabled = true;
                const status = form.querySelector('[data-auth-status]');
                if (status) {
                    status.classList.remove('d-none', 'alert-danger', 'alert-success');
                    status.classList.add('alert-info');
                    status.textContent = 'Đang xử lý…';
                }
                try {
                    const fields = Object.fromEntries(new FormData(form));
                    if (form.dataset.auth === 'login') {
                        await this.login(fields.email, fields.password);
                        const requested = new URLSearchParams(location.search).get('redirect');
                        const roles = UTEGear.Session.get()?.user?.roles || [];
                        location.href = UTEGear.Session.redirectTarget(requested || (roles.includes('EMPLOYEE') ? '/admin/support' : '/'));
                    } else {
                        await this.register(fields);
                        location.href = '/verify-otp?email=' + encodeURIComponent(fields.email);
                    }
                } catch (error) {
                    if (status) {
                        status.classList.remove('alert-info');
                        status.classList.add('alert-danger');
                        if (error.code === 'ACCOUNT_DISABLED' || (error.message && error.message.toLowerCase().includes('chưa được kích hoạt'))) {
                            const emailParam = encodeURIComponent(form.querySelector('input[name="email"]')?.value || '');
                            status.innerHTML = `<i class="bi bi-exclamation-circle-fill me-1"></i> ${error.message}. <a href="/verify-otp?email=${emailParam}" class="alert-link text-decoration-underline font-semibold">Bấm vào đây để xác thực OTP</a>`;
                        } else {
                            status.innerHTML = `<i class="bi bi-exclamation-triangle-fill me-1"></i> ${error.message}`;
                        }
                    }
                } finally {
                    delete form.dataset.busy;
                    if (button) button.disabled = false;
                }
            });
        });

        // Handler for Verify OTP Page
        this.initVerifyOtpPage();

        // Handler for Forgot Password Page
        this.initForgotPasswordPage();

        window.addEventListener('utegear:unauthorized', () => {
            const redirect = encodeURIComponent(location.pathname + location.search);
            location.href = '/login?redirect=' + redirect;
        });
    },

    initVerifyOtpPage() {
        const form = document.getElementById('verifyOtpForm');
        if (!form) return;

        const emailInput = document.getElementById('otpEmail');
        const otpCodeInput = document.getElementById('otpCode');
        const alertBox = document.getElementById('otpAlert');
        const btnVerify = document.getElementById('btnVerifyOtp');
        const btnResend = document.getElementById('btnResendOtp');
        const countdownText = document.getElementById('countdownText');

        // Pre-fill email from query param if available
        const params = new URLSearchParams(location.search);
        const emailFromQuery = params.get('email');
        if (emailFromQuery && emailInput) {
            emailInput.value = emailFromQuery;
        }

        const showAlert = (message, type = 'danger') => {
            if (!alertBox) return;
            alertBox.className = `alert alert-${type}`;
            alertBox.innerHTML = (type === 'success' ? '<i class="bi bi-check-circle-fill me-1"></i> ' : '<i class="bi bi-exclamation-triangle-fill me-1"></i> ') + message;
            alertBox.classList.remove('d-none');
        };

        const hideAlert = () => {
            if (alertBox) alertBox.classList.add('d-none');
        };

        const startCountdown = (seconds) => {
            if (!btnResend || !countdownText) return;
            btnResend.disabled = true;
            let remaining = seconds;
            countdownText.textContent = `(${remaining}s)`;
            const timer = setInterval(() => {
                remaining--;
                if (remaining <= 0) {
                    clearInterval(timer);
                    countdownText.textContent = '';
                    btnResend.disabled = false;
                } else {
                    countdownText.textContent = `(${remaining}s)`;
                }
            }, 1000);
        };

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            hideAlert();
            const email = emailInput?.value.trim();
            const otpCode = otpCodeInput?.value.trim();

            if (!email || !otpCode || otpCode.length !== 6) {
                showAlert('Vui lòng nhập đầy đủ email và mã OTP 6 số hợp lệ');
                return;
            }

            btnVerify.disabled = true;
            btnVerify.textContent = 'Đang xác thực…';
            try {
                const res = await this.verifyRegistrationOtp(email, otpCode);
                showAlert(res.message || 'Kích hoạt tài khoản thành công! Đang chuyển hướng đến đăng nhập...', 'success');
                setTimeout(() => {
                    location.href = '/login?verified=true';
                }, 1500);
            } catch (err) {
                showAlert(err.message || 'Xác thực OTP thất bại');
                btnVerify.disabled = false;
                btnVerify.innerHTML = 'KÍCH HOẠT TÀI KHOẢN <i class="bi bi-check-circle ms-1"></i>';
            }
        });

        if (btnResend) {
            btnResend.addEventListener('click', async () => {
                hideAlert();
                const email = emailInput?.value.trim();
                if (!email) {
                    showAlert('Vui lòng nhập email để gửi lại mã OTP');
                    return;
                }
                btnResend.disabled = true;
                try {
                    const res = await this.resendRegistrationOtp(email);
                    showAlert(res.message || 'Mã OTP mới đã được gửi đến email của bạn!', 'success');
                    startCountdown(60);
                } catch (err) {
                    showAlert(err.message || 'Không thể gửi lại mã OTP');
                    btnResend.disabled = false;
                }
            });
        }
    },

    initForgotPasswordPage() {
        const reqForm = document.getElementById('stepRequestOtpForm');
        const resetForm = document.getElementById('stepResetPasswordForm');
        if (!reqForm || !resetForm) return;

        const forgotEmail = document.getElementById('forgotEmail');
        const resetEmail = document.getElementById('resetEmail');
        const resetOtpCode = document.getElementById('resetOtpCode');
        const newPassword = document.getElementById('newPassword');
        const confirmNewPassword = document.getElementById('confirmNewPassword');
        const alertBox = document.getElementById('forgotAlert');
        const btnSendOtp = document.getElementById('btnSendOtp');
        const btnSubmitReset = document.getElementById('btnSubmitReset');
        const btnBack = document.getElementById('btnBackToStep1');
        const btnResend = document.getElementById('btnResendResetOtp');
        const countdownText = document.getElementById('resetCountdownText');
        const subtitle = document.getElementById('forgotSubtitle');

        const showAlert = (message, type = 'danger') => {
            if (!alertBox) return;
            alertBox.className = `alert alert-${type}`;
            alertBox.innerHTML = (type === 'success' ? '<i class="bi bi-check-circle-fill me-1"></i> ' : '<i class="bi bi-exclamation-triangle-fill me-1"></i> ') + message;
            alertBox.classList.remove('d-none');
        };

        const hideAlert = () => {
            if (alertBox) alertBox.classList.add('d-none');
        };

        const startCountdown = (seconds) => {
            if (!btnResend || !countdownText) return;
            btnResend.disabled = true;
            let remaining = seconds;
            countdownText.textContent = `(${remaining}s)`;
            const timer = setInterval(() => {
                remaining--;
                if (remaining <= 0) {
                    clearInterval(timer);
                    countdownText.textContent = '';
                    btnResend.disabled = false;
                } else {
                    countdownText.textContent = `(${remaining}s)`;
                }
            }, 1000);
        };

        reqForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            hideAlert();
            const email = forgotEmail?.value.trim();
            if (!email) {
                showAlert('Vui lòng nhập địa chỉ email của bạn');
                return;
            }

            btnSendOtp.disabled = true;
            btnSendOtp.textContent = 'Đang gửi mã…';
            try {
                const res = await this.forgotPassword(email);
                showAlert(res.message || 'Mã OTP đã được gửi đến email của bạn!', 'success');
                resetEmail.value = email;
                reqForm.classList.add('d-none');
                resetForm.classList.remove('d-none');
                if (subtitle) subtitle.textContent = 'Nhập mã OTP vừa nhận được và mật khẩu mới';
                startCountdown(60);
            } catch (err) {
                showAlert(err.message || 'Không thể gửi yêu cầu đặt lại mật khẩu');
            } finally {
                btnSendOtp.disabled = false;
                btnSendOtp.innerHTML = 'GỬI MÃ OTP <i class="bi bi-arrow-right ms-1"></i>';
            }
        });

        resetForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            hideAlert();
            const email = resetEmail?.value.trim();
            const otpCode = resetOtpCode?.value.trim();
            const pass = newPassword?.value;
            const confirmPass = confirmNewPassword?.value;

            if (!otpCode || otpCode.length !== 6) {
                showAlert('Vui lòng nhập mã OTP 6 chữ số hợp lệ');
                return;
            }
            if (pass !== confirmPass) {
                showAlert('Mật khẩu xác nhận không khớp với mật khẩu mới');
                return;
            }
            if (pass.length < 8) {
                showAlert('Mật khẩu phải có độ dài tối thiểu 8 ký tự');
                return;
            }

            btnSubmitReset.disabled = true;
            btnSubmitReset.textContent = 'Đang đặt lại mật khẩu…';
            try {
                const res = await this.resetPassword(email, otpCode, pass);
                showAlert(res.message || 'Đặt lại mật khẩu thành công! Đang chuyển hướng về trang đăng nhập...', 'success');
                setTimeout(() => {
                    location.href = '/login?reset=true';
                }, 1500);
            } catch (err) {
                showAlert(err.message || 'Đặt lại mật khẩu thất bại');
                btnSubmitReset.disabled = false;
                btnSubmitReset.innerHTML = 'ĐẶT LẠI MẬT KHẨU <i class="bi bi-check-circle ms-1"></i>';
            }
        });

        if (btnBack) {
            btnBack.addEventListener('click', () => {
                hideAlert();
                resetForm.classList.add('d-none');
                reqForm.classList.remove('d-none');
                if (subtitle) subtitle.textContent = 'Nhập email đăng ký để nhận mã OTP đặt lại mật khẩu';
            });
        }

        if (btnResend) {
            btnResend.addEventListener('click', async () => {
                hideAlert();
                const email = resetEmail?.value.trim();
                if (!email) return;
                btnResend.disabled = true;
                try {
                    const res = await this.forgotPassword(email);
                    showAlert(res.message || 'Mã OTP mới đã được gửi đến email của bạn!', 'success');
                    startCountdown(60);
                } catch (err) {
                    showAlert(err.message || 'Không thể gửi lại mã OTP');
                    btnResend.disabled = false;
                }
            });
        }
    }
};

// Only implemented BE endpoints are exposed here. No fallback to legacy form POSTs.
UTEGear.Transactions = {
    async quote(request) {
        return UTEGear.API.post('/api/checkout/quote', this.request(request));
    },
    async place(request, quote) {
        if (!quote?.quoteToken || !Number.isFinite(Date.parse(quote.expiresAt)) || Date.parse(quote.expiresAt) <= Date.now()) {
            throw new Error('Báo giá đã hết hạn; vui lòng tính lại trước khi đặt hàng');
        }
        return UTEGear.API.post('/api/orders', { ...this.request(request), quoteToken: quote.quoteToken });
    },
    request(request) {
        if (!['COD', 'BANK_TRANSFER'].includes(request.paymentMethod)) throw new Error('Phương thức thanh toán không hợp lệ');
        if (!request.items?.length) throw new Error('Giỏ hàng đang trống');
        return {
            addressId: UTEGear.Cart.id(request.addressId),
            fulfillmentStoreId: UTEGear.Cart.id(request.fulfillmentStoreId),
            couponCode: request.couponCode || null,
            paymentMethod: request.paymentMethod,
            note: request.note || null,
            items: request.items.map(item => {
                if (!Number.isInteger(Number(item.quantity)) || Number(item.quantity) < 1) throw new Error('Số lượng không hợp lệ');
                return { variantId: UTEGear.Cart.id(item.variantId), quantity: Number(item.quantity) };
            })
        };
    }
};

UTEGear.Catalog = {
    products(filters = {}) { return UTEGear.API.get('/api/products?' + new URLSearchParams(filters), { public: true }); },
    product(id) { return UTEGear.API.get(`/api/products/${UTEGear.Cart.id(id)}`, { public: true }); },
    categories() { return UTEGear.API.get('/api/categories', { public: true }); },
    brands() { return UTEGear.API.get('/api/brands', { public: true }); }
};

UTEGear.Stores = {
    list(filters = {}) { return UTEGear.API.get('/api/stores?' + new URLSearchParams(filters), { public: true }); },
    detail(id) { return UTEGear.API.get(`/api/stores/${UTEGear.Cart.id(id)}`, { public: true }); },
    availability(storeId, variantId) {
        return UTEGear.API.get(`/api/stores/${UTEGear.Cart.id(storeId)}/availability?variantId=${UTEGear.Cart.id(variantId)}`, { public: true });
    }
};
