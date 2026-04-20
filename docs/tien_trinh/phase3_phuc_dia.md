# Phase 3 — Phúc Địa (Tiên Khiếu)
**Trạng thái:** ✅ Hoàn thành  
**Phụ thuộc:** Phase 2 hoàn thành

## Mục tiêu
Tạo dimension Phúc Địa dùng chung (partition approach) + GUI quản lý 3 tab.

## Kiến trúc: Partition Approach
- Một dimension `cotienaddon:phuc_dia` (static JSON)
- Mỗi player có "slot" riêng theo trục X: slot N chiếm `[N*8192, (N+1)*8192)` blocks
- Center của slot N: `(N*8192 + 4096, 5, 4096)`
- Dễ implement hơn per-player dimension, tránh reflection/internal API

## Checklist

### Dimension
- [x] Dimension type JSON: `data/cotienaddon/dimension_type/phuc_dia.json`
- [x] Dimension JSON: `data/cotienaddon/dimension/phuc_dia.json` (flat generator)
- [x] `PhucDiaSavedData` — persist slot assignment (UUID → slot) qua SavedData
- [x] `PhucDiaManager.assignSlotOnAscension()` — cấp slot khi thăng tiên
- [x] World border theo grade (128 / 512 / 2048 / vô hạn) → enforce qua event
- [x] Teleport vào/ra Phúc Địa (keybind P → gửi `TeleportPhucDiaPacket`)
- [x] Zone boundary enforcement (5s/tick check, push về center nếu vượt)
- [x] Portal vào/ra: qua `TeleportPhucDiaPacket` thay portal block

### GUI Phúc Địa (3 Tab)
- [x] **Tab 1 — Tổng quan**: grade, Tiên Nguyên, Thiên/Địa Khí, nút Enter/Exit
- [x] **Tab 2 — Quản lý Khách**:
  - Text box nhập username + nút Mời
  - Member list + 5 permission toggle + nút Kick (có scroll)
  - Kick: teleport ngay ra + xóa whitelist
- [x] **Tab 3 — Hệ sinh thái**: placeholder, Phase 5 hoàn thiện

### Permission Matrix (5 toggles)
- [x] Xây dựng / Phá hủy (PERM_BUILD)
- [x] Tương tác Vật chứa (PERM_CONTAINERS)
- [x] Sát thương Thực thể (PERM_COMBAT) — enforce ở Phase 5
- [x] Truy cập Cốt lõi (PERM_CORE) — enforce ở Phase 5
- [x] Quản lý Cấp cao (PERM_MANAGE)

### Networking
- [x] `OpenPhucDiaPacket` — Client→Server: yêu cầu data (reuse SyncCoTienPacket response)
- [x] `UpdatePermissionPacket` — Client→Server: toggle permission bit
- [x] `ManageMemberPacket` — Client→Server: invite (tên) hoặc kick (UUID)
- [x] `TeleportPhucDiaPacket` — Client→Server: vào/ra Phúc Địa

### Tiên Nguyên Production
- [x] Tick-based (mỗi giây) sinh Tiên Nguyên vào `CoTienData.tienNguyen`
- [x] Rate theo grade: 0.1 / 0.5 / 2.0 / 10.0 per second

## Ghi chú kỹ thuật
- Keybind P mở `PhucDiaScreen`, K mở `KhongKhieuScreen` — phân biệt qua `openingPhucDia` flag trong `CoTienClientHandler`
- Server validate ownership trước khi apply mọi permission change
- `PhucDiaSavedData` lưu trong overworld DataStorage — persist khi restart server
- Permission enforcement: Block break/place trong Phúc Địa kiểm tra chủ nhân vs khách
- Debuff khách khi teleport vào: MiningFatigue II + Weakness I (600 tick = 30s)
