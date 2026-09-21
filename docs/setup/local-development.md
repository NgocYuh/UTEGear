# Phát triển cục bộ

## Yêu cầu

- JDK 21
- Git
- Docker hoặc PostgreSQL cục bộ là tùy chọn; database dùng chung dự kiến ở Supabase

## Cấu hình

Sao chép `src/main/resources/application-local.example.properties` thành
`application-local.properties`, điền giá trị cục bộ và không commit file mới. Không dùng
credential production. Có thể dùng `.env.example` làm danh sách biến cần thiết.

## Lệnh

```bash
./mvnw -B clean verify
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Kiểm thử dùng H2 độc lập theo `application-test.properties`, không kết nối Supabase hay
Cloudinary thật. Migration `V1` sẽ chỉ được tạo sau khi ERD được chốt.
