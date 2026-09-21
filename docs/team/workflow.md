# Workflow nhóm hai thành viên

## Nguyên tắc branch và Pull Request

- `main` luôn phải build và chạy được; không force push lên `main`.
- Không duy trì branch cố định `frontend` hoặc `backend`.
- Trước task mới, checkout `main`, chạy `git pull --ff-only`, rồi tạo branch ngắn hạn
  `feat/...`, `fix/...`, `test/...`, `docs/...` hoặc `chore/...`.
- Một branch chỉ xử lý một mục tiêu. Commit nhỏ, rõ nghĩa.
- Push branch, mở Pull Request và để thành viên còn lại review.
- Chỉ merge khi CI pass; ưu tiên **Squash and Merge**, sau đó xóa branch.

## Phân chia gợi ý

- **Thành viên 1:** catalog, category, brand, Cloudinary, store, inventory và màn hình admin tương ứng.
- **Thành viên 2:** auth, user, cart, wishlist, checkout, order, payment và WebSocket.

Các file dùng chung như `pom.xml`, `SecurityConfig`, migration, layout, header và application
config phải được thông báo trước khi chỉnh sửa để tránh xung đột.

## Quy tắc Flyway

- Đặt migration theo dạng `V1__...sql`, `V2__...sql` và tăng tuần tự.
- Migration đã merge không được sửa; thay đổi tiếp theo phải dùng migration mới.
- Nếu hai branch tạo trùng version, branch merge sau phải đổi sang version kế tiếp rồi kiểm tra lại.
- Mọi thay đổi schema đều phải có migration.
- Không thay đổi database Supabase thủ công nếu không có migration tương ứng trong repository.

## Chu trình một task

1. Đồng bộ `main`, xác nhận issue/phạm vi và thông báo nếu đụng file dùng chung.
2. Tạo branch ngắn hạn và thực hiện commit nhỏ.
3. Chạy `git diff --check` và `./mvnw -B clean verify`.
4. Push branch, mở PR theo template và yêu cầu thành viên còn lại review.
5. Sửa phản hồi, chờ CI pass, squash merge và xóa branch.
