# Phase 4 — Thiên Kiếp Địa Tai
**Trạng thái:** 🔲 Chưa bắt đầu  
**Phụ thuộc:** Phase 2 hoàn thành

## Mục tiêu
Implement hệ thống Thiên Kiếp Địa Tai (tribulation) kiểu Raid.

## Trigger
- Lúc thăng tiên (Phase 2, bước Nạp Khí)
- Định kỳ sau khi đã là Cổ Tiên: mỗi 100 ngày game (72000 ticks * 100)

## Checklist

### Setup
- [ ] Boss Bar cảnh báo xuất hiện trước 1 phút
- [ ] Lock vị trí: không thể teleport ra khi đang độ kiếp
- [ ] Timer scoreboard hiển thị còn bao lâu

### Thiên Kiếp (từ trên trời)
- [ ] Sét spam liên tục vào vị trí player
- [ ] Fireball (Ghast-like) rơi từ trên xuống
- [ ] Falling block độc hại (custom block)
- [ ] Spawn Phantom được buff (nhanh hơn, máu nhiều hơn)
- [ ] Scale theo grade Phúc Địa (siêu đẳng = kinh khủng nhất)

### Địa Tai (từ dưới đất)
- [ ] Magma block xuất hiện dưới chân player
- [ ] Silverfish spawn từ dưới đất trồi lên + đào block
- [ ] Warden spawn ở grade Thượng đẳng+
- [ ] Custom mob hệ Địa spawn

### Kết quả
- [ ] **Vượt qua**: drop Đạo Ngân + Tiên Cổ, Boss Bar biến mất
- [ ] **Thất bại** (chết):
  - Lúc thăng tiên: huỷ hoàn toàn, không thành Cổ Tiên
  - Sau thăng tiên: Phúc Địa bị giảm grade HOẶC mất một phần Thiên/Địa Khí
