# Workflow nhóm hai thành viên

## Nguyên tắc branch và Pull Request

- `main` luôn phải build và chạy được; không force push lên `main`.
- Không duy trì branch cố định `frontend` hoặc `backend`.
- Trước task mới, checkout `main`, chạy `git pull --ff-only`, rồi tạo branch ngắn hạn
  `feat/...`, `fix/...`, `test/...`, `docs/...` hoặc `chore/...`.
- Một branch chỉ xử lý một mục tiêu. Commit nhỏ, rõ nghĩa.
- Push branch, mở Pull Request và để thành viên còn lại review.
- Chỉ merge khi CI pass; ưu tiên **Squash and Merge**, sau đó xóa branch.

## Phân công chính thức

- **Backend — `@NgocYuh`:** PostgreSQL/Supabase, Flyway, entity, repository, service,
  controller, DTO/API, JWT, Security, Cloudinary backend, WebSocket backend, crawler/import
  và backend test.
- **Frontend — `@trongsonho`:** Thymeleaf, layouts/fragments, Bootstrap, CSS/JavaScript,
  giao diện trang khách/admin, gọi API, validation client, WebSocket client, responsive,
  accessibility và frontend test.

Nếu danh tính hoặc vai trò bị thiếu trong tương lai, phải hỏi người dùng trước khi phân công;
không tự gán theo tên branch, lịch sử commit hoặc vị trí file.

Không giao task theo miền nghiệp vụ kiểu một người làm cả Java và giao diện. Một chức năng có
hai task riêng: Backend cung cấp contract/endpoint; Frontend dùng contract để dựng UI và nối API.

Các file dùng chung như `pom.xml`, `SecurityConfig`, migration, layout, header và application
config phải được thông báo trước khi chỉnh sửa để tránh xung đột.

## Contract-first và làm song song

1. Backend tạo hoặc cập nhật model contract, endpoint contract và JSON mẫu trên branch docs.
2. Frontend review tên field, trạng thái, lỗi và dữ liệu cần render trước khi contract merge.
3. Sau khi contract merge, Backend triển khai endpoint và Frontend triển khai UI bằng mock data
   trên hai branch riêng, cùng xuất phát từ `main`.
4. Khi endpoint thật merge, Frontend tạo task tích hợp để thay mock bằng lời gọi API.
5. Nếu phát hiện contract cần đổi, cập nhật contract qua PR trước; không sửa ngầm ở một phía.

## Nhật ký tiến độ

- Backend chịu trách nhiệm cập nhật `reports/backend/progress-log.md`.
- Frontend chịu trách nhiệm cập nhật `reports/frontend/progress-log.md`.
- Mỗi Task ID chỉ có một mục trong file của vai trò phụ trách. Người làm task cập nhật mục đó
  mỗi khi trạng thái thay đổi.
- Dùng `IN PROGRESS` khi bắt đầu, `IN REVIEW` khi đã mở PR, `BLOCKED` khi chưa thể tiếp tục và
  `DONE` khi phạm vi đã hoàn tất, kiểm thử pass và PR đã được duyệt.
- Mục `BLOCKED` phải ghi rõ phần còn thiếu, task hoặc người đang phụ thuộc và hành động cần thực
  hiện để tiếp tục.
- Mục `DONE` phải có PR, ngày hoàn tất, việc đã hoàn thành và kết quả kiểm thử.
- Commit cập nhật `DONE` phải nằm trong chính PR của task ngay trước khi merge. Vì vậy, `main`
  chỉ nhận mục `DONE` khi PR đó được merge.
- Hai file này chỉ ghi tiến độ công việc. Không sao chép application log, log debug, secret hoặc
  dữ liệu production vào repository.

## Quy tắc Flyway

- Đặt migration theo dạng `V1__...sql`, `V2__...sql` và tăng tuần tự.
- Migration đã merge không được sửa; thay đổi tiếp theo phải dùng migration mới.
- Nếu hai branch tạo trùng version, branch merge sau phải đổi sang version kế tiếp rồi kiểm tra lại.
- Mọi thay đổi schema đều phải có migration.
- Không thay đổi database Supabase thủ công nếu không có migration tương ứng trong repository.

## Chu trình một task

1. Đồng bộ `main`, xác nhận issue/phạm vi và thông báo nếu đụng file dùng chung.
2. Tạo branch ngắn hạn, ghi task là `IN PROGRESS` trong nhật ký của vai trò và thực hiện commit nhỏ.
3. Chạy `git diff --check` và `./mvnw -B clean verify`.
4. Cập nhật việc đã làm, kết quả kiểm thử và vướng mắc; chuyển trạng thái sang `IN REVIEW`.
5. Push branch, mở PR theo template và yêu cầu thành viên còn lại review.
6. Nếu không thể tiếp tục, chuyển sang `BLOCKED` và ghi rõ phần còn thiếu.
7. Sửa phản hồi, chờ CI pass và thành viên còn lại duyệt PR.
8. Cập nhật `DONE`, PR và ngày hoàn tất trong commit cuối của cùng branch; reviewer kiểm tra lại,
   sau đó squash merge và xóa branch.
