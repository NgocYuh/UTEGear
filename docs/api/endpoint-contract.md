# Hợp đồng endpoint và trạng thái triển khai

Tài liệu này là mục lục API của UTEGear sau lượt rà soát tích hợp quản trị. Các bảng "Đã triển khai" được đối chiếu
trực tiếp với controller trong `src/main/java/com/utegear/controller`. Các contract miền vẫn là nguồn
chi tiết cho request, response, mã lỗi và mock data; endpoint ghi "chưa triển khai" không được mô tả
như chức năng đang chạy.

## Quy ước chung

- REST base path: `/api`.
- JSON dùng `application/json`; media/import dùng `multipart/form-data`.
- Thời gian dùng ISO 8601 kèm offset; tiền dùng JSON number và `BigDecimal` ở Backend.
- Collection rỗng trả `[]`; `optionValues` rỗng trả `{}`.
- Phân trang bắt đầu từ `page=0`, `size` mặc định 20 và tối đa 100 khi contract miền không ghi khác.
- JWT gửi qua `Authorization: Bearer <token>`, không đặt trong query string.
- `PUBLIC` không cần JWT; `AUTHENTICATED` cần JWT hợp lệ; `ADMIN` cần role `ADMIN`.
- API không trả entity JPA trực tiếp.

Response lỗi dùng một cấu trúc thống nhất:

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

`fieldErrors` chỉ có với lỗi validation theo field. `401` dùng cho thiếu/sai/hết hạn JWT; `403` dùng
cho tài khoản đã xác thực nhưng thiếu role hoặc không có quyền truy cập tài nguyên.

## REST API đã triển khai

### Auth, profile và catalog

| Method | Path | Quyền | Kết quả chính |
|---|---|---|---|
| POST | `/api/auth/register` | PUBLIC | `201 UserProfile` |
| POST | `/api/auth/login` | PUBLIC | `200 AuthSession` |
| POST | `/api/auth/logout` | AUTHENTICATED | `204`; client xóa JWT |
| GET | `/api/profile` | AUTHENTICATED | `200 UserProfile` |
| PATCH | `/api/profile` | AUTHENTICATED | `200 UserProfile` |
| PUT | `/api/profile/password` | AUTHENTICATED | `204` |
| GET | `/api/categories` | PUBLIC | `200 CategorySummary[]` |
| GET | `/api/brands` | PUBLIC | `200 BrandSummary[]` |
| GET | `/api/products` | PUBLIC | `200 ProductPage` |
| GET | `/api/products/{productId}` | PUBLIC | `200 ProductDetail` |

Chi tiết filter, DTO và lỗi: [`auth-catalog-contract.md`](auth-catalog-contract.md).

### Store và quản trị cửa hàng

| Method | Path | Quyền | Kết quả chính |
|---|---|---|---|
| GET | `/api/stores` | PUBLIC | `200 StoreSummary[]` |
| GET | `/api/stores/{storeId}` | PUBLIC | `200 StoreDetail` |
| GET | `/api/stores/{storeId}/availability?variantId=...` | PUBLIC | `200 VariantAvailability` |
| GET | `/api/products/{productId}/availability` | PUBLIC | `200 ProductAvailability` |
| GET | `/api/admin/stores` | ADMIN | `200 StorePage` |
| POST | `/api/admin/stores` | ADMIN | `201 StoreDetail` |
| GET | `/api/admin/stores/{storeId}` | ADMIN | `200 StoreDetail`, gồm cả cửa hàng tạm ngừng |
| PUT | `/api/admin/stores/{storeId}` | ADMIN | `200 StoreDetail` |
| PATCH | `/api/admin/stores/{storeId}/active` | ADMIN | `200 StoreDetail` |
| GET | `/api/admin/inventory?storeId=...` | ADMIN | `200 InventoryView[]` |
| PUT | `/api/admin/stores/{storeId}/inventory/{variantId}` | ADMIN | `200 InventoryResult` |
| POST | `/api/admin/inventory/transfers` | ADMIN | `200 InventoryTransferResult` |

Chi tiết DTO, query và lỗi: [`store-inventory-media-contract.md`](store-inventory-media-contract.md).

### Quản trị catalog, coupon, tài khoản và dashboard

| Method | Path | Quyền | Kết quả chính |
|---|---|---|---|
| GET, POST | `/api/admin/categories` | ADMIN | Danh sách hoặc `201 CategoryView` |
| PUT | `/api/admin/categories/{categoryId}` | ADMIN | `200 CategoryView` |
| GET, POST | `/api/admin/brands` | ADMIN | Danh sách hoặc `201 BrandView` |
| PUT | `/api/admin/brands/{brandId}` | ADMIN | `200 BrandView` |
| GET, POST | `/api/admin/products` | ADMIN | `200 ProductPage` hoặc `201 ProductView` |
| GET, PUT | `/api/admin/products/{productId}` | ADMIN | `200 ProductView` |
| PATCH | `/api/admin/products/{productId}/status` | ADMIN | `200 ProductView` |
| GET, POST | `/api/admin/coupons` | ADMIN | Danh sách hoặc `201 CouponView` |
| PUT | `/api/admin/coupons/{couponId}` | ADMIN | `200 CouponView` |
| GET | `/api/admin/users` | ADMIN | `200 UserView[]` |
| GET | `/api/admin/users/{userId}` | ADMIN | `200 UserView` |
| PATCH | `/api/admin/users/{userId}/enabled` | ADMIN | `200 UserView` |
| PUT | `/api/admin/users/{userId}/roles` | ADMIN | `200 UserView` |
| GET | `/api/admin/dashboard` | ADMIN | `200 DashboardSummary` |

Khi tạo sản phẩm, Backend luôn tạo một biến thể mặc định từ `defaultSku` và `optionValues`; không có
tồn kho gắn trực tiếp với product. Quản trị viên không thể tự khóa tài khoản đang dùng hoặc tự gỡ role
`ADMIN`; có thể bỏ role `CUSTOMER` nếu vẫn giữ `ADMIN`.

### Cart, wishlist và address

| Method | Path | Quyền | Kết quả chính |
|---|---|---|---|
| GET | `/api/cart` | AUTHENTICATED | `200 Cart` |
| POST | `/api/cart/items` | AUTHENTICATED | `201 Cart` |
| PUT | `/api/cart/items/{itemId}` | AUTHENTICATED | `200 Cart` |
| DELETE | `/api/cart/items/{itemId}` | AUTHENTICATED | `204` |
| DELETE | `/api/cart/items` | AUTHENTICATED | `204` |
| GET | `/api/wishlist` | AUTHENTICATED | `200 WishlistItem[]` |
| POST | `/api/wishlist/items` | AUTHENTICATED | `201 WishlistItem` |
| DELETE | `/api/wishlist/items/{productId}` | AUTHENTICATED | `204` |
| GET | `/api/addresses` | AUTHENTICATED | `200 Address[]` |
| POST | `/api/addresses` | AUTHENTICATED | `201 Address` |
| PUT | `/api/addresses/{addressId}` | AUTHENTICATED | `200 Address` |
| PUT | `/api/addresses/{addressId}/default` | AUTHENTICATED | `200 Address` |
| DELETE | `/api/addresses/{addressId}` | AUTHENTICATED | `204` |

### Checkout, order và notification

| Method | Path | Quyền | Kết quả chính |
|---|---|---|---|
| POST | `/api/checkout/quote` | AUTHENTICATED | `200 CheckoutQuote` |
| POST | `/api/orders` | AUTHENTICATED | `201 OrderDetail` |
| GET | `/api/orders` | AUTHENTICATED | `200 OrderPage` của user hiện tại |
| GET | `/api/orders/{orderId}` | AUTHENTICATED | `200 OrderDetail` nếu sở hữu |
| POST | `/api/orders/{orderId}/cancel` | AUTHENTICATED | `200 OrderDetail` |
| GET | `/api/admin/orders` | ADMIN | `200 OrderPage` |
| GET | `/api/admin/orders/{orderId}` | ADMIN | `200 OrderDetail` |
| PUT | `/api/admin/orders/{orderId}/status` | ADMIN | `200 OrderDetail` |
| GET | `/api/notifications` | AUTHENTICATED | `200 NotificationPage` |
| PUT | `/api/notifications/{notificationId}/read` | AUTHENTICATED | `200 Notification` |
| PUT | `/api/notifications/read-all` | AUTHENTICATED | `204` |

Chi tiết transition, ownership và error code:
[`customer-transaction-realtime-contract.md`](customer-transaction-realtime-contract.md).

### Media và import

| Method | Path | Quyền | Kết quả chính |
|---|---|---|---|
| POST | `/api/admin/products/{productId}/images` | ADMIN | `201 ProductImage` |
| PUT | `/api/admin/products/{productId}/images/{imageId}` | ADMIN | `200 ProductImage` |
| DELETE | `/api/admin/products/{productId}/images/{imageId}` | ADMIN | `204` |
| PUT | `/api/admin/products/{productId}/images/order` | ADMIN | `200 ProductImage[]` |
| POST | `/api/admin/products/import` | ADMIN | `200 ProductImportReport` |

Media upload/replace nhận multipart và lưu cả `secureUrl` lẫn `publicId`. Import nhận JSONL UTF-8 theo
[`data-contract.md`](../crawler/data-contract.md).
JSONL có thể gửi `description` nhiều đoạn và `specifications` nhãn → chuỗi. Response chi tiết
`GET /api/products/{id}` trả thông số ở `specifications.technicalSpecifications`; metadata crawler
ở object ngoài phục vụ truy vết, không dùng làm nội dung bảng thông số (`DATA-05-BE` / `FE-CONTENT-01`).

### Tồn kho công khai
`GET /api/products/{productId}/availability` nhận `province` tùy chọn. Bỏ trống hoặc chỉ có
khoảng trắng trả tồn kho ở tất cả cửa hàng hoạt động; không có inventory trả `totalStock: 0`
và `availableStores: []`, không trả HTTP 500 (`FIX-STOCK-API`, phụ thuộc `FE-CHECKOUT-UX`).

## WebSocket/STOMP đã triển khai

| Hành động | Destination | Quyền | Event |
|---|---|---|---|
| CONNECT | `/ws` | JWT trong STOMP header | Thiết lập principal |
| SUBSCRIBE | `/topic/admin/orders` | ADMIN | `ORDER_CREATED` |
| SUBSCRIBE | `/user/queue/orders` | AUTHENTICATED | `ORDER_STATUS_CHANGED` |
| SUBSCRIBE | `/user/queue/notifications` | AUTHENTICATED | `NOTIFICATION_CREATED` |
| SUBSCRIBE | `/user/queue/support` | AUTHENTICATED | `SUPPORT_MESSAGE_CREATED`, `SUPPORT_CONVERSATION_ASSIGNED` |

Event dùng envelope `eventId`, `type`, `occurredAt`, `data` và chỉ được publish sau commit. Destination
khác ba destination trên bị interceptor từ chối.

## Contract đã thiết kế nhưng chưa có controller

Các endpoint sau xuất hiện trong contract miền nhưng chưa được triển khai tại commit được tài liệu hóa:

| Nhóm | Endpoint chưa triển khai |
|---|---|
| Coupon | `POST /api/coupons/validate` |
| Review | `GET /api/products/{productId}/reviews`, `POST /api/reviews`, `POST /api/reviews/{reviewId}/helpful` |

Không gọi các endpoint này trong demo như chức năng hoàn tất. Nếu triển khai sau, cần task Backend,
test, cập nhật trạng thái tài liệu và JSON mẫu tương ứng. MVC `/forgot-password` cũng chỉ là shell vì
chưa có contract/API khôi phục mật khẩu.

## Model và mock data

- Model dùng chung: [`model-contract.md`](model-contract.md).
- Auth/catalog: [`auth-catalog-contract.md`](auth-catalog-contract.md), [`mock/api-01.json`](mock/api-01.json).
- Store/inventory/media: [`store-inventory-media-contract.md`](store-inventory-media-contract.md),
  [`mock/api-02.json`](mock/api-02.json).
- Transaction/realtime: [`customer-transaction-realtime-contract.md`](customer-transaction-realtime-contract.md),
  [`mock/api-03.json`](mock/api-03.json).
- Support chat: [`support-chat-contract.md`](support-chat-contract.md),
  [`mock/support-chat.json`](mock/support-chat.json).

Mock chỉ phục vụ phát triển UI; không phải dữ liệu production và không được import vào database.

## Quy tắc biến thể bắt buộc

- Catalog luôn trả ít nhất một variant cho mỗi product.
- Tồn kho dùng đồng thời `storeId + variantId`; không cập nhật chỉ bằng `productId`.
- Cart, checkout và order nhận `variantId`, kể cả sản phẩm chỉ có variant mặc định.
- Order item hiển thị snapshot lúc mua, không thay bằng dữ liệu catalog hiện tại.
