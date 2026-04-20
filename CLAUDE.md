# Guzhenren Addon/Mixin - NeoForge 1.21.1

## Current Work
- **Making a NeoForge addon/mixin** for the Guzhenren mod (1.21.1)
- Goal: extend/modify mod behavior via addon, NOT editing mod source directly
- Decompiled mod source at: `C:\moddev\guxiandao\decompiled_src` (13,534 files, indexed in Qdrant)
- Use **vibe-hnindex MCP** (`mcp_ck_CoTrung` collection) to search mod internals — query it before guessing class/method names
- Qdrant: `http://localhost:6333`, Ollama: `http://222.253.80.30:11434` (bge-m3:567m)
- Addon source code: `src/main/java/` (currently examplemod template, chưa migrate)

## Feature đang build: Hệ thống Cổ Tiên (Gu Immortal)
Mod gốc max ở Ngũ Chuyển (zhuanshu=5.0). Addon này thêm path Cổ Tiên (thăng tiên từ ngũ chuyển đỉnh phong).

### Phase 1 — Nền tảng ✅
- [x] Setup addon project: mod ID `cotienaddon`, group `com.andyanh.cotienaddon`
- [x] `CoTienData` NeoForge Attachment (`cotienaddon:co_tien_data`)

### Phase 2 — Thăng Tiên ✅
- [x] Nhân Khí tracking: guUsed_tier[1-5] + guCrafted, công thức weighted
- [x] Thiên Khí tick: Y>150, thunderstorm, ngày/đêm
- [x] Địa Khí tick: Y<0, diamond/gold/emerald blocks bán kính 3
- [x] GUI KhongKhieuScreen (keybind K) + nút Đột Phá + phase display
- [x] 3 bước: Phá Khiếu (phase1) → Nạp Khí (phase2, tick) → Ngưng Khiếu (phase3) → hoàn thành (phase4, zhuanshu=6.0)
- [x] Network: OpenKhongKhieuPacket, SyncCoTienPacket, ThangTienRequestPacket

### Phase 3 — Phúc Địa (Tiên Khiếu)
- [ ] 4 grade dimension dựa vào Nhân Khí:
  - Hạ đẳng: 8x8 chunk, 1 biome, sản xuất chậm
  - Trung đẳng: 32x32 chunk, 2-3 biome, sản xuất vừa
  - Thượng đẳng: 128x128+ chunk, quặng hiếm auto-spawn, sản xuất nhanh
  - Siêu đẳng: dimension vô tận, max buff, Thiên Kiếp rất ngắn + hardcore
- [ ] GUI Phúc Địa 3 tab: Tổng quan | Quản lý Khách | Cài đặt Hệ sinh thái
- [ ] Permission matrix 5 quyền: Xây/Phá | Vật chứa | Sát thương | Cốt lõi | Quản lý cấp cao
- [ ] CoTienData NBT: lazy-load dimension, virtual inventory sync

### Phase 4 — Thiên Kiếp Địa Tai
- [ ] Trigger: lúc thăng tiên + định kỳ (100 ngày game)
- [ ] Boss Bar cảnh báo
- [ ] Thiên Kiếp: sét spam, fireball rain, flying mobs (Phantom-buff)
- [ ] Địa Tai: magma under feet, underground mobs trồi lên (Warden, Silverfish)
- [ ] Win: drop Đạo Ngân / Tiên Cổ | Fail: Phúc Địa bị hại hoặc rớt cấp

### Phase 5 — Multiplayer & Địa Linh
- [ ] Annex/Nuốt Phúc Địa: drop Orphaned Blessed Land Node khi chủ chết
- [ ] Địa Linh NPC: daemon tự động vận hành Phúc Địa vô chủ
- [ ] Mini-quest "Điều kiện chấp niệm" để sang tên chủ mới

## Main Mod (Guzhenren)
- **Guzhenren** (Cổ Chân Nhân / 大爱修仙模组本体11.10版本) — cultivation mod
- Mod data in local: `mods/server/coturng/` (extracted jar)
- Lang file: `coturng/assets/guzhenren/lang/vi_vn.json` (6047 lines, Vietnamese)
- Item ID prefix: `guzhenren:` (e.g. `guzhenren:weilianhuadaduwa`, `guzhenren:xiwanggu_spawn_egg`)
- "weilianhua" = chưa luyện hóa (unrefined, KHÔNG phải tier 1 — là dạng thô trước khi vào tier)
- "yizhuan/erzhuan/sanzhuan/sizhuan/wuzhuan" = nhất/nhị/tam/tứ/ngũ chuyển (1st-5th tier)
- Mod dimensions: zhongzhou, beiyuan, donghai, nanjiang, ximo, yijixukong, jie_bi
- Animation system: GeckoLib (.geo.json + .animation.json trong cochannhan/assets/guzhenren/)

### Player Data (NeoForge Attachments - mod gốc)
- Path: `neoforge:attachments."guzhenren:player_variables"`
- Key variables:
  - `zhuanshu` — Chuyển số (cultivation level/tier), max 5.0 trong mod gốc
  - `jieduan` — Giai đoạn trong chuyển (stage within tier)
  - `kongqiao` — Khiếu đã mở (apertures opened)
  - `zhenyuan` — Chân nguyên hiện tại (current true essence)
  - `zuida_zhenyuan` — Chân nguyên tối đa (max true essence)
  - `niantou` — Niệm đầu (thought power)
  - `niantou_rongliang` — Niệm đầu dung lượng (thought capacity)
  - `zuida_hunpo` — Hồn phách tối đa (max soul)
  - `shouyuan_ke` / `shouyuan_miao` / `shouyuan_fen` — Thọ nguyên (lifespan)
  - `gongjili` — Công kích lực (attack power)
  - `fangyuli` — Phòng ngự lực (defense power)
  - `qiyun` — Khí vận (luck/fortune)
  - `renqi` — Nhân khí reputation
  - `benminggu` — Bản mệnh cổ (life gu bound)

## Known Issues
- Attribute IDs use OLD format: `minecraft:generic.max_health` (not `minecraft:max_health`)

## User Preferences
- User speaks Vietnamese, uses informal tone ("tui", "nè", "ko dc")
- Prefers concise bash commands with full absolute paths
- Caps lock = frustrated, fix immediately
- User email: anhvietho113@gmail.com
