# Quy ước giao diện

- Dùng Thymeleaf cho template phía server và Bootstrap cho layout responsive.
- Tái sử dụng fragments/layouts; không sao chép header, footer hoặc navigation giữa các trang.
- CSS chia theo `core`, `components`, `pages`; JavaScript chia theo `core`, `components`, `domain`.
- Ưu tiên HTML semantic, điều hướng bàn phím, label rõ ràng và độ tương phản dễ đọc.
- Không đưa secret hoặc logic phân quyền chỉ dựa vào frontend.
- Khi bổ sung lint, cập nhật và kích hoạt workflow frontend tương ứng.
