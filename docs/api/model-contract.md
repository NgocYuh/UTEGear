# Hợp đồng model

Tài liệu này sẽ mô tả DTO trao đổi qua API: tên trường, kiểu dữ liệu, bắt buộc/tùy chọn,
validation, enum, định dạng thời gian và quy tắc tương thích ngược. Backend (`@NgocYuh`) là
người viết; Frontend (`@trongsonho`) review các field cần hiển thị và tương tác.

Không dùng entity persistence làm hợp đồng API mặc định. Model cụ thể chỉ được thêm sau khi
ERD, quy tắc tồn kho và endpoint contract liên quan được review.

## Bất biến catalog và tồn kho

- `Product` luôn có danh sách `variants` với ít nhất một phần tử.
- `ProductVariant` là đơn vị có SKU và là đối tượng mà cart, checkout, order và inventory tham
  chiếu.
- Nếu sản phẩm không có lựa chọn màu, switch hoặc layout, response vẫn chứa một biến thể mặc
  định. Biến thể này không tạo ra lựa chọn giả trên giao diện.
- Model biến thể phải cho Frontend nhận biết biến thể mặc định và các thuộc tính có thể chọn.
  Tên field cụ thể sẽ được chốt trong DB-01 và API-01.
- Model tồn kho luôn xác định `storeId` và `variantId`. Không dùng `productId` làm khóa tồn kho.
- Số lượng tồn kho cấp sản phẩm, nếu xuất hiện trong response đọc, phải được ghi rõ là giá trị
  tổng hợp từ các biến thể.

Mỗi model đã chốt phải có JSON mẫu tối thiểu cho trạng thái đầy đủ, rỗng và lỗi liên quan.
Thay đổi field đã merge phải được cập nhật ở đây trước khi sửa code Backend hoặc Frontend.
Nếu một trang render dữ liệu trực tiếp bằng Thymeleaf, Backend cũng phải ghi tên model
attribute, kiểu dữ liệu và trường hợp không có dữ liệu để Frontend dựng template độc lập.
