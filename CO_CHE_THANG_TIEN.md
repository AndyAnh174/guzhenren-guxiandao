# Cơ Chế Thăng Tiên (Ascension System)

Hệ thống Thăng Tiên cho phép người chơi vượt qua giới hạn Ngũ Chuyển (zhuanshu=5.0) của mod gốc để trở thành **Cổ Tiên (zhuanshu=6.0)**. Quá trình này diễn ra qua 4 giai đoạn (Phase) nghiêm ngặt.

## 📋 Điều Kiện Bắt Đầu
Để có thể bắt đầu thăng tiên (`startAscension`), người chơi cần đạt:
- **Chuyển số (`zhuanshu`):** $\ge 5.0$
- **Giai đoạn trong chuyển (`jieduan`):** $\ge 4.0$
- **Trạng thái:** Chưa từng thăng tiên thành công (không có Phúc Địa).

---

## 🌀 Quy Trình Thăng Tiên (4 Phase)

### Phase 1: Phá Toái Tiên Khiếu
- **Mô tả:** Người chơi phá vỡ giới hạn cơ thể, bay vút lên không trung.
- **Hiệu ứng:** 
    - Nhận hiệu ứng `Levitation 4` trong 80 ticks.
    - Chạy animation `dazuo3`.
    - Bị làm chậm (`MOVEMENT_SLOWDOWN`) và phát sáng (`GLOWING`).
- **Kết thúc:** Tự động chuyển sang Phase 2 sau 80 ticks.

### Phase 2: Nạp Khí (Giai đoạn thử thách)
- **Mô tả:** Trạng thái thiền định trên không trung để hấp thụ Thiên, Địa, Nhân Khí.
- **Cơ chế:**
    - **Hover:** Trạng thái không trọng lực (`setNoGravity(true)`) và vận tốc bằng 0.
    - **Thời gian:** Cần duy trì thiền định trong **3 phút (3600 ticks)**.
    - **Thử thách (Thiên Kiếp/Địa Tai):** Cứ mỗi **30 giây**, hệ thống sẽ spawn các mob tấn công người chơi dựa trên lượng Khí đã hấp thụ:
        - **Thiên Kiếp:** Spawn `Điện Lang` $ightarrow$ `Hào Điện Lang` $ightarrow$ `Lôi Điện Lang`.
        - **Địa Tai:** Spawn `Hùng` $ightarrow$ `Hồng Hùng` $ightarrow$ `Điện Hùng` (và các loại Lang hệ đất).
- **Điều kiện vượt qua:**
    1. Đạt đủ thời gian 3 phút.
    2. Lượng Thiên Khí và Địa Khí hấp thụ phải đạt mức tối thiểu (mỗi loại $\ge 50\%$ Nhân Khí).
- **Rủi ro:** Nếu lượng Khí bị mất cân bằng quá mức (vượt quá 2 lần Nhân Khí), quá trình thăng tiên sẽ **Thất Bại**.

### Phase 3: Ngưng Khiếu
- **Mô tả:** Giai đoạn nén toàn bộ Khí đã hấp thụ để hình thành Tiên Khiếu.
- **Hiệu ứng:** Xuất hiện hiệu ứng hạt `FLASH`.
- **Thời gian:** Delay 60 ticks trước khi hoàn tất.

### Phase 4: Cổ Tiên (Hoàn tất)
- **Mô tả:** Chính thức trở thành Cổ Tiên.
- **Kết quả:**
    - **Chuyển số:** Cập nhật lên `6.0`.
    - **Phúc Địa:** Được chỉ định một Phúc Địa (Grade dựa trên Nhân Khí).
    - **Phần thưởng:** Nhận hiệu ứng `Regeneration 4` và `Absorption 3`.
    - **Yêu cầu cuối cùng:** Phải có **Bản Mệnh Cổ** (`benminggu > 0`), nếu không sẽ thất bại ở bước cuối cùng.

---

## ❌ Thất Bại Thăng Tiên (Fail Ascension)
Nếu thất bại ở bất kỳ bước nào (do mất cân bằng Khí, không có Bản Mệnh Cổ, hoặc bị tiêu diệt):
- Toàn bộ Thiên Khí, Địa Khí bị reset về 0.
- Phase quay về 0.
- Người chơi nhận sát thương cực lớn (gần như chết ngay lập tức).
- Thông báo lý do thất bại sẽ hiển thị trong chat.
