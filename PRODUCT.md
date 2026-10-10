# UTEGear Product Brief

UTEGear là website bán gaming gear dành cho sinh viên, game thủ và người dùng công nghệ.
Sản phẩm hỗ trợ mô hình chuỗi cửa hàng: khách có thể xem tồn kho theo từng cửa hàng,
mua trực tuyến để giao hàng hoặc chọn nhận tại cửa hàng.

Khu vực quản trị hỗ trợ quản lý sản phẩm, cửa hàng, tồn kho và đơn hàng. Phạm vi ban đầu
tập trung vào trải nghiệm mua sắm rõ ràng, dữ liệu tồn kho đáng tin cậy và quy trình vận hành
phù hợp với một chuỗi bán lẻ nhỏ.

## Quyết định sản phẩm và tồn kho

- `Product` lưu thông tin chung của sản phẩm.
- `ProductVariant` là đơn vị có SKU, được đưa vào giỏ hàng, đặt hàng và quản lý tồn kho.
- Mỗi sản phẩm phải có ít nhất một biến thể.
- Sản phẩm không có lựa chọn màu, switch hoặc layout vẫn có đúng một biến thể mặc định. Giao
  diện không hiển thị bộ chọn biến thể giả cho trường hợp này.
- Tồn kho được quản lý theo cặp `Store + ProductVariant`. Mỗi cặp chỉ có một bản ghi tồn kho.
- Số lượng hiển thị ở cấp sản phẩm chỉ là dữ liệu tổng hợp từ các biến thể, không phải nguồn tồn
  kho độc lập.
