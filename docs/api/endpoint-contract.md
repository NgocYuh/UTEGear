# Hợp đồng endpoint

Tài liệu này là nơi ghi hợp đồng HTTP trước khi triển khai endpoint. Backend (`@NgocYuh`)
chịu trách nhiệm viết và cập nhật; Frontend (`@trongsonho`) review khả năng sử dụng trước khi
contract được merge.

Mỗi endpoint phải nêu method, path, quyền truy cập, request, response, mã lỗi, phân trang và
ví dụ. Nhóm public dự kiến gồm sản phẩm, danh mục, thương hiệu và cửa hàng. Nhóm cần bảo vệ
gồm giỏ hàng, wishlist, checkout, đơn hàng, đánh giá, người dùng và quản trị. Endpoint admin
phải yêu cầu role `ADMIN`.

Mỗi endpoint phải có ít nhất một JSON response thành công, JSON lỗi chính và dữ liệu biên cần
cho loading/empty/error state. Frontend được dùng chính các JSON này làm mock data và không
phải chờ implementation Backend hoàn tất.

## Quy tắc contract theo biến thể

- Catalog response luôn trả ít nhất một biến thể cho mỗi sản phẩm.
- JSON mẫu phải có một sản phẩm chỉ có biến thể mặc định và một sản phẩm có nhiều lựa chọn.
- API tra cứu hoặc cập nhật tồn kho dùng `storeId + variantId`. API admin không được cập nhật
  tồn kho chỉ bằng `productId`.
- Cart và checkout nhận `variantId`, kể cả khi sản phẩm chỉ có biến thể mặc định.
- Response đơn hàng phải giữ thông tin biến thể đã mua cùng snapshot tên, giá và ảnh tại thời
  điểm đặt hàng.
- API có trả mức tồn kho tổng ở cấp sản phẩm phải mô tả rõ cách tổng hợp từ các biến thể.

## Quy ước HTTP chung

- Base path của REST API là `/api`.
- Request và response dùng `application/json`, trừ upload media dùng `multipart/form-data`.
- Thời gian dùng ISO 8601 UTC, ví dụ `2026-10-11T08:30:00Z`.
- Tiền là JSON number với tối đa hai chữ số thập phân; Backend dùng `BigDecimal`.
- Collection rỗng trả `[]`, object thuộc tính biến thể rỗng trả `{}`; không dùng `null` thay cho collection.
- JWT gửi qua `Authorization: Bearer <token>`. Không đặt token trong query string.
- Endpoint phân trang nhận `page` bắt đầu từ `0`, `size` mặc định `20`, tối đa `100`, và `sort`
  theo mẫu `field,direction`.

Response thành công không bọc thêm `success` hoặc `message`. Response lỗi dùng một cấu trúc duy nhất:

```json
{
  "timestamp": "2026-10-11T08:30:00Z",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "Dữ liệu không hợp lệ",
  "path": "/api/auth/register",
  "fieldErrors": {
    "email": "Email không đúng định dạng"
  }
}
```

`fieldErrors` chỉ xuất hiện với lỗi validation theo field. `requestId` có thể được bổ sung sau để truy vết,
nhưng nếu Backend bổ sung thì phải cập nhật contract trước.

## Danh mục contract

| Task | Phạm vi | Tài liệu | Mock data |
|---|---|---|---|
| API-01 | Auth, profile, category, brand, product, search | [Auth và Catalog](auth-catalog-contract.md) | [`mock/api-01.json`](mock/api-01.json) |
| API-02 | Store, inventory, media admin | Chờ task API-02 | Chờ task API-02 |
| API-03 | Cart, wishlist, address, coupon, checkout, order, notification, realtime | Chờ task API-03 | Chờ task API-03 |

Mock là dữ liệu phát triển giao diện, không phải dữ liệu production và không được import vào database.
