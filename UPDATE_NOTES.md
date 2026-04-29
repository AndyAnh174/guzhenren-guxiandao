# Cổ Tiên Addon — Bản Cập Nhật 1.0.0
> Addon cho mod Guzhenren (Cổ Chân Nhân) | NeoForge 1.21.1

---

## ✦ Tổng quan

Addon này mở rộng hệ thống tu luyện của Guzhenren, cho phép người chơi vượt qua giới hạn Ngũ Chuyển và bước lên con đường **Cổ Tiên**. Ngoài ra bổ sung hàng loạt tính năng multiplayer như Phúc Địa riêng, Tông Môn, NPC hỗ trợ và hệ thống danh hiệu.

---

## ⚔ Hệ thống Thăng Tiên (Cổ Tiên Path)

Sau khi đạt **Ngũ Chuyển Đỉnh Phong**, người chơi có thể thực hiện nghi lễ Thăng Tiên để đạt **Lục Chuyển — Cổ Tiên**.

### Điều kiện Thăng Tiên
- Đã có **Bản Mệnh Cổ** trong slot tương ứng
- Ít nhất **2 loại Đạo Ngân** đạt ngưỡng **Chuẩn Vô thượng Đại Tông Sư** (100,000 điểm)

### Quy trình 4 Phase
| Phase | Tên | Mô tả |
|-------|-----|-------|
| 1 | Phá Toái Tiên Khiếu | Bay lên, hội tụ Thiên Địa Nhân Khí |
| 2 | Nạp Khí | Thiền định 3 phút giữa không trung, Thiên Kiếp bắt đầu |
| 3 | Ngưng Khiếu | Khí tụ thành đan, chuyển hóa |
| 4 | Cổ Tiên | Hoàn thành, nhận thưởng lớn |

### Thưởng khi thành công
- **Chỉ số tăng mạnh**: Sinh Mệnh (+200,000), Công Kích (+50,000), Hồn Phách tối đa ×2
- **Phúc Địa** được cấp phát tự động theo Nhân Khí
- Danh hiệu **[Tiên Cổ]** trên đầu

### Hậu quả nếu thất bại / chết trong Phase 2
> **⚠ Toàn bộ tu vi bị xóa về 0** (Chuyển số, Giai đoạn, Khiếu, Chân Nguyên)

---

## 🌀 Thiên Kiếp & Địa Tai

Trong suốt Phase 2 Thăng Tiên, trời đất sẽ giáng xuống 3 đợt thử thách.

### 6 loại Thiên Kiếp
1. **Hạo Điện Lang Kiếp** — Bầy Hạo Điện Lang + Phantom tấn công
2. **Lôi Điện Lang Kiếp** — Lôi Điện Lang, Blaze + cầu lửa
3. **Vex Điện Kiếp** — Vex, Điện Lang, Wither Skeleton
4. **Điện Kiếp Sét** — Wither Skeleton, Điện Hùng + sét liên tục
5. **Hư Ảnh Kiếp** — Các loại Hư Ảnh + Phantom bao vây
6. **Long Quyển Phong** — Lôi Điện Lang + hàng trăm tia sét

### 6 loại Địa Tai
1. **Hồng Hùng Tai** — Hồng Hùng, Hùng + magma block
2. **Điện Hùng Tai** — Điện Hùng, Huy Hùng, Lôi Quán Đầu
3. **Kim Nhân Vương Hổ** — Kim Nhân Vương Hổ + Tiểu Kim Nhân
4. **Thủy Long Tai** — Thủy Long + magma + sét
5. **Hỏa Viêm Tai** — Hỏa Viêm Hùng, Liệt Viêm Hùng, Liêu Nguyên
6. **Ngũ Tộc Điểu** — Ngũ Tộc Điểu + Cự Xỉ Kim Ô

### Phần thưởng
- Thắng Kiếp → +điểm **Thiên Đạo** hoặc **Thổ Đạo** (Đạo Ngân)
- Siêu đẳng Phúc Địa: Thiên Kiếp tiếp tục định kỳ sau khi thăng tiên

---

## 🏔 Phúc Địa (Tiên Khiếu)

Sau khi thăng tiên, mỗi Cổ Tiên nhận một **không gian riêng** — Phúc Địa.

### 4 Cấp độ theo Nhân Khí
| Cấp | Nhân Khí | Kích thước | Đặc điểm |
|-----|---------|-----------|---------|
| Hạ đẳng | < 1,000 | 256×256 | Cơ bản |
| Trung đẳng | 1,000–9,999 | 1024×1024 | Mở rộng |
| Thượng đẳng | 10,000–99,999 | 4096×4096 | Quặng Nguyên Thạch tự spawn |
| Siêu đẳng | ≥ 100,000 | Vô hạn | Thiên Kiếp định kỳ |

### Tính năng Phúc Địa
- **Ranh giới vùng**: tường particle cyan, đẩy người vượt ra ngoài trở vào
- **GUI 3 Tab** (phím P):
  - *Tổng quan*: thông tin Phúc Địa, nâng cấp
  - *Quản lý Khách*: whitelist, quyền BUILD / CONTAINERS / COMBAT / CORE / MANAGE
  - *Hệ sinh thái*: toggle ngày cố định, mưa, hòa bình, mob Guzhenren
- **Quặng Nguyên Thạch** (Thượng đẳng+): tự spawn mỗi 10 phút
- **Khối Tiên Nguyên**: spawn kèm với quặng

### Multiplayer Phúc Địa
- Mời khách qua GUI → khách nhận lời mời → dùng `/cotien acceptinvite <tên chủ>`
- Khách bị giới hạn trong vùng của chủ nhân
- Khách cũng thấy tường particle của chủ

### Nuốt Phúc Địa (Annex)
Khi chủ nhân chết trong Phúc Địa, **OrphanedNodeItem** rơi ra. Người khác có thể:
1. Nhặt OrphanedNode
2. Chuột phải → tiêu tốn `grade × 500 điểm Thiên Đạo`
3. Nuốt Phúc Địa — toàn bộ tài nguyên chuyển về tay mình

---

## 👥 Tông Môn / Team (phím Y)

Hệ thống nhóm chơi nhiều người.

### Tạo Tông Môn
- Phím **Shift+Y** → mở màn hình tạo Tông Môn
- Chi phí: **100,000 Nguyên Thạch** trong Nguyên Lão Cổ
- Loại tự động: Cổ Tiên Tông (nếu đã thăng tiên) hoặc Phàm Nhân Tông

### Tính năng Tông Trưởng
- **Mời thành viên**: nhập tên player online
- **Kick thành viên**: nhấn [X] bên cạnh tên
- **Đặt Home**: lưu vị trí hiện tại làm điểm tập hợp

### Hiệu ứng Tông Môn
- Cổ Tiên Tông: **Ôn Dưỡng +4**
- Phàm Nhân Tông: **Ôn Dưỡng +2**
- Thành viên cùng Tông không thể gây sát thương cho nhau

### Lệnh liên quan
```
Shift+Y          — Mở tạo Tông Môn
Y                — Xem Tông Môn hiện tại
/cotien sect accept  — Chấp nhận lời mời
/cotien sect deny    — Từ chối lời mời
```

---

## 🏠 Địa Linh (NPC Phúc Địa)

Mỗi Phúc Địa có một **Địa Linh** — người quản gia gắn bó với chủ nhân.

### Kho đồ 4 cấp
| Cấp | Slots | Chi phí nâng |
|-----|-------|-------------|
| 1 | 27 | — |
| 2 | 36 | 300 Tiên Nguyên |
| 3 | 45 | 600 Tiên Nguyên |
| 4 | 54 | 1,200 Tiên Nguyên |

### Bond Quest (Nhiệm vụ Nhận Chủ)
Hoàn thành 3 nhiệm vụ ngẫu nhiên để mở khóa đặt tên:
- Thu thập Tiên Nguyên
- Đào quặng Nguyên Thạch
- Tiếp nhận Phúc Địa cô hồn

### Địa Linh Cô Hồn
Khi chủ nhân qua đời, Địa Linh chuyển thành **☠ Cô Hồn Địa Linh** — bất tử, phát hạt soul. Player khác có thể Shift+Click để nhận **Chấp Niệm Quest** sang tên Địa Linh.

### Đặt tên Địa Linh
```
/cotien dialinhname <tên>   (cần hoàn thành Bond Quest)
```

---

## ⚒ Thạch Nhân (NPC Thợ Mỏ)

Robot đào quặng Nguyên Thạch tự động trong Phúc Địa.

- 3 trạng thái AI: Tìm quặng → Di chuyển → Đào bề mặt
- Không tự dịch chuyển (đi bộ đến quặng)
- Giới hạn số lượng theo cấp Phúc Địa (grade = số tối đa)
- Chi phí mua: 80 Tiên Nguyên

---

## ✦ Danh Hiệu Tôn

Hệ thống danh hiệu dành cho Cổ Tiên cấp cao.

### Điều kiện
- Đã thăng tiên (Cổ Tiên) + Phúc Địa Cấp 8 trở lên

### Loại danh hiệu
- **☯ Tiên Tôn** — Đạo Đức ≥ 0 (thiện)
- **☠ Ma Tôn** — Đạo Đức < 0 (ác)

### Hiển thị
- Hiện trên **nameplate** và **chat** của toàn server
- Màu sắc tùy chỉnh: 7 màu preset + hex `#RRGGBB`
- GUI trong màn hình Tiên Khiếu (phím K → nút Tiên Tôn)

### Lệnh
```
/cotien tonhieu set <tên>     — Đặt tên danh hiệu
/cotien tonhieu color <màu>   — Đổi màu (vang/do/xanh/tim/la/cam/hong/trang hoặc #RRGGBB)
/cotien tonhieu reset         — Xóa danh hiệu
/cotien tonhieu info          — Xem thông tin
```

---

## ☯ Trấn Vũ Cổ (Item)

Pháp khí phong ấn không gian trong bán kính **50 block**.

### Hiệu ứng
- Người bị phong ấn: không thể di chuyển, quan tài băng xuất hiện
- Chặn **mọi loại dịch chuyển**: teleport, đổi chiều, map waypoint, combat blink xa hơn 30 block
- Chặn cả các entity khác trong vùng 50 block
- Hiệu ứng: tuyết rơi vòng tròn + enchant particles

---

## 📜 Items mới

| Item | Cách nhận | Công dụng |
|------|-----------|---------|
| **Tiên Nguyên** | Drop quặng / Địa Linh | Tiền tệ Phúc Địa |
| **Định Tiên Du Cổ** | Crafting | Teleport đến 5 tọa độ đã lưu (25 durability + 1 TN mỗi lần) |
| **Trấn Vũ Cổ** | Crafting | Phong ấn không gian bán kính 50 block |
| **OrphanedNode** | Drop khi chủ Phúc Địa chết | Dùng để nuốt Phúc Địa |

---

## 🎮 Phím tắt

| Phím | Tác dụng |
|------|---------|
| `K` | Mở màn hình Tiên Khiếu (thăng tiên, danh hiệu) |
| `P` | Mở màn hình Phúc Địa |
| `Y` | Xem Tông Môn |
| `Shift+Y` | Tạo Tông Môn mới |

---

## 🔧 Lệnh Debug (Op only)

```
/cotien debug ascend          — Bắt đầu thăng tiên ngay
/cotien debug complete        — Hoàn thành thăng tiên
/cotien debug reset           — Reset về phase 0
/cotien debug status          — Xem toàn bộ dữ liệu
/cotien debug setnk <số>      — Set Nhân Khí
/cotien debug settn <số>      — Set Tiên Nguyên
/cotien debug setgrade <1-4>  — Set cấp Phúc Địa
/cotien debug kiep            — Bắt đầu Thiên Kiếp
/cotien debug ditai           — Bắt đầu Địa Tai
/cotien debug seal/unseal     — Test Trấn Vũ Cổ
/cotien setkongqiao <0-36>    — Set số Khiếu
/cotien setdaode <value>      — Set Đạo Đức
/cotien setqiyun <value>      — Set Khí Vận
/cotien settizhi <0-15>       — Set Thể Chất
```

---

## ⚙ Yêu cầu cài đặt

- **Minecraft**: 1.21.1
- **NeoForge**: 1.21.1 (tương thích)
- **Mod gốc**: Guzhenren (Cổ Chân Nhân) — bắt buộc
- **Java**: 21+
- Copy `cotienaddon-1.0.0.jar` vào thư mục `mods/` của server và client

---

*Addon phát triển bởi AndyAnh174 — Cổ Tiên addon cho Guzhenren 1.21.1*
