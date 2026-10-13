# FE-INT-FIX — Đề xuất quản lý phiên JWT (DRAFT)

- Ngày: 2026-10-13.
- Liên kết: INT-01 / INT-02 / INT-03, branch `fix/frontend-integration`.
- Trạng thái quyết định: `CHƯA ĐƯỢC DUYỆT`; cần Backend review trước merge.
- Người đề xuất: Frontend `@trongsonho`; người review: Backend `@NgocYuh`.
- Không thay thế quyết định kiến trúc/bảo mật đã chốt trong repository.

## Bối cảnh và yêu cầu

Backend trả accessToken/tokenType/expiresAt/user từ POST /api/auth/login. Protected REST
API nhận Authorization: Bearer. WebSocket sử dụng STOMP qua /ws, JWT nằm trong header
Authorization của frame STOMP CONNECT. Chưa có contract refresh token hoặc cookie session.

FE cần xử lý login/logout/hết phiên, giữ token khỏi log/URL/repository, và không gửi token
sang origin khác. Client không thay thế kiểm tra JWT, role hoặc quyền sở hữu ở server.

## Các lựa chọn để BE review

| Lựa chọn | Tác động và giới hạn |
|---|---|
| Chỉ giữ trong bộ nhớ | Không ghi token vào Web Storage; reload hoặc chuyển trang đầy đủ làm mất phiên. JavaScript cùng trang vẫn có thể đọc token. |
| sessionStorage theo tab | Giữ phiên khi reload cùng tab; bị JavaScript cùng origin đọc được, nên XSS có thể lấy token. Không đồng nghĩa token được bảo vệ như HttpOnly cookie. |
| Cookie HttpOnly do BE quản lý | FE không đọc token bằng JavaScript; cần quyết định BE về cookie, CSRF, SameSite/Secure và cơ chế xác thực STOMP. Không thể chỉ đổi FE rồi coi tương thích với CONNECT Bearer hiện có. |

## Phương án trong diff — chỉ là prototype

Diff đang dùng sessionStorage để BE review hành vi client theo API JWT hiện có; đây là
đề xuất, KHÔNG phải lựa chọn chính thức. Chỉ lưu AuthSession, không lưu email/password
của request login; kiểm tra expiresAt trước sử dụng. 401 protected/logout xóa phiên,
403 không xóa phiên. API chỉ cho cùng origin và không tự đi theo redirect.

Rủi ro còn lại: XSS và script bên thứ ba cùng origin có thể đọc token; logout hiện không
được hiểu là server đã thu hồi JWT; kiểm tra expiresAt ở client không thay xác thực server.
JWT sessionStorage cũng không tự tạo isAuthenticated/currentUser cho SSR Thymeleaf.

## Điều kiện trước merge

- BE chọn/duyệt rõ cách lưu phiên, hành vi logout/hết hạn/refresh và tích hợp SSR/STOMP.
- Ghi kết quả review, ngày và tham chiếu PR ngay trong tài liệu này.
- Nếu chọn phương án khác: thay prototype và test trước merge; phần cần đổi server có
  task/branch/PR Backend riêng, không đưa vào PR FE.
- Kiểm thử trình duyệt/API thật sau khi routing/model sẵn sàng. Unit tests không xác nhận
  quyết định bảo mật hoặc chứng minh frontend không có XSS.

Kết quả BE review: `CHỜ REVIEW CODE VÀ QUYẾT ĐỊNH`, chưa có lựa chọn được chấp thuận.
