# QA-04 — Bản kiểm thử để FE/BE review

- Phụ trách: Frontend `@trongsonho`.
- Branch: `test/frontend-flows`, base `35a5e78` (merge PR #52).
- Ngày chạy lại: 2026-10-14, 11:59 giờ Việt Nam/Thái Lan (UTC+7).
- Trạng thái: `BLOCKED`; người dùng đã duyệt push branch để BE review ngày 2026-10-14.
  Chưa mở PR hoặc được duyệt merge; chưa xác nhận DONE.
- Chỉ thêm tooling kiểm thử và tài liệu FE. Không đổi Java, pom.xml, config, migration, template,
  JavaScript sản phẩm hoặc quyết định bảo mật.

## Kết quả chạy thực tế

Ứng dụng Spring Boot thật chạy trên `http://127.0.0.1:18080`, profile `test`, H2 in-memory,
create-drop chỉ áp dụng qua tham số lệnh cho database test. Flyway tắt theo test properties.
Không kết nối Supabase/production, không seed và không gọi endpoint ghi dữ liệu.

`tests/frontend/qa04-live.mjs` đã gọi HTTP thật, không intercept fetch và không dùng mock API.
Kết quả sau PR #52: **79 PASS, 0 FAIL, 7 BLOCKED**. Đây là số mục HTTP/DOM và responsive
đã kiểm tra, không phải 79 luồng giao dịch tích hợp đã pass. Lần trước PR #51 là
10 PASS, 0 FAIL, 15 BLOCKED; không dùng kết quả 404 cũ để kết luận các route đã được sửa.

| Kiểm tra thật | Kết quả |
|---|---|
| GET categories, brands, products có q/page/size, stores | 4 PASS: HTTP 200 và shape response đúng phần được kiểm tra |
| GET product ID không tồn tại | PASS: HTTP 404, PRODUCT_NOT_FOUND |
| GET profile, cart, wishlist, notifications, admin/orders khi không có JWT | 5 PASS: HTTP 401, UNAUTHORIZED |
| /, /wishlist, /login, /register, /products, /stores, /cart, /checkout, /profile | 9 PASS: HTTP 200, HTML render thật |
| /orders, /admin/orders | 2 BLOCKED: HTTP 404 |
| Responsive 1440/768/390, keyboard focus, labels, uncaught exceptions trên 9 trang | 54 PASS; không overflow/uncaught exception trong các điều kiện đã chạy |
| Login/register required field và email sai, register password không khớp | 5 PASS trên DOM thật |
| Menu mobile: mở, Escape đóng và trả focus | PASS trên trang products |
| Validation server errors, JWT/role UI, cart/wishlist, checkout/history, WebSocket reconnect | 5 BLOCKED: thiếu policy/account/data hoặc UI chưa nối đầy đủ |

Checkout HTTP 200 hiện empty state giỏ hàng trống. Runner cũ chỉ tìm checkoutForm nên đánh
dấu nhầm BLOCKED; đã sửa chấp nhận cart-empty-box. Đây là PASS render empty state, không
phải PASS đặt đơn. Catalog/store/cart/wishlist ban đầu rỗng do database test, không phải lỗi.

Raw JSON và server log là artifact local, không commit: `target/qa04/` khi chạy. Bản JSON lần này
được lưu ngoài worktree tại `D:/UTEGear/.local-reference/qa04-review-artifacts/pr52/live-report.json`
để tránh bị clean verify xóa. Không chứa JWT, password, response body hoặc dữ liệu cá nhân.

Kiểm tra bổ sung: node --check runner PASS. Unit JS 16/16 được xác nhận trên base PR #51
ở lần trước; không tính vào số check HTTP/browser lần này. Maven verify PR #52 với
-Dnet.bytebuddy.experimental=true trên Java 26 BUILD SUCCESS: 175 tests được phát hiện,
172 pass, 3 PostgreSQL tests skip vì Docker local chưa chạy. Unit test doubles không tính vào
kết quả UI tích hợp. Test server và Chromium riêng đã dừng sau khi lưu report/ảnh.

## Cách chạy lại

Yêu cầu Node 22+, Java/Maven phù hợp repository. Không dùng local/prod profile hoặc dữ liệu production.
Ở PowerShell trong worktree QA-04, chạy server test trong một terminal riêng:

```powershell
.\mvnw.cmd -B spring-boot:run `
  "-Dspring-boot.run.useTestClasspath=true" `
  "-Dspring-boot.run.profiles=test" `
  "-Dspring-boot.run.arguments=--server.port=18080 --server.address=127.0.0.1 --spring.config.additional-location=file:src/test/resources/application-test.properties --spring.jpa.hibernate.ddl-auto=create-drop --app.websocket.allowed-origins=http://127.0.0.1:18080"
```

Chỉ chạy runner sau khi server báo Started. Terminal khác:

```powershell
node tests/frontend/qa04-live.mjs --base=http://127.0.0.1:18080 --report=target/qa04/live-report.json
```

Runner chỉ chấp nhận loopback origin, HTTP preflight chỉ GET và không tự tạo tài khoản/đơn hàng.
Browser kiểm tra form không hợp lệ, không submit đăng ký hợp lệ hoặc giao dịch. Thiếu trang/server
được ghi BLOCKED; response sai schema/status được ghi FAIL. Node exit code: 0 PASS, 1 FAIL,
2 BLOCKED (PowerShell/runner bên ngoài có thể ánh xạ nonzero thành 1).

Khi routing/model đã có, có thể bổ sung Chromium CDP local:

```powershell
node tests/frontend/qa04-live.mjs --base=http://127.0.0.1:18080 --cdp=http://127.0.0.1:9237 --report=target/qa04/live-report.json
```

Trình duyệt phải được khởi động riêng với remote debugging, profile kiểm thử và cùng origin test.
Runner tạo tab riêng, kiểm tra overflow ở 1440/768/390px, labels, Tab/focus và uncaught exceptions,
lưu ảnh vào thư mục report rồi đóng tab của chính nó. Không gọi API giả hoặc submit giao dịch.
Các flow authenticated vẫn cần checklist thủ công; runner không tự ghi chúng PASS.
CDP đã chạy với Chromium headless trên /, wishlist, login, register, products, stores, cart, checkout
và profile. Ảnh 27 kích thước trang lưu cạnh report, không commit.

## Checklist thủ công — chưa có kết quả PASS

Chuẩn bị môi trường test với trang render thật, quyết định phiên được BE duyệt, user A/user B và
admin test, sản phẩm có default/multiple variants, tồn kho theo Store + Variant. Không ghi password/JWT
vào báo cáo. Ghi thời điểm, bước tái hiện, expected/actual và ảnh đã che dữ liệu nếu có.

| ID | Ca kiểm tra | Tiêu chí đạt | Lần này |
|---|---|---|---|
| FE-QA-01 | Login/register: field trống, email sai, password ngắn/không khớp, server lỗi | Validation đúng, lỗi đọc được, khóa submit kép, không báo thành công khi API thất bại | BLOCKED toàn nhóm; required/email/mismatch PASS, server lỗi và các ca còn lại chưa chạy |
| FE-QA-02 | Login/logout/hết phiên, redirect | Dùng policy phiên đã duyệt; 401 kết thúc phiên; 403 giữ phiên; redirect cùng origin | BLOCKED: policy + tài khoản test |
| FE-QA-03 | Catalog/search/filter/pagination | Gửi q và zero-based page; field giá/ảnh đúng; loading/empty/error; lựa chọn không bị response cũ ghi đè | BLOCKED: chưa có sản phẩm test; trang rỗng render PASS |
| FE-QA-04 | Store availability cho từng biến thể | Đúng storeId/variantId; còn/hết hàng và inactive phân biệt được | BLOCKED: chưa có store/variant test |
| FE-QA-05 | Cart: quick add, chọn variant, sửa/xóa/clear, thiếu tồn kho | Không gửi giá; defaultVariantId thật; tổng/badge theo server; lỗi không làm UI hiển thị sai | BLOCKED: chưa có tài khoản/sản phẩm test và dữ liệu UI từ JWT |
| FE-QA-06 | Wishlist: add/remove, click kép, 401/lỗi mạng | Đúng state server và icon; không thao tác trùng; không nuốt lỗi | BLOCKED: tài khoản/data/UI binding |
| FE-QA-07 | Quote/checkout và history | addressId/storeId/variantId đúng; quoteToken; hết hạn/thiếu hàng không tạo đơn; history theo user | BLOCKED: address/order API + form chưa nối; checkout empty state PASS |
| FE-QA-08 | User A/B và admin | UI xử lý 401/403 đúng; server chặn dữ liệu user khác và API ADMIN | BLOCKED: admin route/tài khoản test |
| FE-QA-09 | Admin order, inventory và media | PUT status; Store + Variant; transfer lỗi; media giữ secureUrl/publicId, upload lỗi có thông báo | BLOCKED: admin routing/UI chưa nối đủ |
| FE-QA-10 | WebSocket CONNECT, events, disconnect/reconnect | STOMP qua /ws; Authorization ở CONNECT; envelope/dedup; đúng orderId; REST resync trước áp event | BLOCKED: module realtime chưa nạp vào layout |
| FE-QA-11 | Responsive 1440/768/390, menu/modal | Không overflow; điều khiển dùng được; menu/modal đóng đúng, focus được trả lại | PASS phần overflow và menu/Escape/focus; modal/nội dung đầy đủ chưa chạy |
| FE-QA-12 | Keyboard/accessibility | Tab/Shift+Tab/Escape, focus rõ, label/name, thông báo aria-live, loading/empty/error đọc được | PASS phần Tab focus và labels; chưa đánh giá đầy đủ accessibility/error/loading |

## Blocker cần giải quyết trước khi hoàn tất QA-04

1. PR #52 đã bổ sung MVC/layout dialect cho public/auth/customer shell. /orders và /admin/orders
   vẫn 404; profile update/password, address và customer order theo contract vẫn cần hoàn thiện.
2. Quyết định lưu phiên: PR #51 được merge không tự chứng minh sessionStorage đã được BE chấp thuận;
   tài liệu quyết định trên main vẫn là DRAFT.
3. FE còn phải nối checkout/admin/realtime và thay model/helper cũ. Layout admin và order-detail hiện
   vẫn dùng SockJS và CONNECT không có JWT; module realtime mới chưa được nạp.
4. Môi trường/tài khoản/dữ liệu test và chạy browser thật; không dùng unit mocks thay E2E.

QA-05 phụ thuộc QA-04: chưa bắt đầu hoặc ghi báo cáo E2E đã hoàn thành. Sau khi QA-04 pass,
tạo branch `docs/manual-test-report` riêng để ghi kết quả end-to-end và BE review.
