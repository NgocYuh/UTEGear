# QA-05 — Kết quả kiểm thử end-to-end

- Phụ trách: Frontend `@trongsonho`; Backend review và chạy media thật `@NgocYuh`.
- Nhánh báo cáo: `docs/manual-test-report`.
- Commit được kiểm tra: `71a50d2` (main sau khi merge PR #60).
- Ngày chạy: 2026-10-15.
- Kết luận tổng hợp: `PASS`.

## Kết quả

- Checklist giao dịch/realtime: `44 PASS`, `0 FAIL`; bước media thật được kiểm tra bằng companion
  suite thay vì bị chuyển thành PASS giả trong checklist.
- Media UI và Cloudinary thật: `28 PASS`, `0 FAIL`, `0 BLOCKED`; `realMedia=true`.
- Upload ảnh trả đủ `secureUrl` và `publicId`, ảnh tải lên render thành công.
- Thay file tạo `secureUrl` và `publicId` mới, ảnh thay thế render thành công.
- Xóa thành công đúng ảnh do lượt QA tạo. Runner dùng folder riêng `utegear-qa/<UUID>` và không xóa
  fixture hoặc tài nguyên có sẵn trong tài khoản.
- Quyền `CUSTOMER` bị từ chối đúng và phiên đăng nhập không bị mất.
- Responsive đạt ở viewport 1440, 768 và 390; không có browser exception chưa xử lý.

Kết quả tổng hợp trong `qa05-summary.json` là `PASS`: bước 8 của checklist được companion media suite
xác nhận bằng `step8VerifiedByRealMediaSuite=true`.

## Xác nhận nghiệp vụ Backend

- Hủy đơn `PENDING`: tồn kho `storeId=100`, `variantId=200` có số lượng ban đầu `4` và sau hủy là
  `4`. Hành vi hoàn tồn kho đã đúng sau PR #60.
- COD chuyển `COMPLETED`: `paymentStatus=PAID`, `amount=630000` bằng `total=630000`.
  Hành vi ghi nhận thanh toán đã đúng sau PR #60.

## Phạm vi đã kiểm tra

Đăng ký/đăng nhập; tìm/lọc/sắp xếp/phân trang; variants và tồn kho theo chi nhánh; wishlist/cart;
address/quote/coupon/place order; trạng thái đơn customer/admin và ownership; native STOMP
events/reconnect; media upload/replace/delete, metadata/primary, validation, duplicate submit, focus,
responsive, offline/retry, `CUSTOMER` 403 và logout. API, STOMP và `MediaStorage` không bị mock.

## Môi trường và bằng chứng

- Spring Boot chạy với H2 tạm dành riêng cho QA; trình duyệt Microsoft Edge qua CDP.
- Node.js 20 với WebSocket runtime flag; JDK 26 biên dịch project ở release 21.
- Credential Cloudinary chỉ được nạp vào process từ cấu hình local, không ghi vào command line, report,
  tài liệu hoặc Git.
- Artifact chỉ lưu local, không commit:
  - `.local-reference/storefront-test-artifacts/qa05-live-20261014/qa05-summary.json`
  - `.local-reference/storefront-test-artifacts/qa05-live-20261014/checklist/report.json`
  - `.local-reference/storefront-test-artifacts/qa05-live-20261014/checklist/admin-completed.png`
  - `.local-reference/storefront-test-artifacts/qa05-live-20261014/browser/report.json`
  - `.local-reference/storefront-test-artifacts/qa05-live-20261014/browser/media-manager.png`

Không còn blocker chức năng trong phạm vi QA-05. Task giữ trạng thái `IN REVIEW` cho đến khi PR báo cáo
này được review và merge; sau merge mới đối soát thành `DONE` theo quy tắc repository.
