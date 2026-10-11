# UI-01: CSS, layout và component dùng chung

Người phụ trách: `@trongsonho`. Backend reviewer: `@NgocYuh`.
Người dùng đã yêu cầu gom phần A và B vào cùng task UI-01 và hẹn push một lần
lúc 23:37 ngày 11/10/2026 (UTC+7). Branch: `feat/shared-ui-layout`.

| Phần | Phạm vi |
|---|---|
| A | CSS core, CSS component chung, điểm nhập CSS và preview kiểm tra |
| B | Layout/fragment Thymeleaf, menu mobile, toast, pagination, mock theo API-01 |

FND-01, FND-04 và API-01 đã merge. UI-01 vẫn cần kiểm tra, review và merge;
push theo lịch không có nghĩa là task đã DONE. Không triển khai trang nghiệp vụ
hoặc thay đổi Java/backend trong PR này.

## Nguồn và phạm vi CSS

CSS core và chín component được lấy từ `Project-LTWeb.zip`, giữ nguyên hành vi và
dọn khoảng trắng cuối dòng/file. `app.css` được tạo riêng; `shell.css` bổ sung responsive, skip link và
trạng thái toast cho layout mới. Giữ alias cũ trong `variables.css` để tương thích.

- Core: variables, reset, typography và utilities.
- Components: buttons, forms, navbar, footer, toast, pagination, badges, empty-state, skeleton.
- Không import CSS các trang hoặc product card/quick-view modal trong bundle này.
- Không chuyển template nghiệp vụ, JavaScript gọi API, ảnh hoặc Java/backend từ ZIP.

Layout tải Bootstrap 5.3.3 trước rồi `/css/app.css`; cần mạng để tải Bootstrap từ
CDN. Font `Be Vietnam Pro` dùng fallback hệ thống, chưa thêm tải font bên ngoài.

## Dùng layout và mock

Layout dùng fragment Thymeleaf chuẩn, không cần thêm layout dialect vào `pom.xml`:

```html
<html xmlns:th="http://www.thymeleaf.org"
      th:replace="~{layouts/main :: main('Tên trang', ~{::section})}">
  <section>Nội dung trang</section>
</html>
```

Các fragment chung: `header`, `footer`, `toastArea`, `pagination`, `empty-state`.
Menu mobile hỗ trợ Enter/Space và Escape, có `aria-expanded`; khi JS không chạy,
các liên kết điều hướng vẫn hiển thị. Toast dùng textContent, có nút đóng, tạm dừng
khi hover/focus; toast lỗi không tự mất. Pagination nhận DTO ProductPage, ẩn khi
0/1 trang, giới hạn năm nút số trang và không làm mất các bộ lọc trong callback.

`UTEGear.MockData.load()` đọc fixture tĩnh `/js/mock/api-01.json`, là bản sao nguyên
vẹn của `docs/api/mock/api-01.json`. `get(key)` trả bản sao để thao tác preview
không làm thay đổi fixture gốc. Đây không phải API client hoặc cơ chế đăng nhập;
không lưu token hoặc truy cập database. `preview/ui-foundation.html` dùng fixture
cho trạng thái ready, empty và error; không có controller/endpoint preview mới.

## Cách kiểm tra

Mở `docs/frontend/css-foundation-preview.html` bằng trình duyệt từ checkout.
Preview CSS chỉ dùng HTML tĩnh để xem các primitive, không phải trang ứng dụng
hoặc Thymeleaf template được triển khai. File CSS được tải qua đường dẫn tương đối.

Kiểm tra viewport 1440px và 390px, không tràn chiều ngang; kiểm tra focus trên
button/input/pagination và chế độ reduced motion. Preview minh họa button, form,
badge, empty state, skeleton và toast. Render template `preview/ui-foundation`
bằng Thymeleaf trong môi trường kiểm tra để kiểm tra layout/fragment và JavaScript.
Không sử dụng preview để kết luận chất lượng giao diện đã tích hợp.

Chạy `node --test tests/frontend/mock-data.test.mjs` để kiểm tra fixture loader;
chạy `node --check` cho từng file JavaScript. Trước push chạy Maven verify và
`git diff --check`.

`tests/frontend/browser-check.mjs` kiểm tra preview đã render trong Chromium qua
CDP (Node 22+):

```powershell
node tests/frontend/browser-check.mjs <URL-preview> <URL-debugging-CDP> <thu-muc-screenshot>
```

URL preview phải phục vụ HTML đã render từ `preview/ui-foundation` và các asset
tĩnh; endpoint CDP là của trình duyệt kiểm tra riêng, không phải trình duyệt cá nhân.
Script kiểm tra Bootstrap đã tải, viewport 1440/390px, menu/Escape/focus, toast
không chèn HTML, pagination gần trang cuối, empty state và reduced motion.
Các screenshot là artifact cục bộ, không commit vào repository.
Workflow Frontend vẫn ở chế độ thủ công do FND-04; CSS trong task này chưa thêm
lint dependency hoặc kích hoạt CI tự động cho Frontend.
