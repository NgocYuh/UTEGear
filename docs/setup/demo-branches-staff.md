# Ba chi nhánh và tài khoản nhân viên development

Task Backend `DEMO-STAFF-BE-02` (@NgocYuh), Frontend liên kết `CHAT-FE-02` (@trongsonho).

Chỉ chạy trên database development/demo đã được xác nhận. Giữ cửa hàng hiện tại, thêm hai
cửa hàng demo Hà Nội/Đà Nẵng; địa chỉ là nhãn demo, không phải địa điểm kinh doanh thật.
Không sửa migration, không đặt lại tồn kho, không thay đổi đơn hàng hay tài khoản hiện có.

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--app.demo.setup=true --app.demo.environment=development"
```

Chạy bình thường sau lần tạo đầu, bỏ hai cờ `app.demo.*`. Tính năng này không được bật mặc định,
không tồn tại trong profile prod. Toàn bộ thao tác ghi dữ liệu và xuất report nằm trong một
transaction; lỗi xuất report sẽ rollback database. Nếu database không có đúng một cửa hàng
ban đầu hoặc ba cửa hàng của lượt demo trước, lệnh sẽ từ chối và giữ nguyên dữ liệu.

File JSON tại `crawler/data/output/demo-accounts/staff-<uuid>.json` bị Git ignore, chứa ID thật,
mã hiển thị `CN-<storeId>`, `NV-<userId>`, email đăng nhập và mật khẩu ngẫu nhiên của chín nhân viên.
Email có dạng `employee.cn<storeId>.<1..3>@demo.utegear.test`. Mã hiển thị được suy ra từ ID thật,
không sửa khóa chính hoặc sequence của database. Mật khẩu chỉ được lưu dưới dạng hash trong database.
Chạy lại giữ nguyên ID và mật khẩu; report mới ghi password null cho tài khoản đã có, hãy giữ report đầu.
Nếu email trùng tài khoản khác hoặc chi nhánh có nhân viên ngoài bộ demo, transaction bị hủy.

Đăng nhập nhân viên ở `/login?redirect=/admin/support`, khách ở `/login`, mở chatbox và chọn chi nhánh.
Các cửa hàng mới chưa có tồn kho; quản lý theo Store + ProductVariant qua API/admin hiện có nếu cần bán hàng.

Contract API/model tiếp tục theo `docs/api/support-chat-contract.md` và JSON mẫu
`docs/api/mock/support-chat.json`: `storeId` chọn chi nhánh; conversation trả `employeeId` và
`employeeName`; message có `id`, `conversationId`, `senderId`, `senderName`, `content`, `sentAt`.
Chatbox và inbox subscribe `/user/queue/support`, tải lại REST khi kết nối lại; giới hạn 2.000 ký tự.
