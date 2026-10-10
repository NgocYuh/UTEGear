# UTEGear

> Đồ án cuối kỳ môn Lập trình Web — hiện đang khởi tạo.

## Đề tài

Xây dựng website thương mại điện tử UTEGear theo mô hình chuỗi cửa hàng, phục vụ sinh viên, game thủ và người dùng công nghệ.

## Công nghệ

- Java 21, Spring Boot 3.3.x và Maven
- Thymeleaf, Bootstrap và WebSocket
- Spring Data JPA, PostgreSQL (Supabase) và Flyway
- Spring Security và JWT cho API cần bảo vệ
- Cloudinary cho ảnh sản phẩm
- Python cho công cụ crawler nhập dữ liệu ban đầu

## Cấu trúc dự án

- `src/`: ứng dụng Spring Boot, tài nguyên Thymeleaf và kiểm thử.
- `crawler/`: công cụ hỗ trợ thu thập dữ liệu ban đầu; không phải dịch vụ chạy liên tục.
- `docs/`: tài liệu kiến trúc, hợp đồng dữ liệu/API, giao diện và quy trình nhóm.
- `scripts/`: hướng dẫn script cơ sở dữ liệu và nhập sản phẩm.
- `reports/`: báo cáo theo từng mảng và nhật ký tiến độ riêng của Backend/Frontend.
- `.github/`: CI, CODEOWNERS và mẫu Pull Request.

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

Các biến ứng dụng cần có: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`,
`JWT_SECRET`, `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` và `CLOUDINARY_API_SECRET`.

## Chạy ứng dụng và kiểm thử

```bash
./mvnw spring-boot:run
./mvnw -B clean verify
```

Trên Windows, thay `./mvnw` bằng `mvnw.cmd`.

Xem [kế hoạch phát triển](UTEGear_INCREMENTAL_DEVELOPMENT_PLAN.md), [quy trình nhóm](docs/team/workflow.md) và
[hướng dẫn đóng góp](CONTRIBUTING.md) trước khi bắt đầu một task.

Backend cập nhật [nhật ký Backend](reports/backend/progress-log.md); Frontend cập nhật
[nhật ký Frontend](reports/frontend/progress-log.md) mỗi khi task đổi trạng thái.
