window.UTEGear = window.UTEGear || {};
UTEGear.Reviews = {
    selectedRating: 5,

    init() {
        const root = document.querySelector('[data-reviews-root]');
        if (!root) return;

        const productId = root.dataset.productId;
        if (!productId) return;

        this.setupRatingPicker(root);
        this.setupCharCounter(root);
        this.setupFormSubmit(root, productId);
        this.loadReviews(root, productId);
    },

    setupRatingPicker(root) {
        const starButtons = root.querySelectorAll('[data-rating-star]');
        const ratingInput = root.querySelector('#reviewRatingInput');

        starButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const val = Number(btn.dataset.ratingStar);
                this.selectedRating = val;
                if (ratingInput) ratingInput.value = String(val);
                this.highlightStars(root, val);
            });
        });
        this.highlightStars(root, this.selectedRating);
    },

    highlightStars(root, count) {
        const starButtons = root.querySelectorAll('[data-rating-star]');
        starButtons.forEach(btn => {
            const val = Number(btn.dataset.ratingStar);
            const icon = btn.querySelector('i');
            if (!icon) return;
            if (val <= count) {
                icon.className = 'bi bi-star-fill text-warning';
            } else {
                icon.className = 'bi bi-star text-muted';
            }
        });
    },

    setupCharCounter(root) {
        const comment = root.querySelector('#reviewComment');
        const counter = root.querySelector('#reviewCharCount');
        const submitBtn = root.querySelector('#btnSubmitReview');
        if (!comment || !counter) return;

        comment.addEventListener('input', () => {
            const len = comment.value.trim().length;
            counter.textContent = `${len} / 50 ký tự tối thiểu`;
            if (len >= 50) {
                counter.className = 'small text-success fw-semibold';
                if (submitBtn) submitBtn.disabled = false;
            } else {
                counter.className = 'small text-danger';
                if (submitBtn) submitBtn.disabled = true;
            }
        });
    },

    setupFormSubmit(root, productId) {
        const form = root.querySelector('#productReviewForm');
        const alertBox = root.querySelector('#reviewFormAlert');
        const submitBtn = root.querySelector('#btnSubmitReview');
        if (!form) return;

        form.addEventListener('submit', async event => {
            event.preventDefault();
            const comment = root.querySelector('#reviewComment')?.value?.trim();
            const imageUrlInput = root.querySelector('#reviewImageUrl')?.value?.trim();

            if (!comment || comment.length < 50) {
                this.showAlert(alertBox, 'Nội dung đánh giá phải có tối thiểu 50 ký tự.', 'danger');
                return;
            }

            if (submitBtn) submitBtn.disabled = true;
            try {
                const payload = {
                    rating: this.selectedRating,
                    comment: comment,
                    imageUrls: imageUrlInput ? [imageUrlInput] : []
                };

                await UTEGear.API.post(`/api/products/${productId}/reviews`, payload);
                this.showAlert(alertBox, 'Gửi đánh giá thành công! Cảm ơn bạn đã đóng góp ý kiến.', 'success');
                form.reset();
                this.selectedRating = 5;
                this.highlightStars(root, 5);

                // Reload reviews
                setTimeout(() => this.loadReviews(root, productId), 1000);
            } catch (err) {
                this.showAlert(alertBox, err.message || 'Không thể gửi đánh giá. Vui lòng thử lại.', 'danger');
            } finally {
                if (submitBtn) submitBtn.disabled = false;
            }
        });
    },

    showAlert(alertBox, message, type) {
        if (!alertBox) return;
        alertBox.className = `alert alert-${type} mt-3 py-2 small`;
        alertBox.textContent = message;
        alertBox.hidden = false;
    },

    async loadReviews(root, productId) {
        const statusBox = root.querySelector('[data-reviews-status]');
        const contentBox = root.querySelector('[data-reviews-content]');
        const formContainer = root.querySelector('#reviewFormContainer');
        const noticeContainer = root.querySelector('#reviewNoticeContainer');
        const noticeText = root.querySelector('#reviewNoticeText');
        const listContainer = root.querySelector('[data-reviews-list]');

        try {
            const summary = await UTEGear.API.get(`/api/products/${productId}/reviews`, {}, { public: true });
            if (statusBox) statusBox.hidden = true;
            if (contentBox) contentBox.hidden = false;

            // Render stats
            const avgScore = root.querySelector('[data-stat-avg]');
            if (avgScore) avgScore.textContent = summary.averageRating.toFixed(1);

            const starDisplay = root.querySelector('[data-stat-stars]');
            if (starDisplay) {
                starDisplay.innerHTML = this.renderStarsHtml(summary.averageRating);
            }

            const countDisplay = root.querySelector('[data-stat-count]');
            if (countDisplay) countDisplay.textContent = `(${summary.totalReviews} đánh giá)`;

            // Render distribution bars
            for (let i = 1; i <= 5; i++) {
                const bar = root.querySelector(`[data-dist-bar="${i}"]`);
                const val = root.querySelector(`[data-dist-val="${i}"]`);
                const count = (summary.distribution && summary.distribution[i]) || 0;
                const percent = summary.totalReviews > 0 ? (count / summary.totalReviews) * 100 : 0;
                if (bar) bar.style.width = `${percent}%`;
                if (val) val.textContent = String(count);
            }

            // Check eligibility for form
            if (formContainer && noticeContainer) {
                if (summary.userEligible) {
                    formContainer.hidden = false;
                    noticeContainer.hidden = true;
                } else {
                    formContainer.hidden = true;
                    noticeContainer.hidden = false;
                    if (noticeText) {
                        noticeText.textContent = summary.userIneligibilityReason || 'Đăng nhập để đánh giá sản phẩm này.';
                    }
                }
            }

            // Render reviews list
            if (listContainer) {
                listContainer.replaceChildren();
                if (!summary.reviews || summary.reviews.length === 0) {
                    listContainer.innerHTML = `
                        <div class="text-center py-4 text-muted">
                            <i class="bi bi-chat-square-text fs-2 d-block mb-2 text-secondary opacity-50"></i>
                            <p class="mb-0">Chưa có đánh giá nào cho sản phẩm này.</p>
                            <small class="text-muted">Hãy là người đầu tiên mua và để lại đánh giá nhé!</small>
                        </div>
                    `;
                } else {
                    for (const r of summary.reviews) {
                        const itemCard = document.createElement('article');
                        itemCard.className = 'border-bottom py-3';

                        const dateFormatted = new Date(r.createdAt).toLocaleDateString('vi-VN', {
                            year: 'numeric', month: '2-digit', day: '2-digit'
                        });

                        let imagesHtml = '';
                        if (r.imageUrls && r.imageUrls.length > 0) {
                            imagesHtml = `
                                <div class="d-flex flex-wrap gap-2 mt-2">
                                    ${r.imageUrls.map(url => `
                                        <a href="${url}" target="_blank" rel="noopener noreferrer">
                                            <img src="${url}" alt="Ảnh đánh giá" class="rounded border" style="width: 72px; height: 72px; object-fit: cover;">
                                        </a>
                                    `).join('')}
                                </div>
                            `;
                        }

                        itemCard.innerHTML = `
                            <div class="d-flex align-items-center justify-content-between mb-1">
                                <div class="d-flex align-items-center gap-2">
                                    <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center fw-bold" style="width: 34px; height: 34px; font-size: 0.85rem;">
                                        ${r.userName ? r.userName.charAt(0) : 'U'}
                                    </div>
                                    <div>
                                        <div class="fw-semibold small">${r.userName}</div>
                                        ${r.verifiedPurchase ? '<span class="badge bg-success-subtle text-success border border-success-subtle" style="font-size: 0.7rem;"><i class="bi bi-patch-check-fill me-1"></i>Đã mua tại UTEGear</span>' : ''}
                                    </div>
                                </div>
                                <span class="text-muted small">${dateFormatted}</span>
                            </div>
                            <div class="mb-2">
                                ${this.renderStarsHtml(r.rating)}
                            </div>
                            <p class="mb-0 text-dark" style="white-space: pre-line; font-size: 0.95rem;">${r.comment}</p>
                            ${imagesHtml}
                        `;
                        listContainer.appendChild(itemCard);
                    }
                }
            }
        } catch (err) {
            if (statusBox) statusBox.textContent = 'Không thể tải đánh giá sản phẩm.';
        }
    },

    renderStarsHtml(rating) {
        let html = '';
        const rounded = Math.round(rating * 2) / 2;
        for (let i = 1; i <= 5; i++) {
            if (i <= rounded) {
                html += '<i class="bi bi-star-fill text-warning me-1"></i>';
            } else if (i - 0.5 === rounded) {
                html += '<i class="bi bi-star-half text-warning me-1"></i>';
            } else {
                html += '<i class="bi bi-star text-muted me-1"></i>';
            }
        }
        return html;
    }
};

document.addEventListener('DOMContentLoaded', () => UTEGear.Reviews.init());

