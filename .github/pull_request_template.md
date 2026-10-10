## Mô tả thay đổi

<!-- Thay đổi gì và vì sao? -->

## Task và tiến độ

- Task ID:
- Nhật ký đã cập nhật: `reports/backend/progress-log.md` hoặc
  `reports/frontend/progress-log.md`
- Trạng thái khi mở PR: `IN REVIEW`

- [ ] Nhật ký ghi việc đã làm, kết quả kiểm thử, vướng mắc hoặc phần còn thiếu và bước tiếp theo

## Loại thay đổi

- [ ] Tính năng
- [ ] Sửa lỗi
- [ ] Kiểm thử
- [ ] Tài liệu
- [ ] Công việc bảo trì

## Phạm vi ảnh hưởng

<!-- Backend, frontend, crawler, database, cấu hình hoặc tài liệu. -->

- [ ] Backend
- [ ] Frontend
- [ ] Contract/API model
- [ ] Tích hợp FE-BE
- [ ] Chỉ tài liệu/quy trình

<!-- Một PR tính năng không nên đồng thời triển khai nghiệp vụ Backend và giao diện Frontend. -->

## Cách kiểm thử

<!-- Liệt kê lệnh và các trường hợp đã kiểm tra. -->

- [ ] Đã chạy `./mvnw -B clean verify`
- [ ] CI liên quan đã pass
- [ ] Contract/JSON mẫu đã cập nhật trước nếu API hoặc model thay đổi
- [ ] Mock data phía Frontend khớp contract đã merge

## Database và cấu hình

- [ ] Không có migration
- [ ] Có migration mới và đã mô tả cách áp dụng
- [ ] Không có biến môi trường mới
- [ ] Có biến môi trường mới và đã cập nhật file ví dụ/tài liệu

## Bảo mật

- [ ] Không chứa secret, mật khẩu, token, URL database thật hoặc API key thật
- [ ] Phân biệt rõ API public và API được bảo vệ

## Ảnh giao diện

<!-- Đính kèm ảnh trước/sau nếu có thay đổi frontend; nếu không, ghi N/A. -->
