# Tổng quan hệ thống

UTEGear là một ứng dụng Spring Boot dạng modular monolith, render giao diện bằng Thymeleaf
và cung cấp API cho các tương tác động. PostgreSQL trên Supabase là nguồn dữ liệu chính;
Flyway là nguồn sự thật cho schema. Cloudinary lưu file ảnh, còn database chỉ lưu
`secureUrl` và `publicId`. WebSocket phục vụ các cập nhật thời gian thực đã được xác định.

Các API xem sản phẩm, danh mục, thương hiệu và cửa hàng có thể public. API giỏ hàng,
wishlist, đơn hàng, đánh giá, người dùng và quản trị phải xác thực; API quản trị yêu cầu
role `ADMIN`.

## Mô hình catalog và tồn kho đã chốt

- `Product` chứa thông tin chung; `ProductVariant` là đơn vị có SKU và được giao dịch.
- Mỗi `Product` có ít nhất một `ProductVariant`.
- Sản phẩm không có lựa chọn màu, switch hoặc layout có đúng một biến thể mặc định với tập
  thuộc tính lựa chọn rỗng.
- `Inventory` tham chiếu `Store` và `ProductVariant`; cặp `store_id + variant_id` là duy nhất.
- Cart item, order item và thao tác trừ kho luôn tham chiếu biến thể, kể cả biến thể mặc định.
- Tồn kho tổng ở cấp sản phẩm chỉ được tính từ tồn kho các biến thể khi cần hiển thị.

DB-01 sẽ cụ thể hóa quyết định này thành ERD, khóa ngoại và constraint trước khi Backend tạo
migration Flyway đầu tiên.
