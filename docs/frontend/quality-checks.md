# Kiểm tra chất lượng Frontend — FND-04

Người phụ trách: `@trongsonho`. Backend reviewer: `@NgocYuh`.

## Trạng thái hiện tại

Repository chưa có giao diện nghiệp vụ hoặc bộ công cụ kiểm tra Frontend thật.
Workflow `.github/workflows/frontend-check.yml` chỉ được kích hoạt bằng
`workflow_dispatch`; chưa chạy tự động trên Pull Request.

Khi chạy thủ công, workflow báo lỗi và kết thúc với exit code `1`, vì chưa có
kiểm tra thực tế. Kết quả này có nghĩa là chưa sẵn sàng kiểm tra Frontend,
không phải đã phát hiện lỗi giao diện. Không dùng workflow này làm required check
trước khi các kiểm tra thật được triển khai.

## Kiểm tra cần triển khai khi có code

| Phạm vi | Kiểm tra phù hợp |
|---|---|
| Thymeleaf/HTML | Kiểm tra cấu trúc template; kiểm tra HTML đã render để tránh kết quả sai do cú pháp Thymeleaf |
| JavaScript | Kiểm tra cú pháp và lint cho mã thuộc dự án; kiểm tra tương tác có rủi ro như validation và xử lý lỗi API |
| CSS | Lint với cấu hình phù hợp CSS đang dùng; kiểm tra responsive trên giao diện đã render |
| Accessibility | Keyboard, focus, label, độ tương phản; bổ sung kiểm tra tự động khi có trang chạy được |
| Tích hợp | Kiểm tra loading/empty/error theo contract; sau task INT dùng endpoint thật |

HTMLHint, ESLint và Stylelint là các công cụ có thể đánh giá khi có code tương ứng;
FND-04 chưa thêm dependency hoặc chọn cấu hình lint chưa được kiểm chứng.
Lighthouse trong dự án ZIP có thể tham khảo khi ứng dụng chạy được, nhưng báo cáo
cũ không chứng minh chất lượng của branch hiện tại.

## Điều kiện bật kiểm tra Pull Request

1. Có code Frontend và lệnh kiểm tra thực tế chạy được trên checkout sạch.
2. Dependency và cấu hình được quản lý trong repository, không phụ thuộc `node_modules` từ ZIP.
3. Xác minh lệnh pass với code hợp lệ và fail với lỗi đại diện trong phạm vi kiểm tra.
4. Ghi lệnh, phạm vi và giới hạn kiểm tra trong tài liệu này.
5. Thay bước `pending-tooling` bằng các kiểm tra thật, rồi thêm trigger PR vào `main`.

Mỗi task giao diện vẫn phải kiểm tra desktop/mobile và keyboard phù hợp phạm vi.
Trước khi push, chạy Maven verify và `git diff --check` theo quy tắc repository.

## Phạm vi PR FND-04

Chỉ bao gồm workflow Frontend, tài liệu này và nhật ký Frontend.
Mã nguồn ZIP được lưu tại `.local-reference/Project-LTWeb` trên máy để tham khảo;
thư mục này được loại khỏi Git bằng `.git/info/exclude`, không thuộc PR.
Các template, CSS và JavaScript sẽ được chuyển theo task UI/FE tương ứng sau khi
contract và các task phụ thuộc đã merge.
