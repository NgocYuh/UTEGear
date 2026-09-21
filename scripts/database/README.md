# Database scripts

Thư mục này dành cho công cụ hỗ trợ database đã được review. Schema dùng chung phải được
quản lý bằng Flyway trong `src/main/resources/db/migration`; không dùng script ở đây để
thay đổi Supabase thủ công và không lưu dump chứa dữ liệu hoặc credential thật.
