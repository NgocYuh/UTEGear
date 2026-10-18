# Hợp đồng API: Trợ Lý Tư Vấn Gear Thông Minh (Smart Gear Consultant)

Tài liệu đặc tả hợp đồng API cho chức năng Chatbox tư vấn cấu hình Gaming Gear và Combo theo nhu cầu game, form tay, ngân sách và đối soát trực tiếp với tồn kho thực tế tại từng chi nhánh.

## 1. Lấy danh sách câu hỏi nhanh (Quick Prompts)

- **Endpoint:** `GET /api/consultant/quick-prompts`
- **Quyền truy cập:** Public (không yêu cầu đăng nhập)
- **Response:** `200 OK`
```json
[
  {
    "id": "combo-fps-danang",
    "title": "Combo FPS tay nhỏ dưới 1.5tr (Đà Nẵng)",
    "prompt": "Tui hay chơi game FPS (CS2/Valorant), tay nhỏ, ngân sách dưới 1.5 triệu, đề xuất combo chuột và phím phù hợp ở chi nhánh Đà Nẵng",
    "icon": "bi-crosshair"
  }
]
```

## 2. Tư vấn trực tiếp qua Chatbox (Consultation Chat)

- **Endpoint:** `POST /api/consultant/chat`
- **Quyền truy cập:** Public (không yêu cầu đăng nhập)
- **Request Headers:** `Content-Type: application/json`
- **Request Body:**
```json
{
  "message": "Tui hay chơi game FPS (CS2/Valorant), tay nhỏ, ngân sách dưới 1.5 triệu, đề xuất combo chuột và phím phù hợp ở chi nhánh Đà Nẵng",
  "storeId": 3
}
```
- **Validation:**
  - `message`: Bắt buộc, tối đa 1000 ký tự.
  - `storeId`: Tùy chọn (nếu null, hệ thống tự động bóc tách từ tên tỉnh/thành trong câu hỏi).

- **Response:** `200 OK`
```json
{
  "reply": "Chào bạn! Dựa trên nhu cầu chiến game FPS (CS2/Valorant) với form tay nhỏ và ngân sách khoảng 1.500.000₫ tại chi nhánh **UTEGear Demo Đà Nẵng**, UTEGear đề xuất **combo tối ưu sau:**...",
  "detectedIntent": "Combo Chuột + Phím • FPS (CS2/Valorant) • Tay nhỏ/Siêu nhẹ • Ngân sách ≤ 1.500.000₫",
  "store": {
    "id": 3,
    "name": "UTEGear Demo Đà Nẵng",
    "province": "Đà Nẵng",
    "district": "Hải Châu"
  },
  "budget": 1500000.00,
  "totalEstimate": 1298000.00,
  "products": [
    {
      "id": 101,
      "name": "Chuột Gaming Logitech G102 Lightsync",
      "slug": "chuot-gaming-logitech-g102-lightsync",
      "categoryName": "Chuột Gaming",
      "categorySlug": "mouse",
      "brandName": "Logitech",
      "basePrice": 450000.00,
      "salePrice": 399000.00,
      "effectivePrice": 399000.00,
      "primaryImageUrl": "https://res.cloudinary.com/...",
      "availableQuantity": 10,
      "matchScore": 135,
      "matchReason": "Cảm biến độ chính xác cao chuẩn FPS, Form nhỏ gọn siêu nhẹ, kiểm soát chuột linh hoạt không mỏi tay • Sẵn hàng tại UTEGear Demo Đà Nẵng"
    }
  ],
  "nextSuggestions": [
    "Nâng ngân sách lên 2 triệu xem combo cao cấp hơn",
    "Tư vấn riêng chuột không dây chơi FPS"
  ]
}
```
