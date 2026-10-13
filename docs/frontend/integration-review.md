# Bản review FE-INT-FIX — INT-01 / INT-02 / INT-03

- Người phụ trách: Frontend `@trongsonho`.
- Base GitHub: `b2225f8` (PR #50), kiểm tra bằng `git fetch origin` ngày 2026-10-13.
- Branch: `fix/frontend-integration`.
- BE đã yêu cầu push branch để review code qua phản hồi người dùng ngày 2026-10-13.
  Lượt này commit/push bản prototype để review; chưa mở PR, chưa được duyệt merge.
  Không thay Java, pom.xml, application properties, migration hoặc backend tests.

## Đã chuẩn bị

1. Khôi phục 7 CSS theo ZIP: badges, empty-state, footer, navbar, pagination, skeleton,
   core/utilities. SHA-256 đã đối chiếu 7/7. Giữ nguyên whitespace của nguồn ZIP.
2. API client: header JWT cho API cùng origin, JSON, 204 không body, lỗi 401/403,
   PUT/PATCH/DELETE, multipart giữ boundary do trình duyệt tạo.
3. Auth: POST JSON login/register, prototype phiên JWT trong sessionStorage theo tab, không lưu password,
   redirect cùng origin, lỗi/đang xử lý tại form và khóa submit kép. Register không phụ thuộc
   registerForm cũ. Password 8–72 ký tự theo request BE. sessionStorage CHƯA được BE duyệt;
   xem `auth-session-decision.md`, không merge khi chưa chốt quyết định.
4. Autocomplete: q, content, effectivePrice, primaryImage.secureUrl; bỏ response cũ.
   Quick view dùng optionValues, escape nội dung và kiểm tra URL ảnh.
5. Cart: endpoint /items, defaultVariantId thật khi quick add, request chỉ variantId/quantity,
   badge dùng itemCount. Cả hàm cũ main.js và event delegation đều gọi cùng module.
6. Wishlist: đọc state từ GET /api/wishlist rồi POST /items hoặc DELETE /items/{productId};
   khóa thao tác trùng, chỉ đổi icon khi server thành công.
7. Client checkout quote/place: addressId, fulfillmentStoreId, COD/BANK_TRANSFER,
   variantId/quantity và quoteToken; không gửi giá client, từ chối quote hết hạn.
   Đây là hàm client, CHƯA nối form checkout cũ.
8. Client admin: order list/detail/status PUT, inventory Store + Variant, transfer và media multipart.
   Form đổi trạng thái admin order đã nối handler PUT; các form admin khác CHƯA chuyển hết.
9. Client realtime mới: STOMP qua WebSocket thuần /ws (không SockJS), header
   Authorization: Bearer ở frame STOMP CONNECT, không ở HTTP handshake, destinations đúng,
   envelope, lọc orderId/type/status, dedup eventId, tải REST trước áp event, retry có giới hạn,
   dừng khi logout/hết phiên/rời trang. Module mới CHƯA được nạp vào layout/page realtime cũ.

## Phần còn thiếu — không ghi INT-01/02/03 DONE

### Phụ thuộc Backend

- Main chưa có MVC @Controller trả các template và cung cấp model DTO cần thiết.
- Template dùng layout dialect, nhưng pom.xml chưa khai báo dependency đó.
- Profile hiện chỉ có GET: chưa có PATCH profile và PUT password.
- Chưa có address controller để đọc/tạo địa chỉ, trong khi quote/place bắt buộc addressId.
- Orders hiện chỉ POST tạo đơn: thiếu GET list/detail/cancel của khách hàng.

### Đối chiếu scope theo contract/backlog

| Phần từ template cũ | Contract/backlog hiện có | Kết luận cho lượt FE này |
|---|---|---|
| Forgot-password | API-01 và AUTH-01/02/03 chưa định nghĩa flow khôi phục mật khẩu | Chưa có phạm vi/contract; không gán mặc định thành blocker Backend. Giữ ngoài phạm vi tích hợp hiện tại, cần task và contract được duyệt nếu triển khai. |
| Admin user/coupon | UI-03 có màn hình user/coupon; ORD-01 có domain coupon; API-03 có validate coupon, không có CRUD admin user/coupon | Khoảng trống cần làm rõ scope/contract; không kết luận Backend thiếu CRUD chỉ từ form ZIP. |
| Admin product/brand/category | UI-02 có các màn hình; API-01 định nghĩa public catalog, API-02 có media/import nhưng không có CRUD admin product/brand/category | Chỉ nối media/import có contract; CRUD cũ cần chốt endpoint/task, không tự giao Backend thêm API. |
| Admin store/inventory/order | API-02/03 định nghĩa endpoint admin tương ứng | Đối chiếu implementation hiện có và nối FE đúng contract, giữ role ADMIN ở server. |

### Quyết định bảo mật chưa chốt

- sessionStorage chỉ là prototype trong diff. BE chưa chấp thuận làm quyết định chính thức.
- Tài liệu `auth-session-decision.md` ghi lựa chọn, rủi ro, giới hạn và điều kiện BE review
  trước merge; không thay API/kiến trúc trong PR FE.

### FE cần nối tiếp

- Thay model/helper cũ trong toàn bộ template bằng field DTO cùng contract; các template còn dùng
  priceFormatter/categoryHelper, isAdmin(), stock/attributeValue và các field entity cũ.
- Nạp admin/realtime module trong hai layout dùng chung, bỏ đoạn SockJS/STOMP cũ.
  BE đã nhận và review tài liệu phạm vi theo phản hồi người dùng. Hai layout chưa sửa trong diff này.
- Nối form checkout bằng select địa chỉ/cửa hàng và quote được server cấp khi API address có sẵn.
- Nối profile/order/admin còn lại theo endpoint BE đã có hoặc chờ endpoint contract được bổ sung.
- Hiển thị session/JWT ở header; client JWT hiện không làm model SSR isAuthenticated tự thay đổi.

## Kiểm thử

- node --test tests/frontend/integration.test.mjs tests/frontend/mock-data.test.mjs: 16/16 PASS.
- node --check: 9 JS đã sửa/thêm PASS; app.js được Node VM nạp lại sau bổ sung Catalog/Stores.
- mvnw.cmd -B clean verify -Dnet.bytebuddy.experimental=true: BUILD SUCCESS trên Java 26,
  169 tests được phát hiện, 166 pass, 3 PostgreSQL tests skip vì Docker local chưa chạy.
  Flag Byte Buddy chỉ dùng khi chạy lệnh, không thay pom.xml/config.
- git diff --check báo whitespace ở 7 CSS khôi phục đúng ZIP; không sửa nguồn để xóa cảnh báo.
- Chưa chạy browser/E2E/API thật. Kiểm thử JS dùng test doubles, không phải kết quả QA-04/QA-05.

## Thông báo phạm vi layout đã được BE review qua tài liệu

“FE-INT-FIX liên kết INT-01/02/03, branch fix/frontend-integration: FE dự kiến sửa
layouts/main.html và layouts/admin.html để nạp API/JWT/admin/realtime scripts, bỏ SockJS client
cũ. Không sửa Java, pom.xml, config hoặc migration. BE cần xử lý routing/model/dependency/API
đã xác nhận bằng task/branch/PR riêng. Phần ngoài contract cần chốt scope trước khi phân công.”
