# Guzhenren Addon — Cổ Tiên System (NeoForge 1.21.1)
> Context file cho Gemini. Cập nhật song song với CLAUDE.md.

## Tổng quan
NeoForge addon cho mod Guzhenren (Cổ Chân Nhân). Không chỉnh sửa mod gốc — chỉ extend qua addon.
- Mod ID: `cotienaddon` | Group: `com.andyanh.cotienaddon`
- Addon source: `src/main/java/com/andyanh/cotienaddon/`
- Decompiled mod gốc: `C:\moddev\guxiandao\decompiled_src\` (để tham khảo class/method)
- Qdrant index: collection `mcp_ck_CoTrung` tại `http://localhost:6333`

## Build & Run

```bash
export JAVA_HOME="/c/Users/ADMIN/AppData/Local/Programs/Eclipse Adoptium/jdk-21.0.10.7-hotspot"
JAVA_HOME="..." ./gradlew compileJava   # compile check
JAVA_HOME="..." ./gradlew runClient     # chạy game
JAVA_HOME="..." ./gradlew build         # build jar
# Log: run/logs/latest.log
```

## Debug Commands (cần op, permission 2)

```
/cotien debug ascend         — thăng tiên ngay (bypass điều kiện)
/cotien debug complete       — force hoàn thành phase 3→4
/cotien debug reset          — reset phase = 0
/cotien debug status         — in toàn bộ CoTienData
/cotien debug setnk <số>     — set Nhân Khí
/cotien debug settn <số>     — set Tiên Nguyên
/cotien debug setgrade <1-4> — set grade Phúc Địa
/cotien debug kiep           — bắt đầu Thiên Kiếp ngay
/cotien debug ditai          — bắt đầu Địa Tai ngay
/cotien debug spawnores      — spawn quặng Nguyên Thạch
/cotien debug xray           — hiện particle vị trí quặng
/cotien debug seal/unseal    — test Trấn Vũ Cổ phong ấn
/cotien setkongqiao <0-36>   — set Khiếu (op)
/cotien setdaode <value>     — set Đạo Đức (op)
/cotien setqiyun <value>     — set Khí Vận (op)
/cotien settizhi <0-15>      — set Thể Chất (op)
```

## Player Commands (mọi người, permission 0)

```
/cotien tizhi                    — liệt kê thể chất
/cotien dialinhname <tên>        — đặt tên Địa Linh
/cotien tonhieu set <tên>        — đặt Danh Hiệu Tôn
/cotien tonhieu color <màu>      — đổi màu (preset hoặc hex)
/cotien tonhieu reset/info       — xóa / xem danh hiệu
/cotien acceptinvite <ownerName> — vào Phúc Địa của người khác
```

## Feature: Hệ thống Cổ Tiên

Mod gốc max Ngũ Chuyển (zhuanshu=5.0). Addon thêm path Cổ Tiên (zhuanshu=6.0).

### Thăng Tiên Flow (Phase 1→4)

| Phase | Tên | Mô tả |
|-------|-----|-------|
| 1 | Phá Toái Tiên Khiếu | Levitation 4, bay lên (80 ticks) |
| 2 | Nạp Khí | Hover (setNoGravity), 3 phút, Thiên Kiếp/Địa Tai bắt đầu |
| 3 | Ngưng Khiếu | Flash particles, delay 60 ticks |
| 4 | Cổ Tiên | zhuanshu=6.0, Dame +10k (hoặc ×2 nếu >10k), HP +5k (hoặc ×2 nếu >5k) |

**Điều kiện thăng tiên**: ≥2 `liupai_*dao` > 100,000 (Chuẩn Vô thượng Đại Tông Sư)  
**Chết trong phase 2**: reset toàn bộ tu vi về 0

### Nhân Khí công thức
```
nk = tier1×1 + tier2×3 + tier3×9 + tier4×27 + tier5×81 + crafted×10
```
Tracking: scan inventory mỗi giây (không hoạt động ở creative).

### Phúc Địa — 4 Grade

| Grade | Nhân Khí | Tính năng thêm |
|-------|---------|----------------|
| Hạ đẳng | < 1000 | Cơ bản |
| Trung đẳng | 1000–9999 | Mở rộng hơn |
| Thượng đẳng | 10000–99999 | Quặng Nguyên Thạch auto-spawn |
| Siêu đẳng | ≥ 100000 | Thiên Kiếp định kỳ |

### Thiên Kiếp & Địa Tai (6 loại mỗi)
Trigger: lúc thăng tiên phase 2 + định kỳ khi ở Siêu đẳng.

**Thiên Kiếp**: HAO_DIAN_LANG+Phantom | LEI_DIAN_LANG+Blaze+fireball | Vex+Wither Skeleton | DIAN_XIONG+sét | XU_YING+mobs | LONG_JUAN_FENG+massive lightning  
**Địa Tai**: HONG_XIONG+magma | DIAN_XIONG+LEI_GUAN_TOU | JINRENWANGHU | SHUI_LONG+magma | HUOYANXIONG+LIEYAN+LIAOYUAN | WU_ZU_NIAO+JU_CHI_JIN_WU

Win → cộng liupai_tiandao hoặc liupai_tudao | Fail → reset tu vi

### Địa Linh (DiaSinhEntity)
NPC quản lý Phúc Địa, gắn với 1 player owner.
- **Storage**: 4 cấp (27/36/45/54 slots), chi phí 300/600/1200/2400 TN
- **Bond quest**: 3 nhiệm vụ → mở bond complete → player có thể đặt tên
- **Orphan state**: khi chủ chết → "☠ Cô Hồn Địa Linh", soul particles, không bị giết
- **Chấp Niệm**: player khác Shift+Click Địa Linh cô hồn → nhận quest → sang tên

### Danh Hiệu Tôn
Điều kiện: Bát Chuyển Đỉnh Phong (`zhuanshu>=8.0, jieduan>=4`) hoặc Cửu Chuyển trở lên (`zhuanshu>=9.0`)  
Type tự động: `daode >= 0` → "Tiên Tôn" | `daode < 0` → "Ma Tôn"  
Hiển thị: Scoreboard team prefix (`cttm_<uuid4>`) — nameplate + chat  
Màu: 7 preset + hex #RRGGBB

### Trấn Vũ Cổ
Radius 50 block. Quan tài theo player mỗi tick.  
Chặn: EntityTeleportEvent + EntityTravelToDimensionEvent (mọi loại teleport)

### Đạo Ngân (liupai_*dao)
44 biến trong GuzhenrenModVariables. Ngưỡng 100,000 = Chuẩn Vô thượng Đại Tông Sư.  
Thăng tiên cần ≥2 loại > 100,000. Nuốt Phúc Địa (annex) tốn liupai_tiandao (grade×500).

## Items

| Item | ID | Mô tả |
|------|-----|-------|
| Định Tiên Du Cổ | `cotienaddon:dinh_tien_du` | Teleport gu, bản mệnh cổ Không Đạo (benminggu=45) |
| Tiên Nguyên | `cotienaddon:tien_nguyen` | Currency, stacksTo(64) |
| Trấn Vũ Cổ | `cotienaddon:tran_vu` | Coffin seal, chặn teleport bán kính 50 |
| Orphaned Node | `cotienaddon:orphaned_node` | Chứa data Phúc Địa, dùng để annex |

## Entities

| Entity | ID | Mô tả |
|--------|-----|-------|
| Thạch Nhân | `cotienaddon:thach_nhan` | Đào quặng, 3-state AI (find→walk→mine surface) |
| Địa Linh | `cotienaddon:dia_sinh` | NPC storage+quest, 4 storage levels |

## Player Data (mod gốc)

Attachment: `neoforge:attachments."guzhenren:player_variables"` (`GuzhenrenModVariables.PLAYER_VARIABLES`)

| Field | Ý nghĩa |
|-------|---------|
| `zhuanshu` | Chuyển số (1.0–5.0, addon thêm 6.0) |
| `jieduan` | Giai đoạn trong chuyển |
| `benminggu` | Bản mệnh cổ (45 = Không Đạo) |
| `kongqiao` | Số Khiếu đã mở |
| `daode` | Đạo đức (+ thiện → Tiên Tôn, - ác → Ma Tôn) |
| `liupai_*dao` | 44 biến tiến độ Đạo Ngân |
| `gongjili` | Công kích lực |
| `zuida_hunpo` | Hồn phách tối đa |

## Code Conventions
- Sau khi sửa `CoTienData` phải gọi `sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data)`
- Sau khi sửa `GuzhenrenModVariables` phải gọi `vars.markSyncDirty()`
- Server events: `@EventBusSubscriber(modid=MODID)` (không có `bus=MOD`)
- MOD bus (register, keybind): thêm `bus = EventBusSubscriber.Bus.MOD`
- Attribute format cũ: `minecraft:generic.max_health` (KHÔNG phải `minecraft:max_health`)
- Effect: `MobEffects.MOVEMENT_SLOWDOWN` (KHÔNG phải `MOVEMENT_SLOWNESS`)
- ChestMenu nhiều hàng: `new ChestMenu(MenuType.GENERIC_9x4, id, playerInv, container, rows)` — không có `.fourRows()`
- Scoreboard team prefix = cách duy nhất đổi nameplate+chat trong multiplayer
- Player joining dimension: `EntityJoinLevelEvent` (check `entity instanceof ServerPlayer`) — không có `PlayerChangedDimensionEvent`
- Mob phải có type MONSTER mới attack player (AMBIENT mobs không tấn công)
- Hover phase 2: `sp.setNoGravity(true)` + `sp.setDeltaMovement(0, 0, 0)` — KHÔNG dùng Levitation
- Animation: `PacketDistributor.sendToPlayer(sp, new SetupAnimationsProcedure.GuzhenrenModAnimationMessage("dazuo3", sp.getId(), override))`
- **LUÔN LUÔN** sau khi code xong phải chạy `compileJava` kiểm tra lỗi, nếu pass thì chạy `runClient` để test trong game

## Texture Workflow (xóa phông + resize 16×16)

**Quy trình chuẩn** — tránh ảnh mờ hoặc viền trắng:

1. **Flood fill xóa nền tại full resolution** (512–2048px gốc)  
   Sample bg từ `pixels[10, h//2]` | Tolerance = 50 | Flood từ 4 cạnh

2. **Crop sát object** → pad về hình vuông

3. **Resize LANCZOS** → 16×16 (KHÔNG dùng NEAREST)

4. **Hard alpha threshold** sau resize: `a < 100 → 0, else → 255` (bắt buộc!)

Script đầy đủ: xem `CLAUDE.md → Texture Workflow`

## Tiến độ

- Phase 1 — Nền tảng ✅
- Phase 2 — Thăng Tiên ✅ (+ Đạo Ngân condition)
- Phase 3 — Phúc Địa ✅ (+ Địa Linh, Thạch Nhân)
- Phase 4 — Thiên Kiếp Địa Tai ✅ (6 loại mỗi, win/fail rewards)
- Phase 5 — Multiplayer & Địa Linh ✅ (Orphaned Node, Annex, Chấp Niệm)
- Danh Hiệu Tôn ✅
- Trấn Vũ Cổ nâng cấp ✅
- Command permission system ✅

## Known Issues
- Texture `orphaned_node` item chưa có file PNG
- Nhân Khí tracking không hoạt động ở creative mode
