# Kế hoạch phát triển UTEGear

## Nguyên tắc

- Mỗi giai đoạn phải có tài liệu, migration và kiểm thử phù hợp trước khi merge.
- Crawler chỉ chạy đến khi dữ liệu ban đầu đủ dùng rồi dừng; crawler không phải dịch vụ chạy liên tục.
- **PENDING:** Chưa quyết định tồn kho theo sản phẩm hay theo biến thể tại từng cửa hàng. Nhóm phải chốt trong ERD trước khi tạo migration đầu tiên.

## Các giai đoạn

1. **Khởi tạo repository:** tạo scaffold, cấu hình Maven, CI, tài liệu và quy trình cộng tác.
2. **Thiết kế ERD và quy tắc tồn kho:** xác định thực thể, quan hệ, ràng buộc và chốt quyết định tồn kho đang `PENDING`.
3. **Tạo Flyway migration:** tạo `V1__...sql` sau khi ERD được duyệt; không sửa migration đã merge.
4. **Cấu hình Supabase:** tạo môi trường phát triển dùng chung, quyền truy cập tối thiểu và tài liệu biến môi trường.
5. **Authentication và JWT:** xác thực người dùng; chỉ áp dụng JWT cho API cần bảo vệ; API admin yêu cầu `ADMIN`.
6. **Catalog sản phẩm:** xây dựng sản phẩm, danh mục, thương hiệu và API xem công khai.
7. **Chuỗi cửa hàng và tồn kho:** quản lý cửa hàng, số lượng tồn và truy vấn tồn kho theo mô hình đã chốt.
8. **Cloudinary:** upload/xóa ảnh và lưu cả `secureUrl` lẫn `publicId`.
9. **Giỏ hàng, wishlist và checkout:** hoàn thiện luồng mua hàng được bảo vệ và lựa chọn nhận tại cửa hàng.
10. **Đơn hàng và WebSocket:** quản lý trạng thái đơn, thanh toán phù hợp và cập nhật thời gian thực.
11. **Giao diện Thymeleaf:** xây dựng layout, component và trang responsive bằng Bootstrap.
12. **Testing, CI và báo cáo:** hoàn thiện test pyramid, kiểm tra frontend, CI và báo cáo đồ án.
