# Phase 2 — Thăng Tiên
**Trạng thái:** ✅ Hoàn thành (một số chi tiết Bước 3 defer sang Phase 4)
**Phụ thuộc:** Phase 1 hoàn thành

## Mục tiêu
Implement toàn bộ flow thăng tiên từ Ngũ Chuyển đỉnh phong lên Cổ Tiên.

## Checklist

### Nhân Khí
- [x] Event hook: item use → detect tier cổ trùng → tăng `guUsed_tierX`
- [x] Event hook: LianGu craft → tăng `guCrafted`
- [x] Công thức tính Nhân Khí từ các counter trên

### Thiên Khí & Địa Khí (tick-based)
- [x] Player tick: Y > 150 + thunderstorm → tăng `thienKhi`
- [x] Player tick: Y < 0 + diamond/gold/emerald blocks bán kính 3 → tăng `diaKhi`

### GUI Tiên Khiếu (keybind K)
- [x] `KhongKhieuScreen` (đổi tên thành Tiên Khiếu in-game) — hiện Nhân Khí, grade, usage, phase
- [x] Button "Thăng Tiên" — luôn active, server validate
- [x] JSON-driven layout (`khong_khieu_layout.json`) — customize vị trí không recompile
- [x] Custom background texture + jade button texture
- [x] Không blur, không dark overlay

### 3 Bước Thăng Tiên
- [x] **Bước 1 — Phá Toái Không Khiếu**: set phase=1, irreversible
- [x] **Bước 2 — Nạp Khí**: tick-based, cân bằng 3 khí, set phase=2→3
- [x] **Bước 3 — Ngưng Khiếu**: set phase=3→4, `zhuanshu=6.0`, assign Phúc Địa slot
- [ ] Detect Bản Mệnh Cổ → convert Tiên Cổ (TODO: cần mapping item ID, defer Phase 4)

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
