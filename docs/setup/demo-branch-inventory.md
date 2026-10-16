# Tồn kho development theo chi nhánh

Chỉ dùng database development/demo đã xác nhận. CN-1 giữ nguyên dữ liệu; CN-2 Hà Nội và CN-3
Đà Nẵng nhận 10 chiếc cho mỗi biến thể đang bán đã có bản ghi kho tại CN-1. Chỉ thêm cặp
Store + ProductVariant chưa tồn tại; chạy lại không reset hàng đã bán hoặc dòng hết hàng.

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--app.demo.inventory-setup=true --app.demo.environment=development"
```

Dừng ứng dụng đang giữ cổng trước khi chạy. Sau lần bổ sung, chạy lại bình thường không kèm
cờ demo. Runner không bật mặc định và không hoạt động khi có profile prod. Không có migration
hoặc thay đổi schema. Transaction khóa ghi bảng kho trong thời gian nhập, không sửa catalog,
chi nhánh, tài khoản hoặc đơn hàng.

Contract checkout hiện có: `POST /api/checkout/quote` và `POST /api/orders` đều nhận
`fulfillmentStoreId`. Quote trả `fulfillmentStore: {id, name}`; thay đổi chi nhánh làm quote cũ
không hợp lệ (`QUOTE_CHANGED`). Backend kiểm tra/trừ kho đúng Store + ProductVariant,
hủy đơn hoàn kho chi nhánh của đơn. Frontend hiển thị mã CN và bắt tính lại khi đổi chi nhánh.
