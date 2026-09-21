# UTEGear initial-data crawler

Crawler này chỉ hỗ trợ chuẩn bị dữ liệu ban đầu. Nó không chạy liên tục và sẽ dừng khi dữ
liệu đủ dùng. Scaffold hiện chưa chứa logic crawl thật.

## Pipeline dự kiến

1. **Crawl:** lấy nội dung từ nguồn đã được cho phép và cấu hình trong `config/`.
2. **Parse:** trích xuất trường dữ liệu nguồn.
3. **Clean:** chuẩn hóa văn bản, giá trị và danh mục.
4. **Validate:** kiểm tra trường bắt buộc, kiểu dữ liệu và record trùng.
5. **Export:** tạo đầu ra trung gian để công cụ import riêng xử lý.

Không commit dữ liệu thật, dữ liệu thô, ảnh tải về hoặc credential. Xem thêm
`docs/crawler/crawler-design.md` và `docs/crawler/data-contract.md`.

## Chạy scaffold

```bash
python src/main.py
```
