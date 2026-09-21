# Hợp đồng endpoint

Tài liệu này là nơi ghi hợp đồng HTTP trước khi triển khai endpoint.

Mỗi endpoint phải nêu method, path, quyền truy cập, request, response, mã lỗi, phân trang và
ví dụ. Nhóm public dự kiến gồm sản phẩm, danh mục, thương hiệu và cửa hàng. Nhóm cần bảo vệ
gồm giỏ hàng, wishlist, checkout, đơn hàng, đánh giá, người dùng và quản trị. Endpoint admin
phải yêu cầu role `ADMIN`.

Chưa có endpoint nghiệp vụ nào trong commit scaffold.
