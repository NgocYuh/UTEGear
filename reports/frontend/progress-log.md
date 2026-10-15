# Nhật ký tiến độ Frontend

## FE-HOME-COMPACT-01: Triển khai giao diện trang chủ đã duyệt

- Trạng thái: `IN REVIEW` (bàn giao trong FE-LOCAL-PUBLISH; chưa merge).
- Phụ trách: `@trongsonho` (Frontend).
- Branch: `codex/local-home-ui` (tiếp tục lượt tinh chỉnh giao diện đã được duyệt).
- Ngày cập nhật: 2026-10-15.
- Phạm vi: header/footer/card dùng chung, trang chủ, tab sản phẩm và ảnh banner.
- Đã làm: header một hàng tìm kiếm/giỏ/tài khoản và một hàng danh mục; hero ảnh không chứa chữ,
  hai ô chuột/tai nghe, thanh danh mục từ model thật. Gộp Flash Sale với tối đa bốn sản phẩm
  giảm giá; ba tab Nổi bật/Bán chạy/Mới về có hỗ trợ bàn phím, mỗi tab tối đa bốn thẻ.
  Thu gọn dải cam kết và footer ba cột; mobile dùng hai cột sản phẩm và hai cột cam kết.
  Thẻ giữ thêm giỏ theo variant, yêu thích/xem nhanh; giá gốc và nhãn giảm chỉ hiển thị khi có
  chênh lệch thật trong DTO. Giữ thời hạn Flash Sale `2026-12-25T23:59:00+07:00`.
- Dữ liệu: controller hiện trả `bestSellers` rỗng, tab báo chưa có sản phẩm;
  Mới về dùng `latestProducts` nếu có, nếu thiếu dùng danh sách hiện được controller lấy theo
  `createdAt,desc`. Không tạo số liệu bán chạy, rating hoặc dữ liệu production.
- Kiểm tra: 32/32 JS unit tests PASS; syntax JS và git diff --check PASS.
  Maven `mvnw.cmd -B clean verify -Dnet.bytebuddy.experimental=true`: BUILD SUCCESS,
  194 tests, 0 failures, 0 errors, 3 PostgreSQL tests skip local.
  Lượt không có cờ thất bại vì Byte Buddy hiện có hỗ trợ tối đa Java 23, máy dùng Java 26;
  cờ chỉ áp dụng khi chạy kiểm thử, không sửa dependency hoặc cấu hình repo.
- Browser: 21 checks PASS trên server H2 riêng `127.0.0.1:18082`, Edge headless:
  tab/keyboard/empty state, bốn thẻ, giá giảm thật, đăng nhập CUSTOMER/ẩn ADMIN,
  lớp menu phía trên navigation, thêm giỏ/cập nhật badge và yêu thích qua API thật,
  không tràn trang/menu ở 1440/768/390, không có browser exception.
- Bằng chứng local (không commit): `.local-reference/compact-home-verify-final.log`,
  `.local-reference/compact-home-browser-final/summary.json`, `home-1440.png`, `home-768.png`,
  `home-390.png` và các ảnh `home-account-*.png` trong cùng thư mục. Ảnh dùng fixture QA,
  không phải danh mục/ảnh sản phẩm production. Dữ liệu test chỉ tồn tại trong H2 in-memory.
- Ảnh banner mới: `src/main/resources/static/images/banners/home-compact-hero.png`,
  `src/main/resources/static/images/banners/home-compact-mouse.png`,
  `src/main/resources/static/images/banners/home-compact-headset.png`.
  Dùng công cụ image_gen tích hợp, kiểm tra trực quan cả ba ảnh, không ghi đè ảnh cũ.
- Prompt ảnh (bộ prompt cuối):
  - Hero: Wide cinematic realistic ecommerce photograph for UTEGear gaming peripherals website hero.
    1536x1024 landscape. Black mechanical keyboard with muted cream and navy blue keycaps, blue
    backlight, on dark navy desk in a tasteful gaming desk setting. Keyboard on right half and bottom,
    out-of-focus monitor and plant behind. Left half is dark clean navy negative space for white HTML
    headline added separately. Professional product photography, crisp keys, blue ambient lighting,
    restrained, no people, no lettering, no text, no logo, no watermarks. Must look like a real high
    quality product photo.
  - Mouse: Realistic studio product photograph for gaming shop website rectangular landscape promo
    tile. Black wireless gaming mouse on right half of dark charcoal navy desk, side profile three
    quarter view, restrained red accent light, subtle texture, left bottom dark empty negative space
    for an HTML category title added separately. Wide landscape 3:2 composition, sophisticated product
    photography, clean high detail, no text no logo no watermarks no people.
  - Headset: Realistic studio product photograph for gaming shop website rectangular landscape promo
    tile. Black over-ear gaming headset with tasteful red accent trim on right half of dark charcoal
    navy surface, three quarter view, excellent materials soft lighting, bottom left dark empty
    negative space for HTML category title added separately. Wide landscape 3:2 composition,
    professional ecommerce photography, no text no logo no watermarks no people.
- Pull Request: chưa mở; chưa push.

## FE-ACCOUNT-LAYER-01: Sửa thanh điều hướng đè menu tài khoản

- Trạng thái: `IN REVIEW` (bàn giao trong FE-LOCAL-PUBLISH; chưa merge).
- Phụ trách: `@trongsonho` (Frontend).
- Branch: `codex/local-home-ui`.
- Ngày cập nhật: 2026-10-15.
- Nguyên nhân: panel và thanh điều hướng cùng z-index dropdown; thanh điều hướng xuất hiện
  sau trong DOM nên che thông tin tài khoản ở đầu panel.
- Đã làm: đặt stacking context của cụm tài khoản cao hơn thanh điều hướng một mức,
  vẫn nằm trong stacking context sticky header. Không đổi vị trí hoặc tương tác menu.
- Kiểm tra: đối chiếu CSS stacking context và git diff --check PASS;
  4/4 tests tương tác menu PASS. Chưa xác minh trực tiếp bằng trình duyệt.
- Pull Request: chưa mở; chưa push.

## FE-ACCOUNT-MENU-01: Gom thao tác tài khoản vào menu rê chuột

- Trạng thái: `IN REVIEW` (bàn giao trong FE-LOCAL-PUBLISH; chưa merge).
- Phụ trách: `@trongsonho` (Frontend).
- Branch: `codex/local-home-ui` (cùng lượt tinh chỉnh header).
- Ngày cập nhật: 2026-10-15.
- Đã làm: gom thông tin tên/email, tài khoản, đơn hàng, yêu thích, quản trị và đăng xuất
  vào một menu tài khoản; giữ giỏ hàng riêng. Khách thấy đăng nhập/đăng ký,
  quản trị chỉ hiện cho ADMIN theo session hiện có. Bỏ thanh đăng xuất riêng dưới header.
- Tương tác: rê chuột mở menu; bấm/chạm hoặc Enter/Space dùng disclosure HTML native;
  Escape đóng và trả focus, bấm ngoài/rời focus đóng; không đóng khi đang thao tác trong menu.
- Kiểm tra: 29/29 tests Frontend PASS, gồm 4 tests mới cho hover, touch/native toggle,
  bàn phím/focus, bấm ngoài và logout; JavaScript syntax và git diff --check PASS.
  Route đơn hàng đã đối chiếu `/orders`. Chưa kiểm tra trình duyệt hoặc Maven build.
- Pull Request: chưa mở; chưa push.

## FE-FLASH-01: Bộ đếm Flash Sale chạy theo thời điểm kết thúc

- Trạng thái: `IN REVIEW` (bàn giao trong FE-LOCAL-PUBLISH; chưa merge).
- Phụ trách: `@trongsonho` (Frontend).
- Branch: `codex/local-home-ui` (cùng lượt tinh chỉnh trang chủ).
- Ngày cập nhật: 2026-10-15.
- Đã làm: thay banner ảnh chứa số tĩnh bằng HTML/CSS responsive; đếm ngược mỗi giây đến
  `2026-12-25T23:59:00+07:00` theo thời điểm người dùng xác nhận. Giờ có thể vượt 24;
  tải lại trang không đặt lại thời hạn. Hết giờ dừng ở 0, báo đã kết thúc và đổi CTA.
- Kiểm tra: 25/25 tests Frontend PASS, trong đó 5 tests bộ đếm kiểm tra múi giờ,
  chuyển phút, tab tạm dừng, tải lại, hết hạn và cấu hình không hợp lệ.
  JavaScript syntax và git diff --check PASS. Chưa kiểm tra trình duyệt hoặc Maven build.
- Bộ đếm dùng đồng hồ thiết bị; đây là hiển thị Frontend, không thay đổi giá hoặc thời hạn ưu đãi Backend.
- Pull Request: chưa mở; chưa push.

## FE-FOOTER-01: Xóa cột sản phẩm trong footer

- Trạng thái: `IN REVIEW` (bàn giao trong FE-LOCAL-PUBLISH; chưa merge).
- Phụ trách: `@trongsonho` (Frontend).
- Branch: `codex/local-home-ui` (cùng lượt tinh chỉnh giao diện theo yêu cầu).
- Ngày cập nhật: 2026-10-15.
- Đã làm: xóa cột Sản phẩm và các liên kết bên trong; chia footer thành ba cột đều
  trên desktop, thương hiệu toàn hàng và hai cột liên kết trên tablet, xếp dọc trên mobile.
- Kiểm tra: kiểm tra tĩnh nội dung và Bootstrap grid PASS; 20/20 tests Frontend PASS;
  git diff --check PASS. Chưa kiểm tra bằng trình duyệt.
- Pull Request: chưa mở; chưa push.

## FE-HEADER-01: Xóa thanh thông tin trên cùng

- Trạng thái: `IN REVIEW` (bàn giao trong FE-LOCAL-PUBLISH; chưa merge).
- Phụ trách: `@trongsonho` (Frontend).
- Branch: `codex/local-home-ui`.
- Ngày cập nhật: 2026-10-15.
- Đã làm: xóa top utility bar trong fragment header dùng chung theo ảnh người dùng cung cấp;
  giữ header chính với logo, tìm kiếm, tài khoản và giỏ hàng.
- Kiểm tra: 20/20 tests Frontend PASS; kiểm tra tĩnh xác nhận topbar đã xóa,
  các phần chính còn nguyên; git diff --check PASS. Chưa kiểm tra bằng trình duyệt.
- Pull Request: chưa mở; chưa push.

## REL-01: Chuẩn bị UTEGear v1.0.0 release candidate — phần Frontend

- Trạng thái: `IN REVIEW` — bản FE/tài liệu sẵn review; chờ DOC-03 merge và xác nhận hai phía.
- Phụ trách: `@trongsonho` (FE); phối hợp/review Backend: `@NgocYuh`.
- Branch: `release/v1.0.0`; chứa DOC-03 `0275bd3`, runtime trùng main `2836a1a`.
- Ngày cập nhật: 2026-10-15.
- Đã làm: `docs/release/v1.0.0-candidate.md` ghi scope release, phạm vi chưa triển khai,
  bằng chứng, điều kiện review/merge và lệnh tạo tag đúng SHA sau merge. Không sửa Backend/runtime.
- Kiểm tra mới: Maven clean verify BUILD SUCCESS; 187 phát hiện, 184 pass, 3 PostgreSQL skip local;
  unit JS 18 PASS, syntax PASS. QA-04 summary PASS: 93 public, 39 transaction/realtime, 54 risk checks.
- QA-05 chạy lại trên main mới: checklist 44 PASS/0 FAIL; media UI 23 PASS/0 FAIL. Local summary
  BLOCKED duy nhất phần Cloudinary live do chủ động tắt QA_MEDIA_REAL; không báo suite này PASS toàn bộ.
  Nghiệp vụ quan sát lại: COD COMPLETED có PAID/amount=total=630000; stock PENDING cancel 4 → 4.
- Bằng chứng live dùng báo cáo BE PR #61: 28 PASS, realMedia=true. Diff `71a50d2..2836a1a`
  chỉ tài liệu/nhật ký, không đổi source runtime hoặc test; tái sử dụng được cho candidate hiện tại,
  chờ BE xác nhận trong review. CI main `2836a1a` success:
  https://github.com/NgocYuh/UTEGear/actions/runs/38607996065 .
- Artifact local: `.local-reference/storefront-test-artifacts/doc03-rel01-verify.log`,
  `rel01-qa04/summary.json`, `rel01-qa05/qa05-summary.json`. Không commit report/log/ảnh/secret.
- Còn cần: DOC-03 review/merge; BE đối soát log Backend và xác nhận candidate/live evidence;
  review/duyệt PR REL-01, merge, rồi tạo tag. Không tự sửa log Backend hoặc gắn tag trước merge.
- Bản này gửi lên GitHub để review; chưa PR được duyệt, chưa DONE, chưa tag/release/deploy.

## DOC-03: Hướng dẫn sử dụng/demo và kiểm tra giao diện

- Trạng thái: `IN REVIEW` — tài liệu và kiểm tra DOC-03 hoàn tất, chờ duyệt.
- Phụ trách: `@trongsonho` (Frontend); reviewer: `@NgocYuh`.
- Branch: `docs/frontend-demo-guide`; base main `2836a1a` (PR #63).
- Ngày cập nhật: 2026-10-15.
- Đã làm: `docs/frontend/demo-and-verification.md` mô tả route hiện có, kịch bản customer/admin,
  media, trạng thái lỗi/empty/loading, responsive 1440/768/390 và keyboard/focus/accessibility.
- Hướng dẫn chạy unit/syntax/Maven/CDP, giới hạn H2/client clock, media live và placeholder FE CI;
  liên kết DOC-02 thay vì lặp cấu hình Backend. Không sửa Backend hoặc giao diện runtime.
- Đối soát QA-05: báo cáo PASS đã merge [PR #61](https://github.com/NgocYuh/UTEGear/pull/61),
  merge `56b9e0a`, ngày 2026-10-15 giờ Asia/Bangkok; 44 checklist/28 media live PASS theo báo cáo BE.
  Mục QA-05 bên dưới là trạng thái viết trước merge; không coi còn thiếu media QA thật.
- Kiểm tra mới: Maven clean verify BUILD SUCCESS, 187 phát hiện/184 pass/3 PostgreSQL skip local;
  18 unit JS PASS, syntax JS PASS. QA-04 browser: 93 public/39 giao dịch/54 ca mở rộng PASS, 0 FAIL, summary PASS.
- Bằng chứng local: `.local-reference/storefront-test-artifacts/doc03-rel01-verify.log`;
  không commit log, screenshot, credential. Bản này gửi lên GitHub để review; chưa duyệt/DONE.
- Tiếp theo: review/merge DOC-03 rồi chốt REL-01 trên branch riêng.

## QA-05: End-to-end và kiểm thử media thật

- Trạng thái: `IN REVIEW` — toàn bộ phạm vi chức năng đã PASS, chờ review/merge PR báo cáo.
- Phụ trách: `@trongsonho` Frontend; `@NgocYuh` review nghiệp vụ và chạy Cloudinary thật.
- Branch: `docs/manual-test-report`.
- Ngày cập nhật: 2026-10-15.
- Commit được kiểm tra: `71a50d2` (main sau khi merge PR #60).
- Kết quả tổng hợp: `PASS`; checklist `44 PASS`, `0 FAIL`; companion media thật `28 PASS`,
  `0 FAIL`, `0 BLOCKED`, `realMedia=true`.
- Cloudinary: upload → render → replace → render → delete đạt; chỉ xóa asset do lượt QA tạo trong
  folder riêng `utegear-qa/<UUID>`.
- Nghiệp vụ Backend: hủy đơn PENDING hoàn tồn kho từ số lượng ban đầu `4` về `4`; COD hoàn tất có
  `paymentStatus=PAID`, `amount=630000` bằng `total=630000`.
- Quyền/khả dụng: CUSTOMER nhận 403 mà không mất session; responsive 1440/768/390; không có browser
  exception chưa xử lý. API, STOMP và MediaStorage không bị mock.
- Bằng chứng local: `.local-reference/storefront-test-artifacts/qa05-live-20261014/`; không commit
  report, log, screenshot hay credential.
- Hướng dẫn và kết quả chi tiết: `docs/frontend/qa05-review.md` và
  `docs/frontend/admin-media-review.md`.
- Vướng mắc: không còn blocker chức năng; chỉ còn review và merge PR báo cáo.
- Ngày hoàn tất: chờ merge PR để chuyển sang `DONE` theo quy tắc repository.

## FE-MEDIA-01: UI quản lý ảnh cho QA-05

- Trạng thái: `DONE`.
- Phụ trách: `@trongsonho` Frontend; liên kếtQA05, không tự giao/sửaBackend.
- Branch: `feat/admin-media-manager`; [PR #59](https://github.com/NgocYuh/UTEGear/pull/59),
  merge commit `97dc680`.
- Ngày cập nhật: 2026-10-15.
- Đã làm: media native details panel tại /admin/orders#product-media, gallery/search activeproducts,
  upload/replace/delete API contract, metadata/primary/receipt, confirmation, role/session/validation,
  loading/duplicate-submit/errorresync/product-change; productdetail ưu tiên primaryimage.
- Kiểm thử cuối: media Cloudinary thật `28 PASS`, `0 FAIL`, `0 BLOCKED`; metadata/API thật,
  card/detail, labels/focus/responsive/negative files/offline/role 403/logout đều đạt.
- Live suite upload/render/replace/render/delete đạt và cleanup đúng asset QA vừa tạo. Launcher ép
  H2 tạm và không kế thừa datasource ngoài QA.
- Review/cách chạy:docs/frontend/admin-media-review.md; artifact
  `.local-reference/storefront-test-artifacts/qa05-live-20261014/`, không commit log/ảnh/token.
- Vướng mắc: không còn trong phạm vi FE-MEDIA-01.
- Bước tiếp theo: merge PR báo cáo QA-05 để ghi nhận kết quả tổng hợp.
- Ngày hoàn tất: 2026-10-15.

## FE-BIND-01: Binding dữ liệu và realtime FE theo BE

- Trạng thái: `DONE`.
- Phụ trách: `@trongsonho`; liên kết INT-01/02/03, QA-04.
- Source: fix/storefront-bindings, commit 462c9eb, [PR #56](https://github.com/NgocYuh/UTEGear/pull/56), merge 7457eb5.
- Realtime tests: [PR #57](https://github.com/NgocYuh/UTEGear/pull/57), merge 8d2d71d.
- Bản bổ sung: `test/qa04-completion`, [PR #58](https://github.com/NgocYuh/UTEGear/pull/58),
  merge commit `8614940`.
- Ngày cập nhật: 2026-10-14.
- Đã làm: API bindings profile/address/cart/wishlist/catalog/checkout/customer-admin orders;
  policy APPROVED, native STOMP; BE PR #55 đã sửa cart/subscription, kiểm thử thật pass.
  Quick-view bổ sung tên nút/dialog và trả focus khi đóng; không sửa Backend.
- Kiểm thử cuối: HTTP/DOM 93 PASS, giao dịch 39 PASS, ca mở rộng 54 PASS, 0 FAIL;
  unit JS 18 PASS. Maven BUILD SUCCESS, 182 pass/3 PostgreSQL skip local (Docker không chạy).
- Phạm vi/giới hạn: docs/frontend/qa04-completion-review.md; dữ liệu H2 tạm, clock expiry là phía client,
  ảnh SVG fixture không chứng minh Cloudinary upload; PostgreSQL thuộc QA-02 BE, CI đã có bằng chứng.
- Tiếp theo: dùng kết quả binding/realtime đã merge làm đầu vào cho QA-05.
- Ngày hoàn tất: 2026-10-14.

## QA-04: Frontend automated và manual tests

- Trạng thái: `DONE`.
- Phụ trách: `@trongsonho` (Frontend).
- Tooling gốc PR #53 đã merge; bổ sung realtime PR #57 đã merge tại 8d2d71d.
- Branch: `test/qa04-completion`; [PR #58](https://github.com/NgocYuh/UTEGear/pull/58),
  merge commit `8614940`.
- Ngày cập nhật: 2026-10-14.
- Đã làm:
  - Real Spring Boot/H2/Chromium, không mock API/STOMP; helper chung và fixture nhiều variant,
    hai store active/một inactive, stock còn/hết/low, ảnh local, ba loại coupon.
  - Validation client/server errors; stock/coupon/quote expiry client/pending quote stale;
    double-submit chỉ tạo một đơn; offline/retry; 401/403/ownership/session expiry client.
  - Responsive và labels trang authenticated; quick-view AX names, Tab/Shift+Tab/Escape/focus.
  - Customer/admin native STOMP events, mất socket/reconnect và REST resync.
- Kết quả: HTTP/DOM `93 PASS`; giao dịch `39 PASS`; ca mở rộng `54 PASS`; không FAIL.
  Unit JS `18 PASS`; Maven clean verify Java 26 với ByteBuddy flag `BUILD SUCCESS`,
  185 tests phát hiện/182 pass/3 PostgreSQL skip local. Backend CI main 8d2d71d success.
- Launcher tổng hợp đối chiếu đúng 5 nhóm ngoài read-only runner với companion suites;
  không đổi FAIL/BLOCKED thật thành PASS, không coi số check là số luồng nghiệp vụ.
- Giới hạn: client clock expiry không thay kiểm thử expiry phía server; H2 không chứng minh
  PostgreSQL local; fixture ảnh không thay upload Cloudinary; accessibility cơ bản, không full audit.
  PostgreSQL/Flyway theo QA-02 của BE; không gán việc Docker local chưa chạy thành blocker FE mới.
- Bằng chứng/cách chạy: docs/frontend/qa04-completion-review.md;
  artifact D:/UTEGear/.local-reference/storefront-test-artifacts/qa04-ready/, không commit log/ảnh/token.
- Tiếp theo: kết quả QA-04 được dùng làm đầu vào cho QA-05.
- Ngày hoàn tất: 2026-10-14.

## FE-INT-FIX: Khôi phục CSS ZIP và đối chiếu INT-01/02/03 với Backend

- Trạng thái: `BLOCKED`
- Người phụ trách: `@trongsonho` (Frontend).
- Task liên kết: `INT-01`, `INT-02`, `INT-03`.
- Branch: `fix/frontend-integration`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/51
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Khôi phục 7 CSS nguyên ZIP; SHA-256 khớp 7/7.
  - Chuẩn bị API/JWT, auth form, search/quick view, cart/wishlist theo Backend main b2225f8.
  - Thêm client checkout/admin/realtime và kiểm thử hành vi; chưa nối đầy đủ mọi trang.
  - Chi tiết phạm vi, phần chưa nối và thông báo layout: `docs/frontend/integration-review.md`.
  - Người dùng chuyển yêu cầu BE: push branch hiện tại để BE review/sửa tiếp; không coi đây là
    phê duyệt sessionStorage hoặc xác nhận INT-01/02/03 hoàn tất.
  - PR #51 đã merge vào main tại `2979dd4`; cập nhật khi chuẩn bị QA-04. Phạm vi source đã merge,
    nhưng trạng thái vẫn BLOCKED vì routing/API/UI/policy chưa hoàn tất.
- Kết quả kiểm thử:
  - JS integration + fixture: `PASS` 16 tests, 0 failure.
  - JS syntax: `PASS` 9 files; module cuối cũng được Node VM nạp trong test.
  - `mvnw.cmd -B clean verify -Dnet.bytebuddy.experimental=true`: `BUILD SUCCESS` trên Java 26;
    169 tests được phát hiện, 166 pass và 3 PostgreSQL tests skip do Docker local chưa chạy.
  - Browser/API thật: `CHƯA CHẠY`; chưa thể xác nhận INT-01/02/03 DONE.
- Vướng mắc hoặc phần còn thiếu:
  - Thiếu MVC routing/model và layout dialect; các API profile update/password, address,
    customer order GET/cancel chưa có trên main; BE đã xác nhận các blocker này.
  - sessionStorage chưa được BE chấp thuận; đã ghi đề xuất DRAFT trong
    `docs/frontend/auth-session-decision.md`, cần review/chốt trước merge.
  - Forgot-password và CRUD admin từ ZIP là phần cần làm rõ scope/contract, không mặc định
    là Backend thiếu API. Đối chiếu chi tiết đã bổ sung trong integration-review.md.
  - Hai layout dùng chung chưa sửa; BE đã nhận và review tài liệu phạm vi theo phản hồi người dùng.
  - Module admin/realtime mới chưa nối đủ UI; template/model cũ và checkout form còn cần chuyển.
- Bước tiếp theo:
  - Theo dõi các phụ thuộc sau merge PR #51; không tiếp tục code mới trên branch đã merge.
  - Chốt quyết định quản lý phiên với BE và hoàn thiện phần FE còn lại.
  - Backend xử lý phần thiếu trên task/branch/PR riêng; sau đó kiểm thử API thật trước QA-04/QA-05.
- Ngày hoàn tất: `Chưa hoàn tất`

Người phụ trách: `@trongsonho`

Cập nhật cùng một mục khi task đổi trạng thái. Task `BLOCKED` phải ghi phần còn thiếu; task
`DONE` phải có Pull Request, ngày hoàn tất và kết quả kiểm thử.

## FND-04: Frontend quality workflow

- Trạng thái: `IN REVIEW`
- Branch: `ci/frontend-quality-gate`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Xem mã nguồn ZIP làm tài liệu tham khảo, chưa chuyển giao diện nghiệp vụ vào branch.
  - Giữ workflow Frontend ở chế độ `workflow_dispatch` khi chưa có công cụ kiểm tra thật.
  - Thay bước TODO báo thành công bằng thông báo chưa sẵn sàng và exit code `1`.
  - Ghi phạm vi kiểm tra cần có và điều kiện bật trigger Pull Request trong `docs/frontend/quality-checks.md`.
- Kết quả kiểm thử:
  - `mvnw.cmd -B clean verify`: `PASS`, 1 test, 0 failure/error/skipped; chạy trên bản sạch từ `origin/main` commit `0ae22f7` kèm ba file FND-04 trong `.local-reference/fnd-04-verification`.
  - Parse YAML bằng SnakeYAML: `PASS`; chỉ có trigger thủ công và bước báo chưa có tooling.
  - Chạy bước workflow bằng Git Bash: `PASS`; kết thúc với exit code `1` đúng dự kiến khi chưa có tooling, không báo thành công giả.
  - `git diff --check` cho phạm vi FND-04: `PASS`.
  - GitHub Actions: `CHƯA CHẠY`; chưa push hoặc mở PR.
- Vướng mắc hoặc phần còn thiếu:
  - Người dùng đã xác nhận phạm vi ba file FND-04; chưa mở PR hoặc có review.
  - Công cụ kiểm tra giao diện sẽ triển khai khi có code Frontend; đây là giới hạn đã định trong FND-04.
- Bước tiếp theo:
  - Push branch đã được xác nhận, mở PR và chuyển sang `IN REVIEW` để Backend review.
- Ngày hoàn tất: `Chưa hoàn tất`

## UI-01: Layout và design foundation (A + B)

- Trạng thái: `IN REVIEW`
- Branch: `feat/shared-ui-layout`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Người dùng yêu cầu gom phần A (CSS) và B (layout/components/mock) vào một PR UI-01, hẹn push 23:37 ngày 11/10/2026 (UTC+7); ghi phạm vi trong `docs/frontend/ui-foundation-plan.md`.
  - Lấy CSS core và chín component chung từ ZIP tham khảo, dọn khoảng trắng và tạo `app.css` chỉ import phạm vi này.
  - Thêm preview CSS và template preview Thymeleaf cho bộ khung dùng chung.
  - Thêm layout/header/footer, skip link, menu mobile, toast, pagination và empty-state.
  - Thêm fixture loader và bản sao JSON API-01 đã merge; không gọi endpoint nghiệp vụ hoặc lưu JWT.
  - Tạo checkout riêng từ `origin/main` commit `4ff0d53`, giữ nguyên file cũ ở thư mục gốc.
- Kết quả kiểm thử:
  - `mvnw.cmd -B clean verify`: `PASS`, 21 tests, 0 failure/error/skipped; Java 26 biên dịch với release 21.
  - CSS phần A: `PASS`, đủ 13 import từ ZIP; thêm `shell.css` cho layout phần B.
  - Preview desktop 1440px và mobile 390px: `PASS`, không tràn chiều ngang; đã xem screenshot cục bộ.
  - Keyboard focus button và chế độ reduced motion của skeleton: `PASS` trên preview.
  - `node --test tests/frontend/mock-data.test.mjs`: `PASS`, 4 tests cho contract fixture, clone độc lập, chặn nguồn sai và xử lý lỗi tải.
  - `node --check` cho JavaScript: `PASS`.
  - Render layout và các fragment bằng SpringTemplateEngine/MockServletContext: `PASS`; không thêm controller hoặc dependency.
  - `tests/frontend/browser-check.mjs` trên Edge với Bootstrap: `PASS` cho viewport 1440/390px, menu mobile/Escape/focus, toast dạng text, pagination biên, mock empty và reduced motion; đã xem screenshot.
  - Bundle CSS cuối cùng: 14 import tải được, không tham chiếu custom property bị thiếu.
  - GitHub Actions: `CHƯA CHẠY`; chưa đến thời gian push/mở PR.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa có PR/review; sẽ chuyển `IN REVIEW` khi PR được mở.
- Bước tiếp theo:
  - Hoàn tất kiểm tra, commit bản đã kiểm tra và lập lịch push một lần lúc 23:37; sau push mở PR để Backend review.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-AUTH-01: Giao diện đăng ký và profile

- Trạng thái: `BLOCKED`
- Branch: `feat/auth-profile-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị đưa template đăng ký/profile và CSS auth/account có sẵn vào branch riêng.
  - Giữ nguyên nội dung bốn file nguồn, không viết mới hoặc chỉnh sửa hành vi.
  - Không thay đổi Backend, dependency, layout chung hoặc API contract.
- Kết quả kiểm thử:
  - Kiểm tra SHA-256: `PASS`, 4/4 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify`: `PASS`, 50 tests, 0 failure, 0 error, 0 skipped.
  - `git diff --cached --check`: có 3 dòng whitespace trong profile.html nguyên bản; giữ nguyên theo yêu cầu không sửa code.
  - Các test hiện có chưa xác nhận những trang mới có thể render hoặc tích hợp với ứng dụng.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa đưa template login vào vì chứa mật khẩu tài khoản hard-code; AGENTS.md không cho commit mật khẩu.
  - Template dùng layout:decorate và model/endpoint cũ, chưa tương thích layout UI-01 và contract API-01.
  - Giữ nguyên nguồn theo yêu cầu; chưa hoàn tất FE-AUTH-01, chưa sẵn sàng merge.
- Bước tiếp theo:
  - Người dùng kiểm tra phạm vi và nội dung trước khi commit/push.
  - Thống nhất xử lý login và tương thích layout/contract trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-CAT-01: Giao diện catalog, tìm kiếm và chi tiết sản phẩm

- Trạng thái: `BLOCKED`
- Branch: `feat/product-catalog-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị ba trang products/search/product-detail, hai fragment product-card/product-filter, bốn file CSS và hai file JavaScript có sẵn.
  - Giữ nguyên nội dung 11 file nguồn; không viết mới hoặc chỉnh sửa hành vi.
  - Tạo branch từ main commit `ea9ce82`, sau khi PR #20 đã merge.
  - Không thay Backend, dependency, layout/header/footer/pagination chung hoặc API contract.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 11/11 file khớp nguyên bản.
  - `node --check` cho filter.js và variant.js: `PASS`.
  - `git diff --cached --check`: có cảnh báo whitespace ở file nguyên bản; giữ nguyên theo yêu cầu không sửa code.
  - `mvnw.cmd -B clean verify`: `PASS`, 50 tests, 0 failure, 0 error, 0 skipped.
  - Maven kiểm tra regression hiện có, chưa xác nhận các template mới render hoặc hoạt động với contract hiện tại.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout:decorate và gọi pagination theo chữ ký cũ, chưa tương thích layout/fragment UI-01.
  - Field imageUrl/stock và groupedVariants() trong template khác DTO API-01; chưa có mock theo contract mới.
  - Các handler quickView/addToCart/toggleWishlist và việc nạp CSS/JS cũ chưa được nối với layout hiện tại.
  - Giữ nguyên code theo yêu cầu; đây là bản đưa nguồn lên để review, chưa hoàn tất FE-CAT-01 hoặc xác nhận giao diện chạy được.
- Bước tiếp theo:
  - Người dùng đã yêu cầu hẹn push bản đã kiểm tra lúc 13:23 ngày 12/10/2026 (UTC+7); chưa có PR/review.
  - Thống nhất cách xử lý tương thích trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-STORE-01: Giao diện cửa hàng

- Trạng thái: `BLOCKED`
- Branch: `feat/store-availability-ui`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/23
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên template danh sách/chi tiết cửa hàng và CSS stores có sẵn.
  - Không sửa nội dung code, Backend, dependency, layout chung hoặc contract.
  - Đã push theo lịch lúc 13:54:21 ngày 12/10/2026 (UTC+7), commit `f2fcf60`.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 3/3 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify`: `PASS`, 50 tests, 0 failure, 0 error, 0 skipped.
  - Kiểm thử regression hiện có chưa chứng minh template mới hoạt động; diff check có whitespace nguyên bản được giữ nguyên theo yêu cầu.
  - Sau đồng bộ main và xử lý conflict PR #23: `mvnw.cmd -B clean verify` PASS với 57 tests, 0 failure/error/skipped; diff check của phần nhật ký PASS.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout:decorate, openTime/closeTime và danh sách sản phẩm theo model cũ; chưa tương thích UI-01/API-02.
  - Chưa có mock loading/error và tồn kho theo Store + ProductVariant; chưa xác nhận trang chạy được.
  - Giữ nguyên code theo yêu cầu; bản đưa nguồn lên review chưa hoàn tất FE-STORE-01.
- Bước tiếp theo:
  - Review PR #23; đã đồng bộ main và giữ cả mục FE-CAT-01 lẫn FE-STORE-01 khi xử lý conflict nhật ký.
  - Thống nhất xử lý tương thích và tồn kho theo biến thể trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-CART-01: Giao diện giỏ hàng

- Trạng thái: `BLOCKED`
- Branch: `feat/cart-page`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/24
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên cart template, shipping-progress fragment, CSS cart và JavaScript cart có sẵn.
  - Không sửa nội dung code, Backend, dependency, layout chung hoặc contract.
  - Đã push theo lịch lúc 14:12:17 ngày 12/10/2026 (UTC+7), commit `d65affc`.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 4/4 file khớp nguyên bản.
  - `node --check` cho cart.js: `PASS`.
  - Sau đồng bộ main và xử lý conflict PR #24: `mvnw.cmd -B clean verify` PASS với 57 tests, 0 failure/error/skipped; diff check phần nhật ký PASS.
  - `mvnw.cmd -B clean verify`: `PASS`, 50 tests, 0 failure, 0 error, 0 skipped.
  - Kiểm thử regression hiện có chưa chứng minh template mới hoạt động; diff check có whitespace nguyên bản được giữ nguyên theo yêu cầu.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout:decorate, cartItems/entity cũ và bean priceFormatter; chưa khớp layout/DTO API-03.
  - cart.js gọi /api/cart/add qua UTEGear.API, chưa có mock trong task này; không chạy thao tác trên production trong kiểm thử.
  - Handler cartPlus/cartMinus/removeCartItem/clearCartAjax và việc nạp CSS/JS chưa được nối với layout hiện tại.
  - Chưa xác nhận xử lý giỏ hàng theo biến thể hoặc trang chạy được; giữ nguyên code theo yêu cầu, chưa hoàn tất FE-CART-01.
- Bước tiếp theo:
  - Review PR #24; đã đồng bộ main và giữ các mục tiến độ trước khi xử lý conflict nhật ký.
  - Thống nhất xử lý layout/model/mock trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-WISH-01: Giao diện danh sách yêu thích

- Trạng thái: `BLOCKED`
- Branch: `feat/wishlist-page`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên wishlist template và JavaScript wishlist có sẵn trên branch từ main `b4ed32e`.
  - Giữ nguyên nội dung hai file; không viết mới hoặc chỉnh sửa hành vi.
  - Không thay đổi Backend, layout/header/footer, dependency hoặc API contract.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 2/2 file khớp nguyên bản.
  - `node --check` cho wishlist.js: `PASS`.
  - Diff check có một dòng whitespace trong wishlist.html nguyên bản; giữ nguyên theo yêu cầu không sửa code.
  - `mvnw.cmd -B clean verify`: `PASS`, 107 tests, 0 failure, 0 error, 0 skipped.
  - Kiểm tra JavaScript cô lập với API/DOM giả lập: `PASS` cho ID rỗng, thêm/xóa và trạng thái icon, lỗi UNAUTHORIZED và lỗi yêu cầu; không gọi API thật.
  - Các kiểm tra này chưa chứng minh template mới render hoặc tích hợp được trên ứng dụng.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout:decorate, wishlist item/entity và product card cũ; chưa có mock theo API-03.
  - JavaScript gọi /api/wishlist/toggle qua UTEGear.API và cần được nạp trong layout; chưa xác nhận tích hợp với ứng dụng hiện tại.
  - Giữ nguyên nguồn theo yêu cầu; bản đưa lên review chưa hoàn tất FE-WISH-01 hoặc xác nhận trang chạy được.
- Bước tiếp theo:
  - Push branch sau kiểm tra; mở PR để review.
  - Thống nhất xử lý layout/model/mock trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-ORD-01: Giao diện checkout và đơn hàng

- Trạng thái: `BLOCKED`
- Branch: `feat/checkout-order-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên checkout, xác nhận đơn, lịch sử đơn, chi tiết đơn, tra cứu đơn và CSS checkout/orders có sẵn.
  - Giữ nguyên bảy file nguồn, gồm cả WebSocket client inline trong trang chi tiết đơn; không tách hoặc viết mới code.
  - Tạo branch từ main `2764ff1` sau khi PR #33 đã merge; không thay đổi Backend, dependency, layout chung hoặc API contract.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 7/7 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify`: `PASS`, 107 tests, 0 failure, 0 error, 0 skipped.
  - Kiểm tra cú pháp ba khối JavaScript inline: `PASS`, chỉ parse, không thực thi hoặc gọi API/WebSocket thật.
  - Diff check có whitespace nguyên bản trong các file nguồn; giữ nguyên theo yêu cầu không sửa code.
  - Các test regression hiện có chưa chứng minh các trang mới render hoặc tích hợp được.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout:decorate, model currentUser/orderItems và bean priceFormatter cũ; chưa tương thích layout/DTO API-03.
  - Form POST /checkout và pagination dùng giao thức/chữ ký cũ; chưa có mock loading/validation/empty/error theo contract.
  - WebSocket inline cần SockJS/STOMP WebJars và server/destination tương ứng; chưa kiểm tra realtime hoặc reconnect.
  - Không chạy checkout hay tạo đơn trên production trong kiểm thử. Giữ nguyên nguồn theo yêu cầu; chưa xác nhận các trang hoạt động hoặc hoàn tất FE-ORD-01.
- Bước tiếp theo:
  - Push branch sau kiểm tra, mở PR để review.
  - Thống nhất xử lý tương thích layout/model/mock trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-WS-01: WebSocket client

- Trạng thái: `BLOCKED`
- Branch: `feat/order-websocket-client`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Đối soát main `c0ea201`: client cập nhật trạng thái đơn trong order-detail.html đã có từ FE-ORD-01/PR #34.
  - Chuẩn bị nguyên layouts/admin.html có client SockJS/STOMP nhận thông báo đơn mới tại /topic/admin/orders và retry sau 5 giây.
  - Giữ nguyên file nguồn; không tách hoặc viết mới code, không thay Backend, dependency hoặc layout khách hàng.
  - Các trang/sidebar/CSS admin thuộc UI-02/UI-03; không đưa vào commit FE-WS-01.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, layout admin khớp nguyên bản.
  - `mvnw.cmd -B clean verify`: `PASS`, 119 tests, 0 failure, 0 error, 0 skipped.
  - Script inline: `PASS` kiểm tra cú pháp, destination admin, thông báo, frame JSON lỗi và retry 5 giây bằng SockJS/STOMP/DOM giả lập; không kết nối thật.
  - `git diff --cached --check`: `PASS`.
  - Các kiểm tra này chưa chứng minh realtime hoặc layout hoạt động trong ứng dụng.
- Vướng mắc hoặc phần còn thiếu:
  - Layout dùng Thymeleaf layout dialect và tham chiếu sidebar/admin CSS, main.js/utils.js và WebJars chưa có đủ trong main.
  - Client gốc kết nối /ws trực tiếp, chưa có mock event/API-03 envelope, trạng thái kết nối hoặc khử trùng event theo contract.
  - Chưa kiểm tra phân quyền WebSocket server, quyền nhận event hoặc tích hợp thật; không kết nối production trong kiểm thử.
  - Giữ nguyên nguồn theo yêu cầu; chưa hoàn tất FE-WS-01 hoặc xác nhận giao diện realtime hoạt động.
- Bước tiếp theo:
  - Push branch sau kiểm tra, mở PR để review.
  - UI-02 tái sử dụng layout đã có; thống nhất xử lý dependency/contract trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## UI-02: Admin catalog và cửa hàng

- Trạng thái: `BLOCKED`
- Branch: `feat/admin-catalog-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên product list/form, brand/category list, store list/detail, sidebar admin và CSS admin có sẵn.
  - Giữ nguyên tám file nguồn, tái sử dụng layouts/admin.html đã merge qua FE-WS-01/PR #36.
  - Tạo branch từ main `157456a`; không thay Backend, dependency, layout chung, app.css hoặc API contract.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 8/8 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - Lần chạy Java 26 lỗi do Byte Buddy chưa hỗ trợ phiên bản JVM; chạy lại bằng Java 21 thành công, không sửa code hoặc dependency.
  - JavaScript: `PASS` kiểm tra cú pháp 1 script inline và 4 handler tĩnh; không thực thi thao tác admin.
  - `git diff --cached --check`: `PASS`.
  - Build và kiểm tra cú pháp chưa chứng minh các trang hoạt động với Backend hiện tại.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout dialect, model/entity, pagination và POST /admin/* theo giao thức cũ; chưa có mock API-02.
  - Trang tồn kho dùng inv.product.id và stock cũ, khác mô hình Store + ProductVariant; chưa xác nhận các thao tác hoạt động.
  - CSS admin chưa được nạp vào app.css; dependency layout/script chung chưa có đầy đủ.
  - Không gọi API admin hay ghi database trong kiểm thử; bản nguồn chưa hoàn tất UI-02 hoặc sẵn sàng merge.
- Bước tiếp theo:
  - Push branch sau kiểm tra và mở PR để review.
  - Thống nhất xử lý tương thích và inventory theo biến thể trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## Mẫu mục mới

```md
## TASK-ID: Tên task

- Trạng thái: `IN PROGRESS | IN REVIEW | BLOCKED | DONE`
- Branch: `type/ten-branch`
- Pull Request: URL hoặc `Chưa mở`
- Ngày cập nhật: YYYY-MM-DD
- Đã làm:
  - ...
- Kết quả kiểm thử:
  - Lệnh hoặc trường hợp kiểm thử: `PASS | FAIL | CHƯA CHẠY`
- Vướng mắc hoặc phần còn thiếu:
  - Không có, hoặc ghi rõ phần đang thiếu và task phụ thuộc.
- Bước tiếp theo:
  - ...
- Ngày hoàn tất: YYYY-MM-DD hoặc `Chưa hoàn tất`
```

## UI-03: Admin người dùng và đơn hàng

- Trạng thái: `BLOCKED`
- Branch: `feat/admin-user-order-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Chuẩn bị nguyên dashboard, user list/detail, coupon list và order list/detail có sẵn.
  - Giữ nguyên sáu file nguồn; layout admin đã có từ FE-WS-01, sidebar/CSS được chuẩn bị trên branch UI-02.
  - Tạo branch riêng từ main `157456a`; không thay Backend, dependency, layout chung hoặc API contract.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 6/6 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - Lần chạy Java 26 lỗi do Byte Buddy chưa hỗ trợ phiên bản JVM; chạy lại bằng Java 21 thành công, không sửa code hoặc dependency.
  - JavaScript: `PASS` kiểm tra cú pháp 2 handler tĩnh; không thực thi thao tác admin.
  - `git diff --cached --check`: `PASS`.
  - Build và kiểm tra cú pháp chưa chứng minh các trang hoạt động với Backend hiện tại.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout dialect, user.isAdmin(), model order/entity và pagination cũ; chưa có mock API-03.
  - Các form coupon/order status dùng POST /admin/* khác API hiện tại; chưa xác nhận hoạt động.
  - Sidebar/CSS của UI-02 cần được merge trước khi kiểm tra bộ giao diện admin đầy đủ.
  - Không gọi API admin hoặc đổi trạng thái đơn trên production; bản nguồn chưa hoàn tất UI-03 hoặc sẵn sàng merge.
- Bước tiếp theo:
  - Push branch sau kiểm tra và mở PR riêng; review UI-02 trước khi tích hợp phần giao diện dùng chung.
  - Thống nhất xử lý model/API/mock trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## INT-01: Nguồn hỗ trợ API và tìm kiếm sản phẩm

- Trạng thái: `BLOCKED`
- Branch: `feat/integrate-auth-catalog-store`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/40
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Chuẩn bị nguyên ba file có sẵn: core/api.js, core/utils.js và components/autocomplete.js.
  - Tạo branch từ main `ae95070`; giữ nguyên code nguồn, không thay layout, Backend, dependency hoặc contract.
  - Đối soát main: UI-02 đã merge qua https://github.com/NgocYuh/UTEGear/pull/38 (ae95070), UI-03 qua https://github.com/NgocYuh/UTEGear/pull/39 (8fc2437). Phần nguồn đã merge nhưng các giới hạn tương thích còn nguyên; chưa xác nhận hai task hoàn tất.
  - Gộp main ddea4a4 sau PR #41 vào nhánh PR #40, giải quyết xung đột nhật ký bằng cách giữ cả mục INT-01 và INT-02; nội dung code giữ nguyên.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 3/3 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - `git diff --cached --check`: `PASS`.
  - JavaScript: `PASS` cú pháp 3 file và kiểm tra bằng fetch giả lập cho GET/POST/DELETE, lỗi 400, 401 và redirect, JSON không hợp lệ, lỗi mạng và định dạng giá.
  - Không kết nối API thật hoặc ghi dữ liệu trong kiểm thử; kiểm tra trên chưa chứng minh tích hợp hoạt động.
- Vướng mắc hoặc phần còn thiếu:
  - Layout hiện tại chưa nạp các script mới; chưa có lời gọi API auth/profile/store trong ba file này.
  - Autocomplete dùng keyword thay vì q; price/imageUrl khác basePrice/effectivePrice/primaryImage trong contract hiện tại.
  - API wrapper chưa tự gửi JWT; chưa kiểm thử đầy đủ loading/empty/error, bàn phím hoặc tích hợp với backend.
  - Đây là phần nguồn hỗ trợ API và tìm kiếm, chưa hoàn tất phạm vi INT-01; giữ nguyên nguồn theo yêu cầu.
- Bước tiếp theo:
  - Push branch sau khi build pass, mở PR để review phần nguồn.
  - Thống nhất xử lý tương thích và các luồng auth/catalog/store trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## INT-02: Nguồn JavaScript giỏ hàng và wishlist

- Trạng thái: `BLOCKED`
- Branch: `feat/integrate-customer-transactions`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Thêm nguyên main.js có sẵn: thao tác giỏ hàng, wishlist, thông báo, chọn biến thể/ảnh, quick view, nhận tại cửa hàng, lọc giá và phím tắt.
  - Tạo branch từ main ae95070; không thay các module/template đã push, app.js, layout, Backend hoặc contract.
  - INT-01 đã push trên branch feat/integrate-auth-catalog-store (b7db7bd), chưa nằm trong main tại thời điểm tạo branch này.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, main.js khớp nguyên bản.
  - JavaScript: `PASS` cú pháp và kiểm tra fetch/DOM giả lập cho thêm/cập nhật/xóa giỏ, xóa toàn bộ, wishlist, redirect 401, lỗi mạng và giới hạn quantity 1-99.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - `git diff --cached --check`: cảnh báo khoảng trắng cuối dòng tại main.js:49, 82, 142; giữ nguyên theo yêu cầu bảo toàn code nguồn.
  - Không gọi API thật hoặc ghi database trong kiểm thử; chưa xác nhận luồng tích hợp hoạt động.
- Vướng mắc hoặc phần còn thiếu:
  - Endpoint /api/cart/add, /api/cart/{itemId}, DELETE /api/cart và /api/wishlist/toggle khác contract hiện tại /api/cart/items và /api/wishlist/items.
  - Layout hiện tại chưa nạp main.js; chưa xử lý JWT và request/response đúng contract.
  - File không có tích hợp API tạo đơn/lịch sử đơn; phần checkout chỉ bật/tắt lựa chọn nhận tại cửa hàng.
  - Quick view dùng model cũ; thông báo và một số dữ liệu được đưa trực tiếp vào innerHTML, chưa kiểm thử hoặc xác nhận an toàn với dữ liệu không tin cậy.
  - Giữ nguyên nguồn theo yêu cầu; chưa hoàn tất INT-02 hoặc sẵn sàng merge.
- Bước tiếp theo:
  - Push branch sau khi build pass, mở PR để review phần nguồn.
  - Thống nhất xử lý tương thích, an toàn hiển thị và luồng checkout/order trước khi hoàn tất task.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-RESTORE-01: Khôi phục bộ khung giao diện và bổ sung fragment

- Trạng thái: `BLOCKED`
- Branch: `fix/restore-frontend-shell`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Khôi phục nguyên tám file nguồn: css/app.css, js/app.js, js/components/toast.js, layouts/main.html và fragments/header.html, footer.html, pagination.html, toastArea.html.
  - Bổ sung nguyên bảy file còn thiếu: css/style.css, fragments/alerts.html, fragments/global/header.html, footer.html, toastArea.html và fragments/product/product-card.html, product-specs.html.
  - Tạo branch từ main 1ee539b; giữ nguyên nội dung nguồn, không thay Backend, dependency hoặc contract.
  - Layout khôi phục tham chiếu các module JavaScript đã push trước đó; app.css khôi phục danh sách CSS component và CSS từng trang.
  - Đối soát: PR #40 đã merge tại 1ee539b và PR #41 tại ddea4a4; phạm vi tích hợp INT-01/INT-02 vẫn chưa hoàn tất.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 15/15 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - JavaScript: `PASS` cú pháp 2 file JS và 2 script inline; khởi tạo autocomplete/filter, delegation cart/wishlist/quick view, toast escaping/bridge/timer bằng DOM/module giả lập.
  - `node --test tests/frontend/mock-data.test.mjs`: `PASS`, 4 tests; chỉ xác minh fixture loader, không kiểm tra layout đã render.
  - `git diff --cached --check`: cảnh báo dòng trống cuối footer.html/product-card.html và khoảng trắng cuối main.html:8; giữ nguyên nguồn theo yêu cầu.
  - Kiểm tra mẫu credential trên 15 file: không phát hiện private key, GitHub/AWS token hoặc mật khẩu demo đã biết.
  - Không kết nối API thật hoặc ghi dữ liệu trong kiểm thử; build và giả lập chưa chứng minh giao diện render hoặc tích hợp hoạt động.
- Vướng mắc hoặc phần còn thiếu:
  - Layout gốc cần Thymeleaf layout dialect; header/fragment dùng model, helper và security expression cũ chưa xác nhận tương thích với Backend hiện tại.
  - home.css, favicon và ảnh dự phòng/banner nằm trong lượt 2, chưa có đủ trong branch này.
  - Chưa kiểm tra render, responsive, bàn phím hoặc accessibility; bản khôi phục thay thế phần hỗ trợ menu/toast của UI-01 đã viết lại trước đó.
  - Giữ nguyên source theo phạm vi được duyệt; chưa xác nhận ứng dụng frontend hoàn chỉnh hoặc các task tích hợp DONE.
- Bước tiếp theo:
  - Push để review lượt 1; sau khi merge chuẩn bị lượt 2 trang chủ và tài nguyên giao diện.
  - Đối chiếu dependency/model/API và kiểm thử giao diện thực tế trước khi xác nhận hoàn tất tích hợp.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-HOME-01: Trang chủ và tài nguyên giao diện

- Trạng thái: `BLOCKED`
- Branch: `feat/home-page-assets`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Bổ sung nguyên home.html, home.css, favicon.ico/favicon.svg, placeholder.png/placeholder.svg và bốn banner home-main, home-side-top, home-side-bottom, home-flash-sale với hai định dạng jpg/svg.
  - Giữ nguyên 14 file nguồn/tài nguyên; tạo branch từ main 49089c5, không sửa Backend, layout, dependency hoặc API contract.
  - Đối soát lượt 1: https://github.com/NgocYuh/UTEGear/pull/42 đã merge tại 49089c5. Các giới hạn tương thích của FE-RESTORE-01 chưa được giải quyết; chưa ghi hoàn tất tích hợp.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 14/14 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - `node --test tests/frontend/mock-data.test.mjs`: `PASS`, 4 tests; chỉ kiểm tra fixture loader.
  - SVG: `PASS`, 6/6 file .svg parse XML và có root svg.
  - JPEG: `PASS`, bốn banner mở được, kích thước lần lượt 920x420, 320x200, 320x200 và 1240x130.
  - Kiểm tra định dạng placeholder.png/favicon.ico: `FAIL`, dữ liệu gốc là SVG văn bản; không phải PNG/ICO, bộ giải mã PNG/ICO không mở được.
  - `git diff --cached --check`: cảnh báo khoảng trắng cuối home.html:16, 181; giữ nguyên nguồn theo yêu cầu.
  - Chưa kiểm tra render, responsive, accessibility hoặc kết nối API thật; build pass không chứng minh trang chủ hoạt động.
- Vướng mắc hoặc phần còn thiếu:
  - home.html dùng layout dialect, categoryHelper, model product/store và các field cũ; dependency/controller/model hiện tại chưa được xác nhận tương thích.
  - placeholder.png và favicon.ico có nội dung không khớp đuôi file trong nguồn; giữ nguyên theo yêu cầu, chưa xác nhận trình duyệt hiển thị đúng.
  - Chưa hoàn tất chức năng trang chủ hoặc xác nhận task tích hợp DONE.
- Bước tiếp theo:
  - Push để review lượt 2; sau merge chuẩn bị lượt 3 các trang thông tin.
  - Thống nhất xử lý định dạng tài nguyên và tương thích trước khi xác nhận giao diện chạy đầy đủ.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-INFO-01: Các trang thông tin và lỗi

- Trạng thái: `BLOCKED`
- Branch: `feat/information-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Bổ sung nguyên ba template có sẵn: about.html, contact.html và error.html; không chỉnh sửa code nguồn.
  - Tạo branch từ main abe524f, không thay Backend, dependency, layout hoặc API contract.
  - Đối soát lượt 2: https://github.com/NgocYuh/UTEGear/pull/43 đã merge tại abe524f. Các giới hạn tương thích và lỗi định dạng tài nguyên vẫn chưa được xử lý.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 3/3 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7: `PASS`, 131 tests, 0 failure, 0 error, 0 skipped.
  - `node --test tests/frontend/mock-data.test.mjs`: `PASS`, 4 tests; chỉ kiểm tra fixture loader.
  - JavaScript: `PASS` cú pháp một handler onsubmit; giả lập xác minh form contact ngăn điều hướng, hiện alert và reset form. Ba template không có script inline riêng.
  - `git diff --cached --check`: cảnh báo khoảng trắng cuối about.html:9,25; contact.html:9,25,37,42; error.html:11. Giữ nguyên nguồn theo yêu cầu.
  - Kiểm tra mẫu credential trên ba file: không phát hiện private key, GitHub/AWS token hoặc mật khẩu demo đã biết.
  - Không gọi API thật hoặc ghi dữ liệu trong kiểm thử; chưa kiểm tra HTML đã render, responsive hoặc accessibility.
- Vướng mắc hoặc phần còn thiếu:
  - Template dùng layout dialect của nguồn cũ; dependency/routing/render thực tế chưa được xác nhận tương thích.
  - Form contact chỉ hiện thông báo và reset, không gửi dữ liệu tới server; chưa phải chức năng gửi yêu cầu hỗ trợ.
  - error.html dùng model status/error/message; chưa kiểm tra trang lỗi thực tế của ứng dụng.
  - Bản nguồn được bảo toàn theo yêu cầu; chưa xác nhận giao diện hoạt động hoàn chỉnh.
- Bước tiếp theo:
  - Push để review lượt 3; sau merge chuẩn bị phần auth còn thiếu của lượt 4.
  - login.html cần xử lý mật khẩu demo theo quy tắc repository trước khi được phép commit; chưa được đưa vào lượt này.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-AUTH-RESTORE-01: Các trang auth còn thiếu

- Trạng thái: `BLOCKED`
- Branch: `feat/remaining-auth-pages`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Bổ sung nguyên login.html và forgot-password.html có sẵn, không sửa nội dung hai template.
  - Giữ nguyên khối hiển thị tài khoản/mật khẩu demo trong login.html theo xác nhận trực tiếp của người dùng ngày 2026-10-13; đây là ngoại lệ do người dùng yêu cầu đối với quy tắc không commit mật khẩu. Chưa thử đăng nhập các tài khoản này.
  - Tạo branch từ main 0762605; không thay Backend, dependency, layout hoặc contract.
  - Đối soát lượt 3: https://github.com/NgocYuh/UTEGear/pull/45 đã merge tại 0762605. Các giới hạn render/tích hợp vẫn chưa được xử lý.
- Kết quả kiểm thử:
  - SHA-256: `PASS`, 2/2 file khớp nguyên bản.
  - `mvnw.cmd -B clean verify` với Java 21.0.7, đủ hai template: `PASS`, 142 tests, 0 failure, 0 error, 0 skipped.
  - `git diff --cached --check`: `PASS`.
  - `node --test tests/frontend/mock-data.test.mjs`: `PASS`, 4 tests; chỉ kiểm tra fixture loader.
  - Template tĩnh: `PASS`, POST target, liên kết, label/type/name/required/autocomplete của email/password, trạng thái login error/logout và khối demo được giữ nguyên.
  - Không gọi API, gửi yêu cầu khôi phục hoặc thử tài khoản demo; chưa kiểm tra HTML đã render và hành vi trình duyệt.
- Vướng mắc hoặc phần còn thiếu:
  - Login dùng POST /login theo nguồn cũ, khác API JWT POST /api/auth/login của Backend hiện tại.
  - Forgot-password dùng POST /forgot-password; chưa thấy endpoint xử lý khôi phục mật khẩu trên main.
  - Cả hai trang dùng layout dialect cũ; chưa xác nhận routing/render, responsive hoặc accessibility.
  - Hoàn tất việc đưa nguồn của lượt 4 lên để review không đồng nghĩa auth hoặc INT-01 đã hoạt động hoàn chỉnh.
- Bước tiếp theo:
  - Push để review lượt 4; đối chiếu lại nguồn frontend sau merge.
  - Thống nhất xử lý routing, dependency và API trước khi xác nhận tích hợp DONE.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-CONTENT-01: Hiển thị mô tả và bảng thông số sản phẩm

- Phụ trách: `@trongsonho`
- Trạng thái: `IN REVIEW`
- Branch: `codex/product-content-ui`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-15
- Task liên kết: `DATA-05-BE` trên branch `codex/product-content-backend`.
- Đã làm:
  - Dùng model/JSON response đã chốt của Backend: description là văn bản thuần nhiều đoạn, thông số ở specifications.technicalSpecifications.
  - Thay JSON thô bằng bảng nhãn/giá trị có caption và row header; không hiển thị metadata crawler cho khách hàng.
  - Giữ xuống dòng mô tả, thêm empty state và CSS chống tràn trên màn hình nhỏ; dùng th:text cho nội dung nguồn.
  - Không thay nghiệp vụ hoặc entity/service Backend trong branch giao diện.
- Kết quả kiểm thử:
  - `mvnw.cmd -B --no-transfer-progress -Dtest=ProductContentTemplateTest test`: PASS, 3 test render Thymeleaf thật: bảng thông số, escape HTML và empty state/ẩn provenance.
  - Trình duyệt Edge headless với profile riêng: PASS cho trang sản phẩm thật /products/145, 9 dòng thông số, mô tả nhiều đoạn, row header và không tràn ngang ở 390px.
- Vướng mắc hoặc phần còn thiếu:
  - Visual QA desktop 1365px và mobile 390px: PASS, bảng 9 dòng đọc được, không tràn ngang; ảnh kiểm tra nằm trong crawler/data/output/ và bị Git ignore. Chưa mở PR.
- Bước tiếp theo:
  - Review giao diện và PR riêng với DATA-05-BE; chỉ đánh dấu DONE sau merge.
- Ngày hoàn tất: `Chưa hoàn tất`

## FE-CHECKOUT-UX: Mua ngay và thông tin đơn hàng, sản phẩm

- Phụ trách: `@trongsonho`
- Trạng thái: `IN REVIEW`
- Branch: `codex/customer-checkout-ui`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-16
- Task liên kết: `FIX-STOCK-API`, branch `codex/fix-store-availability`; dựa trên FE-CONTENT-01 commit local 3e12afb, chưa merge main nên task trước vẫn IN REVIEW.
- Phạm vi bổ sung theo phản hồi người dùng 2026-10-16: làm rõ thông tin đơn hàng và sản phẩm; liên kết DATA-06-BE để sửa nội dung nguồn. Frontend tiếp tục dùng OrderDetail và ProductDetail đã có, không đổi DTO/API.
- Bàn giao GitHub: `https://github.com/NgocYuh/UTEGear/tree/codex/customer-checkout-ui`; push theo yêu cầu người dùng, chưa mở PR/merge.
- Kiểm tra trước push: checkout sạch `e5809d9`, `mvnw.cmd -B --no-transfer-progress -Dnet.bytebuddy.experimental=true clean verify` BUILD SUCCESS, 197 test, 0 failure/error, 3 PostgreSQL test skip do Docker không sẵn có; Node PASS 29/29. Thay đổi sau build chỉ cập nhật dòng nhật ký này.
- Đã làm:
  - Thêm Mua ngay: thêm biến thể/số lượng vào giỏ hiện tại rồi chuyển checkout để khách xác nhận; khách chưa đăng nhập chuyển login với URL quay lại sản phẩm.
  - Đọc tồn kho mới từ availability theo contract Backend. Khóa nút mua khi đang tải, lỗi, hết hàng hoặc số lượng không hợp lệ; cho thử tải lại khi lỗi và ngăn bấm Mua ngay lặp trong cùng yêu cầu.
  - Hiển thị biến thể mặc định bằng Mặc định thay cho SKU kèm JSON rỗng; giữ giao diện quản trị và khách hàng theo đúng vai trò.
  - Chỉ sửa template/JavaScript và kiểm thử frontend; Backend và dữ liệu demo do FIX-STOCK-API xử lý riêng.
  - Bổ sung giao diện chi tiết đơn: trạng thái tiếng Việt, ngày giờ Việt Nam, ảnh/tên/mã/biến thể/số lượng/đơn giá/thành tiền, người nhận/điện thoại/địa chỉ, cửa hàng, phương thức và trạng thái thanh toán, tiền hàng/phí vận chuyển/giảm giá/tổng và ghi chú. Đưa hủy đơn vào mục mở rộng; đổi bộ lọc sẽ đóng chi tiết đơn cũ, ẩn Đăng nhập sau khi đã đăng nhập.
  - Trang sản phẩm có breadcrumb, nhãn thương hiệu/danh mục, mã sản phẩm đồng bộ biến thể, khối giá và vùng mua hàng rõ ràng, liên kết đến mô tả và thông số; bố cục nội dung 2 cột desktop/1 cột mobile. Không sửa layout/header/footer hoặc nội dung home đang có thay đổi riêng.
- Kết quả kiểm thử:
  - `node --test tests/frontend/integration.test.mjs tests/frontend/mock-data.test.mjs`: PASS 25/25, gồm 5 ca Mua ngay, guest login, vượt kho, bấm lặp và lỗi giỏ hàng.
  - Edge headless với profile riêng, API thật localhost: PASS trang /products/218, label Mặc định, còn 10, Mua ngay, checkout COD, quote và xác nhận đơn, giỏ rỗng, xem/hủy đơn khách hàng và kho hoàn về 10. Không có lỗi JavaScript runtime.
  - Desktop 1365px và mobile 390px: nút mua và trang checkout hoạt động, sản phẩm không tràn ngang; ảnh/báo cáo trong crawler/data/output bị Git ignore.
  - Phạm vi bổ sung: `node --test tests/frontend/order-information.test.mjs tests/frontend/integration.test.mjs tests/frontend/mock-data.test.mjs`: PASS 29/29, gồm nội dung đơn, coupon/biến thể, missing payment, escape nội dung không tin cậy và ngày giờ/status.
  - Browser thật với API local sau DATA-06-BE: PASS Lexar H31 có 8 dòng thông số và mô tả >1.000 ký tự; đơn demo UTE-1792085983-BEC9842A hiển thị thông tin nhận hàng, ảnh sản phẩm, COD, phí 30.000đ/tổng 780.000đ; lọc PENDING/CANCELLED đúng và không giữ chi tiết cũ. Đơn QA đã hủy, tồn kho trở về 9 đúng mức trước lượt QA, giữ đơn người dùng đang chờ. Không có lỗi JavaScript. Đã xem ảnh toàn trang sản phẩm/đơn hàng desktop và mobile, không tràn ngang.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa mở PR hoặc merge. Runtime dùng jar backend FIX-STOCK-API; worktree frontend không chứa thay đổi nghiệp vụ backend.
- Bước tiếp theo:
  - Review task frontend riêng và liên kết FIX-STOCK-API; chỉ đánh dấu DONE sau merge.
- Ngày hoàn tất: `Chưa hoàn tất`


## FE-LOCAL-PUBLISH: Bàn giao giao diện giống bản đang chạy local

- Phụ trách: `@trongsonho` (Frontend).
- Trạng thái: `IN REVIEW`.
- Branch: `codex/local-home-ui`.
- Pull Request: `Chưa mở`.
- Ngày cập nhật: 2026-10-16.
- Task liên kết: `INT-LOCAL-DEMO`, `BE-LOCAL-DEMO-TOOLS`, `FE-CHECKOUT-UX`.
- Đã làm: Đưa đầy đủ header/footer/cards, trang chủ gọn, banner, menu tài khoản, tab sản phẩm và Flash Sale đang chạy local vào commit 91f680f, giữ nguyên nội dung giao diện. Các task giao diện trước trong cùng lượt tinh chỉnh được bàn giao tại đây. Không sửa nghiệp vụ Backend.
- Kết quả kiểm thử: JS PASS 41/41; clean verify bản tích hợp BUILD SUCCESS, 202 test, 0 failure/error, 4 PostgreSQL skip (Docker thiếu). 128 templates/static khớp local sau chuẩn hóa line endings; ảnh nhị phân khớp nguyên byte.
- Vướng mắc hoặc phần còn thiếu: Chưa merge main; bản tích hợp dùng cùng database local để giữ nguyên dữ liệu.
- Bước tiếp theo: Review trong INT-LOCAL-DEMO và chỉ đánh dấu DONE sau khi merge.
- Ngày hoàn tất: `Chưa hoàn tất`.
