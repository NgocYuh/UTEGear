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

- Trạng thái: `IN PROGRESS`
- Branch: `chore/application-profiles`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-10
- Đã làm:
  - Rà soát cấu hình chung, profile test, file ví dụ local và hướng dẫn phát triển cục bộ.
  - Tạo branch từ `origin/main` sau khi cập nhật remote và giữ nguyên các thay đổi tài liệu đã duyệt.
- Kết quả kiểm thử:
  - `mvnw.cmd -B clean verify`: `CHƯA CHẠY`.
- Vướng mắc hoặc phần còn thiếu:
  - Không có blocker hiện tại.
- Bước tiếp theo:
  - Tách cấu hình `local`, `test`, `prod`, thêm kiểm thử profile test và cập nhật tài liệu.
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
