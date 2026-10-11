# Nhật ký tiến độ Backend

Người phụ trách: `@NgocYuh`

Cập nhật cùng một mục khi task đổi trạng thái. Task `BLOCKED` phải ghi phần còn thiếu; task
`DONE` phải có Pull Request, ngày hoàn tất và kết quả kiểm thử.

## FND-01: Khởi tạo project scaffold

- Trạng thái: `DONE`
- Branch: `chore/project-scaffold`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/1
- Merge commit: `707e7099e55e9dc7687ac844c3ec9f3d0e6d8271`
- Ngày cập nhật: 2026-09-21
- Ngày hoàn tất: 2026-09-21
- Đã làm:
  - Khởi tạo Spring Boot/Maven scaffold và cây thư mục dự án.
  - Thêm Maven Wrapper, CI và tài liệu nền.
- Kết quả kiểm thử:
  - `mvnw.cmd -B clean verify`: pass cho scaffold.
  - Commit scaffold đã được merge vào `main` qua PR #1.
- Vướng mắc hoặc phần còn thiếu:
  - Không có blocker trong phạm vi FND-01.
  - Nhánh remote `chore/project-scaffold` vẫn còn; cần xóa sau khi nhóm xác nhận không dùng lại.
- Bước tiếp theo:
  - Backend thực hiện `FND-02`; Frontend có thể thực hiện `FND-04` song song.

## FND-02: Cấu hình môi trường và profile

- Trạng thái: `DONE`
- Branch: `chore/application-profiles`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/2
- Merge commit: `0940e852745e8c3f63080dc2962f49a86780c874`
- Ngày cập nhật: 2026-10-10
- Ngày hoàn tất: 2026-10-10
- Đã làm:
  - Rà soát cấu hình chung, profile test, file ví dụ local và hướng dẫn phát triển cục bộ.
  - Tạo branch từ `origin/main` sau khi cập nhật remote và giữ nguyên các thay đổi tài liệu đã duyệt.
  - Tách cấu hình dùng chung khỏi credential của profile `local` và `prod`.
  - Cấu hình `prod` chỉ đọc Supabase, JWT và Cloudinary từ biến môi trường.
  - Thêm kiểm thử xác nhận profile `test` khởi động với H2 trong bộ nhớ.
  - Đã push branch lên `origin/chore/application-profiles`; commit triển khai: `ee658f2`.
- Kết quả kiểm thử:
  - `mvnw.cmd -B clean verify`: `PASS` với 1 test, 0 failure, 0 error, 0 skipped.
  - `ApplicationProfilesTest`: `PASS`; profile `test` dùng JDBC H2 trong bộ nhớ.
  - Môi trường kiểm thử dùng JDK 26 và Maven biên dịch với `release 21`.
  - [Backend CI của PR #2](https://github.com/NgocYuh/UTEGear/actions/runs/38065901846): `PASS` với Java 21.
- Vướng mắc hoặc phần còn thiếu:
  - Không có blocker trong phạm vi FND-02.
- Bước tiếp theo:
  - Backend thực hiện `FND-03`; Frontend có thể tiếp tục `FND-04` độc lập.

## FND-03: CI và Pull Request workflow

- Trạng thái: `DONE`
- Branch: `ci/backend-quality-gate`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/3
- Merge commit: `0ae22f7aa94361b4f76c9943c6e48be84c201643`
- Ngày cập nhật: 2026-10-11
- Ngày hoàn tất: 2026-10-11
- Đã làm:
  - Xác nhận workflow hiện có đã chạy `clean verify` bằng Java 21 và Maven cache trên PR #2.
  - Bổ sung giới hạn thời gian, concurrency và quyền checkout chỉ đọc cho Backend CI.
  - Merge branch vào `main` qua PR #3.
- Kết quả kiểm thử:
  - `mvnw.cmd -B --no-transfer-progress clean verify`: `PASS` với 1 test, 0 failure, 0 error, 0 skipped.
  - Parse YAML cục bộ cho `backend-ci.yml`: `PASS`.
  - [Maven verify (Java 21)](https://github.com/NgocYuh/UTEGear/actions/runs/38068623890/job/114261280983):
    `PASS` trên commit `ecb811f` của PR #3.
- Vướng mắc hoặc phần còn thiếu:
  - Không có blocker trong phạm vi FND-03.
- Bước tiếp theo:
  - Backend thực hiện DB-01; Frontend có thể tiếp tục FND-04 độc lập.

## DB-01: Chốt ERD và model contract

- Trạng thái: `IN PROGRESS`
- Branch: `docs/database-model`
- Pull Request: [Chưa mở - tạo PR từ branch đã push](https://github.com/NgocYuh/UTEGear/compare/main...docs/database-model?expand=1)
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Đối chiếu toàn bộ entity của dự án cũ, tập trung vào user, catalog, variant, store, inventory,
    cart, order, payment và media.
  - Chốt ERD PostgreSQL, danh mục bảng, khóa chính, khóa ngoại, unique constraint, check constraint,
    quy tắc xóa và danh sách index dự kiến cho DB-02.
  - Chốt tồn kho chỉ theo `store_id + variant_id`; loại `Product.stock`, `ProductVariant.stock` và
    `StoreInventory.product_id` khỏi mô hình mục tiêu.
  - Chốt mỗi product có đúng một biến thể mặc định; product không có lựa chọn dùng một biến thể với
    `optionValues = {}`.
  - Chốt cart item và order item bắt buộc dùng `variantId`; order item giữ snapshot lịch sử.
  - Cập nhật model contract để Frontend review field và hành vi trước API-01.
- Kết quả kiểm thử:
  - Kiểm tra 2 liên kết Markdown tương đối: `PASS`.
  - Kiểm tra ERD: `PASS` với 21 bảng dự kiến và đủ invariant cho variant, inventory, cart, order,
    payment và media.
  - `mvnw.cmd -B --no-transfer-progress clean verify`: `PASS` với 1 test, 0 failure, 0 error,
    0 skipped.
  - `git diff --check`: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Chờ `@trongsonho` review `optionValues`, biến thể mặc định, tồn kho theo cửa hàng và snapshot đơn
    hàng trước khi DB-01 được ghi `DONE`.
- Bước tiếp theo:
  - Chạy kiểm tra, push branch, mở Pull Request và yêu cầu Frontend review tài liệu.
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
