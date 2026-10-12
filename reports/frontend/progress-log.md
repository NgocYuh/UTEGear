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
