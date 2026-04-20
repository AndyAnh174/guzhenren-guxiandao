# Phase 2 — Thăng Tiên
**Trạng thái:** 🔲 Chưa bắt đầu  
**Phụ thuộc:** Phase 1 hoàn thành

## Mục tiêu
Implement toàn bộ flow thăng tiên từ Ngũ Chuyển đỉnh phong lên Cổ Tiên.

## Checklist

### Nhân Khí
- [ ] Event hook: item use → detect tier cổ trùng → tăng `guUsed_tierX`
- [ ] Event hook: LianGu craft → tăng `guCrafted`
- [ ] Công thức tính Nhân Khí từ các counter trên

### Thiên Khí & Địa Khí (tick-based)
- [ ] Player tick event → tính tốc độ hút Thiên Khí:
  - Y > 150: hút nhanh
  - Thunderstorm: hút rất nhanh (rủi ro cao)
  - Ban ngày/đêm: dương/âm variation
- [ ] Player tick event → tính tốc độ hút Địa Khí:
  - Y < 0: hút nhanh
  - Đứng cạnh diamond/gold/emerald block: tăng thêm
  - Biome núi đá: tăng thêm

### GUI Thăng Tiên
- [ ] Nút "Đột Phá / Thăng Tiên" sáng khi: `zhuanshu==5.0 AND jieduan==max`
- [ ] Click → Confirm dialog hiện:
  - Kiểm tra môi trường (Y-level, thời tiết)
  - Progress bar tỷ lệ thành công (dựa vào Nhân Khí)
  - Cảnh báo "KHÔNG THỂ QUAY LUI"

### 3 Bước Thăng Tiên
- [ ] **Bước 1 — Phá Toái Không Khiếu**: animation, set phase=1, irreversible
- [ ] **Bước 2 — Nạp Khí**: tick-based balance loop
  - Cân bằng Thiên + Địa + Nhân Khí
  - Quá mất cân bằng → chết
  - Thiên Kiếp Địa Tai kích hoạt đồng thời
- [ ] **Bước 3 — Ngưng Khiếu + Phóng Cổ**:
  - Detect Bản Mệnh Cổ trong inventory
  - "Ném" vào vòng xoáy → convert thành Tiên Cổ item
  - Tạo Phúc Địa (grade dựa vào Nhân Khí)
  - Set `zhuanshu = 6.0`, `phucDiaGrade`, `thangTienPhase = 4`

## Công thức Nhân Khí
```
nhanKhi = (guUsed_tier1 * 1)
        + (guUsed_tier2 * 3)
        + (guUsed_tier3 * 9)
        + (guUsed_tier4 * 27)
        + (guUsed_tier5 * 81)
        + (guCrafted * 10)
```

## Grade Phúc Địa theo Nhân Khí
| Nhân Khí | Grade |
|----------|-------|
| < 1000 | Hạ đẳng |
| 1000 – 9999 | Trung đẳng |
| 10000 – 99999 | Thượng đẳng |
| ≥ 100000 | Siêu đẳng |
*(Số liệu tạm thời, cần balance test)*
