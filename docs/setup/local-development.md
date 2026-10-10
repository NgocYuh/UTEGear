# Phát triển cục bộ

## Yêu cầu

- JDK 21
- Git
- Docker hoặc PostgreSQL cục bộ là tùy chọn; database dùng chung dự kiến ở Supabase

## Cấu hình

Sao chép `src/main/resources/application-local.example.properties` thành
`application-local.properties`. File đích đã được Git bỏ qua. Cung cấp các biến môi trường được
liệt kê trong `.env.example`, hoặc chỉ thay placeholder trong file local bị bỏ qua. Không dùng
credential production.

Spring Boot không tự nạp file `.env`. Nếu tạo file này cho IDE hoặc công cụ hỗ trợ, phải cấu
hình công cụ đó nạp biến trước khi chạy ứng dụng.

Ba profile có trách nhiệm riêng:

- `local`: PostgreSQL cục bộ hoặc Supabase development; credential nằm ngoài Git.
- `test`: H2 trong bộ nhớ, tắt Flyway và dùng giá trị giả dành riêng cho kiểm thử.
- `prod`: PostgreSQL/Supabase, JWT và Cloudinary chỉ lấy từ biến môi trường của môi trường chạy.

## Lệnh

```bash
./mvnw -B clean verify
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Trên Windows PowerShell:

```powershell
.\mvnw.cmd -B clean verify
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Khi triển khai, đặt `SPRING_PROFILES_ACTIVE=prod` cùng các biến bắt buộc trong
`.env.example`. Không sao chép `application-local.properties` lên production.

Kiểm thử dùng H2 độc lập theo `application-test.properties`, không kết nối Supabase hay
Cloudinary thật. Migration `V1` sẽ chỉ được tạo sau khi ERD được chốt.
