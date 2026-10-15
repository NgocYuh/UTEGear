# Cài đặt và demo UTEGear

Hướng dẫn này dành cho Windows PowerShell và môi trường phát triển dùng Supabase PostgreSQL cùng
Cloudinary. Không dùng credential production, không gửi secret qua chat và không commit `.env` hoặc
`application-local.properties`.

## 1. Yêu cầu

- JDK 21 trở lên; project biên dịch với `release 21`.
- Git và kết nối Internet để Maven tải dependency lần đầu.
- Một Supabase project development/QA.
- Một Cloudinary account hoặc environment dành cho development/QA.
- PowerShell chạy tại thư mục gốc repository.

Kiểm tra công cụ:

```powershell
java -version
.\mvnw.cmd -v
git status --short --branch
```

## 2. Chuẩn bị cấu hình local

Tạo file local từ mẫu đã được Git bỏ qua:

```powershell
Copy-Item `
  -LiteralPath 'src/main/resources/application-local.example.properties' `
  -Destination 'src/main/resources/application-local.properties'
```

Có hai cách cung cấp giá trị:

1. Đặt biến môi trường trong terminal/IDE và giữ nguyên `${...}` trong file local.
2. Thay placeholder trực tiếp trong `application-local.properties` bị Git ignore.

Không dùng `.env` với kỳ vọng Spring Boot tự đọc; project không có dotenv loader.

### Supabase

Trong Supabase Dashboard, mở **Connect** và dùng connection string dành cho JDBC. Mạng chỉ có IPv4 nên
ưu tiên **Session pooler**; direct connection chỉ phù hợp khi máy có IPv6 hoặc project hỗ trợ kết nối đó.
Giữ password ở property/biến riêng, không nhúng password vào URL:

```properties
spring.datasource.url=jdbc:postgresql://YOUR_POOLER_HOST:5432/postgres?sslmode=require
spring.datasource.username=postgres.YOUR_PROJECT_REF
spring.datasource.password=YOUR_DATABASE_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver
```

Nếu Supabase hiển thị port/database/username khác, dùng đúng các giá trị ở màn hình Connect. Không dán
riêng host vào `spring.datasource.url`; URL phải bắt đầu bằng `jdbc:postgresql://`.

### JWT secret

Tạo chuỗi ngẫu nhiên 64 byte. Không tự đặt một câu dễ đoán:

```powershell
$jwtBytes = New-Object byte[] 64
$jwtRng = [Security.Cryptography.RandomNumberGenerator]::Create()
$jwtRng.GetBytes($jwtBytes)
[Convert]::ToBase64String($jwtBytes)
$jwtRng.Dispose()
```

Chép dòng Base64 vừa in vào biến `JWT_SECRET` hoặc `jwt.secret` của file local. Không commit, chụp màn
hình hoặc đưa chuỗi đó vào issue/PR.

### Cloudinary

Lấy ba giá trị từ Cloudinary Console của environment development/QA:

```properties
cloudinary.cloud-name=YOUR_CLOUD_NAME
cloudinary.api-key=YOUR_API_KEY
cloudinary.api-secret=YOUR_API_SECRET
```

Giữ folder riêng cho dự án, ví dụ qua biến `CLOUDINARY_FOLDER=utegear`. Giới hạn mặc định của project
là 5 MiB mỗi ảnh. QA tự động dùng folder `utegear-qa/<UUID>` và chỉ dọn asset do chính lượt QA tạo.

### WebSocket và cấu hình còn lại

Với ứng dụng chạy tại cổng mặc định:

```properties
app.websocket.allowed-origins=http://localhost:8080
```

Không dùng `*`. Nếu đổi port hoặc domain demo, khai báo đúng origin, nhiều origin phân tách bằng dấu
phẩy. Danh sách biến đầy đủ nằm trong [`.env.example`](../../.env.example).

## 3. Khởi tạo schema bằng Flyway

Project có hai migration đã merge:

- `V1__create_core_schema.sql`: schema 21 bảng.
- `V2__seed_reference_data.sql`: role `ADMIN` và `CUSTOMER`.

Với database development/QA trống, chỉ cần khởi động ứng dụng; Flyway tự chạy trước JPA validation.
Không sửa V1/V2 và không chạy tay nội dung migration trong SQL Editor.

Không bật `baseline-on-migrate` cho một database lạ chỉ để bỏ qua lỗi. Nếu database đã có bảng do tạo
thủ công, nên dùng database QA mới hoặc đối chiếu schema và `flyway_schema_history` trước khi quyết định
baseline. Baseline sai có thể đánh dấu migration đã chạy dù schema không khớp.

Sau lần khởi động đầu, có thể xác nhận trong Supabase SQL Editor:

```sql
SELECT installed_rank, version, description, type, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Database mới hợp lệ có version `1` và `2`, cả hai `success = true`. Kiểm tra role:

```sql
SELECT name FROM roles ORDER BY name;
```

Kết quả phải có `ADMIN` và `CUSTOMER`.

## 4. Build, test và chạy ứng dụng

Chạy toàn bộ test trước:

```powershell
.\mvnw.cmd -B --no-transfer-progress clean verify
```

Test mặc định dùng profile `test`, H2 in-memory, tắt Flyway và không gọi Supabase/Cloudinary thật. Ba
PostgreSQL integration test có thể bị skip nếu Docker không chạy; điều này phải được ghi đúng trong log,
không đổi thành tuyên bố đã test PostgreSQL local.

Chạy ứng dụng bằng profile local:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Khi log có `Started UteGearApplication`, mở:

- Storefront: `http://localhost:8080/`
- Đăng ký: `http://localhost:8080/register`
- Đăng nhập: `http://localhost:8080/login`
- Admin shell: `http://localhost:8080/admin/orders`

Trang admin có thể render shell khi chưa đăng nhập, nhưng API `/api/admin/**` vẫn yêu cầu JWT có role
`ADMIN`.

## 5. Tạo tài khoản demo an toàn

Không seed password trong Flyway. Tạo hai customer bằng trang `/register`, dùng email `.test` và mật
khẩu tạm được lưu trong password manager:

- Customer A: dùng cho cart, wishlist, checkout và order.
- Customer B: dùng kiểm tra quyền sở hữu dữ liệu.

Đăng ký thêm một tài khoản `.test` làm admin. Chỉ trên database development/QA, cấp role bằng Supabase
SQL Editor sau khi thay email mẫu:

```sql
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'qa-admin@example.test'
ON CONFLICT DO NOTHING;
```

Đăng xuất rồi đăng nhập lại để lấy JWT mới chứa role vừa cấp. Không tạo admin bằng endpoint public,
không lưu JWT trong tài liệu và không chạy câu SQL này trên production. Chi tiết dữ liệu QA tối thiểu:
[`qa-account-data-setup.md`](../testing/qa-account-data-setup.md).

## 6. Nạp dữ liệu demo

Dữ liệu catalog dùng pipeline crawler/import, không dùng JSON mock trong `docs/api/mock/`:

1. Chạy crawler và validator theo [`crawler/README.md`](../../crawler/README.md).
2. Kiểm tra JSONL theo [`data-contract.md`](../crawler/data-contract.md).
3. Đăng nhập admin và import theo
   [`scripts/import-products/README.md`](../../scripts/import-products/README.md).
4. Kiểm tra mỗi product có ít nhất một variant mặc định và tồn kho chỉ nằm ở cặp Store + Variant.

Mock API chỉ dành cho phát triển giao diện, không phải dữ liệu để import.

## 7. Kịch bản demo Backend

1. Đăng ký và đăng nhập Customer A.
2. Xem catalog, variant và availability theo cửa hàng.
3. Thêm wishlist, cart, địa chỉ và tạo checkout quote.
4. Đặt đơn; xác nhận tồn kho giảm ở đúng store/variant.
5. Đăng nhập admin, cập nhật trạng thái đơn và quan sát customer nhận STOMP event.
6. Hủy đơn `PENDING` hợp lệ và xác nhận tồn kho được hoàn.
7. Chuyển đơn COD từ `SHIPPING` sang `COMPLETED` và xác nhận payment là `PAID`.
8. Tại `/admin/products#product-media`, upload, thay và xóa một ảnh QA.

Không demo các endpoint được ghi "chưa triển khai" trong
[`endpoint-contract.md`](../api/endpoint-contract.md).

## 8. Lỗi thường gặp

| Hiện tượng | Kiểm tra |
|---|---|
| `password authentication failed` | Username/password đúng theo Supabase Connect; không nhầm database password với API key |
| Không kết nối được Supabase | Dùng Session pooler cho IPv4, URL JDBC đầy đủ và `sslmode=require` |
| Flyway báo schema không rỗng | Không baseline mù; kiểm tra `flyway_schema_history` và nguồn tạo schema |
| JPA `Schema-validation` lỗi | Migration chưa đủ hoặc đang trỏ nhầm database/schema |
| API trả 401 | JWT thiếu, sai hoặc hết hạn; đăng nhập lại và không đưa token vào URL |
| API admin trả 403 | Tài khoản chưa có `ADMIN`, hoặc đang dùng JWT phát hành trước khi cấp role |
| STOMP bị từ chối | JWT phải ở header CONNECT; destination và allowed origin phải đúng contract |
| Upload ảnh lỗi | Kiểm tra ba Cloudinary credential, MIME/size và folder; không in secret vào log |

Khi cần gửi lỗi cho thành viên khác, chỉ gửi từ dòng `Caused by` liên quan và che URL, username,
password, JWT, Cloudinary key/secret.

## 9. Trước khi push hoặc demo

- `git status` không có `.env`, `application-local.properties`, log, ảnh tải về hoặc `target/`.
- `.\mvnw.cmd -B clean verify` thành công hoặc ghi rõ test bị skip/block.
- Flyway history đúng version trên database QA.
- Tài khoản demo dùng email `.test`, không chứa dữ liệu cá nhân thật.
- Cloudinary QA không dùng nhầm folder/tài nguyên production.
- Sau demo, đổi mật khẩu tạm và xóa dữ liệu QA nếu không còn sử dụng.
