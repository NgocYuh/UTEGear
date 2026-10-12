# Contract API-03: Giao dịch khách hàng và realtime

Contract này phục vụ FE-CART-01, FE-WISH-01, FE-ORD-01, UI-03, FE-WS-01 và các task Backend tương ứng.
Endpoint chưa được triển khai trong task này. Frontend dùng [`mock/api-03.json`](mock/api-03.json).

Tất cả endpoint trong tài liệu này yêu cầu JWT, trừ danh sách review sản phẩm và kiểm tra coupon được ghi
rõ là public. Tài nguyên của khách hàng luôn được xác định từ subject JWT; client không gửi `userId` để
chọn chủ sở hữu.

## Cart và wishlist

| Method | Path | Quyền | Request | Response |
|---|---|---|---|---|
| GET | `/api/cart` | AUTHENTICATED | Không có | `200 Cart` |
| POST | `/api/cart/items` | AUTHENTICATED | `{ "variantId": long, "quantity": integer }` | `201 Cart` |
| PUT | `/api/cart/items/{itemId}` | AUTHENTICATED | `{ "quantity": integer }` | `200 Cart` |
| DELETE | `/api/cart/items/{itemId}` | AUTHENTICATED | Không có | `204` |
| DELETE | `/api/cart/items` | AUTHENTICATED | Không có | `204` |
| GET | `/api/wishlist` | AUTHENTICATED | Không có | `200 WishlistItem[]` |
| POST | `/api/wishlist/items` | AUTHENTICATED | `{ "productId": long }` | `201 WishlistItem` |
| DELETE | `/api/wishlist/items/{productId}` | AUTHENTICATED | Không có | `204` |

`Cart` gồm `id`, `items`, `itemCount`, `subtotal`, `updatedAt`. `CartItem` gồm `id`, `variantId`,
`quantity`, `product`, `variant`, `unitPrice`, `lineTotal`, `availableStock`, `available`.
`product` là summary gồm `id`, `name`, `slug`, `primaryImage`; `variant` gồm `sku`, `optionValues`,
`isDefault`. Thêm lại cùng `variantId` tăng quantity của dòng hiện có. `quantity` phải lớn hơn 0.

`WishlistItem` gồm `product` theo ProductSummary rút gọn và `createdAt`. Wishlist unique theo product,
không theo variant.

## Address và coupon

| Method | Path | Quyền | Request | Response |
|---|---|---|---|---|
| GET | `/api/addresses` | AUTHENTICATED | Không có | `200 Address[]` |
| POST | `/api/addresses` | AUTHENTICATED | `AddressRequest` | `201 Address` |
| PUT | `/api/addresses/{addressId}` | AUTHENTICATED | `AddressRequest` | `200 Address` |
| DELETE | `/api/addresses/{addressId}` | AUTHENTICATED | Không có | `204` |
| PUT | `/api/addresses/{addressId}/default` | AUTHENTICATED | Không có | `200 Address` |
| POST | `/api/coupons/validate` | AUTHENTICATED | `{ "code": string, "subtotal": decimal }` | `200 CouponQuote` |

`AddressRequest/Address` gồm `id` chỉ ở response, `fullName`, `phone`, `street`, `ward`, `district`,
`province`, `label`, `default`. Mỗi user có tối đa một địa chỉ mặc định.

`CouponQuote` gồm `code`, `discountType`, `discountValue`, `discountAmount`, `minimumOrderValue`,
`subtotalAfterDiscount`, `valid`. Enum `discountType`: `PERCENTAGE`, `FIXED_AMOUNT`.

## Checkout và order

| Method | Path | Quyền | Request | Response |
|---|---|---|---|---|
| POST | `/api/checkout/quote` | AUTHENTICATED | `CheckoutQuoteRequest` | `200 CheckoutQuote` |
| POST | `/api/orders` | AUTHENTICATED | `PlaceOrderRequest` | `201 OrderDetail` |
| GET | `/api/orders` | AUTHENTICATED | `status`, phân trang | `200 OrderPage` |
| GET | `/api/orders/{orderId}` | AUTHENTICATED | Không có | `200 OrderDetail` |
| POST | `/api/orders/{orderId}/cancel` | AUTHENTICATED | `{ "reason": string }` | `200 OrderDetail` |

`CheckoutQuoteRequest` và `PlaceOrderRequest` gồm `addressId`, `fulfillmentStoreId`, `couponCode`,
`paymentMethod`, `note`, `items`. Mỗi item bắt buộc có `variantId` và `quantity`; server tải lại giá và tồn
kho, không tin giá từ client. `PlaceOrderRequest` thêm `quoteToken` ngắn hạn do quote trả về để phát hiện
quote hết hạn. Enum `paymentMethod`: `COD`, `BANK_TRANSFER`.

`CheckoutQuote` gồm `quoteToken`, `expiresAt`, `items`, `subtotal`, `discount`, `shippingFee`, `total`,
`coupon`, `fulfillmentStore`, `warnings`. `warnings` rỗng là `[]`.

`OrderSummary` gồm `id`, `orderCode`, `status`, `paymentStatus`, `itemCount`, `total`, `createdAt`.
`OrderDetail` thêm `customer`, `shippingAddress`, `fulfillmentStore`, `items`, `subtotal`, `discount`,
`shippingFee`, `couponCode`, `payment`, `note`, `updatedAt`.

Mỗi order item gồm `variantId` và snapshot bất biến: `productName`, `sku`, `optionValues`, `imageUrl`,
`unitPrice`, `quantity`, `subtotal`. UI lịch sử dùng snapshot, không tải catalog hiện tại để thay thế.

Enum `orderStatus`: `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPING`, `COMPLETED`, `CANCELLED`.
Chuyển trạng thái hợp lệ:

- `PENDING -> CONFIRMED | CANCELLED`
- `CONFIRMED -> PROCESSING | CANCELLED`
- `PROCESSING -> SHIPPING | CANCELLED`
- `SHIPPING -> COMPLETED`
- `COMPLETED` và `CANCELLED` là trạng thái cuối.

Enum `paymentStatus`: `PENDING`, `PAID`, `FAILED`, `CANCELLED`.

## Admin order

| Method | Path | Quyền | Request | Response |
|---|---|---|---|---|
| GET | `/api/admin/orders` | ADMIN | `status`, `storeId`, `q`, phân trang | `200 OrderPage` |
| GET | `/api/admin/orders/{orderId}` | ADMIN | Không có | `200 OrderDetail` |
| PUT | `/api/admin/orders/{orderId}/status` | ADMIN | `{ "status": OrderStatus }` | `200 OrderDetail` |

`q` tìm theo `orderCode`, email hoặc số điện thoại snapshot. Cập nhật sai transition trả
`409 INVALID_ORDER_TRANSITION`.

## Review và notification

| Method | Path | Quyền | Request | Response |
|---|---|---|---|---|
| GET | `/api/products/{productId}/reviews` | PUBLIC | `rating`, phân trang | `200 ReviewPage` |
| POST | `/api/reviews` | AUTHENTICATED | `ReviewRequest` | `201 Review` |
| POST | `/api/reviews/{reviewId}/helpful` | AUTHENTICATED | Không có | `200 Review` |
| GET | `/api/notifications` | AUTHENTICATED | `read`, phân trang | `200 NotificationPage` |
| PUT | `/api/notifications/{notificationId}/read` | AUTHENTICATED | Không có | `200 Notification` |
| PUT | `/api/notifications/read-all` | AUTHENTICATED | Không có | `204` |

`ReviewRequest` gồm `productId`, `rating` từ 1 đến 5, `comment`; upload ảnh review thuộc task media riêng
nếu được mở rộng. `Notification` gồm `id`, `type`, `title`, `message`, `read`, `referenceId`, `createdAt`.
User chỉ đọc/sửa notification của chính mình.

## WebSocket/STOMP

- Handshake endpoint: `/ws`.
- Client gửi JWT trong STOMP `CONNECT` header `Authorization: Bearer <token>`.
- Admin subscribe `/topic/admin/orders` để nhận `ORDER_CREATED`.
- User subscribe `/user/queue/orders` để nhận `ORDER_STATUS_CHANGED` của chính họ.
- User subscribe `/user/queue/notifications` để nhận notification của chính họ.
- Production không cho phép origin `*`.

Payload realtime dùng envelope:

```json
{
  "eventId": "evt_01J9Y6F4YQ",
  "type": "ORDER_STATUS_CHANGED",
  "occurredAt": "2026-10-11T08:30:00Z",
  "data": {
    "orderId": 9001,
    "orderCode": "UTE-20261011-0001",
    "previousStatus": "CONFIRMED",
    "status": "PROCESSING"
  }
}
```

Client bỏ qua event không đúng schema hoặc không thuộc user hiện tại. Reconnect phải tải lại REST state trước
khi áp event mới; event có thể được gửi lặp và được khử trùng bằng `eventId`.

## Mã lỗi chính

| HTTP | Code | Khi nào |
|---|---|---|
| 400 | `VALIDATION_FAILED` | Quantity, address, review hoặc request sai |
| 401 | `UNAUTHORIZED` | Thiếu, sai hoặc hết hạn JWT |
| 403 | `FORBIDDEN` | Không sở hữu tài nguyên hoặc thiếu role ADMIN |
| 404 | `CART_ITEM_NOT_FOUND` | Không có item thuộc cart hiện tại |
| 404 | `ORDER_NOT_FOUND` | Không có order thuộc phạm vi truy cập |
| 409 | `INSUFFICIENT_STOCK` | Một hoặc nhiều variant không đủ tồn kho |
| 409 | `COUPON_INVALID` | Coupon sai, hết hạn, hết lượt hoặc không đạt giá trị tối thiểu |
| 409 | `QUOTE_EXPIRED` | Quote không còn hợp lệ |
| 409 | `INVALID_ORDER_TRANSITION` | Chuyển trạng thái order không hợp lệ |
| 409 | `PAID_ORDER_CANNOT_CANCEL` | Đơn đã thanh toán cần hoàn tiền trước khi hủy |

## Model attribute cho Thymeleaf

- Cart: `cart`.
- Wishlist: `wishlistItems`.
- Checkout: `addresses`, `cart`, `quote`.
- Order list/detail: `orderPage`, `order`.
- Admin order: `orderPage`, `order`, `filters`.

Controller MVC dùng cùng DTO với REST, không trả entity JPA.

## Mock và trạng thái giao diện

`mock/api-03.json` có cart đầy/rỗng, wishlist, checkout quote, order detail/page, notification, validation,
thiếu hàng, coupon lỗi, unauthorized, forbidden và ba event STOMP. Tất cả item giao dịch đều dùng
`variantId`, kể cả biến thể mặc định.
