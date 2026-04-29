# Cơ Chế Thiên - Địa - Nhân Khí

Hệ thống Khí là cốt lõi của quá trình Thăng Tiên, quyết định khả năng thành công và phẩm cấp của Phúc Địa mà người chơi nhận được.

## 👤 Nhân Khí (Human Qi)
Nhân Khí đại diện cho "dung tích" hoặc "cái lu" chứa khí của người chơi. Nó được tích lũy vĩnh viễn thông qua việc sử dụng Cổ trùng.

### 📈 Công thức tính Nhân Khí
Nhân Khí được tính dựa trên số lượng Cổ trùng của các cấp (tier) đã tiêu thụ:
$$	ext{Nhân Khí} = (	ext{T1} 	imes 1) + (	ext{T2} 	imes 3) + (	ext{T3} 	imes 9) + (	ext{T4} 	imes 27) + (	ext{T5} 	imes 81) + (	ext{Cổ Phương Crafted} 	imes 10)$$

### 🛠️ Cơ chế Tracking
- Hệ thống quét inventory mỗi giây (20 ticks).
- So sánh số lượng Cổ trùng hiện tại với snapshot trước đó.
- Nếu số lượng giảm đi $ightarrow$ tính là đã tiêu thụ $ightarrow$ cộng vào Nhân Khí.
- **Lưu ý:** Không hoạt động trong chế độ Creative.

### 🏆 Phân cấp Phúc Địa (dựa trên Nhân Khí)
| Grade | Nhân Khí (NK) | Quy mô Chunk |
| :--- | :--- | :--- |
| **Hạ đẳng** | $NK < 1,000$ | $8 	imes 8$ |
| **Trung đẳng** | $1,000 \le NK < 10,000$ | $32 	imes 32$ |
| **Thượng đẳng** | $10,000 \le NK < 100,000$ | $128 	imes 128+$ |
| **Siêu đẳng** | $NK \ge 100,000$ | Vô tận |

---

## ☁️ Thiên Khí (Heavenly Qi)
Thiên Khí được hấp thụ trong Phase 2 của quá trình Thăng Tiên từ bầu trời.

### ⚡ Tốc độ hấp thụ (ThienRate)
Tốc độ tăng dần dựa trên các điều kiện môi trường:
- **Độ cao:** Nếu $Y > 150$, mỗi block cao hơn cộng thêm $0.05 	ext{ đơn vị/tick}$.
- **Thời tiết:** 
    - Có sấm sét (`thundering`): $+5.0 	ext{ đơn vị/tick}$.
    - Có mưa (`raining`): $+1.0 	ext{ đơn vị/tick}$.
- **Thời gian:** Ban ngày $+1.0$, Ban đêm $+0.5$.
- **Tối thiểu:** Luôn hấp thụ ít nhất $0.1 	ext{ đơn vị/tick}$.

---

## ⛰️ Địa Khí (Earthly Qi)
Địa Khí được hấp thụ trong Phase 2 từ lòng đất và khoáng sản xung quanh.

### 💎 Tốc độ hấp thụ (DiaRate)
- **Độ sâu:** Nếu $Y < 0$, mỗi block sâu hơn cộng thêm $0.05 	ext{ đơn vị/tick}$.
- **Khoáng sản:** Quét vùng xung quanh ($7 	imes 7 	imes 7$). Mỗi block Kim Cương, Vàng, hoặc Lục Bảo (`Diamond`, `Gold`, `Emerald`) cộng thêm $+0.3 	ext{ đơn vị/tick}$.
- **Tối thiểu:** Luôn hấp thụ ít nhất $0.1 	ext{ đơn vị/tick}$.

---

## ⚖️ Sự Cân Bằng Khí (Qi Balance)
Trong quá trình Nạp Khí (Phase 2), người chơi phải giữ sự cân bằng giữa Thiên và Địa:
- **Yêu cầu thành công:** Cả Thiên Khí và Địa Khí phải đạt ít nhất **50% giá trị Nhân Khí**.
- **Giới hạn nguy hiểm:** Nếu bất kỳ loại Khí nào vượt quá **200% (2 lần) Nhân Khí**, người chơi sẽ bị coi là "mất cân bằng" và quá trình thăng tiên sẽ thất bại ngay lập tức.
