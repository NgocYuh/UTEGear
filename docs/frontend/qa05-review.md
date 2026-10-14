# QA-05 — Bàn giao BE kiểm thử và xác nhận

- Frontend: @trongsonho; Backend review và kiểm thử ảnh thật: @NgocYuh.
- Nhánh FE: feat/admin-media-manager; phụ thuộc bản bổ sung test/qa04-completion.
- FE đã kiểm tra checklist giao dịch/realtime: 44 PASS, 0 FAIL; giao diện media: 23 PASS,
  0 FAIL. Upload/thay/xóa Cloudinary thật còn BLOCKED vì chưa có môi trường QA được xác nhận.
- Đây là số assertion. Không coi metadata PUT hoặc ảnh fixture là bằng chứng media thật.
- Maven clean verify đạt: 185 tests phát hiện, 182 pass, 3 PostgreSQL skip local do Docker
  không chạy. PostgreSQL/Flyway thuộc QA-02 BE; không ghi đã chạy PostgreSQL local.

## Yêu cầu BE hoàn tất

1. Review code FE và contract media, quyền ADMIN, secureUrl/publicId; mở giao diện tại
   /admin/orders#product-media. Không cần thêm route /admin/products để dùng panel này.
2. Cấu hình CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET trong môi trường
   riêng đã xác nhận là QA. Không ghi giá trị vào repository, PR hoặc bằng chứng.
3. Chạy live suite theo docs/frontend/admin-media-review.md, bật QA_RUN_CHECKLIST=1,
   QA_MEDIA_REAL=1 và QA_CLOUDINARY_CONFIRMED=1. Launcher dùng H2 tạm, tạo folder QA riêng,
   chạy POST upload → PUT thay → DELETE ảnh vừa tạo; kiểm receipt và ảnh render.
4. Lưu report.json, qa05-summary.json và ảnh chụp đã che dữ liệu nhạy cảm ở nơi review truy cập
   được; ghi môi trường, ngày chạy, commit thực tế, kết quả và đường dẫn bằng chứng trong mục
   QA-05 của reports/frontend/progress-log.md. Không commit log, secret hoặc ảnh tải về.
5. Xác nhận hai quan sát dưới đây, cập nhật contract nếu cần. Lỗi backend xử lý bằng task/branch
   BE riêng; lỗi frontend chuyển FE sửa, sau đó chạy lại các ca bị ảnh hưởng.
6. Review/duyệt PR QA-04 và QA-05. Chỉ ghi DONE khi phạm vi hoàn tất, kiểm thử pass và PR đã
   được duyệt; bản cập nhật DONE phải nằm trong chính PR của task trước khi merge.

## Hai quan sát cần BE quyết định

- Hủy đơn PENDING: tồn kho store100 + variant200 từ 4 giảm còn 3 khi đặt đơn; sau hủy vẫn 3,
  order/payment chuyển CANCELLED. Contract chưa chốt việc hoàn tồn kho; chưa coi là lỗi đã xác nhận.
- COD chuyển COMPLETED: paymentStatus vẫn PENDING, amount 630000 bằng total 630000.
  BE cần xác nhận bước ghi nhận thu tiền và luồng giao diện tương ứng.

## Phạm vi đã kiểm tra

Đăng ký/đăng nhập; tìm/lọc/sắp xếp/phân trang; variants và stock theo chi nhánh; wishlist/cart;
address/quote/coupon/place order; trạng thái đơn customer/admin và ownership; native STOMP
events/reconnect; media metadata/primary, validation, duplicate submit, focus, responsive,
offline/retry, CUSTOMER 403 và logout. Không mock API hoặc MediaStorage.

QA-04 còn chờ duyệt bản bổ sung; QA-05 còn chờ kiểm thử Cloudinary thật và review nghiệp vụ.
Việc push hoặc merge code riêng lẻ không thay thế các điều kiện này.
