# Cập nhật QA-04 sau BE PR #55 — 2026-10-14

Đã kiểm thử source sau main `70ca4b9` bằng merge `cdcb689`. Sau đó GitHub merge FE qua PR #56
tại `7457eb5`; tree nguồn trùng bản đã kiểm thử. Phần kiểm thử/báo cáo mới đưa lên nhánh
`test/qa04-realtime-verification` từ `7457eb5` theo yêu cầu push của người dùng.
Không tự sửa Backend; code BE mới lấy từ PR #55.

## Kết quả mới nhất

- Cart PUT quantity/subtotal: PASS, không còn lỗi 500.
- Customer/admin STOMP CONNECT/subscription: PASS, không còn lỗi chưa xác thực.
- Customer nhận event trạng thái và admin nhận event đơn mới; UI cập nhật không navigation: PASS.
- Customer/admin reconnect sau đóng socket native và có GET REST resync mới: PASS.
- Suite giao dịch thật: **39 PASS, 0 FAIL**. HTTP/DOM: **93 PASS, 0 FAIL**; 5 placeholder
  BLOCKED thuộc nhóm ngoài read-only runner, đối chiếu với suite giao dịch, không phải API thiếu.
- Unit JS: 18 PASS; browser script syntax PASS.
- Maven clean verify (Java 26, -Dnet.bytebuddy.experimental=true): BUILD SUCCESS,
  185 tests được phát hiện, 182 pass, 3 PostgreSQL skip vì Docker chưa chạy.

Test dùng H2 tạm và real HTTP/Chromium/STOMP. CDP chỉ ghi command và cờ có Authorization,
không giữ frame/JWT. Theo dõi native socket để chủ động đóng rồi xác nhận reconnect;
không mock frame, API hoặc sự kiện server. GET sau reconnect được xác nhận qua CDP Network.

Artifact local, không commit:
- D:/UTEGear/.local-reference/storefront-test-artifacts/pr55-final/browser/report.json
- D:/UTEGear/.local-reference/storefront-test-artifacts/pr55-final/public/report.json
- D:/UTEGear/.local-reference/storefront-test-artifacts/pr55-final/browser/cart-390.png
- D:/UTEGear/.local-reference/storefront-test-artifacts/pr55-verify.log

## Trạng thái

FE-BIND-01 và QA-04 chuyển từ BLOCKED sang IN PROGRESS: hai blocker BE đã được xác nhận hết.
Chưa DONE: còn các ca/dataset chưa bao phủ (multi-variant/media, inactive/stock/coupon/quote expiry,
race/double-submit, JWT expiry trên browser và keyboard/accessibility còn lại), PostgreSQL/Flyway
chưa xác nhận. PR #56 FE đã merge; cập nhật kiểm thử mới chưa mở/duyệt PR. Chưa bắt đầu QA-05.

Cách chạy fixture/launcher ở phần lịch sử dưới vẫn áp dụng. Bản dưới lưu kết quả PR #54,
không dùng các lỗi cũ để kết luận main hiện tại còn lỗi.

---

# FE-BIND-01 — Đồng bộ Frontend với BE PR #54

- Phụ trách: Frontend @trongsonho. Branch: fix/storefront-bindings.
- Base: ef285f9, merge PR #54; đã đối chiếu remote main ngày 2026-10-14.
- Người dùng đã duyệt commit/push branch để BE review; chưa mở PR. Chỉ đổi templates,
  JavaScript, frontend tests và tài liệu FE.
- Policy sessionStorage theo tab đã được BE APPROVED trong PR #54; không thêm refresh/token revocation.

## Nội dung để review

- Cart/wishlist/profile chuyển từ model SSR cũ sang shell gọi API JWT, render bằng textContent,
  có loading/empty/error. Cart PUT lỗi trả input về quantity server, không giả subtotal thành công.
- Profile PATCH cập nhật header/session; đổi mật khẩu PUT rồi xóa phiên client và về login.
  Address GET/POST/PUT/DELETE và đặt mặc định theo DTO isDefault.
- Checkout tải cart/address/store thật, quote rồi place bằng quoteToken. Thay đổi dữ liệu
  vô hiệu quote, chặn thao tác trùng; thành công dẫn tới /orders?focus=id.
- Catalog/card/detail dùng effectivePrice/primaryImage/variants/optionValues/inStock theo DTO.
  Bộ lọc và pagination theo contract; description/specifications được escape thành text.
  Chọn variant và quantity đúng, availability theo Store + ProductVariant.
- Store dùng openingTime/closingTime, category rail dùng categoryId.
- Customer/admin order list có filter/pagination/detail. Route detail BE redirect về list?focus=id;
  FE đọc focus. Customer hủy đơn theo trạng thái/payment; admin cập nhật trạng thái qua API.
- Hai layout nạp các module cần thiết; bỏ SockJS/WebJars cũ, dùng native STOMP /ws.
  Authorization chỉ ở CONNECT, không đặt JWT trong URL. REST resync trước khi áp dụng event;
  chống event trùng và response detail cũ. Server ERROR dừng kết nối, không reconnect vô hạn.
- Thêm fixture H2 và launcher/browser test riêng, tài khoản/password sinh tạm ở runtime.

CSS/header/footer gốc không đổi. Phần content của các template đã thay để khớp contract BE;
không tuyên bố các template này giống ZIP từng byte. Phạm vi layout dùng chung đã được thông báo
trong tài liệu BE review của lượt FE-INT-FIX. Người dùng đã duyệt push bản diff để BE review code.

## Bằng chứng kiểm thử ngày 2026-10-14

- Maven clean verify với -Dnet.bytebuddy.experimental=true, Java 26: BUILD SUCCESS.
  185 tests được phát hiện, 182 pass, 3 PostgreSQL tests skip do Docker local chưa chạy.
- Unit JS: 18/18 PASS, gồm server STOMP refusal không retry và expired session phát logout.
- HTTP/Chromium read-only runner: 93 PASS, 0 FAIL, 5 BLOCKED là các nhóm authenticated
  mà runner này cố ý không thực hiện. Đọc kết hợp suite giao dịch bên dưới; không coi những
  placeholder này là bằng chứng API/policy còn thiếu.
- Browser giao dịch thật: 32 PASS, 3 FAIL. PASS: login, catalog/escape, add/cart hydrate,
  rollback PUT lỗi, wishlist, profile PATCH, address CRUD/default, CUSTOMER bị admin API từ chối
  nhưng giữ session, quote/invalidate/place, order focus/detail, ownership User B trả 404,
  customer cancellation, admin list/detail/status, customer REST resync, password/login lại,
  logout, cart 390px không overflow và không uncaught browser exception.
- CDP xác nhận customer/admin gửi Authorization ở STOMP CONNECT; không lưu frame/JWT.
- 11 trang render HTTP 200; responsive 1440/768/390, labels, Tab/focus, form validation và
  menu Escape/focus đã kiểm tra bằng trình duyệt. Đây không phải accessibility audit đầy đủ.

Artifact local (không commit):
D:/UTEGear/.local-reference/storefront-test-artifacts/pr54-final/public/report.json
D:/UTEGear/.local-reference/storefront-test-artifacts/pr54-final/browser/report.json
D:/UTEGear/.local-reference/storefront-test-artifacts/pr54-final/browser/cart-390.png
Build log: D:/UTEGear/.local-reference/storefront-test-artifacts/pr54/verify.log

## Hai lỗi cần BE xử lý trên branch riêng

### Cart PUT — HTTP 500

Register/login CUSTOMER trên H2 fixture; POST /api/cart/items variantId=100 quantity=2;
GET cart lấy itemId của chính user; PUT /api/cart/items/{itemId} quantity=3.
Actual: HTTP 500, IllegalArgumentException “Dòng sản phẩm không thuộc giỏ hàng” tại Cart.java:75.
Expected: cập nhật quantity/subtotal. FE hiện báo lỗi và rollback quantity=2.
BE cần kiểm tra guard entity identity item.getCart() != this và items.contains(item).

### Customer/admin STOMP — subscription bị từ chối

Sau login hợp lệ, mở customer order detail hoặc admin/orders. Native WebSocket /ws,
CONNECT có JWT; client subscribe /user/queue/orders hoặc /topic/admin/orders.
Server log: “Kết nối STOMP chưa được xác thực” từ StompSubProtocolHandler, cả hai vai trò.
FE hiển thị bị từ chối và dừng retry. Có thể liên quan việc principal của CONNECT không được
truyền sang SUBSCRIBE: interceptor hiện wrap message rồi MessageBuilder set USER_HEADER.
Đây là giả thuyết cần BE xác minh; branch FE không sửa interceptor hoặc bỏ kiểm tra quyền.

## Chạy lại

Windows, Node 22+, JDK/Maven phù hợp repo, Chromium chạy CDP loopback port 9238 với profile riêng.
Trong worktree này, đặt QA_MAVEN_CACHE nếu dependency cache khác ~/.m2/repository:

```powershell
$env:QA_MAVEN_CACHE='D:/maven-repository'
node tests/frontend/run-storefront-checks.mjs D:/UTEGear/.local-reference/storefront-test-artifacts/qa-rerun
```

Launcher kiểm tra port 18081 chưa dùng, chạy Spring Boot test profile/H2 create-drop và fixture tạm,
chạy hai browser runners rồi dừng cây process server của nó. Chỉ loopback, không production.
Browser CDP do người chạy quản lý riêng. Fixture không phải Flyway migration/schema dùng chung.
Password chỉ trong bộ nhớ/environment process; helper/seed chứa hash tạm được xóa sau test.

## QA-04 chưa hoàn tất

Hai lỗi BE trên cần sửa và chạy lại. Chưa xác nhận realtime delivery/reconnect end-to-end;
chưa bao phủ đầy đủ multi-variant/media, coupon/stock/quote hết hạn hoặc race/double-submit,
JWT hết hạn trên browser và checklist accessibility đầy đủ. Test doubles không thay E2E.
Các API profile/address/order và policy đã có, không còn là blocker thiếu contract trước PR #54.
Chưa đánh dấu DONE và chưa chuyển sang QA-05; code cần BE review/PR theo quy tắc repository.
