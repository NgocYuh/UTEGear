# UTEGear

[![Backend CI](https://github.com/NgocYuh/UTEGear/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/NgocYuh/UTEGear/actions/workflows/backend-ci.yml)

> Đồ án cuối kỳ môn Lập trình Web — Hệ thống thương mại điện tử chuyên biệt Gaming Gear & Chuỗi cửa hàng công nghệ.

## Đề tài

Xây dựng website thương mại điện tử UTEGear theo mô hình chuỗi cửa hàng bán lẻ công nghệ chuyên biệt (B2C), phục vụ sinh viên, game thủ và người dùng đam mê công nghệ.

## Công nghệ sử dụng

- **Backend:** Java 21, Spring Boot 3.3.x, Spring Data JPA, Spring Security (JWT), WebSocket (STOMP), Flyway Migration.
- **Frontend:** Thymeleaf Template Engine, HTML5/CSS3, JavaScript (ES6+), Bootstrap 5.3.3, Bootstrap Icons 1.11.3.
- **Cơ sở dữ liệu & Đám mây:** PostgreSQL (Supabase), Cloudinary CDN (quản lý hình ảnh đa phương tiện).
- **Cổng thanh toán:** Tích hợp cổng VNPay Sandbox (hỗ trợ QR-Code, thẻ ATM nội địa NCB).
- **Hỗ trợ dữ liệu:** Python Crawler thu thập và làm sạch danh mục dữ liệu ban đầu.

## Các tính năng chính của hệ thống (Demo Checklist)

### 1. Dành cho Khách hàng & Người dùng (Storefront)
- **Trang chủ & Danh mục sản phẩm:** Phân loại theo Bàn phím cơ, Chuột, Tai nghe, Phụ kiện; hỗ trợ tìm kiếm từ khóa, lọc theo thương hiệu, sắp xếp giá và phân trang.
- **Chi tiết sản phẩm & Tồn kho biến thể:**
  - Lựa chọn màu sắc, biến thể, hiển thị trực quan số lượng tồn kho từng loại Switch (`Còn X sp` / `Hết hàng`).
  - Thử âm thanh switch bàn phím cơ bằng HTML5 Web Audio API, xoay mô hình 3D 360°, đổi chế độ đèn LED RGB.
- **Giỏ hàng thông minh (Smart Cart):**
  - Cho phép **tick chọn từng món hàng muốn mua** thay vì phải thanh toán toàn bộ giỏ.
  - Tự động tính tổng tiền theo các món được chọn; nút "Mua ngay" hỗ trợ mua nhanh trực tiếp.
  - Giữ lại các món chưa chọn trong giỏ hàng sau khi đặt hàng thành công.
- **Thanh toán đa phương thức (Checkout):**
  - Thanh toán khi nhận hàng (COD).
  - Thanh toán trực tuyến qua **Cổng thanh toán VNPay Sandbox** (hỗ trợ quét mã QR, thẻ ATM demo NCB).
  - Tích hợp chọn chi nhánh xuất hàng và thêm nhanh địa chỉ nhận hàng mới.
- **Đánh giá & Nhận xét sản phẩm (Reviews & Rating):**
  - Ràng buộc tài khoản đã mua đơn hoàn tất (`COMPLETED`) mới được đánh giá (chống đánh giá ảo).
  - Chấm điểm 1–5 sao, tối thiểu 50 ký tự, đính kèm link ảnh thật.
  - Tự động cập nhật điểm trung bình sao và số lượt đánh giá của sản phẩm.
- **Sản phẩm đã xem gần đây (Recently Viewed):**
  - Tự động ghi nhớ lịch sử các sản phẩm người dùng đã mở xem và hiển thị ở chân trang chi tiết.
- **Trợ lý tư vấn cấu hình Gear AI (Gear Consultant):**
  - Hộp thoại trò chuyện tư vấn chọn bàn phím, chuột theo tầm giá và nhu cầu sử dụng.

### 2. Dành cho Quản trị viên (Admin)
- **Dashboard quản trị:**
  - Thống kê **Tổng doanh thu thực tế (VNĐ)** từ các đơn hàng giao thành công.
  - Đếm số lượng đơn hoàn tất, đơn đang xử lý, đơn đã hủy.
  - Bảng xếp hạng **Top 5 sản phẩm bán chạy nhất** hệ thống.
- **Quản lý danh mục & Sản phẩm:** Thêm mới, chỉnh sửa thông tin, giá bán, upload ảnh Cloudinary.
- **Quản lý tồn kho theo Chi nhánh:** Quản lý số lượng tồn theo từng cặp `Store + ProductVariant`, hỗ trợ luân chuyển hàng giữa các showroom.
- **Quản lý & Duyệt đơn hàng:** Theo dõi trạng thái đơn (`PENDING` -> `CONFIRMED` -> `SHIPPING` -> `COMPLETED` / `CANCELLED`), kiểm soát trạng thái thanh toán online trước khi duyệt đơn.

## Cấu trúc dự án

- `src/`: ứng dụng Spring Boot, tài nguyên Thymeleaf và kiểm thử.
- `crawler/`: công cụ hỗ trợ thu thập dữ liệu ban đầu; không phải dịch vụ chạy liên tục.
- `docs/`: tài liệu kiến trúc, hợp đồng dữ liệu/API, giao diện và quy trình nhóm.
- `scripts/`: hướng dẫn script cơ sở dữ liệu và nhập sản phẩm.
- `reports/`: báo cáo theo từng mảng và nhật ký tiến độ riêng của Backend/Frontend.
- `.github/`: CI và CODEOWNERS.

## Phân công nhóm

- Backend (`@NgocYuh`): database/Flyway, Java backend, API, security, tích hợp dịch vụ phía
  server, crawler/import và backend test.
- Frontend (`@trongsonho`): Thymeleaf, Bootstrap, CSS/JavaScript, gọi API, WebSocket client,
  responsive, accessibility và frontend test.

Backend công bố model contract, endpoint contract và JSON mẫu trước. Frontend dùng các JSON
mẫu để làm giao diện song song; việc nối API thật được thực hiện ở task tích hợp riêng.

## Chuẩn bị môi trường

1. Sao chép `.env.example` thành `.env` cho công cụ hỗ trợ nếu cần.
2. Sao chép `src/main/resources/application-local.example.properties` thành
   `src/main/resources/application-local.properties` và điền cấu hình phát triển cục bộ.
3. Tuyệt đối không commit các tệp chứa mật khẩu, URL database thật, JWT secret hoặc API secret.

Các biến ứng dụng cần có: `SPRING_PROFILES_ACTIVE`, `DATABASE_URL`, `DATABASE_USERNAME`,
`DATABASE_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `CLOUDINARY_CLOUD_NAME`,
`CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` và `CLOUDINARY_FOLDER`.

Spring Boot không tự đọc file `.env`. Hãy nạp biến bằng IDE, terminal hoặc dùng file
`application-local.properties` đã được Git bỏ qua. Profile `prod` chỉ lấy credential từ biến
môi trường; profile `test` luôn dùng H2 trong bộ nhớ.

## Tài khoản demo / thử nghiệm hệ thống

Dưới đây là thông tin tài khoản mẫu để đăng nhập và trải nghiệm các tính năng của UTEGear:

| Vai trò | Email đăng nhập | Mật khẩu | Quyền hạn & Chức năng |
|---|---|---|---|
| **Quản trị viên (Admin)** | `admin@utegear.vn` | `admin123` | Quản trị hệ thống: Dashboard doanh thu & đơn hàng, top sản phẩm bán chạy, quản lý tồn kho chi nhánh, quản lý danh mục và duyệt đơn hàng |
| **Khách hàng (Customer)** | `user@utegear.vn` | `user123` | Trải nghiệm mua sắm: chọn từng sản phẩm trong giỏ hàng để thanh toán, thanh toán COD / VNPay Sandbox, lưu địa chỉ giao hàng, viết đánh giá & nhận xét sản phẩm (chọn số sao, tối thiểu 50 ký tự, đính kèm ảnh) |

## Chạy ứng dụng và kiểm thử

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -B clean verify
```

Trên Windows, thay `./mvnw` bằng `mvnw.cmd`.

Xem [hướng dẫn cài đặt và demo](docs/setup/setup-and-demo.md) để cấu hình Supabase, Cloudinary,
Flyway và tài khoản QA an toàn. Hướng dẫn không chứa credential thật.

Xem [kế hoạch phát triển](UTEGear_INCREMENTAL_DEVELOPMENT_PLAN.md), [quy trình nhóm](docs/team/workflow.md) và
[hướng dẫn đóng góp](CONTRIBUTING.md) trước khi bắt đầu một task.

Backend cập nhật [nhật ký Backend](reports/backend/progress-log.md); Frontend cập nhật
[nhật ký Frontend](reports/frontend/progress-log.md) mỗi khi task đổi trạng thái.
