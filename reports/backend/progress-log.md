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

- Trạng thái: `IN REVIEW`
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

- Trạng thái: `IN REVIEW`
- Branch: `feat/wishlist`
- Pull Request: `Chưa mở`
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
- Ngày hoàn tất: `Chưa merge`

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
