# Thiết kế crawler

Crawler là công cụ chạy hữu hạn để chuẩn bị đủ dữ liệu ban đầu, không phải dịch vụ nền.
Pipeline: **crawl → parse → clean → validate → export**. Mỗi nguồn phải có cấu hình,
giới hạn tốc độ và điều kiện sử dụng được review trước khi chạy.

## Phạm vi DATA-01

- Adapter đầu tiên là GEARVN; parser đọc `Product` JSON-LD từ HTML công khai thay vì gọi API nội bộ.
- HTTP client kiểm tra `robots.txt`, dùng User-Agent riêng và mặc định chờ 2 giây giữa hai request.
- CLI cần cờ `--allow-network` mới tải URL thật và chỉ chấp nhận host thuộc nguồn đã chọn.
- KICAP được giữ ở trạng thái tắt đến khi robots và điều khoản nguồn được review.
- Test dùng HTML fixture nhỏ, cố định; CI không phụ thuộc website thật.
- Mục tiêu 100 sản phẩm thuộc DATA-02, không phải tiêu chí hoàn thành DATA-01.

Đầu ra chỉ được import sau khi validate. Dữ liệu thô, dữ liệu đã xử lý và ảnh tải xuống
không được commit. Khi dataset đã đủ dùng, nhóm dừng crawler và duy trì dữ liệu qua ứng dụng.
