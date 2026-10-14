# FE-MEDIA-01 — Frontend quản lý ảnh để BE review

- Frontend @trongsonho; branch feat/admin-media-manager, base main 8d2d71d.
- Được gửi lên GitHub để BE review; chưa được duyệt. Không sửa Java/BE/config/migration, không đổi policy phiên.
- UI mới: /admin/orders#product-media trên MVC route đã có. Chỉ dùng public catalog/detail
  và POST/PUT/DELETE media contract hiện tại. Không thêm route BE hoặc static admin architecture.

## Đã hoàn thiện FE

- Tìm/chọn sản phẩm đang hiển thị, gallery API thật; form upload JPEG/PNG/WebP≤ 5 MiB,
  sửa altText/primary, thay file qua multipart PUT, xóa qua DELETE có confirmation/focus.
- ADMIN gating ở UI; BE vẫn thực thi role ADMIN. Logout xóa gallery/disablecontrols;
  DTO render bằng textContent, URL ảnh qua helper safeImage, label/aria-live/loading/error/empty.
- Chống submit trùng, khóa controls khi loading, đổi sản phẩm xóa file/gallery cũ;
  lỗi mutation đọc lại server state nếu online, offline giữ gallery/file để retry.
- API receipt kiểm tra có secureUrl/publicId; không đưa secret vào UI/log/URL.
- Product detail ưu tiên ảnh primary; catalog/card đã dùng primaryImage từ API.
- Không sửa hai URLlegacy /admin/products hoặc /admin/products/{id}/edit; chúng vẫn404 trên main.
  Người dùng quản lý ảnh qua route adminOrders đang hoạt động và native details panel.

## Bằng chứng

- UI/media suite: 23 PASS, 0 FAIL, 1 BLOCKED (Cloudinary live chưa cấu hình).
- Toàn bộ checklist QA-05 trên FEcandidate: 44 PASS, 0 FAIL,step8liveBLOCKED.
- Metadata PUT không có file chạy BE thật, kiểm DTO publicId/secureUrl và primary flags/card/detail.
  Thao tác này không gọi Cloudinary; không coi metadata/fixture PASS là upload/replace/delete live PASS.
- Empty/MIME/>5MiB validation, rapid-submit, delete-confirm/cancel/focus, responsive1440/768/390,
  đổi sản phẩm, offlinenetwork, CUSTOMER403 và logout/noexceptions được kiểm trên Chromium.
- Maven clean verify Java26/ByteBuddy experimental: BUILD SUCCESS,185 tests phát hiện,
  182 pass / 3 PostgreSQL skip (Docker local không chạy). UnitJS18pass theo suite gốc.
- Fixture/launcher/testscript H2 riêng; datasource URL/driver/username/password/Flyway được
  override rõ trong CLI để không kế thừa database ngoài QA từ biến môi trường.

Artifact local, không commit:
D:/UTEGear/.local-reference/storefront-test-artifacts/admin-media-review/qa05-summary.json
D:/UTEGear/.local-reference/storefront-test-artifacts/admin-media-review/browser/report.json
D:/UTEGear/.local-reference/storefront-test-artifacts/admin-media-review/browser/media-manager.png
D:/UTEGear/.local-reference/storefront-test-artifacts/admin-media-final-verify.log

## Cloudinary thật — sẵn lệnh chạy nhưng chưa có credential QA

Không có CLOUDINARY_CLOUD_NAME/CLOUDINARY_API_KEY/CLOUDINARY_API_SECRET ở process/user/machine.
Không dùng config chưa xác nhận hoặc production; không ghi giá trị các credential vào tài liệu.
BE cần cấu hình ba biến trên bằng kênh riêng và xác nhận là accountQA.

Có Chromium CDP localhost9238 và Node/JDK/Maven phù hợp repository, chạy ở worktreefeature:

```powershell
$env:QA_MAVEN_CACHE='D:/maven-repository'
$env:QA_RUN_CHECKLIST='1'
$env:QA_MEDIA_REAL='1'
$env:QA_CLOUDINARY_CONFIRMED='1'
node tests/frontend/run-admin-media-checks.mjs D:/UTEGear/.local-reference/storefront-test-artifacts/media-live-qa
```

Chỉ đặt hai flagREAL/CONFIRMED sau khi BE đã xác nhận accountQA. Launcher chặn thiếu keys/confirmation;
credentials chỉ truyền trong environment/SPRING_APPLICATION_JSON của server, không commandline/file.
Dùng folder utegear-qa/<UUID>; DB vẫn H2 tạm. Live test chuẩn bị PNG hợp lệ, POST qua UI ->kiểm
receipt/ảnh render ->PUTfile thay asset ->kiểm URL/publicId mới/render ->DELETE chỉ ảnh do lượt
QA tạo. Cleanup chỉ ảnh vừa tạo, không xóa ảnh fixture hoặc tài nguyên có sẵn của account.
Nhánh live chưa thực thi khi thiếu credential, nên chưa xác nhận đủ media/storage end-to-end.

Chế độ mặc định không có flags live: chạy UI/metadata/validation/role/offline và trả BLOCKED cho
cloudlive, không gọi storage ngoài. QA_RUN_CHECKLIST=1 chạy thêm toàn bộ luồngQA-05;
summary chỉ xác nhận step8 khi realMedia=true và companion suite actualPASS. Không fake MediaStorage.

## BE cần xem/xác nhận

1. Review code/contract UI media qua route hiện có, receipt publicId/secureUrl và quyền ADMIN.
2. BE cấu hình Cloudinary QA và trực tiếp chạy upload → thay → xóa ảnh thật bằng lệnh trên;
   cập nhật kết quả và bằng chứng theo docs/frontend/qa05-review.md. Không gửi secret trong chat/PR.
3. Review quan sát QA-05: PENDING cancel stock4->3 vẫn3; COMPLETED COD paymentPENDING.
   Contract chưa chốt stockrestoration/thu tiềnCOD; không tự sửa BE hoặc xác nhận nghiệp vụ thay BE.
4. Review/duyệt bảnQA-04 bổ sung và các PR FE trước khi ghi QA-04/QA-05DONE theo quy tắc repo.

Không thể chỉ xác nhận báo cáo mà thay bằng chứng storage live khi accountQA chưa được cung cấp.
Tài liệu này là bản code/tests cụ thể để review; không khẳng định QA-05DONE.
