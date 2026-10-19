# Hợp đồng API: Thử nghiệm Âm thanh Switch Bàn phím (Switch Sound Tester)

Ngày ban hành: 18/10/2026.  
Phạm vi: Trải nghiệm tương tác âm thanh switch bàn phím cơ (`Interactive Sound Test & Virtual Switch Tester`), mở rộng từ `YT-05`.

---

## 1. Quy ước chung

- Base path: `/api/products`
- Quyền truy cập: `PUBLIC` (không yêu cầu JWT đăng nhập).
- Response format: `application/json;charset=UTF-8`.
- Media: Mọi file audio mẫu switch bắt buộc lưu trữ theo quy tắc Cloudinary gồm cả `secureUrl` và `publicId`.
- Tích hợp Web Audio API: DTO cung cấp `synthProfile` chứa các tham số âm học chuẩn hóa (tần số cơ bản, tiếng click, tiếng khấc tactile, độ giảm chấn) để bộ tổng hợp âm thanh `AudioContext` tại client có thể phát âm thanh độ trễ 0ms ngay cả khi không tải được audio ngoài.

---

## 2. Danh mục Endpoints

### 2.1. Lấy danh mục tất cả các mẫu Switch chuẩn
- **Method:** `GET`
- **Path:** `/api/products/switch-sounds`
- **Quyền:** `PUBLIC`
- **Mô tả:** Trả về danh sách tất cả các Switch Sound Profiles tiêu chuẩn được hỗ trợ (Red, Blue, Brown, Silver, Silent Red) cùng thông số kỹ thuật và điều kiện thu âm.

#### Response `200 OK`:
```json
[
  {
    "code": "RED_LINEAR",
    "name": "Red Switch (Linear)",
    "switchType": "Linear",
    "actuationForceG": 45,
    "actuationPointMm": 2.0,
    "totalTravelMm": 4.0,
    "noiseLevel": "Êm",
    "soundCharacter": "Trơn mượt, không khấc, âm trầm nhẹ (thocky), phù hợp chơi game tốc độ và văn phòng.",
    "recordingCondition": "Case nhôm CNC, plate PC, keycap PBT Cherry Profile, mic condenser cách 25cm.",
    "audioSecureUrl": "https://res.cloudinary.com/demo/video/upload/v1/utegear/switches/red-linear.mp3",
    "audioPublicId": "utegear/switches/red-linear",
    "colorHex": "#E53E3E",
    "synthProfile": {
      "baseFrequency": 180,
      "clickFrequency": 0,
      "clickGain": 0.0,
      "decayMs": 95,
      "thockFactor": 1.2
    }
  }
]
```

---

### 2.2. Lấy cấu hình Switch Tester cho sản phẩm cụ thể
- **Method:** `GET`
- **Path:** `/api/products/{id}/switch-sounds`
- **Quyền:** `PUBLIC`
- **Mô tả:** Kiểm tra sản phẩm có hỗ trợ tính năng thử switch hay không (thuộc danh mục `ban-phim` hoặc có thông số kỹ thuật/tên liên quan đến bàn phím cơ). Tự động khớp các `ProductVariant` của sản phẩm vào từng profile switch tương ứng dựa trên `optionValues` hoặc `sku`.

#### Response `200 OK` (Sản phẩm là bàn phím cơ):
```json
{
  "productId": 145,
  "productName": "Bàn phím cơ Akko 3087 DS Midnight",
  "categorySlug": "ban-phim",
  "supported": true,
  "defaultSwitchCode": "RED_LINEAR",
  "disclaimer": "Âm thanh thực tế phụ thuộc vào chất liệu vỏ phím, tấm plate, keycap và thiết bị nghe của bạn.",
  "profiles": [
    {
      "code": "RED_LINEAR",
      "name": "Red Switch (Linear)",
      "switchType": "Linear",
      "actuationForceG": 45,
      "actuationPointMm": 2.0,
      "totalTravelMm": 4.0,
      "noiseLevel": "Êm",
      "soundCharacter": "Trơn mượt, không khấc, âm trầm nhẹ, phù hợp chơi game tốc độ và văn phòng.",
      "recordingCondition": "Case nhôm CNC, plate PC, keycap PBT Cherry Profile, mic condenser cách 25cm.",
      "audioSecureUrl": "https://res.cloudinary.com/demo/video/upload/v1/utegear/switches/red-linear.mp3",
      "audioPublicId": "utegear/switches/red-linear",
      "colorHex": "#E53E3E",
      "synthProfile": {
        "baseFrequency": 180,
        "clickFrequency": 0,
        "clickGain": 0.0,
        "decayMs": 95,
        "thockFactor": 1.2
      },
      "matchedVariantIds": [201]
    },
    {
      "code": "BLUE_CLICKY",
      "name": "Blue Switch (Clicky)",
      "switchType": "Clicky",
      "actuationForceG": 50,
      "actuationPointMm": 2.2,
      "totalTravelMm": 4.0,
      "noiseLevel": "Đanh / Tiếng Click rõ",
      "soundCharacter": "Khấc bấm rõ rệt và tiếng click đanh đặc trưng, gõ nảy tay.",
      "recordingCondition": "Case nhôm CNC, plate PC, keycap PBT Cherry Profile, mic condenser cách 25cm.",
      "audioSecureUrl": "https://res.cloudinary.com/demo/video/upload/v1/utegear/switches/blue-clicky.mp3",
      "audioPublicId": "utegear/switches/blue-clicky",
      "colorHex": "#3182CE",
      "synthProfile": {
        "baseFrequency": 220,
        "clickFrequency": 3200,
        "clickGain": 0.7,
        "decayMs": 110,
        "thockFactor": 0.8
      },
      "matchedVariantIds": [202]
    }
  ]
}
```

#### Response `200 OK` (Sản phẩm không phải bàn phím, ví dụ Chuột hoặc Tai nghe):
```json
{
  "productId": 102,
  "productName": "Chuột Gaming Logitech G102 Lightsync",
  "categorySlug": "chuot",
  "supported": false,
  "defaultSwitchCode": null,
  "disclaimer": "Sản phẩm này không hỗ trợ tính năng thử nghiệm switch bàn phím.",
  "profiles": []
}
```

#### Response `404 Not Found` (Sản phẩm không tồn tại hoặc đã ngừng kinh doanh):
```json
{
  "timestamp": "2026-10-18T14:30:00Z",
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "Không tìm thấy sản phẩm",
  "path": "/api/products/9999/switch-sounds"
}
```
