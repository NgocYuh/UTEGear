# Hợp đồng dữ liệu crawler

Đầu ra chuẩn dự kiến dùng UTF-8 và chứa các trường nguồn, tên sản phẩm, mô tả, thương hiệu,
danh mục, giá tham khảo, URL ảnh nguồn và dấu thời gian thu thập. Trường cụ thể, kiểu dữ liệu,
quy tắc làm sạch và mapping sang schema sẽ được chốt cùng ERD.

Đầu ra phải nhóm dữ liệu theo `Product` và `ProductVariant`. Mỗi biến thể có SKU và tập thuộc
tính lựa chọn tương ứng. Nếu nguồn không có màu, switch, layout hoặc lựa chọn khác, bộ làm sạch
phải tạo đúng một biến thể mặc định với tập thuộc tính lựa chọn rỗng. Importer từ chối sản phẩm
không có biến thể và không tạo bản ghi tồn kho chỉ gắn với sản phẩm.

Mỗi record phải truy vết được nguồn, vượt qua validation bắt buộc và không chứa credential.
Crawler không tự ghi vào database production trong giai đoạn scaffold.
