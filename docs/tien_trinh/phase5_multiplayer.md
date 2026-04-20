# Phase 5 — Multiplayer & Địa Linh
**Trạng thái:** 🔲 Chưa bắt đầu  
**Phụ thuộc:** Phase 3 hoàn thành

## Mục tiêu
Implement tính năng PvP, Annex Phúc Địa và Địa Linh NPC.

## Checklist

### Annex / Nuốt Phúc Địa
- [ ] Khi chủ Phúc Địa chết: drop `Orphaned Blessed Land Node` (block đặc biệt)
- [ ] Nghi thức nuốt: cần cảnh giới ≥ kẻ bị nuốt + Đạo Ngân tương thích
- [ ] Khi nuốt thành công:
  - Cộng NBT (tăng trần Thiên/Địa Khí của người thắng)
  - Mở rộng world border Phúc Địa người thắng
  - Xóa hoàn toàn data của kẻ thua

### Địa Linh NPC
- [ ] Spawn khi Phúc Địa mất chủ (chủ chết, không bị ai nuốt ngay)
- [ ] AI behavior:
  - Tự động harvest farm
  - Cho Cổ trùng ăn nếu có tài nguyên
  - Duy trì state Phúc Địa
- [ ] Dần cạn kiệt nếu không có tài nguyên

### Điều kiện Chấp Niệm (Takeover)
- [ ] Mini-quest random khi tiếp cận Địa Linh:
  - Ví dụ: nộp 100 con X, luyện ra Tiên Cổ hệ Y, giết Player Z
- [ ] Hoàn thành → cập nhật UUID chủ mới trong database
- [ ] Địa Linh biến mất sau khi sang tên
