# Guzhenren Addon/Mixin - NeoForge 1.21.1

## Current Work
- **Making a NeoForge addon/mixin** for the Guzhenren mod (1.21.1)
- Goal: extend/modify mod behavior via addon, NOT editing mod source directly
- Decompiled mod source at: `C:\moddev\guxiandao\decompiled_src` (13,534 files, indexed in Qdrant)
- Use **vibe-hnindex MCP** (`mcp_ck_CoTrung` collection) to search mod internals — query it before guessing class/method names
- Qdrant: `http://localhost:6333`, Ollama: `http://222.253.80.30:11434` (bge-m3:567m)
- Addon source code: `src/main/java/com/andyanh/cotienaddon/`

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
- [x] Thăng Tiên điều kiện: ≥2 liupai_*dao > 100,000 (Chuẩn Vô thượng Đại Tông Sư)
- [x] Thăng tiên boost: ×2 HP max, ×2 gongjili, ×2 zuida_hunpo

### Phase 3 — Phúc Địa (Tiên Khiếu) ✅
- [x] 4 grade dimension dựa vào Nhân Khí (phuc_dia_1..4)
- [x] Thượng đẳng: quặng Nguyên Thạch + Khối Tiên Nguyên auto-spawn mỗi 10 phút
- [x] Siêu đẳng: Thiên Kiếp ngưỡng 200 wave định kỳ
- [x] GUI Phúc Địa 3 tab: Tổng quan | Quản lý Khách | Hệ sinh thái
- [x] Tab Hệ sinh thái: toggle Cố định Ngày, Cho phép Mưa, Sinh vật Hòa Bình, Guzhenren Mobs
- [x] Permission matrix 5 quyền: BUILD | CONTAINERS | COMBAT | CORE | MANAGE
- [x] Địa Linh NPC (DiaSinhEntity): kho đồ 4 cấp (27/36/45/54 slots), bond quest, orphan state
- [x] Thạch Nhân (ThachNhanEntity): đào quặng Nguyên Thạch 3-state AI, surface-only mine
- [x] Ngày/đêm hoạt động (đã xóa fixed_time khỏi dimension JSON)

### Phase 4 — Thiên Kiếp Địa Tai ✅
- [x] Trigger: khi thăng tiên phase 2 bắt đầu + định kỳ (Siêu đẳng)
- [x] Boss Bar cảnh báo (BossEvent, màu RED/YELLOW)
- [x] 6 loại Thiên Kiếp: HAO_DIAN_LANG+Phantom | LEI_DIAN_LANG+Blaze+fireball | Vex+DIAN_LANG+Wither Skeleton | Wither Skeleton+DIAN_XIONG+sét | XU_YING+mobs+Phantom | LONG_JUAN_FENG+massive lightning
- [x] 6 loại Địa Tai: HONG_XIONG+XIONG+magma | DIAN_XIONG+HUI_XIONG+LEI_GUAN_TOU | JINRENWANGHU+XIAO | SHUI_LONG+magma+lightning | HUOYANXIONG+LIEYAN+LIAOYUAN | WU_ZU_NIAO+JU_CHI_JIN_WU
- [x] Win: award liupai_tiandao hoặc liupai_tudao (đạo ngân path)
- [x] Fail / chết trong Thăng Tiên: reset toàn bộ tu vi (zhuanshu, jieduan, kongqiao, zhenyuan về 0)
- [x] PlayerEvent.Clone fix: boss bar và kiep_ticks không persist sau khi chết

### Phase 5 — Multiplayer & Địa Linh ✅
- [x] Khi chủ Phúc Địa chết: drop OrphanedNodeItem chứa toàn bộ data
- [x] AnnexPhucDiaPacket: ritual nuốt Phúc Địa, tốn liupai_tiandao (grade×500)
- [x] Địa Linh cô hồn (orphaned): soul particles, "☠ Cô Hồn Địa Linh" name, invulnerable
- [x] Chấp Niệm quest: 3 nhiệm vụ ngẫu nhiên (gather TN / mine Nguyên Thạch / claim orphan node) để sang tên chủ mới
- [x] Địa Linh storage upgrade: 4 cấp, chi phí tăng lũy thừa (300 × 2^level TN)

### Hệ thống Danh Hiệu Tôn ✅
- Điều kiện: `thangTienPhase >= 4 && phucDiaLevel >= 8`
- Loại tự động theo đạo đức: `daode >= 0` → "Tiên Tôn", `daode < 0` → "Ma Tôn"
- Hiển thị: Scoreboard team prefix `cttm_<uuid4>` cho cả nameplate lẫn chat
- Màu sắc tùy chỉnh (7 màu preset + hex #RRGGBB)
- GUI trong KhongKhieuScreen (phím K), cuối màn hình
- Commands: `/cotien tonhieu set <tên>` | `color <màu>` | `reset` | `info`

### Hệ thống Trấn Vũ Cổ ✅
- Radius: 50 block (tăng từ 16)
- Quan tài hiển thị (display entity) di chuyển theo player mỗi tick
- Chặn mọi teleport: EndermanTeleport, SpreadOut, Chorus, DinhTienDu, cả dimension change

### Đạo Ngân (liupai_*dao) ✅
- 44 biến `liupai_*dao` trong GuzhenrenModVariables = tiến độ các Đạo (thiên, địa, nhân, v.v.)
- Ngưỡng Chuẩn Vô thượng Đại Tông Sư = 100,000
- Thăng Tiên: cần ≥2 loại > 100,000
- Win Kiếp/Tai: cộng điểm vào liupai_tiandao hoặc liupai_tudao
- Annex: tốn liupai_tiandao (grade×500)

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
  - `zhenyuan` / `zuida_zhenyuan` — Chân nguyên hiện tại / tối đa
  - `niantou` / `niantou_rongliang` — Niệm đầu / dung lượng
  - `zuida_hunpo` — Hồn phách tối đa (max soul)
  - `shouyuan_ke/miao/fen` — Thọ nguyên (lifespan)
  - `gongjili` — Công kích lực (attack power)
  - `fangyuli` — Phòng ngự lực (defense power)
  - `qiyun` / `qiyun_shangxian` — Khí vận / thượng hạn
  - `renqi` — Nhân khí reputation
  - `daode` — Đạo đức (moral alignment, + = thiện, - = ác)
  - `benminggu` — Bản mệnh cổ (life gu bound)
  - `liupai_*dao` — 44 biến tiến độ Đạo Ngân (thiendao, tudao, xingdao, v.v.)

## Build & Run Commands

```bash
# JAVA_HOME bắt buộc phải set trước khi chạy
export JAVA_HOME="/c/Users/ADMIN/AppData/Local/Programs/Eclipse Adoptium/jdk-21.0.10.7-hotspot"

# Compile kiểm tra lỗi (nhanh)
JAVA_HOME="..." ./gradlew compileJava

# Chạy Minecraft client (có GUI window, dùng để test)
JAVA_HOME="..." ./gradlew runClient

# Chạy dedicated server (headless, không có window)
JAVA_HOME="..." ./gradlew runServer

# Build jar (để deploy)
JAVA_HOME="..." ./gradlew build
```

> Log file: `run/logs/latest.log`

## Debug Commands (in-game, cần op — permission level 2)

| Lệnh | Tác dụng |
|------|----------|
| `/cotien debug ascend` | Bắt đầu thăng tiên (bypass điều kiện) |
| `/cotien debug complete` | Force hoàn thành (phase 3 → 4) |
| `/cotien debug reset` | Reset phase về 0 |
| `/cotien debug status` | In toàn bộ CoTienData |
| `/cotien debug setnk <amount>` | Set Nhân Khí |
| `/cotien debug settn <amount>` | Set Tiên Nguyên |
| `/cotien debug setgrade <1-4>` | Set grade Phúc Địa |
| `/cotien debug kiep` | Bắt đầu Thiên Kiếp ngay |
| `/cotien debug ditai` | Bắt đầu Địa Tai ngay |
| `/cotien debug questcomplete` | Hoàn thành quest Địa Linh ngay |
| `/cotien debug questreset` | Reset toàn bộ quest Địa Linh |
| `/cotien debug spawnores` | Spawn quặng Nguyên Thạch + Khối TN |
| `/cotien debug buythachnhan` | Mua Thạch Nhân (bypass cost) |
| `/cotien debug xray` | Hiển thị vị trí quặng bằng particle |
| `/cotien debug seal` | Test phong ấn Trấn Vũ Cổ |
| `/cotien debug unseal` | Tháo phong ấn |

## Player Commands (mọi player — permission level 0)

| Lệnh | Tác dụng |
|------|----------|
| `/cotien tizhi` | Liệt kê tất cả thể chất (0-15) |
| `/cotien dialinhname <tên>` | Đặt tên Địa Linh (cần hoàn thành bond quest) |
| `/cotien tonhieu set <tên>` | Đặt tên Danh Hiệu Tôn |
| `/cotien tonhieu color <màu>` | Đổi màu danh hiệu |
| `/cotien tonhieu reset` | Xóa danh hiệu |
| `/cotien tonhieu info` | Xem info danh hiệu |
| `/cotien acceptinvite <ownerName>` | Vào Phúc Địa của người khác |

## Op-only Commands (permission level 2)

| Lệnh | Tác dụng |
|------|----------|
| `/cotien setkongqiao <0-36>` | Set số Khiếu |
| `/cotien setdaode <value>` | Set Đạo Đức |
| `/cotien setqiyun <-200..200>` | Set Khí Vận |
| `/cotien settizhi <0-15>` | Set Thể Chất |

## Code Structure

```
src/main/java/com/andyanh/cotienaddon/
├── CoTienAddon.java
├── data/CoTienData.java                — player data, NBT serialize/deserialize
├── init/
│   ├── CoTienAttachments.java
│   ├── CoTienItems.java               — DINH_TIEN_DU, TIEN_NGUYEN, ORPHANED_NODE, TRAN_VU
│   ├── CoTienBlocks.java              — KHOI_TIEN_NGUYEN
│   ├── CoTienEntities.java            — THACH_NHAN, DIA_SINH
│   ├── CoTienCreativeTabs.java
│   └── CoTienNetwork.java             — đăng ký + handle tất cả packets
├── network/                           — *Packet.java records
│   ├── TeleportDinhTienDuPacket.java
│   ├── SaveLocationPacket.java
│   ├── AnnexPhucDiaPacket.java        — nuốt Phúc Địa qua OrphanedNode
│   └── SetTonHieuPacket.java          — set Danh Hiệu Tôn
├── item/
│   ├── DinhTienDuItem.java            — Định Tiên Du Cổ (teleport gu, bản mệnh cổ)
│   ├── TienNguyenItem.java            — Tiên Nguyên (currency)
│   ├── TranVuItem.java                — Trấn Vũ Cổ (coffin seal, radius 50)
│   └── OrphanedNodeItem.java          — Orphaned Blessed Land Node (annex ritual)
├── entity/
│   ├── ThachNhanEntity.java           — Thạch Nhân (3-state mining AI)
│   └── DiaSinhEntity.java             — Địa Linh (storage 4 levels, bond quest, orphan)
├── system/
│   ├── ThangTienManager.java          — ascension logic, Đạo Ngân check (liupai_*dao ≥2 >100k)
│   ├── PhucDiaManager.java            — dimension management, teleport
│   ├── PhucDiaSavedData.java          — per-dimension server saved data
│   └── PhucDiaManager.java
├── event/
│   ├── CoTienEventHandler.java        — inventory scan, Trấn Vũ seal, Danh Hiệu restore
│   └── PhucDiaEventHandler.java       — Thiên Kiếp/Địa Tai (6 loại mỗi), Danh Hiệu Tôn nameplate, clone/death
├── command/CoTienCommand.java         — /cotien commands (phân quyền rõ ràng)
└── client/
    ├── KhongKhieuScreen.java          — GUI Tiên Khiếu (phím K) + Danh Hiệu Tôn section
    ├── PhucDiaScreen.java             — GUI Phúc Địa (phím P), 3 tab
    ├── PhucDiaUpgradeScreen.java      — GUI nâng cấp Phúc Địa
    ├── DinhTienDuScreen.java          — GUI tọa độ dịch chuyển
    ├── CoTienHudOverlay.java          — HUD overlay
    └── CoTienClientHandler.java       — cache CoTienData, handle sync packets
```

## Code Conventions

- Packet: `*Packet.java` trong `network/`, đăng ký trong `CoTienNetwork.java`
- Event handler server-side: `@EventBusSubscriber(modid=MODID)` (không có `bus=MOD`)
- Event handler client-side: thêm `value = Dist.CLIENT`
- MOD bus events (register payload, keybind, etc.): thêm `bus = EventBusSubscriber.Bus.MOD`
- Data lưu server: `CoTienData` via `CoTienAttachments.CO_TIEN_DATA.get()`
- Sau khi đọc/sửa data phải gọi `sp.setData(...)` để persist
- Attribute IDs dùng format CŨ: `minecraft:generic.max_health` (không phải `minecraft:max_health`)
- Mob entity types lấy từ `GuzhenrenModEntities.*` (DIAN_LANG, XIONG, etc.)
- Animation: `PacketDistributor.sendToPlayer(sp, new SetupAnimationsProcedure.GuzhenrenModAnimationMessage(name, entityId, override))`
- ChestMenu nhiều hàng: dùng constructor trực tiếp `new ChestMenu(MenuType.GENERIC_9x4, id, playerInv, container, rows)` — KHÔNG có `.fourRows()` hay `.fiveRows()` trong vanilla 1.21.1
- Scoreboard team prefix: `PlayerTeam.setPlayerPrefix(Component)` — cách duy nhất đổi nameplate + chat trong multiplayer
- Player joining dimension: dùng `EntityJoinLevelEvent` và check `entity instanceof ServerPlayer` — KHÔNG có `PlayerChangedDimensionEvent`

## Known Issues
- Attribute IDs use OLD format: `minecraft:generic.max_health` (not `minecraft:max_health`)
- GeckoLib "Unable to parse animation: death" errors = mod gốc, bình thường, bỏ qua
- Nhân Khí tracking qua inventory snapshot — không hoạt động ở creative mode (items không bị consume)
- Mob có type AMBIENT không attack player — phải dùng mob có type MONSTER (ví dụ: HUOYANXIONG thay cho HUOYOUGUSHITI)
- Texture `orphaned_node` item chưa có file PNG (cần tạo)

## User Preferences
- User speaks Vietnamese, uses informal tone ("tui", "nè", "ko dc")
- Prefers concise bash commands with full absolute paths
- Caps lock = frustrated, fix immediately
- User email: anhvietho113@gmail.com
