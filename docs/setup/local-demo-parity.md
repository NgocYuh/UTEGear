# Chạy bản tích hợp giống local

Nhánh `codex/local-demo-integration` ghép đầy đủ giao diện trang chủ/header/footer/banner/menu tài khoản,
trang sản phẩm/đơn hàng, importer mô tả/thông số và bản sửa API tồn kho. Clone nhánh này để có toàn bộ
code trong một checkout; không cần ghép templates từ workspace khác vào JAR.

```powershell
git clone --branch codex/local-demo-integration https://github.com/NgocYuh/UTEGear.git
cd UTEGear
```

## Cấu hình và dữ liệu hiện có

Làm theo [hướng dẫn cấu hình local](setup-and-demo.md). File `application-local.properties` và các secret
vẫn ở máy của người dùng, không nằm trong GitHub. Nếu dùng lại database development hiện đang chạy,
218 sản phẩm, mô tả/thông số, ảnh Cloudinary, giá Flash Sale và cửa hàng demo vẫn có sẵn. Không import
lại hoặc đặt lại tồn kho trong database đó: các đơn đã đặt có thể đã trừ số lượng.

```powershell
$env:CRAWLER_IMPORT_ALLOWED_IMAGE_HOSTS='cdn.hstatic.net,product.hstatic.net,bizweb.dktcdn.net,res.cloudinary.com'
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=local'
```

Mở `http://localhost:8080` và Ctrl+F5 nếu trình duyệt còn cache CSS/JavaScript. Không cần tham số trỏ đến
`.local-reference`, `spring.thymeleaf.prefix` hoặc static files ở checkout khác.

## Database demo mới

Chỉ dùng database development/QA và tài khoản ADMIN của chính môi trường đó. Chạy Flyway theo hướng dẫn
cài đặt, rồi dùng file JSONL và reference-data đã chuẩn bị cục bộ. Dữ liệu crawler, ảnh tải về, tài khoản,
địa chỉ và đơn hàng không được commit. Các file của lượt thu thập hiện tại ở thư mục bị Git ignore:

- `crawler/data/output/catalog-readable-2026-10-16/products-enriched.jsonl`: 218 sản phẩm có nội dung đã sửa.
- `crawler/data/output/catalog-2026-10-15/reference-data.json`: danh mục và thương hiệu tương ứng.

Giữ hoặc sao chép riêng hai file này khi chuyển sang máy khác. Clone GitHub không tự mang theo database.
Kiểm tra file trước khi import (không đăng nhập, không gọi API):

```powershell
.\scripts\demo\import-catalog.ps1 `
  -ProductsPath '.\crawler\data\output\catalog-readable-2026-10-16\products-enriched.jsonl' `
  -ReferencesPath '.\crawler\data\output\catalog-2026-10-15\reference-data.json' `
  -ValidateOnly
```

Bỏ `-ValidateOnly` để import toàn bộ, hoặc thêm `-Limit 3` để thử trước. Script chỉ gọi địa chỉ loopback,
yêu cầu thông tin đăng nhập ADMIN bằng prompt, không lưu token/mật khẩu và không theo HTTP redirect.
Ảnh nguồn được importer đưa vào Cloudinary, lưu cả secureUrl/publicId. Báo cáo nằm trong thư mục output
bị Git ignore; kiểm tra importedRows/failedRows và báo cáo lỗi sau khi chạy. Không import trùng file thử
vào database đã có sản phẩm; importer có thể báo trùng slug/SKU.

Trên database mới, dùng trang admin tạo cửa hàng `UTEGear Demo` và nhập 10 chiếc cho mỗi biến thể qua
chức năng tồn kho. Tồn kho phải gắn Store + ProductVariant. Gắn nhãn địa chỉ demo, không dùng cho giao hàng
thật. Flash Sale là salePrice của sản phẩm, không phải chiến dịch tự hết hạn phía backend. Các giá demo
đã dùng: AULA F75 1.010.000đ, Logitech G102 340.000đ, AKKO GH300 Black 756.000đ, HyperWork Fable 90x40
416.000đ. Đối chiếu tên/model và giá gốc trước khi đặt giá qua admin; ID có thể khác giữa database.

Để thử đặt hàng: đăng ký CUSTOMER, thêm địa chỉ thử, Mua ngay, chọn cửa hàng demo/COD, tính tổng tiền và
xác nhận. Hủy đơn thử qua trang đơn hàng để hoàn kho. Dữ liệu khách hàng và đơn thử chỉ ở database QA.

## Kiểm tra trước push

```powershell
.\mvnw.cmd -B clean verify
node --test tests/frontend/*.test.mjs
powershell -NoProfile -File tests/scripts/demo-import.test.ps1
```

Nếu máy dùng JDK 26 và gặp giới hạn Byte Buddy hiện có, chạy Maven với
`-Dnet.bytebuddy.experimental=true` như nhật ký kiểm thử; không đổi dependency hoặc schema để tránh lỗi này.
