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
