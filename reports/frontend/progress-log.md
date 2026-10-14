# Nhật ký tiến độ Frontend

## FE-BIND-01: Binding dữ liệu và realtime FE theo BE

- Trạng thái: `IN PROGRESS`
- Phụ trách: `@trongsonho` (Frontend); liên kết INT-01/02/03, QA-04.
- Branch nguồn: `fix/storefront-bindings`; commit FE `462c9eb` đã merge qua [PR #56](https://github.com/NgocYuh/UTEGear/pull/56) tại `7457eb5`.
- Bản kiểm thử/báo cáo tiếp: `test/qa04-realtime-verification`, base `7457eb5`; chưa mở PR.
- Ngày cập nhật: 2026-10-14
- Đã kiểm thử sau PR #55 bằng merge local `cdcb689`; tree nguồn trùng main `7457eb5` sau PR #56.
  Người dùng đã duyệt push phần kiểm thử/báo cáo trên nhánh mới để BE review.
- Đã làm:
  - Catalog/cart/wishlist/profile/address/checkout/customer-admin orders theo BE contract.
  - STOMP native /ws, Authorization ở CONNECT; sessionStorage theo policy APPROVED PR #54.
  - Đồng bộ bản BE sửa cart/STOMP từ PR #55, không tự sửa Backend Java/config/migration.
  - Bổ sung browser test nhận event thật, socket loss, reconnect và REST resync customer/admin.
- Kết quả kiểm thử sau PR #55:
  - `mvnw.cmd -B clean verify -Dnet.bytebuddy.experimental=true`: `BUILD SUCCESS`, Java 26;
    185 tests được phát hiện, 182 pass, 3 PostgreSQL tests skip vì Docker chưa chạy.
  - Unit JS: `18 PASS`; browser script syntax PASS.
  - HTTP/DOM/responsive/labels/focus/validation: `93 PASS, 0 FAIL`; 5 nhóm ngoài phạm vi
    read-only runner giữ placeholder BLOCKED, đối chiếu với suite giao dịch riêng.
  - Browser/API giao dịch thật: `39 PASS, 0 FAIL`, không mock API/frame STOMP.
  - Cart PUT cập nhật quantity/subtotal PASS. Customer/admin CONNECT/subscription PASS.
  - Customer nhận ORDER_STATUS_CHANGED, admin nhận ORDER_CREATED và UI cập nhật không navigation.
  - Customer/admin reconnect sau đóng native socket và GET REST resync mới PASS.
  - Không có uncaught browser exception. Test server/browser riêng đã dừng sau lưu artifact.
- Đối soát blocker cũ:
  - Cart PUT 500 và STOMP subscription từ chối đã được BE sửa PR #55 và xác nhận hết trên H2.
  - Không còn ghi thiếu profile/address/customer-order API, route hoặc policy.
- Còn cần hoàn tất:
  - Review/duyệt cập nhật kiểm thử và các ca QA-04 chưa bao phủ; PR #56 merge không thay thế kiểm thử còn thiếu.
  - Chưa xác nhận PostgreSQL/Flyway thực tế hoặc toàn bộ dataset theo hướng dẫn BE.
- Review/bằng chứng: `docs/frontend/storefront-bindings-review.md`.
- Tiếp theo: review bản đồng bộ/kiểm thử, hoàn thiện QA-04 trước QA-05.
- Ngày hoàn tất: `Chưa hoàn tất`

## QA-04: Frontend automated và manual tests

- Trạng thái: `IN PROGRESS` (đã gỡ hai blocker BE sau PR #55).
- Người phụ trách: `@trongsonho` (Frontend).
- Tooling gốc: `test/frontend-flows`, đã merge [PR #53](https://github.com/NgocYuh/UTEGear/pull/53)
  tại `aba8d3c`; merge tooling không đồng nghĩa QA-04 đã DONE.
- Bản FE đã merge PR #56; kiểm thử/báo cáo tiếp: `test/qa04-realtime-verification`, base `7457eb5`.
  Người dùng đã duyệt push phần mới để BE review, chưa mở PR.
- Ngày cập nhật: 2026-10-14
- Đã làm:
  - Chạy HTTP/CDP trên Spring Boot thật với H2 tạm và CUSTOMER A/B + admin QA.
  - Catalog/cart/wishlist/profile/address/quote/place/cancel/admin status/password/logout và ownership.
  - Responsive 1440/768/390, labels, Tab/focus, form required/email/mismatch, menu Escape/focus.
  - Realtime customer/admin: JWT CONNECT, events thật, mất socket, reconnect và REST resync.
- Kết quả:
  - Sau PR #52: `79 PASS, 0 FAIL, 7 BLOCKED` (lịch sử trên các trang/dữ liệu rỗng).
  - Sau PR #54: giao dịch `32 PASS, 3 FAIL` (cart PUT, customer/admin STOMP; lịch sử).
  - Sau PR #55: giao dịch `39 PASS, 0 FAIL`; read-only `93 PASS, 0 FAIL, 5 BLOCKED` là
    placeholder cho các nhóm không chạy trong runner read-only, không phải 5 lỗi API mới.
  - Unit JS `18 PASS`; Maven `BUILD SUCCESS`, 182 pass/3 PostgreSQL skip.
- Còn thiếu:
  - Dataset nhiều biến thể/media, store inactive, thiếu hàng/coupon/quote hết hạn/race/double-submit,
    JWT expiry trên browser và phần checklist keyboard/accessibility còn lại chưa kiểm tra đầy đủ.
  - Dữ liệu H2 chỉ phủ happy path và các quyền đã ghi, chưa thay kiểm thử PostgreSQL/Flyway.
  - PR #56 của FE đã merge; cập nhật kiểm thử mới chưa mở/duyệt PR, các ca còn thiếu chưa hoàn tất.
    Chưa ghi DONE, chưa bắt đầu QA-05.
- Bằng chứng mới: `D:/UTEGear/.local-reference/storefront-test-artifacts/pr55-final/`;
  build log `D:/UTEGear/.local-reference/storefront-test-artifacts/pr55-verify.log`.
  Artifact/log/ảnh/tài khoản/token không commit.
- Review/cách chạy: `docs/frontend/storefront-bindings-review.md`.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-INT-FIX: Khôi phục CSS ZIP và đối chiếu INT-01/02/03 với Backend

- Trạng thái: `BLOCKED`
- Người phụ trách: `@trongsonho` (Frontend).
- Task liên kết: `INT-01`, `INT-02`, `INT-03`.
- Branch: `fix/frontend-integration`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/51
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Khôi phục 7 CSS nguyên ZIP; SHA-256 khớp 7/7.
  - Chuẩn bị API/JWT, auth form, search/quick view, cart/wishlist theo Backend main b2225f8.
  - Thêm client checkout/admin/realtime và kiểm thử hành vi; chưa nối đầy đủ mọi trang.
  - Chi tiết phạm vi, phần chưa nối và thông báo layout: `docs/frontend/integration-review.md`.
  - Người dùng chuyển yêu cầu BE: push branch hiện tại để BE review/sửa tiếp; không coi đây là
    phê duyệt sessionStorage hoặc xác nhận INT-01/02/03 hoàn tất.
  - PR #51 đã merge vào main tại `2979dd4`; cập nhật khi chuẩn bị QA-04. Phạm vi source đã merge,
    nhưng trạng thái vẫn BLOCKED vì routing/API/UI/policy chưa hoàn tất.
- Kết quả kiểm thử:
  - JS integration + fixture: `PASS` 16 tests, 0 failure.
  - JS syntax: `PASS` 9 files; module cuối cũng được Node VM nạp trong test.
  - `mvnw.cmd -B clean verify -Dnet.bytebuddy.experimental=true`: `BUILD SUCCESS` trên Java 26;
    169 tests được phát hiện, 166 pass và 3 PostgreSQL tests skip do Docker local chưa chạy.
  - Browser/API thật: `CHƯA CHẠY`; chưa thể xác nhận INT-01/02/03 DONE.
- Vướng mắc hoặc phần còn thiếu:
  - Thiếu MVC routing/model và layout dialect; các API profile update/password, address,
    customer order GET/cancel chưa có trên main; BE đã xác nhận các blocker này.
  - sessionStorage chưa được BE chấp thuận; đã ghi đề xuất DRAFT trong
    `docs/frontend/auth-session-decision.md`, cần review/chốt trước merge.
  - Forgot-password và CRUD admin từ ZIP là phần cần làm rõ scope/contract, không mặc định
    là Backend thiếu API. Đối chiếu chi tiết đã bổ sung trong integration-review.md.
  - Hai layout dùng chung chưa sửa; BE đã nhận và review tài liệu phạm vi theo phản hồi người dùng.
  - Module admin/realtime mới chưa nối đủ UI; template/model cũ và checkout form còn cần chuyển.
- Bước tiếp theo:
  - Theo dõi các phụ thuộc sau merge PR #51; không tiếp tục code mới trên branch đã merge.
  - Chốt quyết định quản lý phiên với BE và hoàn thiện phần FE còn lại.
  - Backend xử lý phần thiếu trên task/branch/PR riêng; sau đó kiểm thử API thật trước QA-04/QA-05.
- Ngày hoàn tất: `Chưa hoàn tất`

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

## INT-01: Nguồn hỗ trợ API và tìm kiếm sản phẩm

- Trạng thái: `BLOCKED`
- Branch: `feat/integrate-auth-catalog-store`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/40
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Chuẩn bị nguyên ba file có sẵn: core/api.js, core/utils.js và components/autocomplete.js.
  - Tạo branch từ main `ae95070`; giữ nguyên code nguồn, không thay layout, Backend, dependency hoặc contract.
  - Đối soát main: UI-02 đã merge qua https://github.com/NgocYuh/UTEGear/pull/38 (ae95070), UI-03 qua https://github.com/NgocYuh/UTEGear/pull/39 (8fc2437). Phần nguồn đã merge nhưng các giới hạn tương thích còn nguyên; chưa xác nhận hai task hoàn tất.
  - Gộp main ddea4a4 sau PR #41 vào nhánh PR #40, giải quyết xung đột nhật ký bằng cách giữ cả mục INT-01 và INT-02; nội dung code giữ nguyên.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 3/3 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - `git diff --cached --check`: `PASS`.
  - JavaScript: `PASS` cú pháp 3 file và kiểm tra bằng fetch giả lập cho GET/POST/DELETE, lỗi 400, 401 và redirect, JSON không hợp lệ, lỗi mạng và định dạng giá.
  - Không kết nối API thật hoặc ghi dữ liệu trong kiểm thử; kiểm tra trên chưa chứng minh tích hợp hoạt động.
- Vướng mắc hoặc phần còn thiếu:
  - Layout hiện tại chưa nạp các script mới; chưa có lời gọi API auth/profile/store trong ba file này.
  - Autocomplete dùng keyword thay vì q; price/imageUrl khác basePrice/effectivePrice/primaryImage trong contract hiện tại.
  - API wrapper chưa tự gửi JWT; chưa kiểm thử đầy đủ loading/empty/error, bàn phím hoặc tích hợp với backend.
  - Đây là phần nguồn hỗ trợ API và tìm kiếm, chưa hoàn tất phạm vi INT-01; giữ nguyên nguồn theo yêu cầu.
- Bước tiếp theo:
  - Push branch sau khi build pass, mở PR để review phần nguồn.
  - Thống nhất xử lý tương thích và các luồng auth/catalog/store trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## INT-02: Nguồn JavaScript giỏ hàng và wishlist

- Trạng thái: `BLOCKED`
- Branch: `feat/integrate-customer-transactions`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Thêm nguyên main.js có sẵn: thao tác giỏ hàng, wishlist, thông báo, chọn biến thể/ảnh, quick view, nhận tại cửa hàng, lọc giá và phím tắt.
  - Tạo branch từ main ae95070; không thay các module/template đã push, app.js, layout, Backend hoặc contract.
  - INT-01 đã push trên branch feat/integrate-auth-catalog-store (b7db7bd), chưa nằm trong main tại thời điểm tạo branch này.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, main.js khớp nguyên bản.
  - JavaScript: `PASS` cú pháp và kiểm tra fetch/DOM giả lập cho thêm/cập nhật/xóa giỏ, xóa toàn bộ, wishlist, redirect 401, lỗi mạng và giới hạn quantity 1-99.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - `git diff --cached --check`: cảnh báo khoảng trắng cuối dòng tại main.js:49, 82, 142; giữ nguyên theo yêu cầu bảo toàn code nguồn.
  - Không gọi API thật hoặc ghi database trong kiểm thử; chưa xác nhận luồng tích hợp hoạt động.
- Vướng mắc hoặc phần còn thiếu:
  - Endpoint /api/cart/add, /api/cart/{itemId}, DELETE /api/cart và /api/wishlist/toggle khác contract hiện tại /api/cart/items và /api/wishlist/items.
  - Layout hiện tại chưa nạp main.js; chưa xử lý JWT và request/response đúng contract.
  - File không có tích hợp API tạo đơn/lịch sử đơn; phần checkout chỉ bật/tắt lựa chọn nhận tại cửa hàng.
  - Quick view dùng model cũ; thông báo và một số dữ liệu được đưa trực tiếp vào innerHTML, chưa kiểm thử hoặc xác nhận an toàn với dữ liệu không tin cậy.
  - Giữ nguyên nguồn theo yêu cầu; chưa hoàn tất INT-02 hoặc sẵn sàng merge.
- Bước tiếp theo:
  - Push branch sau khi build pass, mở PR để review phần nguồn.
  - Thống nhất xử lý tương thích, an toàn hiển thị và luồng checkout/order trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-RESTORE-01: Khôi phục bộ khung giao diện và bổ sung fragment

- Trạng thái: `BLOCKED`
- Branch: `fix/restore-frontend-shell`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Khôi phục nguyên tám file nguồn: css/app.css, js/app.js, js/components/toast.js, layouts/main.html và fragments/header.html, footer.html, pagination.html, toastArea.html.
  - Bổ sung nguyên bảy file còn thiếu: css/style.css, fragments/alerts.html, fragments/global/header.html, footer.html, toastArea.html và fragments/product/product-card.html, product-specs.html.
  - Tạo branch từ main 1ee539b; giữ nguyên nội dung nguồn, không thay Backend, dependency hoặc contract.
  - Layout khôi phục tham chiếu các module JavaScript đã push trước đó; app.css khôi phục danh sách CSS component và CSS từng trang.
  - Đối soát: PR #40 đã merge tại 1ee539b và PR #41 tại ddea4a4; phạm vi tích hợp INT-01/INT-02 vẫn chưa hoàn tất.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 15/15 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - JavaScript: `PASS` cú pháp 2 file JS và 2 script inline; khởi tạo autocomplete/filter, delegation cart/wishlist/quick view, toast escaping/bridge/timer bằng DOM/module giả lập.
  - `node --test tests/frontend/mock-data.test.mjs`: `PASS`, 4 tests; chỉ xác minh fixture loader, không kiểm tra layout đã render.
  - `git diff --cached --check`: cảnh báo dòng trống cuối footer.html/product-card.html và khoảng trắng cuối main.html:8; giữ nguyên nguồn theo yêu cầu.
  - Kiểm tra mẫu credential trên 15 file: không phát hiện private key, GitHub/AWS token hoặc mật khẩu demo đã biết.
  - Không kết nối API thật hoặc ghi dữ liệu trong kiểm thử; build và giả lập chưa chứng minh giao diện render hoặc tích hợp hoạt động.
- Vướng mắc hoặc phần còn thiếu:
  - Layout gốc cần Thymeleaf layout dialect; header/fragment dùng model, helper và security expression cũ chưa xác nhận tương thích với Backend hiện tại.
  - home.css, favicon và ảnh dự phòng/banner nằm trong lượt 2, chưa có đủ trong branch này.
  - Chưa kiểm tra render, responsive, bàn phím hoặc accessibility; bản khôi phục thay thế phần hỗ trợ menu/toast của UI-01 đã viết lại trước đó.
  - Giữ nguyên source theo phạm vi được duyệt; chưa xác nhận ứng dụng frontend hoàn chỉnh hoặc các task tích hợp DONE.
- Bước tiếp theo:
  - Push để review lượt 1; sau khi merge chuẩn bị lượt 2 trang chủ và tài nguyên giao diện.
  - Đối chiếu dependency/model/API và kiểm thử giao diện thực tế trước khi xác nhận hoàn tất tích hợp.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-HOME-01: Trang chủ và tài nguyên giao diện

- Trạng thái: `BLOCKED`
- Branch: `feat/home-page-assets`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Bổ sung nguyên home.html, home.css, favicon.ico/favicon.svg, placeholder.png/placeholder.svg và bốn banner home-main, home-side-top, home-side-bottom, home-flash-sale với hai định dạng jpg/svg.
  - Giữ nguyên 14 file nguồn/tài nguyên; tạo branch từ main 49089c5, không sửa Backend, layout, dependency hoặc API contract.
  - Đối soát lượt 1: https://github.com/NgocYuh/UTEGear/pull/42 đã merge tại 49089c5. Các giới hạn tương thích của FE-RESTORE-01 chưa được giải quyết; chưa ghi hoàn tất tích hợp.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 14/14 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - `node --test tests/frontend/mock-data.test.mjs`: `PASS`, 4 tests; chỉ kiểm tra fixture loader.
  - SVG: `PASS`, 6/6 file .svg parse XML và có root svg.
  - JPEG: `PASS`, bốn banner mở được, kích thước lần lượt 920x420, 320x200, 320x200 và 1240x130.
  - Kiểm tra định dạng placeholder.png/favicon.ico: `FAIL`, dữ liệu gốc là SVG văn bản; không phải PNG/ICO, bộ giải mã PNG/ICO không mở được.
  - `git diff --cached --check`: cảnh báo khoảng trắng cuối home.html:16, 181; giữ nguyên nguồn theo yêu cầu.
  - Chưa kiểm tra render, responsive, accessibility hoặc kết nối API thật; build pass không chứng minh trang chủ hoạt động.
- Vướng mắc hoặc phần còn thiếu:
  - home.html dùng layout dialect, categoryHelper, model product/store và các field cũ; dependency/controller/model hiện tại chưa được xác nhận tương thích.
  - placeholder.png và favicon.ico có nội dung không khớp đuôi file trong nguồn; giữ nguyên theo yêu cầu, chưa xác nhận trình duyệt hiển thị đúng.
  - Chưa hoàn tất chức năng trang chủ hoặc xác nhận task tích hợp DONE.
- Bước tiếp theo:
  - Push để review lượt 2; sau merge chuẩn bị lượt 3 các trang thông tin.
  - Thống nhất xử lý định dạng tài nguyên và tương thích trước khi xác nhận giao diện chạy đầy đủ.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-INFO-01: Các trang thông tin và lỗi

- Trạng thái: `BLOCKED`
- Branch: `feat/information-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Bổ sung nguyên ba template có sẵn: about.html, contact.html và error.html; không chỉnh sửa code nguồn.
  - Tạo branch từ main abe524f, không thay Backend, dependency, layout hoặc API contract.
  - Đối soát lượt 2: https://github.com/NgocYuh/UTEGear/pull/43 đã merge tại abe524f. Các giới hạn tương thích và lỗi định dạng tài nguyên vẫn chưa được xử lý.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 3/3 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - `node --test tests/frontend/mock-data.test.mjs`: `PASS`, 4 tests; chỉ kiểm tra fixture loader.
  - JavaScript: `PASS` cú pháp một handler onsubmit; giả lập xác minh form contact ngăn điều hướng, hiện alert và reset form. Ba template không có script inline riêng.
  - `git diff --cached --check`: cảnh báo khoảng trắng cuối about.html:9,25; contact.html:9,25,37,42; error.html:11. Giữ nguyên nguồn theo yêu cầu.
  - Kiểm tra mẫu credential trên ba file: không phát hiện private key, GitHub/AWS token hoặc mật khẩu demo đã biết.
  - Không gọi API thật hoặc ghi dữ liệu trong kiểm thử; chưa kiểm tra HTML đã render, responsive hoặc accessibility.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout dialect của nguồn cũ; dependency/routing/render thực tế chưa được xác nhận tương thích.
  - Form contact chỉ hiện thông báo và reset, không gửi dữ liệu tới server; chưa phải chức năng gửi yêu cầu hỗ trợ.
  - error.html dùng model status/error/message; chưa kiểm tra trang lỗi thực tế của ứng dụng.
  - Bản nguồn được bảo toàn theo yêu cầu; chưa xác nhận giao diện hoạt động hoàn chỉnh.
- Bước tiếp theo:
  - Push để review lượt 3; sau merge chuẩn bị phần auth còn thiếu của lượt 4.
  - login.html cần xử lý mật khẩu demo theo quy tắc repository trước khi được phép commit; chưa được đưa vào lượt này.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-AUTH-RESTORE-01: Các trang auth còn thiếu

- Trạng thái: `BLOCKED`
- Branch: `feat/remaining-auth-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Bổ sung nguyên login.html và forgot-password.html có sẵn, không sửa nội dung hai template.
  - Giữ nguyên khối hiển thị tài khoản/mật khẩu demo trong login.html theo xác nhận trực tiếp của người dùng ngày 2026-10-13; đây là ngoại lệ do người dùng yêu cầu đối với quy tắc không commit mật khẩu. Chưa thử đăng nhập các tài khoản này.
  - Tạo branch từ main 0762605; không thay Backend, dependency, layout hoặc contract.
  - Đối soát lượt 3: https://github.com/NgocYuh/UTEGear/pull/45 đã merge tại 0762605. Các giới hạn render/tích hợp vẫn chưa được xử lý.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 2/2 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7, đủ hai template: `PASS`, 142 tests, 0 failure, 0 error, 0 skipped.
  - `git diff --cached --check`: `PASS`.
  - `node --test tests/frontend/mock-data.test.mjs`: `PASS`, 4 tests; chỉ kiểm tra fixture loader.
  - Template tĩnh: `PASS`, POST target, liên kết, label/type/name/required/autocomplete của email/password, trạng thái login error/logout và khối demo được giữ nguyên.
  - Không gọi API, gửi yêu cầu khôi phục hoặc thử tài khoản demo; chưa kiểm tra HTML đã render và hành vi trình duyệt.
- Vướng mắc hoặc phần còn thiếu:
  - Login dùng POST /login theo nguồn cũ, khác API JWT POST /api/auth/login của Backend hiện tại.
  - Forgot-password dùng POST /forgot-password; chưa thấy endpoint xử lý khôi phục mật khẩu trên main.
  - Cả hai trang dùng layout dialect cũ; chưa xác nhận routing/render, responsive hoặc accessibility.
  - Hoàn tất việc đưa nguồn của lượt 4 lên để review không đồng nghĩa auth hoặc INT-01 đã hoạt động hoàn chỉnh.
- Bước tiếp theo:
  - Push để review lượt 4; đối chiếu lại nguồn frontend sau merge.
  - Thống nhất xử lý routing, dependency và API trước khi xác nhận tích hợp DONE.
- Ngày hoàn tất: `Chưa hoàn tất`
