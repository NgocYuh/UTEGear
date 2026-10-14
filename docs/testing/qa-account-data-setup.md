# Chuẩn bị tài khoản và dữ liệu cho QA-04/QA-05

Tài liệu này mô tả dữ liệu tối thiểu để FE chạy API thật. Không dùng Supabase production, không
commit password/JWT và không dùng dữ liệu cá nhân thật.

## 1. Môi trường

1. Chạy ứng dụng bằng profile local/test trên database QA riêng.
2. Chạy Flyway đến version mới nhất và xác nhận có role `ADMIN`, `CUSTOMER`.
3. Đặt mật khẩu dùng một lần trong password manager hoặc biến môi trường local; không ghi vào file
   tracked, ảnh chụp, log hay Pull Request.

## 2. Tài khoản

Đăng ký hai khách hàng qua `POST /api/auth/register`, dùng email thuộc miền `.test`:

- User A: sở hữu address, cart, wishlist và order dùng cho happy path.
- User B: dùng để xác nhận User A không đọc/sửa được tài nguyên của User B.

Đăng ký thêm một tài khoản QA rồi cấp role admin trong SQL Editor của database QA. Thay email mẫu
bằng email `.test` vừa đăng ký:

```sql
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'qa-admin@example.test'
ON CONFLICT DO NOTHING;
```

Không tạo admin bằng endpoint public và không chạy câu lệnh trên database production.

## 3. Dữ liệu nghiệp vụ tối thiểu

- Một store active và một store inactive.
- Một product có biến thể mặc định; một product có nhiều biến thể.
- Tồn kho theo `Store + ProductVariant`: một dòng còn hàng, một dòng hết hàng và một dòng low stock.
- Một coupon hợp lệ và một coupon hết hạn/không đủ minimum order.
- User A có ít nhất một địa chỉ mặc định.
- Một order của User A ở `PENDING` để thử hủy; một order `SHIPPING` để xác nhận không hủy được.
- Một order của User B để kiểm tra quyền sở hữu.

Catalog có thể nạp bằng pipeline DATA-01/DATA-02 theo
[`scripts/import-products/README.md`](../../scripts/import-products/README.md). Không import file mock
trong `docs/api/mock/` vào database.

## 4. Bàn giao an toàn cho FE

Chỉ gửi qua kênh riêng: base URL QA, ba email `.test` và mật khẩu dùng một lần. FE tự login để lấy
JWT; không gửi hoặc lưu JWT trong báo cáo. Trước QA-05, đổi mật khẩu dùng một lần và xóa dữ liệu QA
nếu database đó không còn sử dụng.
