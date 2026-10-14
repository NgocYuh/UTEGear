# Contract API-01: Auth, profile và catalog

Contract này là nguồn thống nhất cho các task UI-01, FE-AUTH-01 và FE-CAT-01. Frontend có thể dùng
[`mock/api-01.json`](mock/api-01.json) khi phát triển độc lập; các endpoint trong bảng đã có ở Backend.

## Quyền truy cập

- `PUBLIC`: không cần JWT.
- `AUTHENTICATED`: JWT hợp lệ của user đang hoạt động.
- `ADMIN`: JWT hợp lệ và có role `ADMIN`.

`401 UNAUTHORIZED` dùng khi thiếu, sai hoặc hết hạn token. `403 FORBIDDEN` dùng khi đã xác thực nhưng
không có role phù hợp.

## Endpoint Auth và profile

| Method | Path | Quyền | Request | Response |
|---|---|---|---|---|
| POST | `/api/auth/register` | PUBLIC | `RegisterRequest` | `201 UserProfile` |
| POST | `/api/auth/login` | PUBLIC | `LoginRequest` | `200 AuthSession` |
| POST | `/api/auth/logout` | AUTHENTICATED | Không có body | `204` |
| GET | `/api/profile` | AUTHENTICATED | Không có | `200 UserProfile` |
| PATCH | `/api/profile` | AUTHENTICATED | `UpdateProfileRequest` | `200 UserProfile` |
| PUT | `/api/profile/password` | AUTHENTICATED | `ChangePasswordRequest` | `204` |

### Model request/response

`RegisterRequest`:

| Field | Kiểu | Quy tắc |
|---|---|---|
| `fullName` | string | Bắt buộc, 2-100 ký tự |
| `email` | string | Bắt buộc, email hợp lệ, tối đa 100 ký tự |
| `phone` | string | Tùy chọn, 10-20 ký tự, duy nhất khi có |
| `password` | string | Bắt buộc, 8-72 ký tự |

`LoginRequest` gồm `email` và `password`, đều bắt buộc. `AuthSession` gồm `accessToken`, `tokenType`
luôn là `Bearer`, `expiresAt` và `user`. Không trả password, password hash hoặc refresh token giả.

`UserProfile` gồm `id`, `fullName`, `email`, `phone`, `avatarUrl`, `enabled`, `roles`, `createdAt`.
`UpdateProfileRequest` chỉ nhận `fullName` và `phone`; email không đổi qua endpoint này.
`ChangePasswordRequest` gồm `currentPassword`, `newPassword` và `confirmPassword`.

### Lỗi Auth và profile

| HTTP | Code | Khi nào |
|---|---|---|
| 400 | `VALIDATION_FAILED` | Field sai định dạng hoặc password xác nhận không khớp |
| 400 | `CURRENT_PASSWORD_INVALID` | Mật khẩu hiện tại không đúng |
| 400 | `PASSWORD_UNCHANGED` | Mật khẩu mới trùng mật khẩu hiện tại |
| 401 | `INVALID_CREDENTIALS` | Email hoặc password sai |
| 401 | `UNAUTHORIZED` | Thiếu, sai hoặc hết hạn JWT |
| 403 | `ACCOUNT_DISABLED` | Tài khoản đã bị vô hiệu hóa |
| 409 | `EMAIL_ALREADY_EXISTS` | Email đăng ký đã tồn tại |
| 409 | `PHONE_ALREADY_EXISTS` | Số điện thoại đã tồn tại |

## Endpoint catalog public

| Method | Path | Quyền | Query | Response |
|---|---|---|---|---|
| GET | `/api/categories` | PUBLIC | Không có | `200 CategorySummary[]` |
| GET | `/api/brands` | PUBLIC | Không có | `200 BrandSummary[]` |
| GET | `/api/products` | PUBLIC | Bộ lọc và phân trang | `200 ProductPage` |
| GET | `/api/products/{productId}` | PUBLIC | Không có | `200 ProductDetail` |

`GET /api/products` nhận:

| Query | Kiểu | Ý nghĩa |
|---|---|---|
| `q` | string | Tìm không phân biệt hoa thường theo tên sản phẩm |
| `categoryId` | long | Lọc một danh mục |
| `brandId` | long | Lọc một thương hiệu |
| `minPrice` | decimal | Giá hiệu lực tối thiểu, không âm |
| `maxPrice` | decimal | Giá hiệu lực tối đa, không nhỏ hơn `minPrice` |
| `page` | integer | Trang bắt đầu từ 0 |
| `size` | integer | 1-100, mặc định 20 |
| `sort` | string | `name`, `effectivePrice` hoặc `createdAt` với `asc`/`desc` |

`ProductPage` gồm `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`, `empty`.
Trang không có kết quả vẫn trả `200` với `content = []`, `totalElements = 0`, `totalPages = 0`,
`first = true`, `last = true`, `empty = true`.

### Model catalog

`CategorySummary`: `id`, `name`, `slug`, `sortOrder`. `BrandSummary`: `id`, `name`, `slug`.

`ProductSummary` gồm:

- `id`, `name`, `slug`, `category`, `brand`.
- `basePrice`, `salePrice`, `effectivePrice`.
- `primaryImage` gồm `secureUrl` và `altText`, hoặc `null` nếu chưa có ảnh.
- `defaultVariantId`, `variantCount`, `totalStock`, `inStock`.
- `variants` luôn có ít nhất một `ProductVariantSummary`.

`ProductDetail` có tất cả field của summary và thêm `description`, `specifications`, `images`,
`createdAt`, `updatedAt`. `images` gồm `id`, `secureUrl`, `altText`, `primary`, `sortOrder`.

`ProductVariantSummary` gồm `id`, `sku`, `optionValues`, `isDefault`, `priceAdjustment`,
`effectivePrice`, `totalStock`, `inStock`. Product không có lựa chọn dùng đúng một variant mặc định với
`optionValues = {}`. Product nhiều lựa chọn dùng toàn bộ khóa trong `optionValues`; Frontend không hard-code
chỉ `color`, `switch` hoặc `layout`.

`totalStock` ở product là tổng `store_inventory.quantity` của mọi variant thuộc product. `totalStock` ở
variant là tổng quantity của variant đó trên mọi cửa hàng đang hoạt động. Đây là field tổng hợp chỉ đọc.

### Lỗi catalog

| HTTP | Code | Khi nào |
|---|---|---|
| 400 | `INVALID_FILTER` | Giá hoặc tham số phân trang/sort không hợp lệ |
| 404 | `PRODUCT_NOT_FOUND` | Không có product hoạt động theo `productId` |

Category và brand rỗng trả `[]`, không trả 404. Product bị vô hiệu hóa không xuất hiện ở API public.

## Model attribute cho Thymeleaf

Khi controller server-side render dùng cùng dữ liệu, tên attribute là:

- Trang catalog: `products`, `page`, `categories`, `brands`, `filters`.
- Trang chi tiết: `product`.
- Trang profile: `profile`.

Object trong attribute phải dùng cùng DTO và field với REST response, không đưa entity JPA vào view.

## Mock và trạng thái giao diện

File `mock/api-01.json` có các key `loginSuccess`, `profile`, `categories`, `brands`, `productsPage`,
`productsEmpty`, `productDetail`, `validationError`, `unauthorized` và `forbidden`. Frontend có đủ dữ liệu
cho loading do client tự quản lý, còn empty/error lấy trực tiếp từ các payload này.
