# Import products

DATA-02 nhận file JSONL UTF-8 do crawler DATA-01 tạo và import qua API chỉ dành cho `ADMIN`:

```text
POST /api/admin/products/import
Content-Type: multipart/form-data
file=<products.jsonl>
```

Trước khi chạy cần:

1. Tạo sáu category theo slug trong `docs/crawler/data-contract.md` và các brand xuất hiện trong file.
2. Cấu hình Cloudinary local, không ghi credential vào repository.
3. Review `CRAWLER_IMPORT_ALLOWED_IMAGE_HOSTS`; mặc định chỉ có host ảnh đã duyệt.
4. Dùng file trong `crawler/data/output/`, vốn đã bị Git ignore.

Importer phát hiện product slug/SKU trùng thay vì ghi đè. Mỗi dòng có trạng thái `IMPORTED` hoặc
`FAILED`; dòng thất bại không hủy các dòng đã commit. Không chạy file dữ liệu thật trực tiếp vào
Supabase production trước khi xem báo cáo trên môi trường local/test.
