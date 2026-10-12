# Nhật ký tiến độ Backend

Người phụ trách: `@NgocYuh`

Cập nhật cùng một mục khi task đổi trạng thái. Task `BLOCKED` phải ghi phần còn thiếu; task
`DONE` phải có Pull Request, ngày hoàn tất và kết quả kiểm thử.

## FND-01: Khởi tạo project scaffold

- Trạng thái: `DONE`
- Branch: `chore/project-scaffold`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/1
- Merge commit: `707e7099e55e9dc7687ac844c3ec9f3d0e6d8271`
- Ngày cập nhật: 2026-09-21
- Ngày hoàn tất: 2026-09-21
- Đã làm:
  - Khởi tạo Spring Boot/Maven scaffold và cây thư mục dự án.
  - Thêm Maven Wrapper, CI và tài liệu nền.
- Kết quả kiểm thử:
  - `mvnw.cmd -B clean verify`: pass cho scaffold.
  - Commit scaffold đã được merge vào `main` qua PR #1.
- Vướng mắc hoặc phần còn thiếu:
  - Không có blocker trong phạm vi FND-01.
  - Nhánh remote `chore/project-scaffold` vẫn còn; cần xóa sau khi nhóm xác nhận không dùng lại.
- Bước tiếp theo:
  - Backend thực hiện `FND-02`; Frontend có thể thực hiện `FND-04` song song.

## FND-02: Cấu hình môi trường và profile

- Trạng thái: `DONE`
- Branch: `chore/application-profiles`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/2
- Merge commit: `0940e852745e8c3f63080dc2962f49a86780c874`
- Ngày cập nhật: 2026-10-10
- Ngày hoàn tất: 2026-10-10
- Đã làm:
  - Rà soát cấu hình chung, profile test, file ví dụ local và hướng dẫn phát triển cục bộ.
  - Tạo branch từ `origin/main` sau khi cập nhật remote và giữ nguyên các thay đổi tài liệu đã duyệt.
  - Tách cấu hình dùng chung khỏi credential của profile `local` và `prod`.
  - Cấu hình `prod` chỉ đọc Supabase, JWT và Cloudinary từ biến môi trường.
  - Thêm kiểm thử xác nhận profile `test` khởi động với H2 trong bộ nhớ.
  - Đã push branch lên `origin/chore/application-profiles`; commit triển khai: `ee658f2`.
- Kết quả kiểm thử:
  - `mvnw.cmd -B clean verify`: `PASS` với 1 test, 0 failure, 0 error, 0 skipped.
  - `ApplicationProfilesTest`: `PASS`; profile `test` dùng JDBC H2 trong bộ nhớ.
  - Môi trường kiểm thử dùng JDK 26 và Maven biên dịch với `release 21`.
  - [Backend CI của PR #2](https://github.com/NgocYuh/UTEGear/actions/runs/38065901846): `PASS` với Java 21.
- Vướng mắc hoặc phần còn thiếu:
  - Không có blocker trong phạm vi FND-02.
- Bước tiếp theo:
  - Backend thực hiện `FND-03`; Frontend có thể tiếp tục `FND-04` độc lập.

## FND-03: CI và Pull Request workflow

- Trạng thái: `DONE`
- Branch: `ci/backend-quality-gate`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/3
- Merge commit: `0ae22f7aa94361b4f76c9943c6e48be84c201643`
- Ngày cập nhật: 2026-10-11
- Ngày hoàn tất: 2026-10-11
- Đã làm:
  - Xác nhận workflow hiện có đã chạy `clean verify` bằng Java 21 và Maven cache trên PR #2.
  - Bổ sung giới hạn thời gian, concurrency và quyền checkout chỉ đọc cho Backend CI.
  - Merge branch vào `main` qua PR #3.
- Kết quả kiểm thử:
  - `mvnw.cmd -B --no-transfer-progress clean verify`: `PASS` với 1 test, 0 failure, 0 error, 0 skipped.
  - Parse YAML cục bộ cho `backend-ci.yml`: `PASS`.
  - [Maven verify (Java 21)](https://github.com/NgocYuh/UTEGear/actions/runs/38068623890/job/114261280983):
    `PASS` trên commit `ecb811f` của PR #3.
- Vướng mắc hoặc phần còn thiếu:
  - Không có blocker trong phạm vi FND-03.
- Bước tiếp theo:
  - Backend thực hiện DB-01; Frontend có thể tiếp tục FND-04 độc lập.

## DB-01: Chốt ERD và model contract

- Trạng thái: `DONE`
- Branch: `docs/database-model`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/4
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Đối chiếu toàn bộ entity của dự án cũ, tập trung vào user, catalog, variant, store, inventory,
    cart, order, payment và media.
  - Chốt ERD PostgreSQL, danh mục bảng, khóa chính, khóa ngoại, unique constraint, check constraint,
    quy tắc xóa và danh sách index dự kiến cho DB-02.
  - Chốt tồn kho chỉ theo `store_id + variant_id`; loại `Product.stock`, `ProductVariant.stock` và
    `StoreInventory.product_id` khỏi mô hình mục tiêu.
  - Chốt mỗi product có đúng một biến thể mặc định; product không có lựa chọn dùng một biến thể với
    `optionValues = {}`.
  - Chốt cart item và order item bắt buộc dùng `variantId`; order item giữ snapshot lịch sử.
  - Cập nhật model contract để Frontend review field và hành vi trước API-01.
  - Theo quyết định của nhóm, bỏ Pull Request template và dùng progress log làm nơi báo cáo duy nhất
    cho cả Backend và Frontend.
- Kết quả kiểm thử:
  - Kiểm tra 2 liên kết Markdown tương đối: `PASS`.
  - Kiểm tra ERD: `PASS` với 21 bảng dự kiến và đủ invariant cho variant, inventory, cart, order,
    payment và media.
  - `mvnw.cmd -B --no-transfer-progress clean verify`: `PASS` với 1 test, 0 failure, 0 error,
    0 skipped.
  - `git diff --check`: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Không còn blocker; PR #4 đã merge vào `main`.
- Bước tiếp theo:
  - Thực hiện DB-02 từ ERD đã duyệt.
- Ngày hoàn tất: 2026-10-11

## API-01: Contract nền, Auth và Catalog

- Trạng thái: `DONE`
- Branch: `docs/api-contract-baseline`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/6
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Chốt quy ước HTTP, lỗi, JWT, thời gian, tiền và phân trang dùng chung.
  - Chốt endpoint, request, response, quyền và mã lỗi cho auth, profile, category, brand và catalog.
  - Chốt DTO product luôn có `variants`, bao gồm product một biến thể mặc định và product nhiều biến thể.
  - Thêm JSON mock hợp lệ cho success, empty, validation, unauthorized và forbidden để Frontend làm song song.
  - Đối chiếu controller/entity cũ nhưng sửa contract theo DB-01, không giữ SKU hoặc tồn kho ở Product.
- Kết quả kiểm thử:
  - Parse toàn bộ JSON trong `docs/api/mock`: `PASS`.
  - Kiểm tra link Markdown tương đối: `PASS`.
  - `mvnw.cmd -B --no-transfer-progress clean verify`: `CHƯA CHẠY` trong checkout này; chờ Backend CI
    xác nhận vì lần gọi đầu tiên chạy sai working directory và đã được dừng.
  - `git diff --check`: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Không còn blocker; PR #6 đã merge vào `main`.
- Bước tiếp theo:
  - Frontend có thể làm UI-01, FE-AUTH-01 và FE-CAT-01 từ mock API-01.
- Ngày hoàn tất: 2026-10-11

## API-02: Contract Store, Inventory và media admin

- Trạng thái: `DONE`
- Branch: `docs/store-inventory-contract`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/7
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Chốt API store public, availability theo `storeId + variantId` và tổng tồn cấp product.
  - Chốt API admin cho store, inventory, điều chuyển kho và product media.
  - Chốt media admin trả cả `secureUrl` và `publicId`; catalog public không lộ `publicId`.
  - Thêm JSON mock cho còn hàng, hết hàng, store ngừng hoạt động, inventory và lỗi upload.
  - Ghi model attribute cho trang Thymeleaf dùng cùng DTO với REST.
- Kết quả kiểm thử:
  - Parse toàn bộ JSON trong `docs/api/mock`: `PASS`.
  - Kiểm tra link Markdown tương đối: `PASS`.
  - `git diff --check`: `PASS`.
  - Backend CI: xem kết quả kiểm tra của PR #7.
- Vướng mắc hoặc phần còn thiếu:
  - Không còn blocker; PR #7 đã merge vào `main`.
- Bước tiếp theo:
  - Frontend có thể dùng mock API-02 cho FE-STORE-01 và UI-02.
- Ngày hoàn tất: 2026-10-11

## API-03: Contract Cart, Wishlist, Order và realtime

- Trạng thái: `DONE`
- Branch: `docs/customer-transaction-contract`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/8
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Chốt API cart, wishlist, address, coupon, checkout, order, admin order, review và notification.
  - Mọi cart/checkout/order item dùng `variantId`; order response giữ snapshot biến thể và giá.
  - Chốt enum và transition order/payment, quyền sở hữu tài nguyên và nhóm mã lỗi giao dịch.
  - Chốt STOMP endpoint, destination theo user/admin, envelope event, reconnect và khử trùng event.
  - Thêm JSON mock cho full/empty, validation, thiếu hàng, coupon lỗi, unauthorized/forbidden và realtime.
- Kết quả kiểm thử:
  - Parse toàn bộ JSON trong `docs/api/mock`: `PASS`.
  - Kiểm tra link Markdown tương đối: `PASS`.
  - `git diff --check`: `PASS`.
  - Backend CI: xem kết quả kiểm tra của PR #8.
- Vướng mắc hoặc phần còn thiếu:
  - Không còn blocker; PR #8 đã merge vào `main`.
- Bước tiếp theo:
  - Các luồng BE/FE phụ thuộc API-03 có thể bắt đầu song song.
- Ngày hoàn tất: 2026-10-11

## DB-02: Flyway migration khởi tạo

- Trạng thái: `DONE`
- Branch: `feat/initial-database-migration`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/9
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Đối chiếu 21 bảng trong ERD với entity nguồn và ba contract API đã merge.
  - Tạo `V1__create_core_schema.sql` cho PostgreSQL với khóa, ràng buộc, index và JSONB.
  - Chỉ lưu tồn kho tại `store_inventory` theo unique `store_id + variant_id`.
  - Không tạo SKU hoặc tồn kho trên Product; cart item và order item bắt buộc tham chiếu variant.
  - Lưu `secure_url` và `public_id` cho product/review image do Cloudinary quản lý.
- Kết quả kiểm thử:
  - Kiểm tra tĩnh: `PASS` với đúng 21 bảng và đủ invariant chính của DB-01.
  - Flyway trên PostgreSQL 16.15 trống: `PASS`, áp dụng đúng một migration và ghi version `1` thành công.
  - Khởi động lần hai trên cùng database: `PASS`, Flyway báo schema ở version 1 và không cần migration.
  - Metadata PostgreSQL: `PASS`; `store_inventory` chỉ có `store_id + variant_id`, cart/order item có
    `variant_id NOT NULL`, và 5 index trọng yếu đã tồn tại.
  - `mvnw.cmd -B --no-transfer-progress clean verify`: `PASS` với 1 test, 0 failure, 0 error, 0 skipped.
  - `git diff --check`: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Không còn blocker; PR #9 đã merge vào `main`.
- Bước tiếp theo:
  - Thực hiện DB-03 bằng migration mới, không sửa V1.
- Ngày hoàn tất: 2026-10-11

## DB-03: Seed dữ liệu tham chiếu tối thiểu

- Trạng thái: `DONE`
- Branch: `feat/reference-data-migration`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/10
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Tạo `V2__seed_reference_data.sql` nối tiếp V1.
  - Chỉ seed hai role theo contract: `ADMIN` và `CUSTOMER`.
  - Dùng `ON CONFLICT (name) DO NOTHING` để không tạo role trùng.
  - Không tạo tài khoản admin, mật khẩu, category hoặc dữ liệu sản phẩm giả.
- Kết quả kiểm thử:
  - Migration V1 + V2 trên PostgreSQL 16.15 trống: `PASS`; Flyway áp dụng tuần tự version `1` và `2`.
  - Khởi động lần hai: `PASS`; Flyway báo schema ở version `2`, không chạy lại migration và không tạo role trùng.
  - Dữ liệu tham chiếu: `PASS`; bảng `roles` có đúng `ADMIN` và `CUSTOMER`, tổng cộng 2 dòng.
  - `flyway_schema_history`: `PASS`; có hai version thành công là `1` và `2`.
  - `mvnw.cmd -B --no-transfer-progress clean verify`: `PASS` với 1 test, 0 failure, 0 error, 0 skipped.
  - `git diff --check`: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Không còn blocker; PR #10 đã merge vào `main`.
- Bước tiếp theo:
  - AUTH-01 và CAT-01 có thể bắt đầu trên các branch riêng.
- Ngày hoàn tất: 2026-10-11

## AUTH-01: User, Role và repository

- Trạng thái: `DONE`
- Branch: `feat/user-role-domain`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/11
- Merge commit: `94fcabc545642d601ad37c55a79fa450439da35e`
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Tạo entity `User` và `Role` khớp các bảng `users`, `roles` và `user_roles` của Flyway V1.
  - Dùng đúng role `ADMIN` và `CUSTOMER` đã seed trong V2.
  - Tạo repository tra cứu email không phân biệt hoa thường và kiểm tra email/phone trùng.
  - Không chuyển các quan hệ cart, order hoặc address từ dự án cũ vào trước task tương ứng.
- Kết quả kiểm thử:
  - Mapping User-Role và audit timestamp: `PASS`.
  - Unique constraint email và phone: `PASS`.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 4 test, 0 failure, 0 error, 0 skipped.
  - `git diff --check`: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Không có blocker; PR #11 đã merge vào `main`.
- Bước tiếp theo:
  - AUTH-02 đã hoàn tất.
- Ngày hoàn tất: 2026-10-11

## AUTH-02: Đăng ký và mã hóa mật khẩu

- Trạng thái: `DONE`
- Branch: `feat/user-registration`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/12
- Merge commit: `3050bbe278f415dffdb48466d9f74ca402ddf5db`
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Thêm `POST /api/auth/register` với DTO validation đúng API-01.
  - Chuẩn hóa email về chữ thường, chuẩn hóa phone rỗng về `null` và gán role `CUSTOMER` đã seed.
  - Mã hóa mật khẩu bằng BCrypt; response chỉ trả `UserProfile`, không lộ password hash.
  - Trả mã lỗi contract `EMAIL_ALREADY_EXISTS`, `PHONE_ALREADY_EXISTS` và `VALIDATION_FAILED`.
- Kết quả kiểm thử:
  - Đăng ký thành công, email được chuẩn hóa, mật khẩu được hash và role được gán: `PASS`.
  - Email trùng và phone trùng: `PASS`.
  - Toàn bộ regression test: `PASS` với 7 test, 0 failure, 0 error, 0 skipped.
  - `git diff --check`: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Không có blocker; PR #12 đã merge vào `main` sau AUTH-01.
- Bước tiếp theo:
  - AUTH-03 đã hoàn tất.
- Ngày hoàn tất: 2026-10-11

## AUTH-03: JWT login và API security

- Trạng thái: `DONE`
- Branch: `feat/jwt-authentication`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/13
- Merge commit: `4ff0d53e7b59633ef143298d6f37240429069bec`
- Ngày cập nhật: 2026-10-11
- Đã làm:
  - Thêm login trả `AuthSession` đúng contract, gồm access token Bearer, hạn token và `UserProfile`.
  - Tạo/kiểm tra JWT HMAC, JWT filter và xác thực stateless bằng Spring Security.
  - Phân tách endpoint auth/catalog public, API authenticated và `/api/admin/**` chỉ dành cho role `ADMIN`.
  - Trả JSON lỗi thống nhất cho `UNAUTHORIZED`, `FORBIDDEN`, `INVALID_CREDENTIALS` và `ACCOUNT_DISABLED`.
  - Thêm `GET /api/profile` và logout stateless; không sinh refresh token ngoài contract.
- Kết quả kiểm thử:
  - JWT hợp lệ, sai chữ ký và hết hạn: `PASS`.
  - Login thành công, sai thông tin đăng nhập và tài khoản bị vô hiệu hóa: `PASS`.
  - Public/authenticated/admin, thiếu token, token sai và token hết hạn qua MockMvc: `PASS`.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 21 test, 0 failure, 0 error, 0 skipped.
  - `git diff --check`: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Không có blocker; PR #13 đã merge vào `main` sau AUTH-02.
- Bước tiếp theo:
  - Các task phụ thuộc AUTH-03 có thể triển khai.
- Ngày hoàn tất: 2026-10-11

## CAT-01: Brand và Category

- Trạng thái: `DONE`
- Branch: `feat/brand-category-domain`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/16
- Merge commit: `bb445a6123ba72670f6e4971a3beb653e30bcb7c`
- Ngày cập nhật: 2026-10-11
- Đã làm: entity, repository, DTO, service và chuẩn hóa slug cho Brand/Category theo schema V1.
- Kết quả kiểm thử: `mvn -B --no-transfer-progress clean verify` PASS với 22 test; `git diff --check` PASS.
- Vướng mắc hoặc phần còn thiếu: Không có blocker; commit CAT-01 vào `main` trong PR #16 do chuỗi branch CAT xếp tầng.
- Bước tiếp theo: CAT-02 đã hoàn tất.
- Ngày hoàn tất: 2026-10-11

## CAT-02: Product, Variant và ProductImage

- Trạng thái: `DONE`
- Branch: `feat/product-domain`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/16
- Merge commit: `bb445a6123ba72670f6e4971a3beb653e30bcb7c`
- Ngày cập nhật: 2026-10-11
- Đã làm: mapping Product/Variant/Image theo V1; SKU và option thuộc variant; ảnh giữ `secureUrl/publicId`; kiểm tra giá và đúng một default variant.
- Kết quả kiểm thử: `mvn -B --no-transfer-progress clean verify` PASS với 26 test; `git diff --check` PASS.
- Vướng mắc hoặc phần còn thiếu: Không có blocker; commit CAT-02 vào `main` trong PR #16 do chuỗi branch CAT xếp tầng.
- Bước tiếp theo: CAT-03 đã hoàn tất.
- Ngày hoàn tất: 2026-10-11

## CAT-03: Public catalog API

- Trạng thái: `DONE`
- Branch: `feat/public-product-api`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/16
- Merge commit: `bb445a6123ba72670f6e4971a3beb653e30bcb7c`
- Ngày cập nhật: 2026-10-11
- Đã làm: API public category/brand/product; tìm kiếm, filter, khoảng giá, pagination, sort và DTO summary/detail có variants.
- Kết quả kiểm thử: MockMvc empty/pagination, filter lỗi và 404 PASS; `mvn -B --no-transfer-progress clean verify` PASS với 29 test; `git diff --check` PASS.
- Vướng mắc hoặc phần còn thiếu: Tại thời điểm CAT-03, tổng tồn kho tạm giữ 0; INV-02 đã thay bằng dữ liệu tổng hợp thực.
- Bước tiếp theo: Catalog đã được nối với inventory trong INV-02.
- Ngày hoàn tất: 2026-10-11

## INV-01: Store domain và admin API

- Trạng thái: `DONE`
- Branch: `feat/store-management`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/17
- Merge commit: `fc65f71d4a6ea88dcbb3b773886a279241d2c408`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Thêm entity và repository Store khớp bảng `stores` của Flyway V1.
  - Thêm DTO riêng cho request/response; không trả trực tiếp entity JPA.
  - Thêm API admin liệt kê có lọc/phân trang, tạo, cập nhật và bật/tắt cửa hàng theo API-02.
  - Chuẩn hóa field tùy chọn; kiểm tra dữ liệu bắt buộc, email, tọa độ, giờ hoạt động và số điện thoại trùng.
  - Giữ toàn bộ endpoint dưới `/api/admin/**` cho role `ADMIN` theo SecurityConfig hiện có.
- Kết quả kiểm thử:
  - Service tạo, cập nhật, bật/tắt, trùng số điện thoại, không tìm thấy và giờ hoạt động sai: `PASS`.
  - MockMvc request hợp lệ và validation lỗi: `PASS`.
  - Security chặn role `CUSTOMER` và cho phép role `ADMIN` tạo cửa hàng: `PASS`.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 39 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - API public và availability thuộc INV-03; tồn kho theo biến thể thuộc INV-02, không đưa vào INV-01.
  - Kiểm thử dùng H2 và mock repository; chưa kết nối Supabase thật trong task này.
- Bước tiếp theo:
  - Thực hiện INV-02.
- Ngày hoàn tất: 2026-10-12

## INV-02: Inventory domain

- Trạng thái: `IN REVIEW`
- Branch: `feat/store-inventory`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Thêm entity và repository tồn kho theo đúng cặp duy nhất `Store + ProductVariant` của Flyway V1.
  - Thêm nghiệp vụ nhập kho, giảm kho và điều chuyển kho trong transaction.
  - Khóa ghi store và dòng tồn kho theo thứ tự ổn định trước khi cập nhật để hạn chế mất cập nhật và deadlock.
  - Chặn số lượng không dương, cửa hàng ngừng hoạt động, biến thể không tồn tại và tồn kho âm.
  - Điều chuyển bảo toàn tổng tồn; tự tạo dòng tồn kho tại cửa hàng đích khi chưa có.
  - Catalog public tính `totalStock` và `inStock` từ tồn kho của các biến thể tại cửa hàng đang hoạt động,
    thay cho giá trị 0 tạm thời của CAT-03.
- Kết quả kiểm thử:
  - Mapping unique `store_id + variant_id` và invariant không âm: `PASS`.
  - Nhập kho mới, nhập bổ sung và giảm kho: `PASS`.
  - Điều chuyển thành công, tự tạo tồn kho đích, thiếu hàng và bảo toàn tổng kho: `PASS`.
  - Tổng tồn cấp sản phẩm từ nhiều biến thể: `PASS`.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 50 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - INV-02 tập trung domain/service; endpoint tra cứu availability thuộc INV-03 theo kế hoạch.
  - Kiểm thử dùng H2 cho application context và mock repository cho nghiệp vụ; chưa kết nối Supabase thật.
- Bước tiếp theo:
  - Mở và merge PR INV-02; sau đó thực hiện INV-03.
- Ngày hoàn tất: `Chưa hoàn tất`

## Mẫu mục mới

```md
## TASK-ID: Tên task

- Trạng thái: `IN PROGRESS | IN REVIEW | BLOCKED | DONE`
- Branch: `type/ten-branch`
- Pull Request: URL hoặc `Chưa mở`
- Ngày cập nhật: YYYY-MM-DD
- Đã làm:
  - ...
- Kết quả kiểm thử:
  - Lệnh hoặc trường hợp kiểm thử: `PASS | FAIL | CHƯA CHẠY`
- Vướng mắc hoặc phần còn thiếu:
  - Không có, hoặc ghi rõ phần đang thiếu và task phụ thuộc.
- Bước tiếp theo:
  - ...
- Ngày hoàn tất: YYYY-MM-DD hoặc `Chưa hoàn tất`
```
