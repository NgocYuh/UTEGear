# UTEGear initial-data crawler

Crawler này chỉ hỗ trợ chuẩn bị dữ liệu ban đầu. Nó không chạy liên tục và sẽ dừng khi dữ
liệu đủ dùng. DATA-01 hỗ trợ nguồn GEARVN qua dữ liệu `Product` JSON-LD công khai. KICAP mới
chỉ là nguồn dự kiến và chưa được bật.

## Pipeline

1. **Crawl:** kiểm tra `robots.txt`, giới hạn tốc độ rồi lấy HTML trang sản phẩm công khai.
2. **Parse:** trích xuất trường dữ liệu nguồn.
3. **Clean:** chuẩn hóa văn bản, giá trị và danh mục.
4. **Validate:** kiểm tra trường bắt buộc, kiểu dữ liệu và record trùng.
5. **Export:** tạo đầu ra trung gian để công cụ import riêng xử lý.

Không commit dữ liệu thật, dữ liệu thô, ảnh tải về hoặc credential. URL live chỉ được tải khi
truyền rõ `--allow-network`; mặc định CLI không thực hiện request. Xem thêm
`docs/crawler/crawler-design.md` và `docs/crawler/data-contract.md`.

## Chạy kiểm thử

```bash
cd crawler
python -m unittest discover -s tests -v
```

## Chạy bằng HTML mẫu, không dùng mạng

```bash
cd crawler
python src/main.py \
  --html-file tests/fixtures/gearvn-product.html \
  --source-url https://gearvn.com/products/ban-phim-co-e-dra-ek384
```

Kết quả nằm trong `crawler/data/output/products.jsonl` và đã được `.gitignore` loại khỏi Git.

## Chạy với URL công khai đã review

```bash
cd crawler
python src/main.py \
  --url https://gearvn.com/products/<product-slug> \
  --allow-network
```

Không dùng URL giỏ hàng, thanh toán, tài khoản, API nội bộ hoặc URL ngoài host đã cho phép.
