# Hợp đồng model

Tài liệu này chốt ý nghĩa và tên field dùng chung giữa Backend và Frontend. Backend (`@NgocYuh`)
viết contract; Frontend (`@trongsonho`) review dữ liệu cần hiển thị và tương tác trước khi merge.

Entity persistence không phải response API. API-01 đến API-03 đã bổ sung request, response, wrapper,
phân trang và JSON mẫu cho từng miền. Implementation không được tự đổi tên hoặc thêm field trái với
contract đã merge; trạng thái endpoint thực tế được đối chiếu tại
[`endpoint-contract.md`](endpoint-contract.md).

Mô hình bảng, khóa và constraint nằm tại
[`docs/architecture/database-model.md`](../architecture/database-model.md).

## Quy ước dữ liệu

- ID dùng số nguyên 64 bit ở Backend. Frontend không suy ra ý nghĩa từ thứ tự ID.
- Tiền dùng `BigDecimal` ở Backend. Cách biểu diễn tiền trong JSON được chốt tại API-01.
- Thời gian API dùng ISO 8601 kèm múi giờ; database lưu `timestamptz`.
- Enum dùng tên in hoa cố định. Backend phải từ chối giá trị ngoài contract.
- Collection rỗng trả `[]`, object thuộc tính biến thể rỗng trả `{}`. Không dùng `null` thay cho hai
  trạng thái này.
- Field tổng hợp phải được ghi rõ là dữ liệu tính toán, không trở thành nguồn dữ liệu mới.

## Catalog model

### Product

| Field | Kiểu logic | Bắt buộc | Ý nghĩa |
|---|---|---|---|
| `id` | integer 64 bit | Có | ID product |
| `name` | string | Có | Tên chung của sản phẩm |
| `description` | string | Không | Mô tả dài |
| `specifications` | object | Không | `technicalSpecifications` là object nhãn → chuỗi thông số; các khóa truy vết nguồn được giữ riêng trong object ngoài |
| `basePrice` | decimal | Có | Giá cơ sở, không âm |
| `salePrice` | decimal | Không | Giá khuyến mãi ở cấp product, nhỏ hơn hoặc bằng `basePrice` |
| `status` | enum | Có | `ACTIVE`, `INACTIVE` hoặc `DELETED` |
| `brandId` | integer 64 bit | Không | Brand có thể rỗng |
| `categoryId` | integer 64 bit | Có | Category bắt buộc |
| `variants` | array `ProductVariant` | Có | Luôn có ít nhất một phần tử |
| `images` | array `ProductImage` | Có | Có thể rỗng |

`Product` không có field SKU hoặc stock làm nguồn dữ liệu. SKU thuộc biến thể; tồn kho thuộc cặp cửa
hàng và biến thể. Nếu API trả `totalQuantity`, giá trị đó chỉ là tổng hợp từ tồn kho biến thể.

`OUT_OF_STOCK` không phải trạng thái product lưu trong database. Backend tính trạng thái hết hàng từ
tồn kho hiện tại để tránh mâu thuẫn giữa catalog và inventory.

### ProductVariant

| Field | Kiểu logic | Bắt buộc | Ý nghĩa |
|---|---|---|---|
| `id` | integer 64 bit | Có | ID biến thể dùng cho cart, order và inventory |
| `productId` | integer 64 bit | Có | Product sở hữu biến thể |
| `sku` | string | Có | SKU duy nhất toàn hệ thống |
| `optionValues` | object string-to-string | Có | Tổ hợp thuộc tính có thể chọn; `{}` cho biến thể không có lựa chọn |
| `priceAdjustment` | decimal | Có | Mức cộng hoặc trừ vào giá product; mặc định `0` |
| `defaultVariant` | boolean | Có | Biến thể chọn ban đầu của product |
| `active` | boolean | Có | Cho phép chọn và giao dịch biến thể |
| `image` | `MediaImage` | Không | Ảnh riêng của biến thể khi cần |

Mỗi product có đúng một `defaultVariant = true`. Product không có lựa chọn có đúng một biến thể mặc
định và `optionValues = {}`. Product có lựa chọn dùng object có thể chứa nhiều khóa, ví dụ `color`,
`switch` và `layout`; Frontend không được giả định mỗi biến thể chỉ có một thuộc tính.

Giá hiệu lực của biến thể bằng giá hiệu lực của product cộng `priceAdjustment`. API-01 sẽ chốt field
giá đã tính để Frontend không tự lặp lại quy tắc khuyến mãi.

### ProductImage và MediaImage

| Field | Kiểu logic | Bắt buộc | Ý nghĩa |
|---|---|---|---|
| `secureUrl` | string URL | Có khi có ảnh | URL HTTPS để hiển thị |
| `publicId` | string | Có ở model quản trị | Định danh Cloudinary dùng cho thay thế hoặc xóa ảnh |
| `altText` | string | Không | Nội dung thay thế cho ảnh |
| `sortOrder` | integer | Có với ảnh product | Thứ tự hiển thị, không âm |
| `primary` | boolean | Có với ảnh product | Ảnh chính; tối đa một ảnh chính cho mỗi product |

Response public có thể không trả `publicId`; response quản trị cần field này để quản lý tài nguyên.
Database luôn lưu cả `secure_url` và `public_id` cho ảnh Cloudinary do hệ thống quản lý.

## Store và inventory model

| Field | Kiểu logic | Bắt buộc | Ý nghĩa |
|---|---|---|---|
| `storeId` | integer 64 bit | Có | Cửa hàng giữ tồn kho |
| `variantId` | integer 64 bit | Có | Biến thể được lưu kho |
| `quantity` | integer | Có | Số lượng hiện có, không âm |
| `updatedAt` | datetime | Có | Thời điểm thay đổi gần nhất |

Cặp `storeId + variantId` là duy nhất. Không có model cập nhật tồn kho chỉ nhận `productId`. Nếu màn
hình cần tổng tồn cấp sản phẩm, Backend cộng số lượng của các biến thể và ghi rõ đây là field tổng hợp.

## Cart model

`CartItem` luôn có `variantId` và `quantity > 0`. Client phải gửi `variantId` kể cả khi product chỉ có
một biến thể mặc định. Response đọc có thể lồng `product` và `variant` summary để render, nhưng ID ghi
nhận giao dịch vẫn là `variantId`.

Mỗi giỏ chỉ có một dòng cho một biến thể. Thêm lại cùng `variantId` làm tăng số lượng thay vì tạo dòng
trùng.

## Order và payment model

`OrderItem` gồm hai nhóm dữ liệu:

- Tham chiếu bắt buộc: `variantId`.
- Snapshot không đổi: `productName`, `sku`, `optionValues`, `imageUrl`, `unitPrice`, `quantity` và
  `subtotal` tại thời điểm đặt hàng.

Catalog thay đổi sau đó không được làm thay đổi nội dung đơn cũ. Frontend hiển thị snapshot của
`OrderItem`, không tải lại tên, ảnh hoặc giá hiện tại để thay thế lịch sử.

`Order` có `fulfillmentStoreId`. Giá trị có thể rỗng khi đơn mới tạo và chưa phân bổ, nhưng phải có
trước khi xác nhận nhận tại cửa hàng hoặc trừ tồn kho cho giao tận nơi.

`Payment` trong phạm vi hiện tại là quan hệ tối đa một bản ghi cho mỗi order. Model gồm `method`,
`status`, `amount`, `transactionId`, `createdAt` và `paidAt`. API-03 sẽ chốt enum và JSON lỗi cụ thể.

## Bất biến Frontend phải dựa vào

- `variants` không rỗng.
- Khi `variants` chỉ có một phần tử mặc định với `optionValues = {}`, giao diện không tạo bộ chọn giả.
- Khi có nhiều biến thể, giao diện đọc tất cả khóa trong `optionValues`; không hard-code chỉ một thuộc
  tính.
- Mọi thao tác giỏ hàng, checkout và kiểm tra tồn kho dùng `variantId`.
- Tồn kho theo cửa hàng dùng đồng thời `storeId` và `variantId`.
- Lịch sử đơn hàng dùng snapshot trong `OrderItem`.
- Collection rỗng là `[]`; thuộc tính biến thể rỗng là `{}`.

## Tài liệu contract theo miền

1. [`auth-catalog-contract.md`](auth-catalog-contract.md): auth, profile và catalog, gồm product một
   biến thể mặc định và product nhiều biến thể.
2. [`store-inventory-media-contract.md`](store-inventory-media-contract.md): store, availability,
   inventory contract và media quản trị.
3. [`customer-transaction-realtime-contract.md`](customer-transaction-realtime-contract.md): cart,
   wishlist, address, checkout, order, notification, review contract và WebSocket payload.

JSON trong `docs/api/mock/` là fixture phát triển Frontend, không chứng minh endpoint đã được triển khai.
Khi contract có phạm vi dự kiến chưa có controller, `endpoint-contract.md` phải ghi rõ để demo và test
không nhầm mock với chức năng đang chạy.
