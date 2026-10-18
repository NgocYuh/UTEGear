/**
 * UTEGear Smart Gear Consultant (Hệ chuyên gia tư vấn Gear thông minh)
 * Phụ trách: @trongsonho & @NgocYuh
 */
(() => {
  const formatMoney = (val) => {
    if (val === null || val === undefined) return '';
    return new Intl.NumberFormat('vi-VN').format(val) + '₫';
  };

  const escapeHtml = (str) => {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#039;');
  };

  const init = () => {
    const trigger = document.querySelector('.gear-consultant__trigger');
    const panel = document.getElementById('gear-consultant-panel');
    const closeBtn = document.querySelector('.gear-consultant__close');
    const messagesEl = document.getElementById('gear-consultant-messages');
    const form = document.getElementById('gear-consultant-form');
    const input = document.getElementById('gear-consultant-input');
    const storeSelect = document.getElementById('consultant-store-select');
    const promptsContainer = document.getElementById('gear-consultant-quick-prompts');

    if (!trigger || !panel || !form || !input) return;

    // Toggle open/close
    const togglePanel = (open) => {
      const isVisible = !panel.hidden;
      const shouldOpen = open !== undefined ? open : !isVisible;
      panel.hidden = !shouldOpen;
      trigger.setAttribute('aria-expanded', shouldOpen ? 'true' : 'false');
      if (document.body && document.body.classList) {
        document.body.classList.toggle('gear-ai-open', shouldOpen);
      }
      if (shouldOpen) {
        const supportPanel = document.getElementById('support-chat-panel');
        if (supportPanel && !supportPanel.hidden) {
          const supportClose = document.querySelector('.support-chat__close');
          supportClose?.click();
        }
        input.focus();
        scrollToBottom();
      }
    };

    trigger.addEventListener('click', () => togglePanel(true));
    closeBtn?.addEventListener('click', () => togglePanel(false));

    const scrollToBottom = () => {
      if (messagesEl) {
        messagesEl.scrollTop = messagesEl.scrollHeight;
      }
    };

    // Load stores for dropdown
    const loadStores = async () => {
      if (!storeSelect) return;
      try {
        const res = await fetch('/api/stores');
        if (res.ok) {
          const stores = await res.json();
          stores.forEach((s) => {
            const opt = document.createElement('option');
            opt.value = s.id;
            opt.textContent = `${s.name} (${s.province || ''})`;
            storeSelect.appendChild(opt);
          });
        }
      } catch (e) {
        // Fallback silently if offline
      }
    };

    // Load quick prompts
    const loadQuickPrompts = async () => {
      if (!promptsContainer) return;
      try {
        const res = await fetch('/api/consultant/quick-prompts');
        if (res.ok) {
          const prompts = await res.json();
          promptsContainer.innerHTML = '';
          prompts.forEach((p) => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'gear-consultant__prompt-btn';
            btn.innerHTML = `<i class="bi ${escapeHtml(p.icon)} text-danger"></i> <span>${escapeHtml(p.title)}</span>`;
            btn.addEventListener('click', () => {
              input.value = p.prompt;
              submitMessage();
            });
            promptsContainer.appendChild(btn);
          });
        }
      } catch (e) {
        // Fallback
      }
    };

    // Append user message
    const appendUserMessage = (text) => {
      const msg = document.createElement('div');
      msg.className = 'gear-consultant__message gear-consultant__message--user';
      msg.textContent = text;
      messagesEl.appendChild(msg);
      scrollToBottom();
    };

    // Append bot message
    const appendBotResponse = (data) => {
      const msg = document.createElement('div');
      msg.className = 'gear-consultant__message gear-consultant__message--bot';

      let html = '';
      if (data.detectedIntent) {
        html += `<div class="badge bg-danger mb-2 p-1 px-2 font-semibold fs-8"><i class="bi bi-tag-fill me-1"></i>${escapeHtml(data.detectedIntent)}</div>`;
      }

      // Format markdown-like bold text in reply
      const formattedReply = escapeHtml(data.reply || '')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/\n/g, '<br>');

      html += `<div class="mb-2">${formattedReply}</div>`;

      // Render product cards
      if (data.products && data.products.length > 0) {
        html += '<div class="gear-consultant__products mt-2">';
        data.products.forEach((p) => {
          const imgUrl = p.primaryImageUrl || '/images/placeholder.svg';
          const saleFormatted = formatMoney(p.effectivePrice);
          const hasDiscount = p.salePrice && p.basePrice && p.salePrice < p.basePrice;
          const baseFormatted = hasDiscount ? formatMoney(p.basePrice) : '';

          const productHref = `/products/${encodeURIComponent(p.id != null ? p.id : p.slug)}`;

          html += `
            <div class="gear-consultant__product-card">
              <img class="gear-consultant__product-img" src="${escapeHtml(imgUrl)}" alt="${escapeHtml(p.name)}" loading="lazy">
              <div class="gear-consultant__product-info">
                <a href="${productHref}" class="text-decoration-none">
                  <h4 class="gear-consultant__product-title">${escapeHtml(p.name)}</h4>
                </a>
                <div class="gear-consultant__product-price">
                  ${saleFormatted}
                  ${hasDiscount ? `<small class="text-muted text-decoration-line-through ms-1 fs-8">${baseFormatted}</small>` : ''}
                </div>
                <div class="d-flex align-items-center justify-content-between mt-1">
                  <span class="gear-consultant__product-stock"><i class="bi bi-check-circle-fill me-1"></i>Còn ${p.availableQuantity} chiếc</span>
                  <a href="${productHref}" class="btn btn-sm btn-outline-danger gear-consultant__product-link">Xem chi tiết</a>
                </div>
              </div>
            </div>
          `;
        });
        html += '</div>';
      }

      // Next suggestions
      if (data.nextSuggestions && data.nextSuggestions.length > 0) {
        html += '<div class="mt-3 pt-2 border-top"><small class="text-muted d-block mb-1 font-semibold">Gợi ý tiếp theo:</small><div class="d-flex flex-wrap gap-1">';
        data.nextSuggestions.forEach((s) => {
          html += `<button type="button" class="btn btn-sm btn-light border py-1 px-2 fs-8 text-start text-danger consultant-suggestion-btn">${escapeHtml(s)}</button>`;
        });
        html += '</div></div>';
      }

      msg.innerHTML = html;

      // Attach click listeners to suggestions
      msg.querySelectorAll('.consultant-suggestion-btn').forEach((btn) => {
        btn.addEventListener('click', () => {
          input.value = btn.textContent.trim();
          submitMessage();
        });
      });

      messagesEl.appendChild(msg);
      scrollToBottom();
    };

    // Submit message
    const submitMessage = async () => {
      const text = input.value.trim();
      if (!text) return;

      appendUserMessage(text);
      input.value = '';

      // Typing indicator
      const typing = document.createElement('div');
      typing.className = 'gear-consultant__message gear-consultant__message--bot text-muted fst-italic';
      typing.innerHTML = '<i class="bi bi-hourglass-split me-1 text-danger"></i> Đang phân tích thông số & kiểm tra tồn kho tại showroom...';
      messagesEl.appendChild(typing);
      scrollToBottom();

      const selectedStoreId = storeSelect?.value ? parseInt(storeSelect.value, 10) : null;

      try {
        const response = await fetch('/api/consultant/chat', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ message: text, storeId: selectedStoreId })
        });

        typing.remove();

        if (response.ok) {
          const data = await response.json();
          appendBotResponse(data);
          // Auto sync dropdown with detected store
          if (data.store && data.store.id && storeSelect) {
            storeSelect.value = data.store.id;
          }
        } else {
          const errData = await response.json().catch(() => ({}));
          const errMsg = errData.message || 'Không thể kết nối đến hệ thống tư vấn lúc này. Vui lòng thử lại!';
          appendBotResponse({ reply: `⚠️ ${errMsg}`, products: [] });
        }
      } catch (err) {
        typing.remove();
        appendBotResponse({
          reply: '⚠️ Đã xảy ra lỗi khi kết nối máy chủ. Bạn vui lòng kiểm tra kết nối mạng và thử lại nhé!',
          products: []
        });
      }
    };

    form.addEventListener('submit', (e) => {
      e.preventDefault();
      submitMessage();
    });

    // Enter to send (Shift+Enter for newline)
    input.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        submitMessage();
      }
    });

    loadStores();
    loadQuickPrompts();
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();

