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

- Trạng thái: `DONE`
- Branch: `feat/store-inventory`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/19
- Merge commit: `1b68983ac491ba133e6309ba2a97d209ea404184`
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
  - Thực hiện INV-03.
- Ngày hoàn tất: 2026-10-12

## INV-03: API tra cứu tồn kho và cửa hàng

- Trạng thái: `DONE`
- Branch: `feat/store-availability-api`
- Pull Request: [#22](https://github.com/NgocYuh/UTEGear/pull/22)
- Merge commit: `8c36c9e633f0dc6c5b8a833b9e6711396830e06c`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Thêm API public danh sách cửa hàng với filter trạng thái/tỉnh thành và chi tiết cửa hàng hoạt động.
  - Thêm API availability theo đúng cặp `storeId + variantId`; không dùng tồn kho cấp Product.
  - Thêm API availability cấp sản phẩm, tổng hợp tồn kho của từng biến thể tại các cửa hàng đang hoạt động.
  - Response dùng DTO riêng và giữ đúng field của API-02; không trả trực tiếp entity JPA.
  - Phân biệt cửa hàng không tồn tại/ngừng hoạt động, biến thể không tồn tại và trường hợp hết hàng.
  - Chuẩn hóa lỗi thiếu `variantId` về `VALIDATION_FAILED`.
- Kết quả kiểm thử:
  - Danh sách theo tỉnh thành và ẩn chi tiết cửa hàng ngừng hoạt động: `PASS`.
  - Biến thể còn hàng, hết hàng do chưa có dòng inventory và cửa hàng ngừng hoạt động: `PASS`.
  - Tổng tồn sản phẩm qua nhiều biến thể/cửa hàng: `PASS`.
  - API store public không cần JWT: `PASS`.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 57 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Kiểm thử dùng H2 cho application context và mock repository cho response API; chưa kết nối Supabase thật.
  - API quản trị inventory đã có domain/service từ INV-02 nhưng controller quản trị không nằm trong phạm vi INV-03.
- Bước tiếp theo:
  - Thực hiện MED-01; Frontend có thể tích hợp FE-STORE-01 theo API-02.
- Ngày hoàn tất: 2026-10-12

## MED-01: Tích hợp Cloudinary cho ảnh sản phẩm

- Trạng thái: `DONE`
- Branch: `feat/cloudinary-product-images`
- Pull Request: [#25](https://github.com/NgocYuh/UTEGear/pull/25)
- Merge commit: `59433796a404edd38d3efc5cb0baea48f2b3edeb`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Cấu hình một Cloudinary bean dùng chung từ biến môi trường; không lưu cloud name, API key hoặc API secret thật trong repository.
  - Thêm lớp lưu trữ media, upload vào thư mục theo sản phẩm và lưu cả `secureUrl` lẫn `publicId`.
  - Thêm API quản trị để tải ảnh lên, cập nhật/thay ảnh, xóa ảnh và sắp xếp lại ảnh sản phẩm theo contract API-02.
  - Kiểm tra MIME JPEG/PNG/WebP và giới hạn dung lượng mặc định 5 MiB.
  - Khi thay ảnh, chỉ xóa ảnh cũ sau khi transaction database commit; ảnh mới được dọn nếu transaction rollback.
  - Đồng bộ ảnh đại diện và thứ tự ảnh, đồng thời kiểm tra toàn bộ ID khi sắp xếp.
  - Chuẩn hóa lỗi multipart vượt dung lượng thành `FILE_TOO_LARGE`.
- Kết quả kiểm thử:
  - Upload lưu đúng URL/public ID và đúng thư mục sản phẩm: `PASS`.
  - Từ chối MIME không hỗ trợ và file vượt dung lượng: `PASS`.
  - Thay ảnh, cập nhật metadata, xóa ảnh và sắp xếp ảnh: `PASS`.
  - Callback dọn media theo commit/rollback: `PASS`.
  - `mvn -B --no-transfer-progress -Dtest=ProductMediaServiceTest,TransactionCallbacksTest test`: `PASS` với 10 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 67 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Test dùng mock media storage, không gọi Cloudinary thật và không sử dụng credential tài khoản cá nhân trong CI.
  - Việc xác nhận upload thật cần cấu hình biến môi trường Cloudinary trên máy local hoặc môi trường triển khai.
- Bước tiếp theo:
  - Thực hiện DATA-01; phần Frontend liên quan có thể dùng contract media hiện tại.
- Ngày hoàn tất: 2026-10-12

## DATA-01: Crawler pipeline tối thiểu

- Trạng thái: `DONE`
- Branch: `feat/product-crawler-pipeline`
- Pull Request: [#26](https://github.com/NgocYuh/UTEGear/pull/26)
- Merge commit: `37bc1a5f20b12acddecfdcc575223a7508749dfe`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Hoàn thiện pipeline hữu hạn `crawl → parse → clean → validate → export` bằng Python standard library.
  - Thêm adapter GEARVN đọc `Product` JSON-LD từ HTML công khai, không gọi API nội bộ.
  - HTTP client kiểm tra `robots.txt`, dùng User-Agent riêng và giới hạn tốc độ mặc định 2 giây.
  - CLI chỉ truy cập mạng khi có `--allow-network` và từ chối URL ngoài host GEARVN đã cho phép.
  - Chuẩn hóa sáu danh mục dự kiến: bàn phím, chuột, tai nghe, keycap, switch và phụ kiện.
  - Tạo đúng một biến thể mặc định với `option_values` rỗng khi nguồn không có lựa chọn; sinh SKU ổn định nếu nguồn thiếu SKU.
  - Validate URL nguồn/ảnh, giá VND dương, danh mục, SKU duy nhất và đúng một biến thể mặc định.
  - Export JSONL UTF-8 có thông tin truy vết nguồn; dữ liệu output và ảnh không được commit.
  - Bổ sung bước test crawler offline vào Backend CI.
- Kết quả kiểm thử:
  - Parser, cleaner, validation, chống trùng, SKU mặc định, giới hạn host và JSONL UTF-8: `PASS` với 7 test.
  - CLI chạy đầu-cuối bằng HTML fixture, không dùng mạng: `PASS`, export 1 record hợp lệ.
  - `python -m compileall -q src tests`: `PASS`.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 67 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa crawl/import 100 sản phẩm thật; đó là phạm vi DATA-02 sau khi dataset và quyền sử dụng nguồn được rà soát.
  - KICAP đang tắt vì chưa xác minh xong robots/điều khoản; DATA-01 chỉ triển khai adapter GEARVN.
- Bước tiếp theo:
  - Triển khai DATA-02 để thu thập có kiểm soát, kiểm tra và import dữ liệu.
- Ngày hoàn tất: 2026-10-12

## DATA-02: Validate và import sản phẩm

- Trạng thái: `DONE`
- Branch: `feat/product-data-import`
- Pull Request: [#27](https://github.com/NgocYuh/UTEGear/pull/27)
- Merge commit: `88bffcb35032d750e925d42a46a624f59efcb56a`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Thêm API ADMIN nhận file JSONL UTF-8 và trả báo cáo từng dòng `IMPORTED` hoặc `FAILED`.
  - Mỗi dòng chạy trong transaction riêng, nên dòng lỗi không rollback những sản phẩm đã import thành công.
  - Validate trường bắt buộc, thời gian crawl, source URL, category, brand, giá, SKU, URL ảnh và biến thể mặc định.
  - Từ chối product slug/SKU trùng; không ghi đè dữ liệu đã có.
  - Chuẩn hóa một variant duy nhất có option rỗng thành variant mặc định; từ chối sản phẩm không có variant.
  - Resolve category theo slug và brand không phân biệt hoa thường; chỉ nhận reference data đang hoạt động.
  - Lưu `sourceName`, `sourceUrl`, `crawledAt` trong JSON specifications để truy vết nguồn sau import.
  - Tải ảnh bằng HTTPS từ allowlist, giới hạn MIME/dung lượng, upload qua Cloudinary và lưu cả `secureUrl`/`publicId`.
  - Đăng ký dọn ảnh Cloudinary nếu transaction database rollback.
  - Không tạo tồn kho trong importer; inventory vẫn thuộc `Store + ProductVariant`.
  - Cập nhật API contract, mock response, data contract, file cấu hình ví dụ và hướng dẫn import.
- Kết quả kiểm thử:
  - File hợp lệ, report hỗn hợp, file rỗng và giới hạn số dòng: `PASS`.
  - Thiếu field, thiếu variant, SKU trùng, giá sai, brand không tồn tại và sinh default variant: `PASS`.
  - Chặn URL ảnh HTTP hoặc host ngoài allowlist trước khi truy cập mạng: `PASS`.
  - Customer bị chặn khỏi API import: `PASS`.
  - Crawler DATA-01: `PASS` với 7 test.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 80 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa chạy import thật vào Supabase và chưa upload ảnh thật lên Cloudinary cá nhân.
  - Bộ 100 sản phẩm chỉ chạy sau khi category/brand, danh sách URL và quyền sử dụng nguồn được review.
  - Format canonical hiện tại là JSONL từ DATA-01; chưa bổ sung CSV vì dữ liệu variants/images là cấu trúc lồng nhau.
- Bước tiếp theo:
  - Thực hiện CART-01; việc chuẩn bị dataset 100 sản phẩm và chạy thử local/staging là công việc vận hành riêng.
- Ngày hoàn tất: 2026-10-12

## CART-01: Cart domain và service

- Trạng thái: `DONE`
- Branch: `feat/shopping-cart-domain`
- Pull Request: [#28](https://github.com/NgocYuh/UTEGear/pull/28)
- Merge commit: `ecc1d39ce06e7c9ac9933332c6a37d6cecc3e539`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Ánh xạ `Cart` và `CartItem` đúng schema Flyway V1; mỗi người dùng có một giỏ và mỗi biến thể chỉ có một dòng trong giỏ.
  - Bắt buộc mọi dòng giỏ hàng tham chiếu `ProductVariant`, kể cả sản phẩm chỉ có biến thể mặc định.
  - Thêm service đọc giỏ, thêm/cộng dồn biến thể, cập nhật số lượng, xóa dòng và xóa toàn bộ giỏ.
  - Tính `unitPrice` từ giá hiệu lực của sản phẩm cộng điều chỉnh giá biến thể; tính `lineTotal`, tổng số lượng và subtotal tại domain.
  - Kiểm tra người dùng, ID và số lượng hợp lệ; từ chối biến thể không tồn tại, ngừng hoạt động, thuộc sản phẩm ngừng hoạt động hoặc không thuộc danh sách biến thể của sản phẩm.
  - Giới hạn cập nhật/xóa theo chủ sở hữu giỏ hàng để không thao tác được item của người dùng khác.
  - Không sửa migration, API/controller, template hoặc static asset; protected cart API thuộc CART-02.
- Kết quả kiểm thử:
  - Thêm trùng biến thể cộng dồn vào đúng một dòng: `PASS`.
  - Số lượng bằng 0 bị từ chối và item của người dùng khác không thể cập nhật: `PASS`.
  - Tổng tiền dùng giá khuyến mãi cộng điều chỉnh biến thể: `PASS`.
  - Biến thể không gắn đúng sản phẩm bị từ chối: `PASS`.
  - `mvn -B --no-transfer-progress '-Dtest=CartTest,CartServiceTest' test`: `PASS` với 8 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 88 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa kiểm tra tồn kho khi thêm/cập nhật giỏ vì tồn kho phụ thuộc cửa hàng phục vụ; server sẽ kiểm tra lại theo `Store + ProductVariant` ở bước checkout/order.
  - Chưa có HTTP endpoint và DTO response; đây là phạm vi CART-02 theo contract API-03.
- Bước tiếp theo:
  - Thực hiện CART-02.
- Ngày hoàn tất: 2026-10-12

## CART-02: Protected cart API

- Trạng thái: `DONE`
- Branch: `feat/cart-api`
- Pull Request: [#29](https://github.com/NgocYuh/UTEGear/pull/29)
- Merge commit: `76a1defcc8fe49b6b46de24ce100045fac54e53a`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Thêm năm endpoint theo API-03 để đọc giỏ, thêm item, cập nhật số lượng, xóa item và xóa toàn bộ giỏ.
  - Bảo vệ toàn bộ `/api/cart/**` bằng JWT qua quy tắc `/api/**` hiện có; controller chỉ lấy người dùng từ principal và không nhận `userId` từ client.
  - Validate `variantId` và `quantity` ở request DTO; lỗi field trả về `VALIDATION_FAILED` theo error contract chung.
  - Trả Cart DTO riêng gồm product/variant summary, giá đơn vị, thành tiền, tổng số lượng, subtotal và thời điểm cập nhật.
  - Tổng hợp `availableStock` của biến thể trên các cửa hàng đang hoạt động; `available` yêu cầu sản phẩm/biến thể còn hoạt động và tồn tổng đủ số lượng trong giỏ.
  - Không trả trực tiếp entity JPA, không sửa migration và không thay đổi template/static asset của Frontend.
- Kết quả kiểm thử:
  - Thiếu JWT trên API cart: `PASS`, trả `401 UNAUTHORIZED`.
  - Request hợp lệ dùng đúng người dùng từ JWT: `PASS`, trả `201` và Cart response.
  - Số lượng bằng 0: `PASS`, trả `400 VALIDATION_FAILED` kèm lỗi field `quantity`.
  - Item không thuộc người dùng hiện tại: `PASS`, trả `404 CART_ITEM_NOT_FOUND` và không làm lộ giỏ của người khác.
  - Mapping response, giá, ảnh chính và tồn kho tổng hợp: `PASS`.
  - `mvn -B --no-transfer-progress '-Dtest=CartControllerTest,CartResponseTest,CartServiceTest' test`: `PASS` với 10 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 93 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Test dùng H2, mock JWT user/service và dữ liệu entity trong bộ nhớ; chưa kết nối Supabase thật.
  - API chỉ hiển thị tồn khả dụng tổng hợp; việc chọn cửa hàng phục vụ và kiểm tra tồn kho cuối cùng thuộc checkout/order.
- Bước tiếp theo:
  - Thực hiện WISH-01 hoặc ORD-01 theo backlog.
- Ngày hoàn tất: 2026-10-12

## ORD-01: Address và Coupon

- Trạng thái: `DONE`
- Branch: `feat/address-coupon-domain`
- Pull Request: [#30](https://github.com/NgocYuh/UTEGear/pull/30)
- Merge commit: `c5351f41189de56ad7462dd998d09588b3e731c2`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Ánh xạ entity `Address` và `Coupon` đúng hai bảng đã có trong Flyway V1; không tạo hoặc sửa migration.
  - Thêm service tạo, đọc, cập nhật, xóa và chọn địa chỉ mặc định theo đúng người dùng sở hữu.
  - Địa chỉ đầu tiên của người dùng tự động thành mặc định; khi đổi mặc định, địa chỉ cũ được bỏ cờ.
  - Thêm quy tắc coupon theo trạng thái, thời gian hiệu lực, số lượt sử dụng và giá trị đơn tối thiểu.
  - Hỗ trợ giảm theo phần trăm hoặc số tiền cố định, giới hạn mức giảm tối đa và không cho tổng giảm vượt subtotal.
  - Chuẩn hóa mã coupon không phân biệt hoa thường; dùng pessimistic lock khi tiêu thụ lượt coupon để chuẩn bị cho checkout transaction.
  - Trả `COUPON_INVALID` cho mã sai, hết hạn, hết lượt hoặc không đạt giá trị tối thiểu.
  - Chưa tạo Address/Coupon controller vì ORD-01 chỉ gồm domain và service; checkout/order API thuộc các task ORD tiếp theo.
- Kết quả kiểm thử:
  - Coupon hết hạn, hết lượt và đơn chưa đạt giá trị tối thiểu: `PASS`.
  - Coupon phần trăm có mức giảm tối đa và coupon cố định không làm subtotal âm: `PASS`.
  - Coupon giới hạn lượt được khóa và tăng `usedCount`: `PASS`.
  - Địa chỉ đầu tiên thành mặc định, đổi địa chỉ mặc định và chặn truy cập địa chỉ của người khác: `PASS`.
  - `mvn -B --no-transfer-progress '-Dtest=CouponTest,AddressServiceTest,CouponServiceTest' test`: `PASS` với 9 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 102 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Test persistence dùng H2 qua application context; chưa chạy giao dịch thật trên Supabase.
  - Việc tạo Order/Payment, áp coupon trong checkout và API address/coupon chưa nằm trong phạm vi ORD-01.
- Bước tiếp theo:
  - Thực hiện ORD-02.
- Ngày hoàn tất: 2026-10-12

## ORD-02: Order và Payment domain

- Trạng thái: `DONE`
- Branch: `feat/order-payment-domain`
- Pull Request: [PR #31](https://github.com/NgocYuh/UTEGear/pull/31)
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Ánh xạ `Order`, `OrderItem` và `Payment` đúng các bảng Flyway V1; không tạo hoặc sửa migration.
  - Thêm enum order, shipping, payment method và payment status đúng API-03 và constraint database.
  - Order snapshot thông tin khách hàng và địa chỉ giao hàng tại thời điểm tạo đơn.
  - OrderItem luôn tham chiếu `ProductVariant` và snapshot bất biến tên sản phẩm, SKU, option values, ảnh, giá và subtotal.
  - Tự tính subtotal từ các item và tính total theo `subtotal - discount + shippingFee`; từ chối giá trị âm hoặc discount vượt subtotal.
  - Giới hạn mỗi biến thể một dòng trong đơn và mỗi đơn tối đa một Payment.
  - Payment lưu đúng tổng tiền cuối cùng của Order và hỗ trợ trạng thái chờ, thành công, thất bại hoặc hủy.
  - Thêm repository tra cứu đơn theo mã/chủ sở hữu và payment theo order/transaction ID.
  - Chưa trừ tồn kho, áp dụng lượt coupon, xóa giỏ hoặc mở API checkout; các bước giao dịch này thuộc ORD-03.
- Kết quả kiểm thử:
  - Tính subtotal, discount, shipping fee và total: `PASS`.
  - Snapshot product/variant không đổi khi dữ liệu catalog sau đó thay đổi: `PASS`.
  - Từ chối discount vượt subtotal: `PASS`.
  - Tạo đúng một Payment theo total cuối cùng và chặn Payment thứ hai: `PASS`.
  - Chỉ cho phép hoàn tất/hủy payment từ trạng thái phù hợp: `PASS`.
  - `mvn -B --no-transfer-progress '-Dtest=OrderTest' test`: `PASS` với 5 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress clean verify`: `PASS` với 107 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Test mapping dùng H2 qua application context; chưa chạy persistence thật trên Supabase.
  - Chuyển trạng thái Order cho nghiệp vụ quản trị sẽ được hoàn thiện ở ORD-04; ORD-02 mới chốt enum và trường thời gian.
- Bước tiếp theo:
  - Thực hiện ORD-03.
- Ngày hoàn tất: 2026-10-12

## ORD-03: Checkout transaction

- Trạng thái: `DONE`
- Branch: `feat/checkout-service`
- Pull Request: [PR #32](https://github.com/NgocYuh/UTEGear/pull/32)
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Mở `POST /api/checkout/quote` và `POST /api/orders` theo API-03, yêu cầu JWT qua quy tắc `/api/**` hiện có.
  - Báo giá trong 15 phút, gắn với người dùng, địa chỉ, cửa hàng, coupon, phương thức thanh toán và đúng danh sách biến thể/số lượng đã gửi.
  - Tải lại giá sản phẩm khi báo giá và khi đặt hàng; không tin giá do client gửi.
  - Kiểm tra cửa hàng đang hoạt động và tồn kho theo đúng cặp `Store + ProductVariant`.
  - Dùng pessimistic lock khi trừ tồn kho; coupon cũng được khóa khi tăng lượt sử dụng.
  - Tạo Order, snapshot OrderItem, tính `subtotal - discount + shippingFee`, tạo Payment và xóa giỏ trong cùng transaction.
  - Chỉ vô hiệu quote token sau khi transaction commit; lỗi ở bất kỳ bước database nào sẽ rollback tồn kho, coupon, Order, Payment và giỏ hàng.
- Kết quả kiểm thử:
  - Checkout thành công, trừ đúng tồn kho và chỉ xóa giỏ sau khi lưu Order: `PASS`.
  - Tồn kho không đủ trả `INSUFFICIENT_STOCK`: `PASS`.
  - Coupon sai trả `COUPON_INVALID`: `PASS`.
  - Lỗi lưu Order không gọi lưu giỏ đã xóa: `PASS`; service dùng một ranh giới `@Transactional` để database rollback các thay đổi đã thực hiện.
  - `mvn -B "-Dnet.bytebuddy.experimental=true" "-Dtest=CheckoutServiceTest" test`: `PASS` với 4 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `PASS` với 111 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Máy hiện tại chạy Java 26 trong khi Byte Buddy đi kèm chỉ công bố hỗ trợ đến Java 23, nên test Mockito cần cờ `-Dnet.bytebuddy.experimental=true`; đây không phải thay đổi dependency của dự án.
  - Chưa chạy transaction thật trên Supabase; toàn bộ suite hiện dùng H2/test double.
  - Quote token hiện lưu trong bộ nhớ ứng dụng, phù hợp triển khai một instance; nếu triển khai nhiều instance cần chuyển sang kho dùng chung như Redis.
  - API danh sách/chi tiết/hủy đơn của khách và luồng quản trị trạng thái chưa thuộc phần checkout này.
- Bước tiếp theo:
  - Thực hiện ORD-04.
- Ngày hoàn tất: 2026-10-12

## ORD-04: Admin order management

- Trạng thái: `IN PROGRESS`
- Branch: `feat/admin-order-management`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Thêm `GET /api/admin/orders` để tìm theo mã đơn, email hoặc số điện thoại; lọc theo trạng thái/cửa hàng và phân trang từ mới đến cũ.
  - Thêm `GET /api/admin/orders/{orderId}` trả chi tiết snapshot khách hàng, giao hàng, cửa hàng, payment, item và toàn bộ mốc trạng thái.
  - Thêm `PUT /api/admin/orders/{orderId}/status`; toàn bộ đường dẫn dùng quy tắc role `ADMIN` hiện có.
  - Chốt transition: `PENDING -> CONFIRMED|CANCELLED`, `CONFIRMED -> PROCESSING|CANCELLED`, `PROCESSING -> SHIPPING|CANCELLED`, `SHIPPING -> COMPLETED`; trạng thái cuối không đổi tiếp.
  - Lưu thời điểm xác nhận, xử lý, giao, hoàn thành hoặc hủy; khóa pessimistic Order khi đổi trạng thái để tránh hai admin cập nhật đồng thời.
  - Hủy payment đang chờ cùng đơn; chặn hủy đơn đã thanh toán bằng `PAID_ORDER_CANNOT_CANCEL` vì chưa có nghiệp vụ hoàn tiền.
  - Cập nhật API-03 contract với mã lỗi hủy đơn đã thanh toán.
- Kết quả kiểm thử:
  - Đi đủ chuỗi trạng thái và ghi các timestamp: `PASS`.
  - Không cho nhảy cóc hoặc đổi trạng thái cuối: `PASS`.
  - Confirm đơn hợp lệ, hủy đơn đồng thời hủy payment chờ và từ chối transition sai: `PASS`.
  - Customer gọi `/api/admin/orders` nhận `403 FORBIDDEN`: `PASS`.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" "-Dtest=OrderTest,AdminOrderServiceTest,SecurityIntegrationTest" test`: `PASS` với 22 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `PASS` với 119 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa chạy trên Supabase thật; kiểm thử application context hiện dùng H2.
  - Chưa phát notification/WebSocket khi trạng thái đổi; đây là phụ thuộc tiếp theo `WS-01` và `WS-02`.
- Bước tiếp theo:
  - Mở và merge PR ORD-04, sau đó thực hiện WS-01.
- Ngày hoàn tất: `Chưa hoàn tất`

## WS-01: Notification domain

- Trạng thái: `DONE`
- Branch: `feat/order-notifications`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/37
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Ánh xạ `Notification` vào bảng `notifications` đã có trong Flyway V1; không tạo migration mới.
  - Thêm loại `ORDER_STATUS_CHANGED`, lưu người nhận, tiêu đề, nội dung, reference order, trạng thái đọc và thời gian tạo.
  - Mở API danh sách có lọc `read`/phân trang, đánh dấu một thông báo đã đọc và đánh dấu tất cả đã đọc.
  - Mọi truy vấn đọc/sửa đều gắn `userId`; truy cập notification của user khác trả `404 NOTIFICATION_NOT_FOUND` để không lộ tài nguyên.
  - Bổ sung mã lỗi notification vào API-03 contract.
- Kết quả kiểm thử:
  - Danh sách chỉ lấy notification của user hiện tại: `PASS`.
  - Đánh dấu một/tất cả notification đã đọc: `PASS`.
  - User khác không thể đọc notification không thuộc mình: `PASS`.
  - Tạo notification cho user đã persist: `PASS`.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" "-Dtest=NotificationServiceTest,ApplicationProfilesTest" test`: `PASS` với 6 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Phần phát notification tự động khi trạng thái Order đổi được tích hợp ở commit WS-02 kế tiếp trên cùng branch.
- Bước tiếp theo:
  - Tạo commit WS-01 rồi thực hiện WS-02 trên cùng branch và cùng Pull Request.
- Ngày hoàn tất: 2026-10-12; merge commit `157456a`

## WS-02: Realtime order updates

- Trạng thái: `DONE`
- Branch: `feat/order-notifications`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/37
- Ngày cập nhật: 2026-10-12
- Đã làm:
  - Bật WebSocket/STOMP tại `/ws`, simple broker cho `/topic` và `/queue`, user destination prefix `/user`.
  - Xác thực JWT trong STOMP `CONNECT` qua header `Authorization: Bearer <token>` và gắn principal theo email tài khoản.
  - Origin được lấy từ `WEBSOCKET_ALLOWED_ORIGINS`; cấu hình rỗng hoặc chứa `*` bị từ chối ngay khi khởi động.
  - Sau checkout commit, phát `ORDER_CREATED` tới `/topic/admin/orders`.
  - Khi admin đổi trạng thái, lưu notification cùng transaction rồi phát `ORDER_STATUS_CHANGED` tới `/user/queue/orders` và `NOTIFICATION_CREATED` tới `/user/queue/notifications` của đúng chủ đơn.
  - Tất cả event dùng envelope `eventId`, `type`, `occurredAt`, `data` theo API-03 và chỉ được gửi sau khi transaction database commit.
  - Bổ sung biến môi trường mẫu WebSocket; không thêm dependency vì starter WebSocket đã có từ scaffold.
- Kết quả kiểm thử:
  - STOMP CONNECT có JWT hợp lệ được gắn principal; thiếu token bị từ chối: `PASS`.
  - Customer bị chặn subscribe `/topic/admin/orders`; chỉ principal có `ROLE_ADMIN` được phép: `PASS`.
  - Cấu hình origin chứa `*` bị từ chối: `PASS`.
  - Đơn mới phát đúng topic admin và payload `ORDER_CREATED`: `PASS`.
  - Thay đổi trạng thái chỉ phát vào user destination theo email chủ đơn, kèm payload trạng thái trước/sau: `PASS`.
  - Checkout và AdminOrder service vẫn hoạt động với publisher mới: `PASS`.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `PASS` với 131 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa kiểm thử kết nối WebSocket qua reverse proxy/deployment thật; test hiện xác minh cấu hình, xác thực, destination và payload trong application/test double.
  - Frontend cần dùng `WEBSOCKET_ALLOWED_ORIGINS` phù hợp domain triển khai và gửi JWT ở STOMP CONNECT.
- Bước tiếp theo:
  - Tạo commit WS-02 rồi push branch để mở một PR chứa hai commit.
- Ngày hoàn tất: 2026-10-12; merge commit `157456a`

## WISH-01: Wishlist Backend

- Trạng thái: `DONE`
- Branch: `feat/wishlist`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/44
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Ánh xạ `WishlistItem` vào bảng `wishlist_items` đã có trong Flyway V1; không tạo migration mới.
  - Thêm repository, service và API `GET /api/wishlist`, `POST /api/wishlist/items`, `DELETE /api/wishlist/items/{productId}` đúng API-03.
  - Xác định chủ sở hữu hoàn toàn từ JWT; client không gửi `userId`.
  - Chỉ cho thêm sản phẩm đang hoạt động và không tạo bản ghi trùng khi gửi lại cùng `productId`.
  - Response trả product summary rút gọn, biến thể mặc định, ảnh chính và trạng thái còn hàng tổng hợp từ tồn kho của các biến thể đang hoạt động.
  - Xóa sản phẩm bằng truy vấn có cả `userId + productId`; tài nguyên không thuộc user hiện tại trả `404 WISHLIST_ITEM_NOT_FOUND`.
  - Cập nhật API-03 để ghi rõ tính idempotent khi thêm trùng và lỗi quyền sở hữu khi xóa.
- Kết quả kiểm thử:
  - Thêm sản phẩm hợp lệ, thêm lại không tạo trùng và từ chối sản phẩm không tồn tại/không hoạt động: `PASS`.
  - Danh sách và thao tác xóa chỉ dùng dữ liệu của user hiện tại: `PASS`.
  - Thiếu JWT bị từ chối; request hợp lệ và validation `productId` được kiểm tra qua MockMvc: `PASS`.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" "-Dtest=WishlistServiceTest,WishlistControllerTest" test`: `PASS` với 11 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `PASS` với 142 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa kiểm thử với PostgreSQL/Supabase thật; test ứng dụng dùng profile `test` và H2, không kết nối database production.
- Bước tiếp theo:
  - Chạy toàn bộ suite, tạo commit và push branch để mở Pull Request.
- Ngày hoàn tất: 2026-10-13; merge commit `9cbc97e`

## QA-01: Backend service unit tests

- Trạng thái: `DONE`
- Branch: `test/service-coverage`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/47
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Rà soát toàn bộ lớp trong `service` và đối chiếu với test hiện có.
  - Bổ sung test cho `BrandService`, `CategoryService`, `CheckoutQuoteStore`, `DefaultCartApiService`, `ProductCatalogService` và `StoreAvailabilityService`.
  - Bao phủ tạo dữ liệu tham chiếu và chống trùng; vòng đời checkout quote; ánh xạ API cart; catalog detail/filter; tồn kho theo cửa hàng và tổng hợp theo biến thể.
  - Không thêm test cho interface thuần `CartApiService`, `ProductRowImporter` hoặc getter/setter đơn giản.
- Kết quả kiểm thử:
  - 18 test mới chạy tập trung: `PASS`, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `PASS` với 160 test, 0 failure, 0 error, 0 skipped.
- Vướng mắc hoặc phần còn thiếu:
  - Không có thay đổi mã nguồn nghiệp vụ; QA-02 sẽ kiểm tra repository/PostgreSQL và QA-03 sẽ kiểm tra controller/security sâu hơn.
- Bước tiếp theo:
  - Chạy toàn bộ suite, tạo commit và push branch để mở Pull Request.
- Ngày hoàn tất: 2026-10-13; merge commit `66acfee`

## QA-02: Backend repository và PostgreSQL integration tests

- Trạng thái: `DONE`
- Branch: `test/postgresql-integration`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/48 (hotfix: https://github.com/NgocYuh/UTEGear/pull/49)
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Thêm Testcontainers PostgreSQL chỉ trong test scope; không kết nối Supabase hoặc database production.
  - Kiểm tra Flyway áp dụng V1/V2, cột JSONB và ba partial unique index đặc thù PostgreSQL.
  - Kiểm tra repository tồn kho theo `storeId + variantId` và unique constraint chống bản ghi trùng.
  - Kiểm tra Wishlist repository giới hạn theo chủ sở hữu và unique constraint `userId + productId`.
  - Test tự bỏ qua khi không có Docker để máy local không báo lỗi giả; CI/máy có Docker sẽ chạy PostgreSQL thật.
- Kết quả kiểm thử:
  - `PostgresqlMigrationRepositoryTest`: phát hiện 3 test, `SKIPPED` cả 3 vì Docker daemon local không chạy; không có test PostgreSQL nào được báo `PASS` giả.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `BUILD SUCCESS` với 163 test được phát hiện, 160 pass và 3 PostgreSQL test bị skip do không có Docker.
  - Backend CI sau merge PR #48: `FAIL`; Spring ApplicationContext của ba test PostgreSQL không khởi động vì test chưa dùng profile `test` và chưa ghi đè H2 driver sang PostgreSQL.
  - Hotfix `fix/qa02-postgresql-context`: bật profile `test` để dùng credential giả an toàn và ghi đè driver thành `org.postgresql.Driver`.
  - Backend CI trên merge commit hotfix `8320dd4`: `PASS`; ba test Testcontainers đã chạy với PostgreSQL thật trên GitHub Actions.
- Vướng mắc hoặc phần còn thiếu:
  - Docker daemon local không chạy nên máy local vẫn skip ba test PostgreSQL; kết quả PostgreSQL thật đã được GitHub Actions xác nhận.
- Bước tiếp theo:
  - Tiếp tục QA-03 kiểm thử security và controller.
- Ngày hoàn tất: 2026-10-13; merge commit PR #48 `1bbe2b1`, hotfix PR #49 `8320dd4`

## QA-03: Backend security và controller tests

- Trạng thái: `IN REVIEW`
- Branch: `test/security-controller-flows`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-13
- Đã làm:
  - Bổ sung kiểm thử MockMvc cho các API được bảo vệ của checkout, notification, quản trị đơn hàng và quản trị ảnh sản phẩm.
  - Kiểm tra request thiếu JWT bị từ chối với `401 UNAUTHORIZED`.
  - Kiểm tra JWT hợp lệ xác định đúng người dùng và truyền đúng principal vào `CheckoutService` và `NotificationService`.
  - Kiểm tra khách hàng bị từ chối khỏi API admin với `403 FORBIDDEN`, còn admin có thể truy cập controller quản lý đơn hàng.
  - Kiểm tra request cập nhật trạng thái đơn hàng thiếu dữ liệu trả về `400 VALIDATION_FAILED`.
- Kết quả kiểm thử:
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" "-Dtest=ProtectedApiFlowTest,SecurityIntegrationTest" test`: `PASS` với 18 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `BUILD SUCCESS` với 169 test được phát hiện, 166 pass, 0 failure, 0 error và 3 PostgreSQL test bị skip do Docker local không chạy.
- Vướng mắc hoặc phần còn thiếu:
  - Ba test PostgreSQL của QA-02 vẫn bị skip ở local; chúng đã được xác nhận chạy đạt trên GitHub Actions sau hotfix PR #49.
- Bước tiếp theo:
  - Chạy toàn bộ suite, commit và push branch để mở Pull Request.
- Ngày hoàn tất: `Chưa merge`

## INT-BE-01: MVC routing và Thymeleaf runtime

- Trạng thái: `IN REVIEW`
- Branch: `feat/mvc-view-routing`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-14
- Đã làm:
  - Bổ sung Thymeleaf Layout Dialect để xử lý `layout:decorate` trong các template hiện có.
  - Thêm MVC controller cho trang chủ, danh sách/tìm kiếm/chi tiết sản phẩm, cửa hàng, auth, trang thông tin và customer shell.
  - Dùng `CategoryService`, `BrandService`, `ProductCatalogService` và `StoreService` để cấp model server-side; customer shell giữ model rỗng an toàn vì JWT của trình duyệt đang nằm trong `sessionStorage`.
  - Thêm model mặc định dùng chung cho trạng thái đăng nhập, số lượng cart/wishlist và từ khóa tìm kiếm.
  - Bổ sung alias phân trang `number()` và `isEmpty()` cho `ProductPage` để khớp contract mà template Thymeleaf đang đọc.
  - Thêm unit test cho route/model và integration test render các trang public, auth, cart, wishlist, checkout và profile.
- Kết quả kiểm thử:
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" "-Dtest=StorefrontViewControllerTest,StorefrontViewRenderingTest" test`: `PASS` với 6 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `BUILD SUCCESS` với 175 test được phát hiện, 172 pass, 0 failure, 0 error và 3 PostgreSQL test bị skip do Docker local không chạy.
  - `node --test tests/frontend/integration.test.mjs tests/frontend/mock-data.test.mjs`: `PASS` với 16 test, 0 failure.
- Vướng mắc hoặc phần còn thiếu:
  - Một số template sản phẩm/cửa hàng vẫn dùng tên field cũ khi có dữ liệu thật; Frontend cần tiếp tục đồng bộ component với DTO contract hiện tại.
  - Chưa thêm admin MVC route vì template/model admin chưa có contract hoàn chỉnh; không tạo route hoặc dữ liệu giả để che lỗi thiếu tích hợp.
  - Route customer render được shell HTML nhưng dữ liệu riêng của người dùng vẫn phải được JavaScript gọi API bằng JWT trong `sessionStorage`.
- Bước tiếp theo:
  - Mở PR để Frontend kéo về hoàn thiện binding còn thiếu rồi chạy lại QA-04.
- Ngày hoàn tất: `Chưa merge`

## INT-BE-02: Account, customer order và admin MVC integration

- Trạng thái: `DONE`
- Branch: `feat/account-order-integration`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/54
- Ngày cập nhật: 2026-10-14
- Đã làm:
  - Bổ sung API cập nhật profile và đổi mật khẩu; kiểm tra phone duy nhất, mật khẩu hiện tại,
    xác nhận mật khẩu mới và không trả password hash.
  - Bổ sung API danh sách/tạo/sửa/xóa/chọn mặc định cho address; service luôn giới hạn theo user từ JWT.
  - Bổ sung API danh sách/chi tiết/hủy order của khách; truy vấn và lock theo `orderId + userId`, trả
    `404` cho tài nguyên không thuộc chủ sở hữu và chặn hủy order đã thanh toán/trạng thái không hợp lệ.
  - Phát notification và WebSocket event sau khi khách hủy order hợp lệ.
  - Bổ sung MVC shell cho `/orders`, `/orders/{id}`, `/admin/orders` và `/admin/orders/{id}`. Route detail
    redirect về shell kèm `focus` để client tải dữ liệu protected bằng Bearer JWT, không đưa JWT vào model SSR.
  - Duyệt `sessionStorage` theo tab cho phiên JWT hiện tại và ghi rõ hành vi 401/403/logout/STOMP cùng rủi ro XSS.
  - Thêm hướng dẫn chuẩn bị User A, User B, admin và dữ liệu QA mà không commit password, JWT hoặc dữ liệu production.
  - Cập nhật contract auth/customer transaction theo endpoint và mã lỗi thực tế.
  - Commit mã nguồn: `a08f0ec` (`feat: complete account and order integration APIs`).
- Kết quả kiểm thử:
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" "-Dtest=ProfileServiceTest,CustomerOrderServiceTest,AddressServiceTest,StorefrontViewRenderingTest" test`:
    `PASS` với 12 test, 0 failure, 0 error, 0 skipped.
  - `StorefrontViewRenderingTest`: `PASS` với 3 test, gồm render shell `/orders` và `/admin/orders`.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `PASS` với
    185 test được phát hiện, 182 pass, 0 failure, 0 error và 3 PostgreSQL test bị skip do Docker local không chạy.
  - `node --test tests/frontend/integration.test.mjs tests/frontend/mock-data.test.mjs`: `PASS`
    với 16 test, 0 failure.
- Vướng mắc hoặc phần còn thiếu:
  - Frontend vẫn cần bind profile/address/order/admin shell với API thật và hoàn tất các ca browser trong QA-04.
  - Repository chỉ chuẩn bị quy trình dữ liệu QA; mật khẩu dùng một lần và URL môi trường phải gửi ngoài Git.
- Bước tiếp theo:
  - Commit/push branch; sau đó FE kéo branch/PR này và chạy lại QA-04.
- Ngày hoàn tất: 2026-10-14, merge commit `ef285f9`

## QA-04-BE-FIX: Sửa cập nhật giỏ hàng và xác thực STOMP

- Trạng thái: `DONE`
- Branch: `fix/cart-stomp-integration`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/55
- Ngày cập nhật: 2026-10-14
- Đã làm:
  - Sửa luồng cập nhật/xóa cart item để tải cart cùng collection item theo user, rồi tìm item ngay trong aggregate đã được quản lý bởi Hibernate; request không còn trộn hai instance cart khác nhau.
  - Giữ nguyên quy tắc sở hữu: cart item không tồn tại hoặc không thuộc cart của user đều trả `404 CART_ITEM_NOT_FOUND`.
  - Sửa STOMP interceptor để đặt principal lên `StompHeaderAccessor` gốc, cho phép Spring lưu user của phiên CONNECT và dùng lại khi SUBSCRIBE.
  - Bổ sung regression test cho cả cart ownership và callback lưu STOMP principal.
  - Commit mã nguồn: `becfcd1` và `7c5d278`.
- Kết quả kiểm thử:
  - `mvnw.cmd -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" "-Dtest=CartServiceTest,CartControllerTest,StompJwtChannelInterceptorTest" test`: `PASS` với 14 test, 0 failure, 0 error, 0 skipped.
  - `mvnw.cmd -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `BUILD SUCCESS` với 185 test được phát hiện, 182 pass, 0 failure, 0 error và 3 PostgreSQL test bị skip do Docker local không chạy.
- Vướng mắc hoặc phần còn thiếu:
  - Frontend cần ghép hai commit sửa này với `fix/storefront-bindings` và chạy lại browser QA-04 để xác nhận hai lỗi tái hiện đã hết trong luồng thật.
- Bước tiếp theo:
  - Push branch, mở PR, rồi nhờ Frontend chạy lại QA-04 sau khi merge.
- Ngày hoàn tất: 2026-10-14, merge commit `70ca4b9`

## QA-05-BE-FIX: Hoàn tồn kho và hoàn tất thanh toán COD

- Trạng thái: `DONE`
- Branch: `fix/order-cancellation-payment`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/60
- Merge commit: `71a50d2`
- Ngày cập nhật: 2026-10-15
- Đã làm:
  - Khi customer hoặc admin hủy đơn hợp lệ, khóa từng bản ghi tồn kho theo thứ tự variant và hoàn lại số lượng vào đúng cặp `Store + ProductVariant` trong cùng transaction.
  - Nếu không tìm thấy cửa hàng thực hiện hoặc bản ghi tồn kho đã giữ, trả lỗi conflict và rollback toàn bộ thao tác hủy.
  - Khi đơn COD chuyển từ `SHIPPING` sang `COMPLETED`, chuyển payment `PENDING` sang `PAID`; không tạo transaction ID giả cho COD.
  - Cập nhật API contract và bổ sung regression test cho hai hành vi FE phát hiện trong QA-05.
- Kết quả kiểm thử:
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" "-Dtest=InventoryServiceTest,AdminOrderServiceTest,CustomerOrderServiceTest" test`: `PASS` với 18 test, 0 failure, 0 error, 0 skipped.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `BUILD SUCCESS` với 187 test được phát hiện, 184 pass, 0 failure, 0 error và 3 PostgreSQL test bị skip do Docker local không chạy.
- Vướng mắc hoặc phần còn thiếu:
  - Không còn blocker trong phạm vi QA-05-BE-FIX; checklist QA-05 đã xác nhận lại hai hành vi qua
    API/browser thật.
- Bước tiếp theo:
  - Ghi nhận kết quả QA-05 tổng hợp trên branch `docs/manual-test-report`.
- Ngày hoàn tất: 2026-10-14

## REL-01-BE-REVIEW: Đối soát release candidate phía Backend

- Trạng thái: `IN PROGRESS`
- Branch release được review: `release/v1.0.0`, commit `0156b4e`.
- Branch cập nhật trạng thái: `docs/release-status-reconciliation`.
- Pull Request REL-01: `Chưa mở`.
- Ngày cập nhật: 2026-10-15.
- Đã làm:
  - Xác nhận QA-05, DOC-01, DOC-02 và DOC-03 đã lần lượt merge qua PR #61 đến PR #64.
  - Review phạm vi REL-01: branch chỉ đổi tài liệu release và nhật ký Frontend, không đổi source runtime.
  - Đối chiếu CI được dẫn trong hồ sơ và xác nhận run `38607996065` thành công trên `2836a1a`.
- Kết quả kiểm thử trên commit release `0156b4e`:
  - Maven `clean verify`: `BUILD SUCCESS`; 187 test được phát hiện, 184 pass, 3 PostgreSQL test skip.
  - Frontend unit test: 18 pass, 0 fail; kiểm tra cú pháp JavaScript: `PASS`.
  - `git diff --check` và kiểm tra liên kết Markdown nội bộ trong phần thay đổi: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Branch release được tạo trước merge DOC-03 nên còn ghi main `2836a1a` và chờ DOC-03; FE cần đồng bộ
    `main` hiện tại `ba1e777`, cập nhật hồ sơ/nhật ký FE rồi mới mở PR REL-01.
  - Chưa có PR REL-01 và chưa có tag `v1.0.0` tại thời điểm review.
- Bước tiếp theo:
  - Merge PR đối soát trạng thái này; FE đồng bộ branch release, sửa thông tin lỗi thời và mở PR REL-01.
  - Chỉ chuyển REL-01 sang `IN REVIEW` khi PR đã mở; chỉ tạo annotated tag sau khi PR được merge.

## QA-05-REVIEW: Xác nhận end-to-end và Cloudinary thật

- Trạng thái: `DONE`
- Branch: `docs/manual-test-report`
- Pull Request: [PR #61](https://github.com/NgocYuh/UTEGear/pull/61), merge commit `56b9e0a`
- Ngày cập nhật: 2026-10-15
- Đã làm:
  - Chạy checklist giao dịch/realtime và companion media suite trên commit main `71a50d2`.
  - Kiểm tra Cloudinary thật theo chuỗi upload → render → replace → render → delete trong folder QA riêng.
  - Xác nhận hủy đơn PENDING hoàn tồn kho và COD COMPLETED chuyển payment sang PAID.
  - Kiểm tra ảnh bằng chứng desktop/mobile và giữ report, log, screenshot ở local.
- Kết quả kiểm thử:
  - Kết quả tổng hợp: `PASS`; checklist `44 PASS`, `0 FAIL`.
  - Companion media thật: `28 PASS`, `0 FAIL`, `0 BLOCKED`; `realMedia=true`.
  - Tồn kho `storeId=100`, `variantId=200`: ban đầu `4`, sau hủy `4`.
  - Đơn COD COMPLETED: `paymentStatus=PAID`, `amount=630000`, `total=630000`.
  - Responsive 1440/768/390, CUSTOMER 403 không mất session và không có browser exception chưa xử lý.
- Vướng mắc hoặc phần còn thiếu:
  - Không còn blocker chức năng; PR báo cáo đã merge.
- Bước tiếp theo:
  - Dùng kết quả đã merge làm bằng chứng cho DOC-01, DOC-02, DOC-03 và REL-01.
- Ngày hoàn tất: 2026-10-15

## DOC-01: Hoàn thiện tài liệu API và kiến trúc

- Trạng thái: `DONE`
- Branch: `docs/api-architecture`
- Pull Request: [PR #62](https://github.com/NgocYuh/UTEGear/pull/62), merge commit `cfac801`
- Ngày cập nhật: 2026-10-15
- Đã làm:
  - Đối chiếu toàn bộ REST mapping trong 14 controller với ba contract API đã merge.
  - Viết lại mục lục API theo 47 endpoint REST đã triển khai và bốn destination/endpoint STOMP.
  - Tách rõ bảy endpoint đã thiết kế trong contract nhưng chưa có controller để không mô tả sai khi demo.
  - Cập nhật kiến trúc modular monolith, ranh giới package, JWT/session, transaction, tồn kho theo
    Store + Variant, Cloudinary, crawler/import, realtime, Flyway và profile chạy.
  - Sửa phần model contract còn dùng ngôn ngữ trước triển khai và liên kết trạng thái implementation.
- Kết quả kiểm thử:
  - Đối chiếu annotation controller bằng `rg`: `PASS`, 47 REST mappings và 20 MVC GET mappings.
  - Parse ba file JSON mock bằng PowerShell `ConvertFrom-Json`: `PASS`.
  - Kiểm tra liên kết Markdown nội bộ trong ba tài liệu DOC-01: `PASS`.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `BUILD SUCCESS`,
    187 test được phát hiện, 184 pass, 0 failure, 0 error và 3 PostgreSQL test skip do Docker local không chạy.
  - `git diff --check`: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Không còn blocker; QA-05 và DOC-01 đã merge đúng thứ tự phụ thuộc.
- Bước tiếp theo:
  - Dùng tài liệu đã merge làm đầu vào cho DOC-02, DOC-03 và REL-01.
- Ngày hoàn tất: 2026-10-15

## DOC-02: Hướng dẫn cài đặt và demo

- Trạng thái: `DONE`
- Branch: `docs/setup-demo-guide`
- Pull Request: [PR #63](https://github.com/NgocYuh/UTEGear/pull/63), merge commit `2836a1a`
- Ngày cập nhật: 2026-10-15
- Đã làm:
  - Viết hướng dẫn Windows PowerShell cho JDK/Maven, cấu hình local và nguyên tắc không commit secret.
  - Ghi rõ cách điền JDBC URL Supabase, username/password riêng, Session pooler cho IPv4 và SSL.
  - Ghi cách tạo JWT secret ngẫu nhiên, cấu hình Cloudinary và WebSocket allowed origins.
  - Hướng dẫn Flyway V1/V2, kiểm tra history/role và cảnh báo không baseline schema tùy tiện.
  - Ghi cách build, chạy app, tạo customer/admin QA an toàn, nạp JSONL và thực hiện kịch bản demo.
  - Bổ sung bảng xử lý lỗi thường gặp và checklist trước khi push/demo.
- Kết quả kiểm thử:
  - Kiểm tra liên kết Markdown nội bộ của README và hai tài liệu setup: `PASS`.
  - Kiểm tra đủ placeholder và quét mẫu credential trong hướng dẫn: `PASS`, không có giá trị thật.
  - `mvn -B --no-transfer-progress "-Dnet.bytebuddy.experimental=true" clean verify`: `BUILD SUCCESS`,
    187 test được phát hiện, 184 pass, 0 failure, 0 error và 3 PostgreSQL test skip do Docker local không chạy.
  - `git diff --check`: `PASS`.
- Vướng mắc hoặc phần còn thiếu:
  - Không còn blocker; QA-05, DOC-01 và DOC-02 đã merge đúng thứ tự phụ thuộc.
- Bước tiếp theo:
  - Dùng hướng dẫn đã merge làm đầu vào cho DOC-03 và REL-01.
- Ngày hoàn tất: 2026-10-15

## FIX-ADMIN-INTEGRATION: Hoàn thiện liên kết quản trị và kiểm tra tích hợp toàn hệ thống

- Trạng thái: `DONE`
- Branch: `fix/system-integration`
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/67
- Merge commit: `5c84f4283455ca264f1cfaf92f86be75f25b4e62`
- Ngày cập nhật: 2026-10-15
- Đã làm:
  - Bổ sung MVC route và giao diện chạy thật cho tổng quan, đơn hàng, sản phẩm, danh mục,
    thương hiệu, cửa hàng + tồn kho, mã giảm giá và tài khoản quản trị; bỏ các liên kết sidebar
    dẫn tới 404.
  - Bổ sung API/service/DTO quản trị cho catalog, dashboard, coupon, inventory và user; toàn bộ
    endpoint nằm dưới `/api/admin/**` và tiếp tục yêu cầu vai trò `ADMIN`.
  - Giữ mô hình `Store + ProductVariant`: sản phẩm mới luôn có biến thể mặc định; tồn kho và điều
    chuyển thao tác theo biến thể, kể cả sản phẩm có nhiều biến thể.
  - Chặn quản trị viên tự khóa tài khoản hoặc tự gỡ vai trò `ADMIN`; khách hàng truy cập trang quản
    trị được chuyển hướng an toàn mà không mất phiên.
  - Cho phép quản trị viên chuyển từ trang quản trị sang storefront trong cùng tab và quay lại
    `/admin` bằng cùng phiên JWT, không phải đăng nhập lại.
  - Chuyển trình quản lý ảnh sang `/admin/products#product-media`, đồng bộ runner QA và tài liệu;
    bổ sung lọc khuyến mãi, route danh mục và dữ liệu cửa hàng/sản phẩm trang chủ.
  - Sửa Maven wrapper trên Windows khi thư mục `.m2` không phải symbolic link hoặc biến `HOME`
    không được truyền vào tiến trình PowerShell con.
  - Commit mã nguồn và kiểm thử: `160c524` (`feat: complete admin system integration`).
- Kết quả kiểm thử:
  - `mvn ... verify`: `BUILD SUCCESS`; 194 test được phát hiện, 191 pass, 0 failure, 0 error và
    3 PostgreSQL Testcontainers test skip vì Docker không chạy.
  - `node --test tests/frontend/integration.test.mjs tests/frontend/mock-data.test.mjs`: `PASS`,
    20/20 test.
  - Kiểm tra cú pháp JavaScript: `PASS`, 32 file.
  - QA-04 bằng Edge headless + H2 cô lập: `PASS`; 87 public, 48 authenticated/integration và
    54 risk checks, không có `FAIL` hoặc `BLOCKED` trong kết quả tổng hợp.
  - `git diff --check`: `PASS` (chỉ có cảnh báo chuyển LF sang CRLF trên Windows).
- Vướng mắc hoặc phần còn thiếu:
  - Chưa kiểm tra mutation Cloudinary thật hoặc Supabase production trong lượt này; QA trình duyệt
    dùng H2 và tài khoản dùng một lần để không chạm secret/dữ liệu cá nhân.
  - Lệnh `clean` trong sandbox từng gặp `AccessDeniedException` khi JDK 26 đóng JAR trong cache
    người dùng; chạy `verify` sau biên dịch thành công và toàn bộ test đã pass. Dự án vẫn đặt Java 21.
- Bước tiếp theo:
  - Đã đối soát `main` khi bắt đầu DATA-03: merge PR #67 chứa commit `160c524`; tiếp tục task dữ liệu riêng.
- Ngày hoàn tất: 2026-10-15

## DATA-03: Thu thập catalog 20–30 sản phẩm mỗi danh mục

- Phụ trách: `@NgocYuh` (Backend)
- Trạng thái: `IN REVIEW`
- Branch: `codex/data-03-catalog-collection`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-15
- Đã làm:
  - Đối chiếu contract: `ban-phim`, `chuot`, `tai-nghe`, `keycap`, `switch`, `phu-kien`; mục tiêu 20–30 sản phẩm cho mỗi loại trên menu, ưu tiên 25.
  - Người dùng xác nhận lấy toàn bộ menu: bàn phím, chuột, tai nghe, keycap, switch, lót chuột, microphone, dây cáp/hub và tay cầm; bốn nhóm cuối dùng category canonical `phu-kien` và được tách file theo nhóm.
  - Chuẩn bị Python 3.12 riêng trong `crawler/.venv/`, không thay đổi cấu hình ứng dụng.
  - Review robots GEARVN/KICAP: chỉ lấy HTML sản phẩm/danh mục công khai, không gọi API, giỏ hàng hoặc tài khoản.
  - Review `https://kicap.vn/dieu-khoan`: nguồn giới hạn việc sử dụng thương mại khi chưa có cho phép bằng văn bản; dataset lượt này chỉ chuẩn bị cục bộ cho review/demo, chưa cấp quyền xuất bản lại nội dung/ảnh.
  - Dữ liệu và script vận hành đặt trong các thư mục `crawler/data/` đã ignore; không commit dữ liệu thật.
  - Hoàn tất 218 sản phẩm: bàn phím 25, chuột 24, tai nghe 24, keycap 25, switch 25, lót chuột 25, microphone 21, dây cáp/hub 29, tay cầm 20. Tổng category `phu-kien` là 95 sản phẩm thuộc bốn nhóm trên menu.
  - Dùng HTML sản phẩm/JSON-LD và sitemap công khai; giãn request tối thiểu 2 giây, cache HTML và kiểm tra robots trước khi tải. Adapter KICAP chỉ dùng trong script vận hành cục bộ của lượt này.
  - Loại 3 bản trùng model (Acer Cestus 330, Acer Galea 550, Ugreen 50570); chuẩn hóa 1 brand AKKO dựa trên tên nguồn và ghi dấu vết thay đổi trong `normalization-report.json`.
  - Loại URL ảnh GIF ở hai record có ảnh PNG thay thế; tách riêng 5 tay cầm chỉ có GIF vì importer chỉ hỗ trợ JPEG/PNG/WebP. Không sửa extension hoặc tự tạo ảnh thay thế.
  - Bàn giao cục bộ: `crawler/data/output/catalog-2026-10-15/products.jsonl`, 9 file JSONL theo nhóm, `catalog-preview.md`, `reference-data.json`, `collection-report.json`, `validation-report.json` và `normalization-report.json`.
  - Bổ sung script vận hành bị ignore `crawler/data/output/import-local.ps1`: đăng nhập ADMIN bằng prompt, tạo category/brand còn thiếu, mặc định import thử 3 record; `-Limit 0` chọn toàn bộ dataset. Chỉ cho phép địa chỉ localhost; không ghi token hoặc mật khẩu vào file.
  - Đối soát task trước `FIX-ADMIN-INTEGRATION` với `main`, cập nhật `DONE` cùng PR #67, merge commit và ngày hoàn tất theo quy tắc repository.
- Kết quả kiểm thử:
  - `python -m unittest discover -s crawler/tests -v`: `PASS`, 7 test; dùng TEMP trong workspace để tránh giới hạn sandbox.
  - Audit 218 record: `PASS`, không có lỗi; cả 9 nhóm đạt 20–30 sản phẩm, file tách khớp file tổng; URL nguồn/SKU/slug duy nhất, giá VND dương, brand bắt buộc, có ảnh HTTPS, thời gian có timezone, đúng một biến thể mặc định và đáp ứng giới hạn độ dài field trong schema.
  - SHA-256 `products.jsonl`: `ba1673fd9ff8926a31d4c7e1649e3ca12d58f2c8b595e0e44a10ff4538729d8d`.
  - Helper PowerShell: syntax `PASS`; kiểm tra bằng API mock cô lập `PASS` cho reference data UTF-8, header ADMIN, multipart 3 record và xuất report. Chưa gọi API import thật.
  - `git diff --check`: `PASS`; dữ liệu, HTML cache, script vận hành và Python runtime đều nằm trong đường dẫn Git ignore.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa import database, tạo inventory hoặc upload Cloudinary; chưa kiểm tra HTTP/MIME/dung lượng từng ảnh. Không chạy build Java vì không thay đổi mã ứng dụng và không push.
  - Trước import local/test cần category và brand active theo `reference-data.json`, cấu hình Cloudinary và review allowlist ảnh: `cdn.hstatic.net`, `product.hstatic.net`, `bizweb.dktcdn.net` (bổ sung host cần thiết qua cấu hình môi trường local).
  - Một số URL timeout/lỗi TLS hoặc là trang danh mục bị bỏ qua; chi tiết lưu trong `collection-report.json`. Trạng thái tồn tại nguồn được lưu riêng, không quy đổi thành tồn kho UTEGear.
  - Chưa mở PR nhật ký; không chuyển `DONE` trước khi hoàn tất quy trình review/merge của repository.
- Bước tiếp theo:
  - Review catalog cục bộ và nhật ký. Việc import local/test, cấp quyền sử dụng nội dung/ảnh khi xuất bản và tạo tồn kho thuộc task vận hành tiếp theo.
- Ngày hoàn tất: `Chưa hoàn tất`

## DATA-04: Đưa sản phẩm demo vào Flash Sale

- Phụ trách: `@NgocYuh` (Backend)
- Trạng thái: `IN REVIEW`
- Branch: `codex/demo-flash-sale`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-15
- Đã làm:
  - Người dùng yêu cầu chọn một vài sản phẩm đã import cho Flash Sale; chọn 4 sản phẩm thuộc bàn phím, chuột, tai nghe và lót chuột.
  - Đối chiếu controller/template: Flash Sale dùng sản phẩm ACTIVE có `salePrice < basePrice`; không có bảng chiến dịch riêng.
  - Chuẩn bị giá giảm khoảng 15%, làm tròn xuống nghìn đồng: ID 18 → 1.010.000đ, ID 44 → 340.000đ, ID 67 → 756.000đ, ID 145 → 416.000đ.
  - Script vận hành và snapshot trước thay đổi nằm trong `crawler/data/output/`, bị Git ignore; đọc cấu hình local trong tiến trình, không ghi hoặc in credential.
  - Đã commit transaction dữ liệu, cập nhật đúng `sale_price` và `updated_at` của 4 sản phẩm đã chọn; không đổi schema hoặc mã ứng dụng.
- Kết quả kiểm thử:
  - Preview PostgreSQL read-only: `PASS`; đúng 4 sản phẩm ACTIVE, giá gốc khớp public API, chưa có sale price và giá mới dương/thấp hơn giá gốc.
  - Transaction PostgreSQL: `PASS`, đúng 4 dòng được cập nhật; có điều kiện giá cũ/trạng thái, row lock và rollback toàn bộ khi điều kiện không khớp.
  - `GET /api/products?onSale=true`: `PASS`, trả đúng 4 ID 18/44/67/145 và sale price dự kiến.
  - HTML trang chủ: `PASS`, section Flash Sale render đủ AULA F75, Logitech G102, AKKO GH300 và HyperWork Fable.
  - `git diff --check` cho nhật ký: `PASS`; không chạy build Java vì không sửa mã ứng dụng và không push.
- Vướng mắc hoặc phần còn thiếu:
  - Chưa mở PR nhật ký; chỉ chuyển `DONE` sau quy trình review/merge theo quy định repository.
- Bước tiếp theo:
  - Người dùng tải lại trang chủ để xem 4 sản phẩm; review nhật ký. Thay đổi giá này chưa có lịch tự hết hạn vì hệ thống hiện dùng `salePrice` cho mục Flash Sale.
- Ngày hoàn tất: `Chưa hoàn tất`

## DATA-05-BE: Bổ sung mô tả và thông số kỹ thuật sản phẩm đã cào

- Phụ trách: `@NgocYuh`
- Trạng thái: `IN REVIEW`
- Branch: `codex/product-content-backend`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-15
- Đã làm:
  - Chốt model/JSONL contract và JSON response mẫu trong `docs/api/model-contract.md`, `docs/crawler/data-contract.md` trước task phụ thuộc `FE-CONTENT-01`.
  - Importer lưu mô tả vào Product và thông số nhãn/giá trị vào `specifications.technicalSpecifications`, giữ metadata nguồn ở cấp ngoài; không đổi schema hoặc giá/biến thể/tồn kho.
  - Parser đọc JSON-LD, specs_table và mô tả public HTML trong RSC Flight; không thực thi JavaScript. Hỗ trợ HTML mô tả/table/bullet của KICAP và giữ đoạn văn thuần.
  - Chuẩn bị bổ sung nội dung từ cache của 218 sản phẩm, không tải lại/upload ảnh.
  - Đã cập nhật mô tả và technicalSpecifications cho đủ 218 sản phẩm hiện có bằng sourceUrl; snapshot `crawler/data/output/catalog-2026-10-15/content-before.json` và payload `content-updates.json` ở workspace chính, bị Git ignore.
  - Ba Razer Seiren V3 Chroma thiếu bảng thông số ở nguồn bán lẻ được bổ sung từ bảng thông số chính hãng Razer dòng RZ19-0506 (24 bit/96 kHz), không dùng dữ liệu dòng 32-Bit DSP.
  - Bổ sung `products-enriched.jsonl` cho các lần import sau. Không đổi giá, 4 sản phẩm Flash Sale, ảnh, biến thể hoặc tồn kho.
  - Web local được chạy lại bằng JAR đã verify của worktree Backend, dùng template/static source của workspace Frontend để kiểm tra tích hợp trên cổng 8080.
- Kết quả kiểm thử:
  - Parser offline và importer có test hồi quy cho lưu description, nested specs, chống ghi đè provenance, dữ liệu lỗi, chunk UTF-8, timestamp trùng Flight ID và nội dung HTML.
  - `python -m unittest discover -s crawler/tests -v`: PASS, 13 test.
  - `mvnw.cmd -B --no-transfer-progress -Dnet.bytebuddy.experimental=true clean verify`: BUILD SUCCESS, 196 test, 193 pass, 0 failure/error, 3 PostgreSQL Testcontainers test skip vì Docker không chạy. Cờ Byte Buddy cần cho JDK 26 theo nhật ký repo; lượt sandbox đầu bị chặn loopback.
  - Preview + transaction + đối soát sau commit PostgreSQL: PASS, đúng 218 nội dung khớp payload; mô tả không rỗng và technicalSpecifications có dữ liệu ở cả 218 sản phẩm.
  - API sản phẩm ID 145: PASS, mô tả 2.582 ký tự, 9 thông số, salePrice vẫn 416.000đ; FE-CONTENT-01 kiểm tra trình duyệt desktop/mobile.
- Vướng mắc hoặc phần còn thiếu:
  - Có 8 mô tả ngắn do nguồn chỉ cung cấp ít thông tin; giữ đúng dữ kiện có nguồn, không tự bổ sung tính năng hoặc kích thước.
  - Chưa mở PR; chưa chuyển DONE trước khi review/merge theo quy định repository.
- Bước tiếp theo:
  - Review branch Backend riêng, liên kết FE-CONTENT-01; metadata truy vết vẫn lưu trong database nhưng không đưa vào bảng thông số của khách hàng.
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

## FIX-STOCK-API: Sửa tồn kho public và đối soát checkout

- Phụ trách: `@NgocYuh`
- Trạng thái: `IN REVIEW`
- Branch: `codex/fix-store-availability`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-16
- Task liên kết: `FE-CHECKOUT-UX`; phụ thuộc commit local DATA-05-BE `6dadac9`, chưa merge main nên task trước vẫn IN REVIEW.
- Bàn giao GitHub: `https://github.com/NgocYuh/UTEGear/tree/codex/fix-store-availability`; push theo yêu cầu người dùng, chưa mở PR/merge.
- Kiểm tra trước push: checkout sạch efc33e0, `mvnw.cmd -B --no-transfer-progress -Dnet.bytebuddy.experimental=true clean verify` BUILD SUCCESS, 199 test, 0 failure/error, 4 PostgreSQL test skip do Docker không sẵn có. Lượt clean đầu bị Windows khóa JAR localhost đang dùng; build lại bằng checkout riêng thành công, giữ ứng dụng đang chạy. Thay đổi sau build chỉ cập nhật nhật ký.
- Đã làm:
  - Tái hiện PostgreSQL lower(bytea) khi availability gọi với province null.
  - Tách query không lọc tỉnh khỏi query lọc tỉnh; không truyền null vào lower(), giữ khóa tồn kho Store + ProductVariant và pessimistic write của thao tác trừ kho.
  - Cập nhật endpoint contract trước phần frontend: bỏ trống/tỉnh trắng trả tất cả cửa hàng hoạt động; không có inventory trả zero/empty array.
  - Người dùng đã yêu cầu tạo 1 cửa hàng demo và 10 chiếc mỗi sản phẩm. Tạo UTEGear Demo (id 1), 218 dòng Store + ProductVariant, tổng 2.180 chiếc trong database dùng thử đã cấu hình; kiểm tra bảng cửa hàng/tồn kho rỗng trước khi ghi và dùng transaction, không reset tồn kho có sẵn. Script/báo cáo vận hành nằm trong crawler/data/output bị ignore, không đưa dữ liệu demo vào migration.
  - Đối chiếu source admin: AdminCatalogService cộng tồn kho từ StoreInventoryRepository; user dùng cùng dữ liệu Store + ProductVariant. Admin quản trị và user mua hàng có giao diện khác theo vai trò.
- Kết quả kiểm thử:
  - Bổ sung test province null/trắng, empty inventory và integration PostgreSQL cho query không lọc/case-insensitive.
  - `mvnw.cmd -B --no-transfer-progress -Dnet.bytebuddy.experimental=true clean verify`: BUILD SUCCESS, 199 test, 0 failure/error, 4 skip vì Docker không sẵn có.
  - Chạy jar đã build trên localhost:8080: availability không truyền tỉnh, tỉnh rỗng và TP.HCM đều HTTP 200 trước seed (stock 0); sau seed sản phẩm 218 stock 10.
  - Trình duyệt Edge thật, tài khoản CUSTOMER demo riêng: đăng nhập, mua ngay sản phẩm 218, chọn địa chỉ/cửa hàng demo, quote COD, tạo đơn PENDING UTE-1792084956-0B8A2C88, giỏ rỗng, kho giảm 10 xuống 9; hủy qua chức năng customer thành CANCELLED và kho hoàn lại 10. Không dùng tài khoản/cart của người dùng.
- Vướng mắc hoặc phần còn thiếu:
  - PostgreSQL Testcontainers chưa chạy do thiếu Docker; đã kiểm tra API và trừ/hoàn kho trên PostgreSQL local đang cấu hình. Chưa mở PR hoặc merge; không đánh dấu DONE.
- Bước tiếp theo:
  - Review backend riêng với FE-CHECKOUT-UX. Runtime hiện dùng jar từ worktree .local-reference/store-availability-backend và templates/static của workspace frontend; chạy lại backend từ branch cũ sẽ không có bản sửa này.
- Ngày hoàn tất: `Chưa hoàn tất`

## DATA-06-BE: Sửa đọc nội dung Flight và tách bảng thông số

- Phụ trách: `@NgocYuh`
- Trạng thái: `IN REVIEW`
- Branch: `codex/fix-product-content-records`
- Pull Request: `Chưa mở`
- Ngày cập nhật: 2026-10-16
- Task liên kết: `FE-CHECKOUT-UX` (phần hiển thị đơn hàng/sản phẩm); tiếp nối DATA-05-BE commit local 6dadac9 chưa merge nên task trước vẫn IN REVIEW.
- Đã làm: Xác định record Flight dạng JSON đóng ngay trước text record không có newline; cập nhật nhận diện boundary và tách bảng thông số khỏi mô tả. API/model contract không thay đổi.
- Bàn giao GitHub: `https://github.com/NgocYuh/UTEGear/tree/codex/fix-product-content-records`; push theo yêu cầu người dùng, chưa mở PR/merge.
- Kiểm tra trước push: `mvnw.cmd -B --no-transfer-progress -Dnet.bytebuddy.experimental=true clean verify` BUILD SUCCESS tại commit 35258be, 196 test, 0 failure/error, 3 PostgreSQL test skip do Docker không sẵn có; Python PASS 14/14. Thay đổi sau build chỉ cập nhật nhật ký.
- Kết quả kiểm thử:
  - Crawler unittest: PASS 14/14; regression record ngay sau JSON, UTF-8 byte length, timestamp boundary, tách bảng khỏi mô tả, giữ labelled notes. Test xuất file ban đầu bị sandbox chặn thư mục temp hệ thống; chạy lại bằng TMP/TEMP trong crawler/data/raw thành công.
  - Đọc lại 218 trang cache, chỉ chuẩn bị 82 bản ghi có thay đổi; đối chiếu tên/source URL trước khi update. Transaction cập nhật description và technicalSpecifications, giữ nguyên giá/ảnh/biến thể/tồn kho/đơn hàng; snapshot trước ghi trong crawler/data/output/catalog-readable-2026-10-16/content-before.json bị ignore. Chạy verify JDBC sau ghi: VERIFIED 82/82.
  - Lexar H31: từ đoạn tóm tắt bị cắt và 1 dòng sai thành mô tả nhiều đoạn và 8 thông số (model, màu, bảo hành, chất liệu, kích thước, tương thích, thương hiệu, cổng kết nối), lấy từ cache đúng sản phẩm, không tự thêm khả năng.
  - File import 218 sản phẩm đã đồng bộ ở crawler/data/output/catalog-readable-2026-10-16/products-enriched.jsonl (ignored). Không thay đổi model/endpoint/JSON contract.
  - FE-CHECKOUT-UX đã kiểm tra trình duyệt thật desktop/mobile: 8 dòng và mô tả >1.000 ký tự hiển thị đúng. Lượt triển khai local trước yêu cầu push chỉ sửa Python nên chưa chạy lại Maven; đã chạy clean verify trước bàn giao GitHub như ghi ở trên.
- Vướng mắc hoặc phần còn thiếu: Chưa mở PR/merge, task giữ IN REVIEW. Nội dung phụ thuộc độ đầy đủ/chính xác của nguồn đã cào.
- Bước tiếp theo: Review riêng DATA-06-BE và liên kết FE-CHECKOUT-UX; chỉ đánh dấu DONE sau merge.
- Ngày hoàn tất: `Chưa hoàn tất`


## BE-LOCAL-DEMO-TOOLS: Bàn giao cách chạy và nhập dữ liệu demo local

- Phụ trách: `@NgocYuh` (Backend).
- Trạng thái: `IN REVIEW`.
- Branch: `codex/local-demo-tools`.
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/68 (PR tích hợp INT-LOCAL-DEMO).
- Ngày cập nhật: 2026-10-16.
- Task liên kết: `INT-LOCAL-DEMO`, `FE-LOCAL-PUBLISH`.
- Đã làm: Đưa script nhập catalog đã dùng local vào scripts/demo, thêm ValidateOnly và giới hạn loopback/no redirects, nhận đường dẫn dữ liệu cục bộ thay vì commit dataset. Hướng dẫn dùng cùng database đang có để giữ catalog/giá/tồn kho và cách nhập sang database QA mới; không đưa cấu hình hoặc dữ liệu tài khoản/địa chỉ/đơn hàng lên Git. Đồng bộ nhật ký DATA-03/DATA-04 và trạng thái PR #67 từ workspace đang chạy.
- Kết quả kiểm thử: PowerShell script: PASS syntax, ValidateOnly không gọi mạng/đăng nhập, giới hạn số dòng, chặn 3 URL không an toàn và catalog rỗng; dữ liệu cục bộ kiểm tra đủ 218 sản phẩm/6 category/45 brand. Python PASS 14/14; clean verify bản tích hợp BUILD SUCCESS, 202 test, 0 failure/error, 4 PostgreSQL skip do thiếu Docker.
- Vướng mắc hoặc phần còn thiếu: Dữ liệu crawler nằm ở output bị ignore; máy mới cần cấu hình riêng và bản sao file JSONL/reference-data nếu dùng database mới.
- Bước tiếp theo: Review trong INT-LOCAL-DEMO và chỉ đánh dấu DONE sau khi merge.
- Ngày hoàn tất: `Chưa hoàn tất`.


## INT-LOCAL-DEMO: Tích hợp đúng bản đang chạy local

- Phụ trách: `@NgocYuh` (backend).
- Trạng thái: `IN REVIEW`.
- Branch: `codex/local-demo-integration`.
- Pull Request: https://github.com/NgocYuh/UTEGear/pull/68.
- Ngày cập nhật: 2026-10-16.
- Task liên kết: `FE-LOCAL-PUBLISH`, `BE-LOCAL-DEMO-TOOLS`, `FE-CHECKOUT-UX`, `FIX-STOCK-API`, `DATA-06-BE`.
- Đã làm: Hợp nhất các task Backend/Frontend từ nhánh riêng vào một checkout hoàn chỉnh; không thay đổi kiến trúc, API hoặc schema. Toàn bộ 128 file templates/static có SHA-256 khớp workspace local đang chạy sau chuẩn hóa CRLF/LF theo Git; ảnh nhị phân khớp nguyên byte. Giữ secret, catalog crawler, tài khoản và đơn hàng ngoài Git. Hướng dẫn ở docs/setup/local-demo-parity.md.
- Kết quả kiểm thử: clean verify BUILD SUCCESS, 202 test, 0 failure/error, 4 PostgreSQL skip do thiếu Docker; JS PASS 41/41, crawler PASS 14/14, script PowerShell PASS. JAR chạy riêng cổng 18083 không override templates/static: 4 public API trả dữ liệu giống localhost:8080, 10 asset đóng gói (JS/CSS/banner) khớp local; API admin vẫn bảo vệ, home desktop/mobile và Lexar 8 thông số đúng, không tràn ngang, không có browser exception. Đã xem ảnh screenshot; kiểm tra chỉ đọc, không tạo/đổi đơn hàng hoặc tồn kho. Harness cần tải eager ảnh lazy để chụp toàn trang.
- Vướng mắc hoặc phần còn thiếu: Chưa merge main. GitHub chứa đầy đủ code và hướng dẫn; dùng cùng cấu hình/database development hiện có để giữ sản phẩm, ảnh, Flash Sale và tồn kho. Dataset không nằm trong Git.
- Bước tiếp theo: Review PR #68; chỉ đánh dấu DONE sau merge. Nhánh tích hợp đã push, sau build chỉ sửa nhật ký và merge các cập nhật nhật ký, không đổi runtime source.
- Ngày hoàn tất: `Chưa hoàn tất`.

## CHAT-BE-01: Nhân viên cửa hàng và nền tảng chat hỗ trợ

- Phụ trách: `@NgocYuh` (Backend).
- Trạng thái: `IN PROGRESS`.
- Branch: `feat/employee-support-chat-backend`.
- Pull Request: `Chưa mở`.
- Ngày cập nhật: 2026-10-16.
- Đã làm: Tạo migration V3 thêm role `EMPLOYEE`, gán một nhân viên theo cửa hàng, bảng hội thoại/tin nhắn và index PostgreSQL. Thêm API mở/xem/nhận/gửi/đóng hội thoại, API admin phân công cửa hàng cho nhân viên, và realtime `/user/queue/support` publish sau commit.
- Contract: `docs/api/support-chat-contract.md`, `docs/api/mock/support-chat.json`; endpoint/model contract đã liên kết trước khi Frontend bắt đầu task giao diện.
- Kết quả kiểm thử: `mvnw.cmd -B --no-transfer-progress -Dnet.bytebuddy.experimental=true clean verify`: BUILD SUCCESS; toàn bộ Surefire report không có failure/error, 4 test PostgreSQL skip vì Docker không sẵn có. Test riêng phân quyền Admin và STOMP queue: PASS 8/8.
- Vướng mắc hoặc phần còn thiếu: Giao diện chat nổi, inbox nhân viên, JavaScript/STOMP client và responsive/accessibility là task Frontend riêng `CHAT-FE-01`, phụ thuộc contract này. Không có dữ liệu nhân viên production được seed.
- Bước tiếp theo: Chạy full verify backend, mở PR `CHAT-BE-01`; sau đó Frontend triển khai `CHAT-FE-01` trên contract đã chốt.
- Ngày hoàn tất: `Chưa hoàn tất`.
