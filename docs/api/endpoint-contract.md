# Hợp đồng endpoint

Tài liệu này là nơi ghi hợp đồng HTTP trước khi triển khai endpoint. Backend (`@NgocYuh`)
chịu trách nhiệm viết và cập nhật; Frontend (`@trongsonho`) review khả năng sử dụng trước khi
contract được merge.

Mỗi endpoint phải nêu method, path, quyền truy cập, request, response, mã lỗi, phân trang và
ví dụ. Nhóm public dự kiến gồm sản phẩm, danh mục, thương hiệu và cửa hàng. Nhóm cần bảo vệ
gồm giỏ hàng, wishlist, checkout, đơn hàng, đánh giá, người dùng và quản trị. Endpoint admin
phải yêu cầu role `ADMIN`.

Mỗi endpoint phải có ít nhất một JSON response thành công, JSON lỗi chính và dữ liệu biên cần
cho loading/empty/error state. Frontend được dùng chính các JSON này làm mock data và không
phải chờ implementation Backend hoàn tất.

## Quy tắc contract theo biến thể

- Catalog response luôn trả ít nhất một biến thể cho mỗi sản phẩm.
- JSON mẫu phải có một sản phẩm chỉ có biến thể mặc định và một sản phẩm có nhiều lựa chọn.
- API tra cứu hoặc cập nhật tồn kho dùng `storeId + variantId`. API admin không được cập nhật
  tồn kho chỉ bằng `productId`.
- Cart và checkout nhận `variantId`, kể cả khi sản phẩm chỉ có biến thể mặc định.
- Response đơn hàng phải giữ thông tin biến thể đã mua cùng snapshot tên, giá và ảnh tại thời
  điểm đặt hàng.
- API có trả mức tồn kho tổng ở cấp sản phẩm phải mô tả rõ cách tổng hợp từ các biến thể.

Chưa có endpoint nghiệp vụ nào trong commit scaffold.
