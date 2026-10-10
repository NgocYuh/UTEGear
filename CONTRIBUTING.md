# Đóng góp cho UTEGear

## Tạo branch

Luôn cập nhật `main` trước khi bắt đầu. Tạo branch ngắn hạn, chỉ phục vụ một mục tiêu:

```bash
git switch main
git pull --ff-only
git switch -c feat/ten-ngan-gon
```

Dùng tiền tố `feat/`, `fix/`, `test/`, `docs/` hoặc `chore/`.

Không dùng nhánh `backend` hoặc `frontend` cố định. Mỗi task vẫn có branch và Pull Request
riêng. Task chạm cả hai phía phải được tách thành task Backend và task Frontend có liên kết.

## Phạm vi sở hữu

- Backend (`@NgocYuh`): database/Flyway, Java backend, API, security, Cloudinary/WebSocket
  phía server, crawler/import và backend test.
- Frontend (`@trongsonho`): Thymeleaf, fragments, Bootstrap, CSS/JavaScript, gọi API,
  validation phía client, WebSocket client, responsive và frontend test.
- Backend viết hoặc cập nhật model contract, endpoint contract và JSON mẫu trước khi triển
  khai endpoint. Frontend có thể bắt đầu bằng mock data ngay khi contract được review.

## Nhật ký tiến độ

- Backend cập nhật `reports/backend/progress-log.md`; Frontend cập nhật
  `reports/frontend/progress-log.md`.
- Khi bắt đầu task, thêm một mục với trạng thái `IN PROGRESS`. Cập nhật chính mục đó khi task
  chuyển sang `IN REVIEW`, `BLOCKED` hoặc `DONE`; không tạo nhiều mục trùng Task ID.
- Mỗi mục phải có Task ID, branch, Pull Request, việc đã làm, kết quả kiểm thử, vướng mắc hoặc
  phần còn thiếu và bước tiếp theo.
- Nếu task bị chặn, dùng `BLOCKED` và ghi rõ dependency, quyền truy cập, contract hoặc thông tin
  còn thiếu. Nếu đang chờ review, dùng `IN REVIEW`.
- Chỉ ghi `DONE` khi không còn phần việc thuộc phạm vi task, CI hoặc kiểm thử liên quan đã pass
  và PR đã được duyệt. Commit cập nhật `DONE` phải nằm trong chính PR của task ngay trước khi merge.
- Nhật ký tiến độ là tài liệu Markdown. Không đưa application log, log debug hoặc credential vào
  các file này.

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
- Nếu là task Backend có API/model mới, contract và JSON mẫu đã được cập nhật.
- Nếu là task Frontend, dữ liệu mock khớp contract và không tự suy đoán field chưa được duyệt.
- Nhật ký tiến độ của vai trò đã cập nhật đúng trạng thái, kết quả kiểm thử và vướng mắc.

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
