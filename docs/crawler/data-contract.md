# Hợp đồng dữ liệu crawler

Đầu ra chuẩn dự kiến dùng UTF-8 và chứa các trường nguồn, tên sản phẩm, mô tả, thương hiệu,
danh mục, giá tham khảo, URL ảnh nguồn và dấu thời gian thu thập. Trường cụ thể, kiểu dữ liệu,
quy tắc làm sạch và mapping sang schema sẽ được chốt cùng ERD.

Mỗi record phải truy vết được nguồn, vượt qua validation bắt buộc và không chứa credential.
Crawler không tự ghi vào database production trong giai đoạn scaffold.
