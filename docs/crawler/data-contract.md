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

## Record JSONL của DATA-01

Mỗi dòng UTF-8 là một object với các field:

- `source_name`, `source_url`, `crawled_at`: thông tin truy vết nguồn.
- `name`, `description`, `brand`, `category`, `base_price`: dữ liệu sản phẩm đã làm sạch.
- `image_urls`: URL ảnh nguồn, không phải file ảnh tải về.
- `variants`: danh sách gồm `sku`, `option_values`, `price`, `is_default`.

`category` chỉ nhận `ban-phim`, `chuot`, `tai-nghe`, `keycap`, `switch`, `phu-kien`. Giá dùng số
nguyên VND dương. Mỗi sản phẩm phải có ít nhất một biến thể, SKU không rỗng/không trùng và đúng
một biến thể mặc định. Nếu nguồn không có SKU, cleaner sinh SKU ổn định từ nguồn và URL; đây là
khóa trung gian để DATA-02 kiểm tra trước khi import.
