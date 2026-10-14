# Quyết định quản lý phiên JWT cho frontend

- Ngày đề xuất: 2026-10-13; ngày Backend duyệt: 2026-10-14.
- Liên kết: INT-01 / INT-02 / INT-03 và INT-BE-02.
- Trạng thái quyết định: `APPROVED`.
- Người đề xuất: Frontend `@trongsonho`; người review: Backend `@NgocYuh`.
- Không thay thế quyết định kiến trúc/bảo mật đã chốt trong repository.

## Bối cảnh và yêu cầu

Backend trả accessToken/tokenType/expiresAt/user từ POST /api/auth/login. Protected REST
API nhận Authorization: Bearer. WebSocket sử dụng STOMP qua /ws, JWT nằm trong header
Authorization của frame STOMP CONNECT. Chưa có contract refresh token hoặc cookie session.

FE cần xử lý login/logout/hết phiên, giữ token khỏi log/URL/repository, và không gửi token
sang origin khác. Client không thay thế kiểm tra JWT, role hoặc quyền sở hữu ở server.

## Các lựa chọn đã review

| Lựa chọn | Tác động và giới hạn |
|---|---|
| Chỉ giữ trong bộ nhớ | Không ghi token vào Web Storage; reload hoặc chuyển trang đầy đủ làm mất phiên. JavaScript cùng trang vẫn có thể đọc token. |
| sessionStorage theo tab | Giữ phiên khi reload cùng tab; bị JavaScript cùng origin đọc được, nên XSS có thể lấy token. Không đồng nghĩa token được bảo vệ như HttpOnly cookie. |
| Cookie HttpOnly do BE quản lý | FE không đọc token bằng JavaScript; cần quyết định BE về cookie, CSRF, SameSite/Secure và cơ chế xác thực STOMP. Không thể chỉ đổi FE rồi coi tương thích với CONNECT Bearer hiện có. |

## Phương án được chấp thuận

Frontend dùng `sessionStorage` theo tab để tương thích với API JWT hiện tại. Chỉ lưu
`AuthSession`, không lưu email/password của request login; kiểm tra `expiresAt` trước sử dụng.
Response `401` từ API protected và thao tác logout xóa phiên; `403` giữ phiên để hiển thị lỗi
thiếu quyền. Client chỉ gửi Bearer token tới cùng origin và không tự đi theo redirect.

STOMP gửi cùng Bearer token trong header `Authorization` của frame `CONNECT`. Khi token hết hạn
hoặc server từ chối kết nối, client dừng reconnect cho tới khi người dùng đăng nhập lại.

Rủi ro còn lại: XSS và script bên thứ ba cùng origin có thể đọc token; logout hiện không
được hiểu là server đã thu hồi JWT; kiểm tra expiresAt ở client không thay xác thực server.
JWT sessionStorage cũng không tự tạo isAuthenticated/currentUser cho SSR Thymeleaf.

## Giới hạn và điều kiện thay đổi

- Chưa có refresh token; logout phía client không thu hồi access token đã phát hành.
- SSR Thymeleaf chỉ render shell public. Dữ liệu profile, order và admin được tải qua API sau
  khi JavaScript đọc token; không đưa JWT vào URL, cookie không HttpOnly hoặc model server-side.
- Nếu chuyển sang cookie HttpOnly: cập nhật contract, CSRF, SameSite/Secure và xác thực STOMP
  trước khi đổi client. Phần cần đổi server phải có
  task/branch/PR Backend riêng, không đưa vào PR FE.
- FE phải giữ escaping/CSP phù hợp và không thêm script bên thứ ba không cần thiết vì XSS có thể
  đọc token trong `sessionStorage`.
- QA-04/QA-05 vẫn phải kiểm thử trình duyệt/API thật. Unit test không chứng minh frontend không có XSS.

Kết quả BE review: `APPROVED — sessionStorage theo tab với các điều kiện trên`, Backend `@NgocYuh`,
2026-10-14.
