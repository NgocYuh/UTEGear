# Thiết kế crawler

Crawler là công cụ chạy hữu hạn để chuẩn bị đủ dữ liệu ban đầu, không phải dịch vụ nền.
Pipeline dự kiến: **crawl → parse → clean → validate → export**. Mỗi nguồn phải có cấu hình,
giới hạn tốc độ và điều kiện sử dụng được review trước khi chạy.

Đầu ra chỉ được import sau khi validate. Dữ liệu thô, dữ liệu đã xử lý và ảnh tải xuống
không được commit. Khi dataset đã đủ dùng, nhóm dừng crawler và duy trì dữ liệu qua ứng dụng.
