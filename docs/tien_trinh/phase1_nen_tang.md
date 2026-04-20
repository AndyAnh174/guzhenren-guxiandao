# Phase 1 — Nền tảng
**Trạng thái:** 🔲 Chưa bắt đầu

## Mục tiêu
Setup addon project và data model cơ bản.

## Checklist
- [ ] Đổi template examplemod → mod ID thực (ví dụ: `cotienaddon`)
- [ ] Cấu hình `neoforge.mods.toml`: depend vào mod `guzhenren`
- [ ] Tạo `CoTienData` NeoForge Attachment (độc lập với mod gốc)
- [ ] Build + chạy thử in-game không crash

## CoTienData — Fields
```java
// Thăng tiên state
double nhanKhi;           // Nhân Khí tích lũy (tính từ gu usage)
double thienKhi;          // Thiên Khí hiện tại (hút từ môi trường)
double diaKhi;            // Địa Khí hiện tại (hút từ môi trường)
int thangTienPhase;       // 0=chưa bắt đầu, 1=phá khiếu, 2=nạp khí, 3=ngưng khiếu, 4=hoàn thành
int phucDiaGrade;         // 0=none, 1=hạ, 2=trung, 3=thượng, 4=siêu

// Nhân Khí tracking
double guUsed_tier1;      // Số cổ nhất chuyển đã dùng
double guUsed_tier2;
double guUsed_tier3;
double guUsed_tier4;
double guUsed_tier5;
double guCrafted;         // Số cổ phương đã tạo

// Phúc Địa
UUID phucDiaOwner;        // UUID chủ nhân
List<UUID> whitelist;     // Danh sách được phép vào
Map<UUID, Integer> permissions; // Bitfield quyền hạn từng người
```

## Permissions Bitfield
```
Bit 0: Xây/Phá hủy
Bit 1: Tương tác Vật chứa
Bit 2: Sát thương Thực thể
Bit 3: Truy cập Cốt lõi (rút Thiên/Địa Khí)
Bit 4: Quản lý Cấp cao
```

## Ghi chú kỹ thuật
- KHÔNG dùng `guzhenren:player_variables` attachment — đó là của mod gốc, readonly
- Tạo attachment mới: `cotienaddon:co_tien_data`
- Serialize/deserialize qua NBT
