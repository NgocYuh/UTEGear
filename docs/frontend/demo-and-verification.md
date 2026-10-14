# Hướng dẫn sử dụng và kiểm tra giao diện UTEGear

DOC-03 — Frontend @trongsonho. Đối chiếu source main `2836a1a` ngày 2026-10-15.
Thiết lập server, database, tài khoản và dữ liệu QA theo
[hướng dẫn cài đặt/demo](../setup/setup-and-demo.md) và
[dữ liệu kiểm thử](../testing/qa-account-data-setup.md). Tài liệu này hướng dẫn thao tác FE.

## Các trang có thể demo

| URL | Thao tác và trạng thái cần quan sát |
|---|---|
| `/` | Trang chủ, danh mục, sản phẩm nổi bật; dữ liệu trống không được coi là lỗi tải |
| `/products`, `/search?q=...` | Tìm kiếm; catalog lọc danh mục/brand/giá, sắp xếp và phân trang |
| `/products/{id}` | Chọn variant, giá và ảnh primary; tồn kho theo store/variant; variant hết hàng |
| `/stores`, `/stores/{id}` | Danh sách và thông tin cửa hàng đang hiển thị |
| `/about`, `/contact` | Nội dung thông tin; không demo gửi liên hệ như một API đã triển khai |
| `/register`, `/login` | Validation, lỗi trùng email/sai mật khẩu; header cập nhật sau đăng nhập |
| `/profile` | Xem/sửa thông tin, thêm/sửa/xóa địa chỉ và đổi mật khẩu |
| `/wishlist`, `/cart` | Thêm/xóa wishlist; thêm variant, đổi số lượng, xóa item và xóa giỏ |
| `/checkout` | Địa chỉ, cửa hàng, coupon, quote từ server, tổng tiền và đặt đơn |
| `/orders` | Lọc/phân trang, xem chi tiết, hủy đơn đủ điều kiện; trạng thái realtime |
| `/admin/orders` | ADMIN xem/cập nhật đơn; `/admin/orders/{id}` chuyển tới chi tiết trên danh sách |
| `/admin/orders#product-media` | Mở panel ảnh; tìm/chọn sản phẩm, upload/thay/xóa, altText và primary |

`/orders/{id}` chuyển tới `/orders?focus={id}`. Các trang riêng tư nạp dữ liệu qua API được bảo vệ;
việc render được shell admin không chứng minh người dùng có quyền ADMIN.

`/forgot-password` hiện chỉ có shell, chưa có POST recovery. `/admin/products` và trang edit cũ
không phải lối vào media hiện tại. Các endpoint chưa triển khai được ghi trong
[endpoint contract](../api/endpoint-contract.md); không coi chúng là chức năng release đã hoàn tất.

## Kịch bản demo customer và admin

1. Dùng tài khoản QA Customer A, Customer B và ADMIN do BE chuẩn bị. Dùng profile trình duyệt
   riêng cho demo; đăng nhập từng tab vì sessionStorage theo tab, không sao chép JWT.
2. Từ catalog, tìm sản phẩm có nhiều variant, lọc/sắp xếp và chuyển trang. Mở chi tiết, chọn
   variant còn hàng, xem đúng giá và availability ở cửa hàng active; thử variant hết hàng.
3. Thêm wishlist và giỏ; đổi số lượng, xóa một item. Tổng giỏ phải theo response server,
   không lấy giá hiển thị trên client làm giá giao dịch.
4. Tại profile, tạo địa chỉ QA. Mở checkout, chọn địa chỉ/store, thử coupon không hợp lệ rồi
   lấy quote hợp lệ. Khi sửa lựa chọn phải lấy quote mới; chỉ đặt một lần và kiểm giỏ đã xóa.
5. Mở orders của A và tab admin. Admin chuyển CONFIRMED → PROCESSING → SHIPPING → COMPLETED;
   customer thấy cập nhật mà không reload. Sau PR #60, COD hoàn tất có paymentStatus PAID.
6. Tạo một đơn PENDING khác và hủy bằng customer; xác nhận CANCELLED và tồn kho được hoàn.
   Không demo hủy đơn SHIPPING/đơn đã thanh toán như một thao tác được phép. B không đọc được đơn A.
7. ADMIN mở panel media, chỉ dùng sản phẩm/ảnh QA. Upload JPEG/PNG/WebP tối đa 5 MiB, đổi primary,
   sửa altText, thay file, kiểm ảnh mới trên detail/catalog rồi xác nhận xóa ảnh vừa tạo.
   CUSTOMER không dùng được controls và API admin trả 403.
8. Thử mất mạng rồi khôi phục: thông báo lỗi rõ, thao tác có thể retry; realtime reconnect
   và đọc lại REST. Đăng xuất phải xóa thông tin riêng trên trang và header.

## Responsive, bàn phím và accessibility

Kiểm ở chiều rộng 1440, 768 và 390: không tràn ngang; controls, tổng tiền, lỗi và nút hành động
vẫn đọc/dùng được. Kiểm cả trang đã đăng nhập, không chỉ shell hoặc trang trống.

- Dùng Tab/Shift+Tab qua menu, forms, nút xem nhanh và controls media; focus phải nhìn thấy.
- Quick-view có tên dialog/tên nút đóng; Tab giữ trong modal, Escape đóng và trả focus về trigger.
- Input có label; required/email/password mismatch hiện lỗi; thông báo tải/lỗi dùng vùng status.
- Xóa ảnh cần xác nhận; hủy xác nhận giữ ảnh và trả focus. Đổi sản phẩm xóa file chọn trước đó.
- Kiểm tên accessible trên cây accessibility Chromium và thử thao tác không dùng chuột.

Đây là kiểm tra accessibility cơ bản theo QA-04, không phải chứng nhận WCAG đầy đủ hay báo cáo
độ tương phản toàn bộ site. Các ca hết hạn dùng đồng hồ client không chứng minh JWT hết hạn trên BE.

## Chạy frontend checks trên Windows

Chạy từ root của worktree, Node 22+ (có global WebSocket), JDK/Maven theo project. Unit và syntax:

```powershell
node --test tests/frontend/integration.test.mjs tests/frontend/mock-data.test.mjs
Get-ChildItem src/main/resources/static/js -Recurse -Filter '*.js' |
  Where-Object { $_.FullName -notmatch '\\vendor\\' } |
  ForEach-Object {
    node --check $_.FullName
    if ($LASTEXITCODE -ne 0) { throw "JS syntax failed: $($_.Name)" }
  }
.\mvnw.cmd -B clean verify
```

JDK 26 cần thêm `'-Dnet.bytebuddy.experimental=true'` cho Mockito/ByteBuddy. Ba test PostgreSQL
có thể skip khi Docker không chạy; ghi đúng số skip và dùng CI có Docker để xác nhận PostgreSQL.

Browser suite cần Chromium CDP riêng ở localhost:9238. Ví dụ Edge, không dùng profile cá nhân:

```powershell
$qaBrowserProfile = Join-Path $env:TEMP ('utegear-qa-browser-' + [guid]::NewGuid())
$qaBrowser = Start-Process -FilePath 'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe' `
  -ArgumentList @('--headless=new','--remote-debugging-address=127.0.0.1',
    '--remote-debugging-port=9238',"--user-data-dir=$qaBrowserProfile",'about:blank') `
  -WindowStyle Hidden -PassThru
try {
  # Có thể đặt QA_MAVEN_CACHE nếu Maven cache không ở ~/.m2/repository.
  node tests/frontend/run-storefront-checks.mjs target/qa04-browser
  if ($LASTEXITCODE -ne 0) { throw 'QA-04 failed or blocked; inspect summary.json' }
} finally {
  # Chỉ dừng instance QA đã tạo, không dừng browser của người dùng.
  if (-not $qaBrowser.HasExited) { Stop-Process -Id $qaBrowser.Id }
}
```

Chờ endpoint `http://127.0.0.1:9238/json/version` sẵn sàng trước khi chạy runner. Port 18081 phải
trống; launcher tạo H2/fixtures và tài khoản tạm, dừng server của nó sau test. Dùng terminal QA sạch,
không kế thừa datasource/SPRING_APPLICATION_JSON production. Không chạy hai launcher cùng lúc.
Xem [QA-04](qa04-completion-review.md) để hiểu 5 placeholders của read-only runner: chỉ summary
tổng hợp với authenticated companion suites mới kết luận đầy đủ.

QA-05 và media chạy bằng launcher `tests/frontend/run-admin-media-checks.mjs`, đặt
`QA_RUN_CHECKLIST=1`. Mặc định không gọi Cloudinary thật và trả BLOCKED cho bước đó. Chỉ bật
`QA_MEDIA_REAL=1`, `QA_CLOUDINARY_CONFIRMED=1` sau khi BE xác nhận/cấu hình account QA; cách chạy,
cleanup và giới hạn ở [media review](admin-media-review.md). Exit 0=PASS, 1=FAIL, 2=BLOCKED.

## Bằng chứng và giới hạn

- QA-04 đã merge [PR #58](https://github.com/NgocYuh/UTEGear/pull/58): 93 public checks,
  39 giao dịch/realtime và 54 ca mở rộng PASS; 18 unit JS PASS.
- QA-05 do BE kiểm trên `71a50d2`, báo cáo merge
  [PR #61](https://github.com/NgocYuh/UTEGear/pull/61): 44 checklist PASS và 28 media thật PASS,
  không FAIL/BLOCKED; stock hủy về 4, COD COMPLETED thành PAID. Chi tiết ở
  [báo cáo QA-05](qa05-review.md). Đây là bằng chứng BE đã công bố, không phải lượt chạy lại DOC-03.
- Lượt kiểm tra mới của DOC-03/REL-01 ghi ở `reports/frontend/progress-log.md`, nêu chính xác
  source, môi trường, kết quả và ca chưa chạy; không đổi fixture/metadata thành storage thật.
- Workflow `Frontend Check` vẫn là workflow manual báo thiếu tooling, chưa phải CI FE pass.
  Bộ Node/CDP ở trên là kiểm tra thực tế; không dùng placeholder workflow làm chứng nhận release.

Giữ report/log/screenshot trong target hoặc nơi artifact riêng, che dữ liệu nhạy cảm khi review.
Không commit credential, JWT, dữ liệu crawler thô hoặc ảnh tải về. Khi báo lỗi, ghi URL, bước tái
hiện, vai trò, expected/actual và commit, kèm ảnh đã che dữ liệu; lỗi BE tách branch/task BE.
