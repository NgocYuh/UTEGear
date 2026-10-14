# QA-04 — Phạm vi FE đã kiểm tra, chờ review

- Ngày: 2026-10-14. Frontend: @trongsonho.
- Branch: test/qa04-completion, base main 8d2d71d (PR #57 đã merge).
- Bản này được gửi lên GitHub để BE review; không ghi DONE trước khi PR được duyệt theo AGENTS.md.
- Chỉ đổi FE quick-view, frontend tests/fixture và tài liệu. Không sửa Backend/config/migration.

## Kết quả

Spring Boot/API/STOMP và Chromium thật, H2 tạm, CUSTOMER A/B và admin QA; không intercept API,
không fake response/event. Browser harness chỉ theo dõi socket native và CDP Network không chứa JWT.

| Phạm vi | Bằng chứng |
|---|---|
| HTTP, render, responsive, labels/focus, required/email/mismatch | 93 PASS, 0 FAIL |
| Giao dịch và realtime customer/admin | 39 PASS, 0 FAIL |
| Ca mở rộng | 54 PASS, 0 FAIL |
| Unit JS hiện có | 18 PASS |
| Maven clean verify, Java 26, -Dnet.bytebuddy.experimental=true | BUILD SUCCESS; 185 tests phát hiện, 182 pass, 3 PostgreSQL skip local |

Bộ mở rộng phủ: đăng ký hợp lệ qua form và trùng email; login sai; search SSR; multi-variant/giá/ảnh;
stock theo store, inactive/hết hàng; coupon hợp lệ/hết hạn/không đủ minimum; báo giá hết hạn phía
client và thay form khi quote đang pending; submit nhanh nhiều lần chỉ POST/tạo đúng một đơn;
thiếu tồn kho; xóa item/clear cart; offline API error và retry; modal Tab/Shift+Tab/Escape/focus,
accessible names của buttons/dialog trên cây accessibility Chromium; labels và responsive trang
authenticated 1440/768/390; protected 401 và client session expiry xóa dữ liệu riêng.

Quick-view FE đã bổ sung tên nút đóng, tên dialog theo product và trả focus về trigger khi đóng.
Test chờ Bootstrap modal mở/đóng xong để kiểm tra focus. Các lỗi selector/biến scope của test
ở các vòng phát triển đã được sửa, không đưa vào kết luận lỗi sản phẩm.

## Đối chiếu kế hoạch và giới hạn

Theo UTEGear_INCREMENTAL_DEVELOPMENT_PLAN.md, QA-04 là validation/API error/WebSocket reconnect,
responsive/keyboard/focus/accessibility cơ bản phía FE. PostgreSQL/Flyway là QA-02 của BE.
Nhật ký BE ghi QA-02 DONE và CI PostgreSQL đã pass; Backend CI của main 8d2d71d cũng success:
https://github.com/NgocYuh/UTEGear/actions/runs/38538212032
Không suy từ CI green rằng tôi đã chạy PostgreSQL local; Docker local không chạy, 3 tests skip.
Điều kiện trước đây yêu cầu toàn bộ PostgreSQL local để đóng QA-04 đã rộng hơn phạm vi FE.

Expiry tests tăng Date.now phía browser 16 phút/61 phút; chứng minh client boundary và không gửi
order POST khi quote hết hạn theo client. Không chứng minh BE quote/JWT hết hạn theo đồng hồ server.
Media fixture dùng SVG local; không thay bằng chứng upload/thay/xóa Cloudinary thật. Accessibility
là mức cơ bản theo plan, không phải full WCAG audit. Không cộng số check thành số luồng nghiệp vụ.

Runner read-only vẫn ghi 5 placeholder BLOCKED cho nhóm không chạy ở runner đó; launcher mới
đối chiếu chúng với hai suite authenticated. Mọi BLOCKED khác và mọi FAIL vẫn được giữ lại.
Kết quả tổng hợp và limitations: D:/UTEGear/.local-reference/storefront-test-artifacts/qa04-ready/summary.json
Artifact/report/screenshots/log không commit. Build cuối:
D:/UTEGear/.local-reference/storefront-test-artifacts/qa04-completion-final/verify.log

## Chạy lại

Chromium CDP loopback 9238 với profile test riêng, Node 22+, JDK/Maven phù hợp repo:

```powershell
$env:QA_MAVEN_CACHE='D:/maven-repository'
node tests/frontend/run-storefront-checks.mjs D:/UTEGear/.local-reference/storefront-test-artifacts/qa-rerun
```

Launcher kiểm tra port 18081, dùng Spring Boot test profile/H2 create-drop, fixture tạm,
JWT lifetime 1h chỉ ở test launcher. Có thể đặt QA_CHECK_MODE=risks để debug suite ca mở rộng;
không dùng chế độ này thay toàn bộ kết quả QA-04. Tài khoản/password tạm không được lưu vào repo;
launcher xóa seed/hash helper và dừng riêng server do nó khởi động.

## Sang QA-05

Phạm vi kiểm thử QA-04 đã đạt trên môi trường mô tả; bản sửa mới cần review/duyệt PR để ghi DONE.
Đã chuẩn bị QA-05 trên branch tài liệu riêng docs/manual-test-report. Bước quản trị ảnh cần đối
chiếu contract/backlog/UI routing và môi trường Cloudinary QA: /admin/products và
/admin/products/200/edit trả 404 trong probe thật. API media BE tồn tại; không gọi đây là thiếu API BE.
Không ghi upload/thay/xóa ảnh PASS bằng fixture hoặc test double. Chưa ghi QA-05 DONE.
