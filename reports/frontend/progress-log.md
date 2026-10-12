# Nhật ký tiến độ Frontend

Người phụ trách: `@trongsonho`

Cập nhật cùng một mục khi task đổi trạng thái. Task `BLOCKED` phải ghi phần còn thiếu; task
`DONE` phải có Pull Request, ngày hoàn tất và kết quả kiểm thử.

## FND-04: Frontend quality workflow

- Trạng thái: `IN PROGRESS`
- Branch: `ci/frontend-quality-gate`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Xem mã nguồn ZIP làm tài liệu tham khảo, chưa chuyển giao diện nghiệp vụ vào branch.
  - Giữ workflow Frontend ở chế độ `workflow_dispatch` khi chưa có công cụ kiểm tra thật.
  - Thay bước TODO báo thành công bằng thông báo chưa sẵn sàng và exit code `1`.
  - Ghi phạm vi kiểm tra cần có và điều kiện bật trigger Pull Request trong `docs/frontend/quality-checks.md`.
- Kết quả kiểm thử:
  - `mvnw.cmd -B clean verify`: `PASS`, 1 test, 0 failure/error/skipped; chạy trên bản sạch từ `origin/main` commit `0ae22f7` kèm ba file FND-04 trong `.local-reference/fnd-04-verification`.
  - Parse YAML bằng SnakeYAML: `PASS`; chỉ có trigger thủ công và bước báo chưa có tooling.
  - Chạy bước workflow bằng Git Bash: `PASS`; kết thúc với exit code `1` đúng dự kiến khi chưa có tooling, không báo thành công giả.
  - `git diff --check` cho phạm vi FND-04: `PASS`.
  - GitHub Actions: `CHƯA CHẠY`; chưa push hoặc mở PR.
- Vướng mắc hoặc phần còn thiếu:
  - Người dùng đã xác nhận phạm vi ba file FND-04; chưa mở PR hoặc có review.
  - Công cụ kiểm tra giao diện sẽ triển khai khi có code Frontend; đây là giới hạn đã định trong FND-04.
- Bước tiếp theo:
  - Push branch đã được xác nhận, mở PR và chuyển sang `IN REVIEW` để Backend review.
- Ngày hoàn tất: `Chưa hoàn tất`

## UI-01: Layout và design foundation (A + B)

- Trạng thái: `IN PROGRESS`
- Branch: `feat/shared-ui-layout`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Người dùng yêu cầu gom phần A (CSS) và B (layout/components/mock) vào một PR UI-01, hẹn push 23:37 ngày 11/10/2026 (UTC+7); ghi phạm vi trong `docs/frontend/ui-foundation-plan.md`.
  - Lấy CSS core và chín component chung từ ZIP tham khảo, dọn khoảng trắng và tạo `app.css` chỉ import phạm vi này.
  - Thêm preview CSS và template preview Thymeleaf cho bộ khung dùng chung.
  - Thêm layout/header/footer, skip link, menu mobile, toast, pagination và empty-state.
  - Thêm fixture loader và bản sao JSON API-01 đã merge; không gọi endpoint nghiệp vụ hoặc lưu JWT.
  - Tạo checkout riêng từ `origin/main` commit `4ff0d53`, giữ nguyên file cũ ở thư mục gốc.
- Kết quả kiểm thử:
  - `mvnw.cmd -B clean verify`: `PASS`, 21 tests, 0 failure/error/skipped; Java 26 biên dịch với release 21.
  - CSS phần A: `PASS`, đủ 13 import từ ZIP; thêm `shell.css` cho layout phần B.
  - Preview desktop 1440px và mobile 390px: `PASS`, không tràn chiều ngang; đã xem screenshot cục bộ.
  - Keyboard focus button và chế độ reduced motion của skeleton: `PASS` trên preview.
  - `node --test tests/frontend/mock-data.test.mjs`: `PASS`, 4 tests cho contract fixture, clone độc lập, chặn nguồn sai và xử lý lỗi tải.
  - `node --check` cho JavaScript: `PASS`.
  - Render layout và các fragment bằng SpringTemplateEngine/MockServletContext: `PASS`; không thêm controller hoặc dependency.
  - `tests/frontend/browser-check.mjs` trên Edge với Bootstrap: `PASS` cho viewport 1440/390px, menu mobile/Escape/focus, toast dạng text, pagination biên, mock empty và reduced motion; đã xem screenshot.
  - Bundle CSS cuối cùng: 14 import tải được, không tham chiếu custom property bị thiếu.
  - GitHub Actions: `CHƯA CHẠY`; chưa đến thời gian push/mở PR.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa có PR/review; sẽ chuyển `IN REVIEW` khi PR được mở.
- Bước tiếp theo:
  - Hoàn tất kiểm tra, commit bản đã kiểm tra và lập lịch push một lần lúc 23:37; sau push mở PR để Backend review.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-AUTH-01: Giao diện đăng ký và profile

- Trạng thái: `BLOCKED`
- Branch: `feat/auth-profile-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị đưa template đăng ký/profile và CSS auth/account có sẵn vào branch riêng.
  - Giữ nguyên nội dung bốn file nguồn, không viết mới hoặc chỉnh sửa hành vi.
  - Không thay đổi Backend, dependency, layout chung hoặc API contract.
- Kết quả kiểm thử:
  - Kiểm tra SHA-256: `PASS`, 4/4 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify`: `PASS`, 50 tests, 0 failure, 0 error, 0 skipped.
  - `git diff --cached --check`: có 3 dòng whitespace trong profile.html nguyên bản; giữ nguyên theo yêu cầu không sửa code.
  - Các test hiện có chưa xác nhận những trang mới có thể render hoặc tích hợp với ứng dụng.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa đưa template login vào vì chứa mật khẩu tài khoản hard-code; AGENTS.md không cho commit mật khẩu.
  - Template dùng layout:decorate và model/endpoint cũ, chưa tương thích layout UI-01 và contract API-01.
  - Giữ nguyên nguồn theo yêu cầu; chưa hoàn tất FE-AUTH-01, chưa sẵn sàng merge.
- Bước tiếp theo:
  - Người dùng kiểm tra phạm vi và nội dung trước khi commit/push.
  - Thống nhất xử lý login và tương thích layout/contract trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-CAT-01: Giao diện catalog, tìm kiếm và chi tiết sản phẩm

- Trạng thái: `BLOCKED`
- Branch: `feat/product-catalog-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị ba trang products/search/product-detail, hai fragment product-card/product-filter, bốn file CSS và hai file JavaScript có sẵn.
  - Giữ nguyên nội dung 11 file nguồn; không viết mới hoặc chỉnh sửa hành vi.
  - Tạo branch từ main commit `ea9ce82`, sau khi PR #20 đã merge.
  - Không thay Backend, dependency, layout/header/footer/pagination chung hoặc API contract.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 11/11 file khớp nguyên bản.
  - `node --check` cho filter.js và variant.js: `PASS`.
  - `git diff --cached --check`: có cảnh báo whitespace ở file nguyên bản; giữ nguyên theo yêu cầu không sửa code.
  - `mvnw.cmd -B clean verify`: `PASS`, 50 tests, 0 failure, 0 error, 0 skipped.
  - Maven kiểm tra regression hiện có, chưa xác nhận các template mới render hoặc hoạt động với contract hiện tại.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout:decorate và gọi pagination theo chữ ký cũ, chưa tương thích layout/fragment UI-01.
  - Field imageUrl/stock và groupedVariants() trong template khác DTO API-01; chưa có mock theo contract mới.
  - Các handler quickView/addToCart/toggleWishlist và việc nạp CSS/JS cũ chưa được nối với layout hiện tại.
  - Giữ nguyên code theo yêu cầu; đây là bản đưa nguồn lên để review, chưa hoàn tất FE-CAT-01 hoặc xác nhận giao diện chạy được.
- Bước tiếp theo:
  - Người dùng đã yêu cầu hẹn push bản đã kiểm tra lúc 13:23 ngày 12/10/2026 (UTC+7); chưa có PR/review.
  - Thống nhất cách xử lý tương thích trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-STORE-01: Giao diện cửa hàng

- Trạng thái: `BLOCKED`
- Branch: `feat/store-availability-ui`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/23
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên template danh sách/chi tiết cửa hàng và CSS stores có sẵn.
  - Không sửa nội dung code, Backend, dependency, layout chung hoặc contract.
  - Đã push theo lịch lúc 13:54:21 ngày 12/10/2026 (UTC+7), commit `f2fcf60`.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 3/3 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify`: `PASS`, 50 tests, 0 failure, 0 error, 0 skipped.
  - Kiểm thử regression hiện có chưa chứng minh template mới hoạt động; diff check có whitespace nguyên bản được giữ nguyên theo yêu cầu.
  - Sau đồng bộ main và xử lý conflict PR #23: `mvnw.cmd -B clean verify` PASS với 57 tests, 0 failure/error/skipped; diff check của phần nhật ký PASS.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout:decorate, openTime/closeTime và danh sách sản phẩm theo model cũ; chưa tương thích UI-01/API-02.
  - Chưa có mock loading/error và tồn kho theo Store + ProductVariant; chưa xác nhận trang chạy được.
  - Giữ nguyên code theo yêu cầu; bản đưa nguồn lên review chưa hoàn tất FE-STORE-01.
- Bước tiếp theo:
  - Review PR #23; đã đồng bộ main và giữ cả mục FE-CAT-01 lẫn FE-STORE-01 khi xử lý conflict nhật ký.
  - Thống nhất xử lý tương thích và tồn kho theo biến thể trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-CART-01: Giao diện giỏ hàng

- Trạng thái: `BLOCKED`
- Branch: `feat/cart-page`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/24
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên cart template, shipping-progress fragment, CSS cart và JavaScript cart có sẵn.
  - Không sửa nội dung code, Backend, dependency, layout chung hoặc contract.
  - Đã push theo lịch lúc 14:12:17 ngày 12/10/2026 (UTC+7), commit `d65affc`.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 4/4 file khớp nguyên bản.
  - `node --check` cho cart.js: `PASS`.
  - Sau đồng bộ main và xử lý conflict PR #24: `mvnw.cmd -B clean verify` PASS với 57 tests, 0 failure/error/skipped; diff check phần nhật ký PASS.
  - `mvnw.cmd -B clean verify`: `PASS`, 50 tests, 0 failure, 0 error, 0 skipped.
  - Kiểm thử regression hiện có chưa chứng minh template mới hoạt động; diff check có whitespace nguyên bản được giữ nguyên theo yêu cầu.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout:decorate, cartItems/entity cũ và bean priceFormatter; chưa khớp layout/DTO API-03.
  - cart.js gọi /api/cart/add qua UTEGear.API, chưa có mock trong task này; không chạy thao tác trên production trong kiểm thử.
  - Handler cartPlus/cartMinus/removeCartItem/clearCartAjax và việc nạp CSS/JS chưa được nối với layout hiện tại.
  - Chưa xác nhận xử lý giỏ hàng theo biến thể hoặc trang chạy được; giữ nguyên code theo yêu cầu, chưa hoàn tất FE-CART-01.
- Bước tiếp theo:
  - Review PR #24; đã đồng bộ main và giữ các mục tiến độ trước khi xử lý conflict nhật ký.
  - Thống nhất xử lý layout/model/mock trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-WISH-01: Giao diện danh sách yêu thích

- Trạng thái: `BLOCKED`
- Branch: `feat/wishlist-page`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên wishlist template và JavaScript wishlist có sẵn trên branch từ main `b4ed32e`.
  - Giữ nguyên nội dung hai file; không viết mới hoặc chỉnh sửa hành vi.
  - Không thay đổi Backend, layout/header/footer, dependency hoặc API contract.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 2/2 file khớp nguyên bản.
  - `node --check` cho wishlist.js: `PASS`.
  - Diff check có một dòng whitespace trong wishlist.html nguyên bản; giữ nguyên theo yêu cầu không sửa code.
  - `mvnw.cmd -B clean verify`: `PASS`, 107 tests, 0 failure, 0 error, 0 skipped.
  - Kiểm tra JavaScript cô lập với API/DOM giả lập: `PASS` cho ID rỗng, thêm/xóa và trạng thái icon, lỗi UNAUTHORIZED và lỗi yêu cầu; không gọi API thật.
  - Các kiểm tra này chưa chứng minh template mới render hoặc tích hợp được trên ứng dụng.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout:decorate, wishlist item/entity và product card cũ; chưa có mock theo API-03.
  - JavaScript gọi /api/wishlist/toggle qua UTEGear.API và cần được nạp trong layout; chưa xác nhận tích hợp với ứng dụng hiện tại.
  - Giữ nguyên nguồn theo yêu cầu; bản đưa lên review chưa hoàn tất FE-WISH-01 hoặc xác nhận trang chạy được.
- Bước tiếp theo:
  - Push branch sau kiểm tra; mở PR để review.
  - Thống nhất xử lý layout/model/mock trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-ORD-01: Giao diện checkout và đơn hàng

- Trạng thái: `BLOCKED`
- Branch: `feat/checkout-order-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên checkout, xác nhận đơn, lịch sử đơn, chi tiết đơn, tra cứu đơn và CSS checkout/orders có sẵn.
  - Giữ nguyên bảy file nguồn, gồm cả WebSocket client inline trong trang chi tiết đơn; không tách hoặc viết mới code.
  - Tạo branch từ main `2764ff1` sau khi PR #33 đã merge; không thay đổi Backend, dependency, layout chung hoặc API contract.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 7/7 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify`: `PASS`, 107 tests, 0 failure, 0 error, 0 skipped.
  - Kiểm tra cú pháp ba khối JavaScript inline: `PASS`, chỉ parse, không thực thi hoặc gọi API/WebSocket thật.
  - Diff check có whitespace nguyên bản trong các file nguồn; giữ nguyên theo yêu cầu không sửa code.
  - Các test regression hiện có chưa chứng minh các trang mới render hoặc tích hợp được.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout:decorate, model currentUser/orderItems và bean priceFormatter cũ; chưa tương thích layout/DTO API-03.
  - Form POST /checkout và pagination dùng giao thức/chữ ký cũ; chưa có mock loading/validation/empty/error theo contract.
  - WebSocket inline cần SockJS/STOMP WebJars và server/destination tương ứng; chưa kiểm tra realtime hoặc reconnect.
  - Không chạy checkout hay tạo đơn trên production trong kiểm thử. Giữ nguyên nguồn theo yêu cầu; chưa xác nhận các trang hoạt động hoặc hoàn tất FE-ORD-01.
- Bước tiếp theo:
  - Push branch sau kiểm tra, mở PR để review.
  - Thống nhất xử lý tương thích layout/model/mock trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-WS-01: WebSocket client

- Trạng thái: `BLOCKED`
- Branch: `feat/order-websocket-client`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Đối soát main `c0ea201`: client cập nhật trạng thái đơn trong order-detail.html đã có từ FE-ORD-01/PR #34.
  - Chuẩn bị nguyên layouts/admin.html có client SockJS/STOMP nhận thông báo đơn mới tại /topic/admin/orders và retry sau 5 giây.
  - Giữ nguyên file nguồn; không tách hoặc viết mới code, không thay Backend, dependency hoặc layout khách hàng.
  - Các trang/sidebar/CSS admin thuộc UI-02/UI-03; không đưa vào commit FE-WS-01.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, layout admin khớp nguyên bản.
  - `mvnw.cmd -B clean verify`: `PASS`, 119 tests, 0 failure, 0 error, 0 skipped.
  - Script inline: `PASS` kiểm tra cú pháp, destination admin, thông báo, frame JSON lỗi và retry 5 giây bằng SockJS/STOMP/DOM giả lập; không kết nối thật.
  - `git diff --cached --check`: `PASS`.
  - Các kiểm tra này chưa chứng minh realtime hoặc layout hoạt động trong ứng dụng.
- Vướng mắc hoặc phần còn thiếu:
  - Layout dùng Thymeleaf layout dialect và tham chiếu sidebar/admin CSS, main.js/utils.js và WebJars chưa có đủ trong main.
  - Client gốc kết nối /ws trực tiếp, chưa có mock event/API-03 envelope, trạng thái kết nối hoặc khử trùng event theo contract.
  - Chưa kiểm tra phân quyền WebSocket server, quyền nhận event hoặc tích hợp thật; không kết nối production trong kiểm thử.
  - Giữ nguyên nguồn theo yêu cầu; chưa hoàn tất FE-WS-01 hoặc xác nhận giao diện realtime hoạt động.
- Bước tiếp theo:
  - Push branch sau kiểm tra, mở PR để review.
  - UI-02 tái sử dụng layout đã có; thống nhất xử lý dependency/contract trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## UI-02: Admin catalog và cửa hàng

- Trạng thái: `BLOCKED`
- Branch: `feat/admin-catalog-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên product list/form, brand/category list, store list/detail, sidebar admin và CSS admin có sẵn.
  - Giữ nguyên tám file nguồn, tái sử dụng layouts/admin.html đã merge qua FE-WS-01/PR #36.
  - Tạo branch từ main `157456a`; không thay Backend, dependency, layout chung, app.css hoặc API contract.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 8/8 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - Lần chạy Java 26 lỗi do Byte Buddy chưa hỗ trợ phiên bản JVM; chạy lại bằng Java 21 thành công, không sửa code hoặc dependency.
  - JavaScript: `PASS` kiểm tra cú pháp 1 script inline và 4 handler tĩnh; không thực thi thao tác admin.
  - `git diff --cached --check`: `PASS`.
  - Build và kiểm tra cú pháp chưa chứng minh các trang hoạt động với Backend hiện tại.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout dialect, model/entity, pagination và POST /admin/* theo giao thức cũ; chưa có mock API-02.
  - Trang tồn kho dùng inv.product.id và stock cũ, khác mô hình Store + ProductVariant; chưa xác nhận các thao tác hoạt động.
  - CSS admin chưa được nạp vào app.css; dependency layout/script chung chưa có đầy đủ.
  - Không gọi API admin hay ghi database trong kiểm thử; bản nguồn chưa hoàn tất UI-02 hoặc sẵn sàng merge.
- Bước tiếp theo:
  - Push branch sau kiểm tra và mở PR để review.
  - Thống nhất xử lý tương thích và inventory theo biến thể trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## Mẫu mục mới

```md
## TASK-ID: Tên task

- Trạng thái: `IN PROGRESS | IN REVIEW | BLOCKED | DONE`
- Branch: `type/ten-branch`
- Pull Request: URL hoặc `Chưa mở`
- Ngày cập nhật: YYYY-MM-DD
- Đã làm:
  - ...
- Kết quả kiểm thử:
  - Lệnh hoặc trường hợp kiểm thử: `PASS | FAIL | CHƯA CHẠY`
- Vướng mắc hoặc phần còn thiếu:
  - Không có, hoặc ghi rõ phần đang thiếu và task phụ thuộc.
- Bước tiếp theo:
  - ...
- Ngày hoàn tất: YYYY-MM-DD hoặc `Chưa hoàn tất`
```

## UI-03: Admin người dùng và đơn hàng

- Trạng thái: `BLOCKED`
- Branch: `feat/admin-user-order-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên dashboard, user list/detail, coupon list và order list/detail có sẵn.
  - Giữ nguyên sáu file nguồn; layout admin đã có từ FE-WS-01, sidebar/CSS được chuẩn bị trên branch UI-02.
  - Tạo branch riêng từ main `157456a`; không thay Backend, dependency, layout chung hoặc API contract.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 6/6 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - Lần chạy Java 26 lỗi do Byte Buddy chưa hỗ trợ phiên bản JVM; chạy lại bằng Java 21 thành công, không sửa code hoặc dependency.
  - JavaScript: `PASS` kiểm tra cú pháp 2 handler tĩnh; không thực thi thao tác admin.
  - `git diff --cached --check`: `PASS`.
  - Build và kiểm tra cú pháp chưa chứng minh các trang hoạt động với Backend hiện tại.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout dialect, user.isAdmin(), model order/entity và pagination cũ; chưa có mock API-03.
  - Các form coupon/order status dùng POST /admin/* khác API hiện tại; chưa xác nhận hoạt động.
  - Sidebar/CSS của UI-02 cần được merge trước khi kiểm tra bộ giao diện admin đầy đủ.
  - Không gọi API admin hoặc đổi trạng thái đơn trên production; bản nguồn chưa hoàn tất UI-03 hoặc sẵn sàng merge.
- Bước tiếp theo:
  - Push branch sau kiểm tra và mở PR riêng; review UI-02 trước khi tích hợp phần giao diện dùng chung.
  - Thống nhất xử lý model/API/mock trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`
