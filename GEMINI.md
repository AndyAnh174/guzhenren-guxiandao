# Guzhenren Addon/Mixin - NeoForge 1.21.1
> Context file cho Gemini. Giống CLAUDE.md, cập nhật song song.

## Dự án
- NeoForge addon cho mod Guzhenren (Cổ Chân Nhân) 1.21.1
- KHÔNG sửa mod gốc, chỉ build addon/mixin
- Decompiled source: `C:\moddev\guxiandao\decompiled_src` (13,534 files)
- Addon code: `src/main/java/` (template examplemod, chưa migrate)
- Qdrant index: collection `mcp_ck_CoTrung` tại `http://localhost:6333`

## Feature đang build: Hệ thống Cổ Tiên

### Lore (Cổ Chân Nhân / Reverend Insanity)
Mod gốc max Ngũ Chuyển (zhuanshu=5.0). Addon thêm path Cổ Tiên.

**Thăng Tiên = 3 bước:**
1. Phá Toái Không Khiếu — phá vỡ khiếu phàm nhân, không thể quay lui
2. Nạp Khí — cân bằng Thiên Khí (trời) + Địa Khí (đất) + Nhân Khí (nội hàm bản thân)
3. Ngưng Khiếu + Phóng Cổ — ném Bản Mệnh Cổ vào vòng xoáy → tạo Tiên Khiếu (Phúc Địa) + Tiên Cổ

**Nhân Khí** = "thể tích cái lu" (container, không hút từ ngoài vào):
- Tích lũy từ: số cổ dùng theo tier (1-5) + số cổ phương crafted
- Quyết định grade Phúc Địa sau thăng tiên

**Thiên Kiếp Địa Tai** = tribulation khi thăng tiên + định kỳ 100 ngày:
- Thiên Kiếp: sét, fireball, flying mobs
- Địa Tai: magma under feet, underground mobs

### Phúc Địa (Tiên Khiếu) — 4 Grade
| Grade | Chunk Size | Biome | Tiên Nguyên |
|-------|-----------|-------|-------------|
| Hạ đẳng | 8x8 | 1 | Rất chậm |
| Trung đẳng | 32x32 | 2-3 | Vừa |
| Thượng đẳng | 128x128+ | Nhiều + quặng hiếm | Nhanh |
| Siêu đẳng | Vô tận | Max buff | Rất nhanh nhưng Thiên Kiếp hardcore |

### Multiplayer
- Share Phúc Địa: whitelist + 5 permission toggles (Xây/Phá, Vật chứa, Sát thương, Cốt lõi, Quản lý)
- Annex: Orphaned Blessed Land Node drop khi chủ chết → nuốt để mở rộng
- Địa Linh NPC: daemon vận hành Phúc Địa vô chủ, mini-quest để sang tên

## Tiến trình (xem docs/tien_trinh/)
- [ ] Phase 1: Setup addon project + CoTienData attachment
- [ ] Phase 2: Thăng Tiên flow + GUI + 3 luồng Khí
- [ ] Phase 3: Phúc Địa dimension + GUI 3 tab
- [ ] Phase 4: Thiên Kiếp Địa Tai
- [ ] Phase 5: Multiplayer + Địa Linh

## Mod gốc — Key Info
- Namespace: `guzhenren:`
- Player attachment: `neoforge:attachments."guzhenren:player_variables"`
- `zhuanshu` (double) = cultivation tier, max 5.0
- `jieduan` (double) = stage within tier
- `benminggu` = Bản Mệnh Cổ (life gu)
- Animation: GeckoLib, files tại `cochannhan/assets/guzhenren/geo/` và `animations/`
- Attribute ID format CŨ: `minecraft:generic.max_health` (không phải `minecraft:max_health`)
