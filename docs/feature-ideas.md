# Tổng hợp ý tưởng tính năng mở rộng UTEGear

Ngày tổng hợp: 15/10/2026.

Tài liệu tập hợp đủ 24 ý tưởng đã trao đổi để nhóm lựa chọn tính năng sáng tạo cho đồ án. Đây là danh sách đề xuất, chưa phải backlog đã duyệt hoặc cam kết triển khai. Độ khó là ước lượng tương đối theo phạm vi mô tả, chưa phải ước lượng thời gian.

## 1. Cơ sở lựa chọn

Theo nội dung môn học người dùng cung cấp, các phần đã được học hoặc có ví dụ gồm Java/Web, MVC và kiến trúc 3 tầng, Servlet, Session/Cookie/Filter, JSP/EL/JSTL, JDBC, JPA/Hibernate, Spring, Thymeleaf, REST API, bảo mật, Redis, upload/Cloudinary, email và quản lý dự án. Các ví dụ bán hàng gồm CRUD, đăng nhập/phân quyền, catalog/tìm kiếm/phân trang, giỏ hàng, thanh toán, theo dõi đơn và khôi phục tài khoản.

Các ý tưởng dưới đây không được nêu trực tiếp trong danh sách đó. Việc được công nhận là sáng tạo hoặc tính điểm vẫn phụ thuộc đề project và tiêu chí của giảng viên. Không cần dùng công nghệ mới để tạo ra nghiệp vụ có giá trị riêng.

UTEGear hiện có nền tảng catalog, biến thể, tồn kho theo chi nhánh, wishlist, cart, đặt hàng, thông báo đơn qua WebSocket và crawler/import. Tham khảo [tổng quan kiến trúc](architecture/system-overview.md) và [hợp đồng endpoint](api/endpoint-contract.md) khi chốt phạm vi; sự xuất hiện trong contract không đồng nghĩa endpoint đã được triển khai.

## 2. Danh sách tổng hợp

### A. Tư vấn và lựa chọn sản phẩm

| ID | Tính năng | Cách hoạt động và điểm demo | Độ khó |
|---|---|---|---|
| YT-01 | Tư vấn bộ gear theo nhu cầu và ngân sách | Khách chọn ngân sách, nhu cầu, các món cần mua, sở thích và chi nhánh. Hệ thống đề xuất tối đa ba bộ, tổng tiền và lý do lựa chọn; cho thay từng món và thêm cả bộ vào giỏ. | Vừa–cao |
| YT-02 | So sánh sản phẩm theo thông số chuyên biệt | Chọn các sản phẩm cùng nhóm, đối chiếu switch, layout, kết nối, trọng lượng… và làm nổi bật khác biệt. | Vừa |
| YT-03 | Kiểm tra tương thích phụ kiện | Chọn thiết bị đang dùng và phụ kiện; trả về phù hợp, cần adapter hoặc chưa đủ dữ liệu, kèm giải thích từ quy tắc đã xác minh. | Vừa–cao |
| YT-04 | Hồ sơ chọn chuột theo bàn tay | Nhập kích thước tay và kiểu cầm palm/claw/fingertip; lọc chuột theo kích thước và quy tắc tư vấn, giải thích lý do đề xuất. | Vừa |
| YT-05 | Thư viện âm thanh bàn phím | Nghe bản thu từng bàn phím/switch và nghe đối chiếu hai mẫu; hiển thị điều kiện thu để khách hiểu giới hạn so sánh. | Vừa |
| YT-06 | Tự phối bàn phím custom | Chọn kit, switch, keycap; kiểm tra socket, layout, số switch cần mua và tính tổng tiền. | Cao |
| YT-07 | Gợi ý thay thế khi một món hết hàng | Từ giỏ hàng, đề xuất biến thể hoặc sản phẩm có thông số gần nhất; hiển thị chênh lệch giá và yêu cầu khách xác nhận trước khi thay. | Vừa |

### B. Theo dõi giá và tình trạng hàng

| ID | Tính năng | Cách hoạt động và điểm demo | Độ khó |
|---|---|---|---|
| YT-08 | Đăng ký báo có hàng theo biến thể và chi nhánh | Theo dõi đúng màu/switch/layout tại cửa hàng khách muốn mua; thông báo khi biến thể có hàng trở lại. | Vừa |
| YT-09 | Theo dõi giá mục tiêu | Khách đặt mức giá mong muốn cho biến thể; hệ thống theo dõi giá và thông báo khi đạt ngưỡng, tránh gửi trùng. | Vừa |
| YT-10 | Đặt trước sản phẩm sắp về | Khách đăng ký biến thể; quản lý thứ tự và phân bổ khi hàng về, bao gồm trường hợp lượng hàng ít hơn số đăng ký. | Cao |
| YT-11 | Đặt giữ hàng để đến thử | Giữ biến thể tại cửa hàng trong thời hạn ngắn, tự giải phóng khi hết hạn; kiểm soát trường hợp hai khách cùng giữ món cuối. | Cao |

### C. Mua sắm theo nhóm và cộng đồng

| ID | Tính năng | Cách hoạt động và điểm demo | Độ khó |
|---|---|---|---|
| YT-12 | Mua chung theo nhóm sinh viên | Tạo nhóm mua một biến thể, chia sẻ mã mời; đạt số người trong thời hạn thì hưởng giá nhóm. Hiển thị tiến độ và xử lý nhóm không đủ điều kiện. | Cao |
| YT-13 | Chia tiền bộ gear | Tạo danh sách mua chung, gán món cho từng người và tính phần tiền mỗi người cần trả; hỗ trợ nhóm bạn mua chung để đạt ưu đãi. | Vừa |
| YT-14 | Bộ sưu tập góc máy cộng đồng | Người dùng đăng ảnh setup, gắn sản phẩm UTEGear; người khác lưu hoặc bình chọn. Admin duyệt nội dung trước khi công khai theo chính sách đã chốt. | Vừa–cao |

### D. Sau bán hàng và hỗ trợ

| ID | Tính năng | Cách hoạt động và điểm demo | Độ khó |
|---|---|---|---|
| YT-15 | Bảo hành điện tử theo sản phẩm đã mua | Tạo yêu cầu từ đơn hàng, theo dõi tiếp nhận, kiểm tra và xử lý; kiểm soát quyền sở hữu và điều kiện bảo hành. | Vừa–cao |
| YT-16 | Hộ chiếu thiết bị cá nhân | Tập hợp thiết bị đã mua, ngày mua, hạn bảo hành và lịch sử hỗ trợ trong một hồ sơ cá nhân. | Vừa |
| YT-17 | Thu cũ đổi mới có thẩm định | Khách khai tình trạng thiết bị và gửi ảnh; cửa hàng kiểm tra, báo mức hỗ trợ đổi mới; khách chấp nhận hoặc từ chối. | Cao |
| YT-18 | Đổi trả và theo dõi hoàn tiền | Chọn món từ đơn đã nhận, khai lý do, gửi bằng chứng và theo dõi xử lý; áp dụng quy tắc thời hạn, số lượng và quyền sở hữu. | Vừa–cao |
| YT-19 | Trợ lý xử lý sự cố thiết bị | Chọn triệu chứng, trả lời các câu hỏi theo cây quyết định; nhận hướng dẫn hoặc tạo yêu cầu hỗ trợ nếu chưa giải quyết được. | Vừa |

### E. Trải nghiệm và vận hành cửa hàng

| ID | Tính năng | Cách hoạt động và điểm demo | Độ khó |
|---|---|---|---|
| YT-20 | Đặt lịch trải nghiệm gear tại cửa hàng | Chọn sản phẩm muốn thử, chi nhánh và khung giờ; kiểm soát số chỗ và tránh nhận lịch vượt khả năng phục vụ. | Vừa |
| YT-21 | Mượn thiết bị dùng thử | Đăng ký mượn gear, quản lý lịch nhận/trả và ghi nhận tình trạng thiết bị. | Cao |
| YT-22 | Bốc số tư vấn tại cửa hàng | Khách chọn nhu cầu, lấy số và xem lượt chờ; nhân viên gọi lượt, khách nhận cập nhật realtime. | Vừa |
| YT-23 | Yêu cầu chuyển hàng giữa chi nhánh | Tạo yêu cầu khi chi nhánh khách chọn hết hàng nhưng nơi khác còn; bên gửi xác nhận xuất chuyển, bên nhận kiểm hàng và xác nhận nhận. | Cao |
| YT-24 | Kiểm kê bằng mã QR | Nhân viên quét mã biến thể và nhập số lượng thực tế tại chi nhánh; lập phiếu chênh lệch để admin duyệt điều chỉnh. | Vừa–cao |

## 3. Các hướng nên ưu tiên

| Mục tiêu của nhóm | Lựa chọn phù hợp | Lý do và giới hạn cần lưu ý |
|---|---|---|
| Một tính năng chính có nghiệp vụ và dễ giải thích | YT-01 — Tư vấn bộ gear | Tận dụng catalog, biến thể và tồn kho; cần chuẩn hóa thông số và xây quy tắc chấm điểm. |
| Phạm vi vừa sức, demo rõ ràng | YT-08 — Báo có hàng | Tận dụng tồn kho và thông báo; phải xử lý chuyển trạng thái và tránh thông báo trùng. |
| Theo dõi có điều kiện, dễ dựng tình huống demo | YT-09 — Giá mục tiêu | Có thể đổi giá trong admin và quan sát thông báo; cần lịch sử giá và quy tắc gửi lại. |
| Có bản sắc sinh viên | YT-12 — Mua chung | Có tiến độ nhóm và hạn chót; cần chốt quy tắc giá, tồn kho và nhóm thất bại. |
| Giao diện và nội dung trực quan | YT-14 — Góc máy cộng đồng | Có ảnh setup và sản phẩm gắn kèm; cần kiểm soát quyền nội dung và quy trình duyệt. |
| Đặc trưng sản phẩm gear | YT-05 hoặc YT-06 | Âm thanh bàn phím tạo trải nghiệm nghe; phối custom có logic tương thích sâu hơn. |
| Logic rõ, ít phụ thuộc dịch vụ ngoài | YT-19 — Trợ lý xử lý sự cố | Dùng cây quyết định; chất lượng phụ thuộc nội dung hướng dẫn đã kiểm chứng. |
| Thể hiện mô hình chuỗi cửa hàng | YT-23 — Chuyển hàng | Có vòng đời yêu cầu và đối soát hàng; cần quản lý hàng đang chuyển và chống cập nhật tồn sai. |

Nhóm nên chọn một tính năng chính, hoàn thành trọn luồng và kiểm thử trước khi mở rộng. Chưa có quyết định chọn tính năng nào trong tài liệu này.

## 4. Phạm vi bản đầu cho các hướng nổi bật

### YT-01 — Tư vấn bộ gear theo nhu cầu và ngân sách

- Đầu vào: ngân sách, nhu cầu học tập/lập trình/chơi game/sử dụng hỗn hợp, nhóm món cần mua, sở thích và chi nhánh.
- Đầu ra: tối đa ba bộ, tổng tiền, lý do chọn và khả năng thay từng món.
- Dùng quy tắc chấm điểm trong Java/Spring; không cần tích hợp AI bên ngoài.
- Không đề xuất biến thể ngừng bán hoặc hết hàng tại chi nhánh đã chọn.
- Không vượt ngân sách; nếu không có kết quả, giải thích điều kiện cần nới.
- Không suy diễn thuộc tính khi thiếu dữ liệu. Kiểm tra lại giá và tồn kho khi thêm vào giỏ và lúc đặt hàng.
- Demo: đổi ngân sách hoặc nhu cầu để quan sát bộ gợi ý và lý do thay đổi.

### YT-08 — Báo có hàng

- Một đăng ký gắn với người dùng, chi nhánh và biến thể cụ thể.
- Cho phép đăng ký và hủy theo dõi.
- Thông báo khi trạng thái chuyển từ hết hàng sang có hàng; chốt rõ đăng ký dùng một lần hay tiếp tục theo dõi.
- Demo: đăng ký một biến thể hết hàng, cập nhật tồn kho qua luồng được cấp quyền và nhận thông báo.

### YT-09 — Theo dõi giá mục tiêu

- Lưu biến thể và ngưỡng giá khách mong muốn.
- Lưu lịch sử giá cần thiết để đánh giá điều kiện và giải thích thông báo.
- Chốt cách tính giá theo khuyến mãi và quy tắc gửi lại khi giá dao động quanh ngưỡng.
- Demo: giảm giá đến ngưỡng, nhận một thông báo và kiểm tra không phát lặp ngoài ý muốn.

### YT-12 — Mua chung theo nhóm sinh viên

- Giới hạn một nhóm cho một biến thể tại một chi nhánh, một mức giá nhóm và một hạn chót.
- Tạo nhóm, tham gia bằng mã mời, xem tiến độ và chốt kết quả đạt/không đạt.
- Chốt thời điểm giữ tồn kho, số lượng mỗi người, quyền rời nhóm và thời điểm xác nhận giá.
- Bản đầu có thể chưa tích hợp thanh toán trực tuyến; vẫn phải quy định cách tạo đơn và tránh tạo trùng khi nhóm thành công.
- Demo hai nhóm: một nhóm đạt điều kiện và một nhóm hết hạn không đủ người.

### YT-14 — Bộ sưu tập góc máy cộng đồng

- Bản đầu gồm đăng ảnh, gắn sản phẩm, lưu bộ sưu tập và admin duyệt nội dung.
- Có thể mở rộng bình luận và bình chọn sau.
- Làm rõ quyền sử dụng ảnh, quyền sửa/xóa bài và trạng thái duyệt.
- Demo một bài chờ duyệt được công khai sau khi admin chấp thuận.

### YT-05 — Thư viện âm thanh bàn phím

- Lưu bản thu, thông tin bàn phím/switch và điều kiện thu.
- Cho nghe từng mẫu và nghe đối chiếu hai mẫu.
- Chuẩn bị bản thu có quyền sử dụng, điều kiện thu tương đối thống nhất; nói rõ âm thanh thực tế còn phụ thuộc thiết bị nghe và cấu hình bàn phím.
- Demo hai mẫu có đặc tính âm thanh khác nhau cùng phần mô tả điều kiện thu.

### YT-06 — Tự phối bàn phím custom

- Giới hạn bản đầu ở bàn phím cơ hot-swap và danh mục linh kiện được kiểm soát.
- Chọn kit, switch, keycap; kiểm tra socket, layout và số switch cần dùng.
- Quy tắc tương thích phải được kiểm chứng. Dữ liệu thiếu trả về “chưa xác định”.
- Demo một bộ hợp lệ và một lựa chọn bị cảnh báo không tương thích.

### YT-19 — Trợ lý xử lý sự cố

- Chọn một số triệu chứng phổ biến và xây cây câu hỏi có điểm kết thúc rõ ràng.
- Nội dung hướng dẫn cần được kiểm chứng; không yêu cầu khách thực hiện thao tác có thể làm hỏng thiết bị hoặc ảnh hưởng bảo hành.
- Khi chưa xử lý được, chuyển thông tin đã trả lời sang yêu cầu hỗ trợ.
- Demo một trường hợp giải quyết bằng hướng dẫn và một trường hợp chuyển hỗ trợ.

### YT-23 — Chuyển hàng giữa chi nhánh

- Luồng tối thiểu: yêu cầu → xác nhận → xuất chuyển → nhận hàng.
- Chốt cách ghi nhận tồn khả dụng, hàng đang chuyển, hủy yêu cầu và chênh lệch lúc nhận.
- Không cộng hàng vào chi nhánh nhận trước khi xác nhận nhận hàng; không trừ hai lần tại chi nhánh gửi.
- Demo đầy đủ vòng đời cùng số lượng tồn ở hai chi nhánh.

## 5. Các ý tưởng gần nhau nhưng có phạm vi khác

- **Báo có hàng / đặt trước / giữ hàng:** báo có hàng chỉ theo dõi; đặt trước quản lý đăng ký và phân bổ đợt hàng về; giữ hàng tạm dành hàng đang có cho khách trong thời hạn.
- **Mua chung / chia tiền:** mua chung có điều kiện đạt giá nhóm; chia tiền tập trung phân bổ chi phí. Việc tích hợp thanh toán là phạm vi riêng cần chốt.
- **Bảo hành điện tử / hộ chiếu thiết bị:** bảo hành quản lý yêu cầu xử lý; hộ chiếu tập hợp hồ sơ và lịch sử thiết bị, có thể liên kết tới yêu cầu bảo hành.
- **Đặt lịch trải nghiệm / mượn dùng thử / giữ hàng:** đặt lịch quản lý khung giờ; mượn quản lý nhận/trả thiết bị; giữ hàng quản lý lượng hàng tạm dành cho khách.
- **So sánh / kiểm tra tương thích / tư vấn:** so sánh trình bày khác biệt; tương thích kiểm tra quy tắc kỹ thuật; tư vấn xếp hạng lựa chọn theo nhu cầu.

## 6. Nguyên tắc khi chuyển thành task

1. Chốt mục tiêu, luồng demo, phạm vi bản đầu và tiêu chí chấp nhận trước khi triển khai; không tự thay đổi kiến trúc đã duyệt.
2. Backend `@NgocYuh` phụ trách model/database, Flyway, API, nghiệp vụ, bảo mật và kiểm thử backend. Frontend `@trongsonho` phụ trách giao diện, tương tác, gọi API, responsive/accessibility và kiểm thử frontend.
3. Tách Backend và Frontend thành task/branch/PR riêng, liên kết bằng Task ID. Backend cập nhật model contract, endpoint contract và JSON mẫu trước; Frontend phát triển theo mock data của contract.
4. Tồn kho luôn theo `Store + ProductVariant`. Các luồng giữ, mượn, mua chung hoặc chuyển hàng phải chốt riêng cách tính hàng khả dụng và chống xử lý trùng.
5. Tạo migration Flyway mới khi thay đổi schema, không sửa migration đã merge. Các API admin luôn yêu cầu `ADMIN`; dữ liệu cá nhân phải kiểm tra quyền sở hữu.
6. Ảnh tích hợp Cloudinary phải lưu cả `secureUrl` và `publicId`. Không tạo dữ liệu mẫu trực tiếp trong production.
7. Có kiểm thử phù hợp: quyền truy cập, điều kiện biên, thiếu dữ liệu, hết hạn và thao tác đồng thời khi có ảnh hưởng đến tồn kho hoặc tiền. Chạy tối thiểu `./mvnw -B clean verify` trước khi push.
8. Khi task đổi trạng thái, cập nhật nhật ký đúng vai trò và tuân thủ quy tắc tiến độ/review của repository cùng chỉ dẫn người dùng đang áp dụng.

Tài liệu này chỉ tổng hợp ý tưởng; không thay đổi source, schema hoặc trạng thái hoàn tất của bất kỳ tính năng nào.
