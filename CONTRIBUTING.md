# Đóng góp cho UTEGear

## Tạo branch

Luôn cập nhật `main` trước khi bắt đầu. Tạo branch ngắn hạn, chỉ phục vụ một mục tiêu:

```bash
git switch main
git pull --ff-only
git switch -c feat/ten-ngan-gon
```

Dùng tiền tố `feat/`, `fix/`, `test/`, `docs/` hoặc `chore/`.

## Commit

Commit nhỏ, rõ nghĩa và ở dạng mệnh lệnh. Ví dụ: `feat: add public product endpoint`,
`fix: prevent negative inventory` hoặc `docs: clarify local setup`.

## Chạy test

```bash
./mvnw -B clean verify
```

Trên Windows dùng `mvnw.cmd -B clean verify`.

## Checklist trước Pull Request

- Branch chỉ xử lý một mục tiêu và đã đồng bộ `main`.
- `git diff --check` không báo lỗi.
- Build và toàn bộ test liên quan pass.
- Tài liệu và file `.example` đã cập nhật nếu cấu hình thay đổi.
- Không có secret, dữ liệu production hoặc file sinh tự động không cần thiết.
- PR mô tả phạm vi, cách kiểm thử và ảnh giao diện nếu có.
- Có review của thành viên còn lại và CI pass trước khi merge.

## Bảo mật

Không commit `.env`, `application-local.properties`, thông tin Supabase, JWT secret hay
Cloudinary secret. API public phải được liệt kê rõ; giỏ hàng, wishlist, checkout, đơn hàng,
đánh giá, người dùng và quản trị phải được bảo vệ. API quản trị yêu cầu role `ADMIN`.

## Migration

- Mọi thay đổi schema có migration Flyway dạng `V<n>__mo_ta.sql`.
- Không sửa migration đã merge.
- Nếu hai branch trùng version, branch merge sau đổi sang version kế tiếp.
- Không chỉnh schema Supabase thủ công mà thiếu migration tương ứng.
- Migration đầu tiên chỉ được tạo sau khi ERD và mô hình tồn kho được chốt.
