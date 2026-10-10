# Báo cáo

Lưu tài liệu báo cáo theo khu vực `backend/`, `frontend/`, `crawler/`, `testing/` và `final/`.

## Nhật ký bắt buộc

- Backend cập nhật `backend/progress-log.md`.
- Frontend cập nhật `frontend/progress-log.md`.
- Người phụ trách cập nhật cùng một mục Task ID khi task chuyển trạng thái.
- `IN PROGRESS`: đã bắt đầu làm.
- `IN REVIEW`: đã mở Pull Request và đang chờ review hoặc CI.
- `BLOCKED`: chưa thể tiếp tục; phải ghi rõ đang thiếu gì và cần ai hoặc task nào xử lý.
- `DONE`: phạm vi task đã hoàn tất, kiểm thử đã pass và Pull Request đã được duyệt.

Mỗi mục phải ghi Task ID, branch, Pull Request, việc đã làm, kết quả kiểm thử, vướng mắc hoặc
phần còn thiếu, bước tiếp theo và ngày cập nhật. Nếu chưa có vướng mắc, ghi `Không có`.

Người thực hiện cập nhật `DONE` trong commit cuối của chính branch task, ngay trước khi merge.
Nhờ vậy, `main` chỉ nhận trạng thái `DONE` cùng lúc với mã nguồn của task.

Không commit artifact build, application log, log debug, credential, dữ liệu crawler thô hoặc
file nhị phân lớn không cần thiết.
