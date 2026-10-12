# Contract API-02: Store, inventory và media admin

Contract này là nguồn thống nhất cho FE-STORE-01, UI-02, INV-01 đến INV-03 và MED-01. Endpoint chưa
được triển khai trong task này. Frontend dùng [`mock/api-02.json`](mock/api-02.json).

## Store public

| Method | Path | Quyền | Request | Response |
|---|---|---|---|---|
| GET | `/api/stores` | PUBLIC | `active` mặc định `true`, `province` tùy chọn | `200 StoreSummary[]` |
| GET | `/api/stores/{storeId}` | PUBLIC | Không có | `200 StoreDetail` |
| GET | `/api/stores/{storeId}/availability` | PUBLIC | `variantId` bắt buộc | `200 VariantAvailability` |
| GET | `/api/products/{productId}/availability` | PUBLIC | `province` tùy chọn | `200 ProductAvailability` |

`StoreSummary` gồm `id`, `name`, `address`, `province`, `district`, `phone`, `email`, `imageUrl`,
`openingTime`, `closingTime`, `active`. `StoreDetail` thêm `description`, `latitude`, `longitude`.

`VariantAvailability` gồm `store`, `variantId`, `sku`, `optionValues`, `quantity`, `available`,
`updatedAt`. `available` chỉ đúng khi store hoạt động và `quantity > 0`.

`ProductAvailability` gồm `productId`, `totalStock` và `variants`. Mỗi phần tử `variants` gồm
`variantId`, `sku`, `optionValues`, `isDefault`, `totalStock`, `availableStores`. Mỗi store trong
`availableStores` có `storeId`, `storeName`, `quantity`, `active`. Mọi số lượng đều tổng hợp từ
`store_inventory`, không đọc field tồn kho trên Product.

Store không hoạt động vẫn có thể được admin đọc nhưng API public chi tiết trả `404 STORE_NOT_FOUND`.
Tra cứu availability của store vừa ngừng hoạt động trả `409 STORE_INACTIVE` để UI phân biệt với hết hàng.

## Store và inventory admin

Mọi endpoint dưới `/api/admin/**` yêu cầu role `ADMIN`.

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/api/admin/stores` | Filter `active`, phân trang | `200 StorePage` |
| POST | `/api/admin/stores` | `StoreUpsertRequest` | `201 StoreDetail` |
| PUT | `/api/admin/stores/{storeId}` | `StoreUpsertRequest` | `200 StoreDetail` |
| PATCH | `/api/admin/stores/{storeId}/active` | `{ "active": boolean }` | `200 StoreDetail` |
| GET | `/api/admin/inventory` | `storeId`, `variantId`, `lowStock`, phân trang | `200 InventoryPage` |
| PUT | `/api/admin/stores/{storeId}/inventory/{variantId}` | `{ "quantity": integer }` | `200 InventoryItem` |
| POST | `/api/admin/inventory/transfers` | `InventoryTransferRequest` | `200 InventoryTransferResult` |

`StoreUpsertRequest` gồm `name`, `address`, `province`, `district`, `phone`, `email`, `imageUrl`,
`openingTime`, `closingTime`, `description`, `latitude`, `longitude`, `active`. Giờ dùng `HH:mm`.

`InventoryItem` gồm `storeId`, `storeName`, `variantId`, `sku`, `productId`, `productName`,
`optionValues`, `quantity`, `updatedAt`. Cặp `storeId + variantId` là duy nhất. `quantity` không âm.

`InventoryTransferRequest` gồm `sourceStoreId`, `destinationStoreId`, `variantId`, `quantity` lớn hơn 0.
Hai store phải khác nhau và đang hoạt động. Result gồm request đã chuẩn hóa, `sourceQuantity`,
`destinationQuantity`, `transferredAt`. Thao tác là một transaction và tổng kho không đổi.

## Product media admin

| Method | Path | Content-Type | Request | Response |
|---|---|---|---|---|
| POST | `/api/admin/products/{productId}/images` | multipart/form-data | `file`, `altText`, `primary` | `201 ProductImage` |
| PUT | `/api/admin/products/{productId}/images/{imageId}` | multipart/form-data | `file` tùy chọn, `altText`, `primary` | `200 ProductImage` |
| DELETE | `/api/admin/products/{productId}/images/{imageId}` | Không có | Không có | `204` |
| PUT | `/api/admin/products/{productId}/images/order` | application/json | `{ "imageIds": [long] }` | `200 ProductImage[]` |

`ProductImage` gồm `id`, `productId`, `secureUrl`, `publicId`, `altText`, `primary`, `sortOrder`.
Admin response bắt buộc có cả `secureUrl` và `publicId`; public catalog không trả `publicId`.

MIME cho phép: `image/jpeg`, `image/png`, `image/webp`. Dung lượng tối đa được cấu hình phía server;
giá trị mặc định contract là 5 MiB. Upload thay thế chỉ xóa ảnh Cloudinary cũ sau khi ảnh mới đã upload và
database cập nhật thành công. CI mock Cloudinary, không upload thật.

## Product data import admin

| Method | Path | Content-Type | Request | Response |
|---|---|---|---|---|
| POST | `/api/admin/products/import` | multipart/form-data | `file`: JSONL UTF-8 theo data contract | `200 ProductImportReport` |

Mỗi dòng được import trong transaction riêng. Dòng lỗi không rollback những dòng đã thành công.
`ProductImportReport` gồm `totalRows`, `importedRows`, `failedRows`, `rows`; mỗi row có `line`,
`status` (`IMPORTED` hoặc `FAILED`), `productId`, `sourceUrl`, `message`.

Importer yêu cầu category và brand đang hoạt động đã tồn tại. Product slug và SKU trùng bị từ chối.
Ảnh nguồn phải dùng HTTPS, thuộc allowlist, có MIME JPEG/PNG/WebP và không vượt giới hạn upload.
Ảnh được đưa qua Cloudinary rồi mới lưu `secureUrl` cùng `publicId`; transaction rollback sẽ dọn
ảnh vừa upload. Endpoint không tạo tồn kho vì tồn kho thuộc cặp `Store + ProductVariant`.

## Mã lỗi

| HTTP | Code | Khi nào |
|---|---|---|
| 400 | `VALIDATION_FAILED` | Request store/inventory/media sai field |
| 400 | `INVALID_MEDIA_TYPE` | File không thuộc MIME cho phép |
| 400 | `FILE_TOO_LARGE` | File vượt giới hạn |
| 400 | `IMPORT_FILE_REQUIRED` | Thiếu file JSONL |
| 400 | `IMPORT_ROW_LIMIT_EXCEEDED` | File vượt giới hạn số dòng |
| 400 | `SKU_DUPLICATE` | SKU trùng trong file hoặc database |
| 400 | `INVALID_SOURCE_IMAGE` | URL, host, MIME hoặc dung lượng ảnh nguồn không hợp lệ |
| 401 | `UNAUTHORIZED` | Thiếu, sai hoặc hết hạn JWT |
| 403 | `FORBIDDEN` | User không có role ADMIN |
| 404 | `STORE_NOT_FOUND` | Không tìm thấy store phù hợp quyền truy cập |
| 404 | `VARIANT_NOT_FOUND` | Không tìm thấy variant |
| 404 | `PRODUCT_IMAGE_NOT_FOUND` | Không tìm thấy ảnh thuộc product |
| 409 | `STORE_INACTIVE` | Tra cứu/thao tác cần store hoạt động |
| 409 | `INSUFFICIENT_STOCK` | Store nguồn không đủ tồn kho để chuyển |
| 409 | `DUPLICATE_INVENTORY` | Vi phạm unique `storeId + variantId` |
| 502 | `MEDIA_UPLOAD_FAILED` | Cloudinary từ chối hoặc không phản hồi hợp lệ |

## Model attribute cho Thymeleaf

- Danh sách store public: `stores`, `filters`.
- Chi tiết store: `store`.
- Availability: `availability`, `selectedVariantId`.
- Admin inventory: `inventoryPage`, `stores`, `filters`.
- Admin product media: `product`, `images`.

Controller MVC dùng DTO cùng field với REST, không đưa entity JPA vào view.

## Mock và trạng thái giao diện

`mock/api-02.json` có `stores`, `storeDetail`, `defaultVariantInStock`, `selectedVariantOutOfStock`,
`inactiveStoreError`, `inventoryPage`, `mediaUploadSuccess`, `mediaUploadError` và `forbidden`.
