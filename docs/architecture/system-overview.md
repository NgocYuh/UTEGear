# Tổng quan hệ thống

UTEGear là một ứng dụng Spring Boot dạng modular monolith, render giao diện bằng Thymeleaf
và cung cấp API cho các tương tác động. PostgreSQL trên Supabase là nguồn dữ liệu chính;
Flyway là nguồn sự thật cho schema. Cloudinary lưu file ảnh, còn database chỉ lưu
`secureUrl` và `publicId`. WebSocket phục vụ các cập nhật thời gian thực đã được xác định.

Các API xem sản phẩm, danh mục, thương hiệu và cửa hàng có thể public. API giỏ hàng,
wishlist, đơn hàng, đánh giá, người dùng và quản trị phải xác thực; API quản trị yêu cầu
role `ADMIN`. Thiết kế chi tiết sẽ được bổ sung sau khi ERD và mô hình tồn kho được duyệt.
