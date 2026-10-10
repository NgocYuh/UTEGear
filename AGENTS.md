# Quy tắc làm việc trong repository

Các quy tắc này áp dụng cho toàn bộ repository UTEGear.

1. Không commit secret, mật khẩu, token, database URL thật hoặc API secret.
2. Không chỉnh sửa trực tiếp migration Flyway đã merge; tạo migration mới cho mọi thay đổi schema.
3. Không dùng `spring.jpa.hibernate.ddl-auto=update`; schema dùng chung do Flyway quản lý.
4. Không thêm MySQL Driver hoặc SQL Server Driver; database chính là PostgreSQL.
5. Không push code chưa build; tối thiểu phải chạy `./mvnw -B clean verify`.
6. Không sửa file ngoài phạm vi task. Với file dùng chung, thông báo cho thành viên còn lại trước khi sửa.
7. Không tạo dữ liệu mẫu trực tiếp trong production.
8. Phân biệt rõ API public và API được bảo vệ. API admin luôn yêu cầu role `ADMIN`.
9. Tích hợp Cloudinary phải lưu cả `secureUrl` và `publicId`.
10. Mỗi chức năng phải có kiểm thử phù hợp với rủi ro và hành vi của nó.
11. Không commit `target/`, log, dữ liệu crawler thô, ảnh tải về hoặc cấu hình IDE.
12. Không tự ý đổi kiến trúc đã chốt; cập nhật và review tài liệu trước khi triển khai thay đổi kiến trúc.
13. Khi task đổi trạng thái, người phụ trách phải cập nhật nhật ký tiến độ của vai trò mình.
    Backend dùng `reports/backend/progress-log.md`; Frontend dùng
    `reports/frontend/progress-log.md`. Task `BLOCKED` phải ghi rõ đang thiếu gì; task chỉ được
    ghi `DONE` khi phạm vi đã hoàn tất, kiểm thử pass và PR đã được duyệt. Bản cập nhật `DONE`
    phải nằm trong chính PR của task để `main` nhận trạng thái này khi merge.
14. Tồn kho luôn được quản lý theo `Store + ProductVariant`. Mọi `Product` phải có ít nhất một
    `ProductVariant`; sản phẩm không có lựa chọn màu, switch hoặc layout dùng đúng một biến thể
    mặc định. Không tạo tồn kho chỉ gắn với `Product`.

## Phân công bắt buộc

- Backend: `@NgocYuh`. Phụ trách PostgreSQL/Supabase, Flyway, entity, repository, service,
  controller, DTO/API, JWT, Spring Security, Cloudinary phía server, WebSocket phía server,
  crawler/import và kiểm thử backend.
- Frontend: `@trongsonho`. Phụ trách Thymeleaf, layouts/fragments, Bootstrap, CSS/JavaScript,
  giao diện các trang, gọi API, validation phía client, WebSocket client, responsive,
  accessibility và kiểm thử frontend.
- Nếu danh tính hoặc vai trò này không còn được ghi rõ trong repository, phải hỏi người dùng
  trước khi phân công; không tự suy đoán ai là Backend hoặc Frontend.
- Không giao một task vừa sửa nghiệp vụ backend vừa xây giao diện frontend. Nếu cần thay đổi
  hai phía, tách thành hai task/branch/PR và liên kết chúng bằng Task ID.
- Backend phải cập nhật model contract, endpoint contract và JSON mẫu trước khi Frontend bắt
  đầu task phụ thuộc. Frontend được phát triển bằng mock data theo contract, không phải chờ
  endpoint thật hoàn thành.
- Không duy trì nhánh `backend` hoặc `frontend` cố định. Vẫn áp dụng một task, một branch ngắn
  hạn và một Pull Request.
