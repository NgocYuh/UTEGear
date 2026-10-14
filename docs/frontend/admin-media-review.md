# FE-MEDIA-01 — Kết quả review quản lý ảnh

- Frontend: `@trongsonho`; Backend review và chạy live suite: `@NgocYuh`.
- UI: `/admin/orders#product-media` trên MVC route hiện có.
- Commit được kiểm tra: `71a50d2`.
- Ngày chạy live: 2026-10-15.
- Kết luận: `28 PASS`, `0 FAIL`, `0 BLOCKED`; `realMedia=true`.

## Phạm vi đã xác nhận

- Tìm/chọn sản phẩm đang hiển thị và đọc gallery từ API thật.
- Upload JPEG/PNG/WebP tối đa 5 MiB, sửa `altText`/ảnh chính, thay file qua multipart `PUT` và
  xóa qua `DELETE` có xác nhận/focus.
- Receipt upload và replace có `secureUrl`/`publicId`; cả ảnh mới và ảnh thay thế đều render được.
- Replace đổi cả URL lẫn public ID; delete chỉ xóa ảnh do lượt QA tạo.
- Quyền ADMIN được thực thi ở Backend; `CUSTOMER` nhận 403 mà không mất session.
- Chống submit trùng, loading/error/empty state, đổi sản phẩm, offline/retry và logout.
- Responsive 1440/768/390, label/focus hợp lệ và không có uncaught browser exception.
- Product detail và catalog ưu tiên ảnh primary từ API.

Không thêm route `/admin/products` hay `/admin/products/{id}/edit`; quản lý media tiếp tục dùng panel
tại `/admin/orders#product-media`.

## Cách chạy lại

Chỉ bật chế độ live sau khi người chạy đã xác nhận tài khoản Cloudinary dùng cho QA. Không ghi credential
vào repository, tài liệu, log hoặc command line.

```powershell
$env:QA_RUN_CHECKLIST='1'
$env:QA_MEDIA_REAL='1'
$env:QA_CLOUDINARY_CONFIRMED='1'
node tests/frontend/run-admin-media-checks.mjs .local-reference/storefront-test-artifacts/qa05-live
```

Launcher dùng H2 tạm và folder Cloudinary `utegear-qa/<UUID>`. Chuỗi kiểm tra là upload qua UI → kiểm
receipt/render → thay file → kiểm URL/public ID mới/render → xóa ảnh vừa tạo. Cleanup chỉ nhắm tới asset
do chính lượt QA tạo.

## Bằng chứng local

- `.local-reference/storefront-test-artifacts/qa05-live-20261014/qa05-summary.json`
- `.local-reference/storefront-test-artifacts/qa05-live-20261014/browser/report.json`
- `.local-reference/storefront-test-artifacts/qa05-live-20261014/browser/media-manager.png`

Các artifact trên không được commit. QA-05 đã đạt về chức năng và còn chờ review/merge PR báo cáo để
chuyển trạng thái từ `IN REVIEW` sang `DONE`.
