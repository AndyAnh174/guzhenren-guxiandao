# Guzhenren Addon — Project Context (NeoForge 1.21.1)

## Dự án là gì
NeoForge 1.21.1 addon cho mod **Guzhenren** (Cổ Chân Nhân — cultivation/tu tiên mod).  
Mục tiêu: extend hành vi mod gốc qua addon + event hook, **KHÔNG sửa mod gốc trực tiếp**.

- **Mod ID addon**: `cotienaddon` | **Group**: `com.andyanh.cotienaddon`
- **Addon source**: `src/main/java/com/andyanh/cotienaddon/`
- **Mod gốc decompiled** (chỉ đọc, KHÔNG sửa): `decompiled_src/net/guzhenren/`
- **Assets mod gốc** (chỉ đọc): `cochannhan/`
- **Qdrant semantic search**: collection `mcp_ck_CoTrung` @ `http://localhost:6333`
- **Ollama embeddings**: `http://222.253.80.30:11434` (model: bge-m3:567m)

## Build

```bash
export JAVA_HOME="/c/Users/ADMIN/AppData/Local/Programs/Eclipse Adoptium/jdk-21.0.10.7-hotspot"
JAVA_HOME="..." ./gradlew compileJava   # compile check (nhanh)
JAVA_HOME="..." ./gradlew runClient     # chạy game client
JAVA_HOME="..." ./gradlew runServer     # chạy server headless
JAVA_HOME="..." ./gradlew build         # build jar output
# Log: run/logs/latest.log
```

## Cấu trúc source chính

```
src/main/java/com/andyanh/cotienaddon/
├── CoTienAddon.java                   — entry point, register event buses
├── data/CoTienData.java               — toàn bộ player data của addon, NBT serialize
├── init/
│   ├── CoTienAttachments.java         — NeoForge data attachment registration
│   ├── CoTienItems.java               — register items
│   ├── CoTienBlocks.java              — register blocks (KhoiTienNguyen)
│   ├── CoTienEntities.java            — register entity types (ThachNhan, DiaSinh)
│   ├── CoTienCreativeTabs.java        — creative mode tab
│   └── CoTienNetwork.java             — register + handle ALL network packets
├── network/                           — packet records (Client↔Server)
├── item/                              — item classes
├── entity/
│   ├── ThachNhanEntity.java           — Thạch Nhân: đào quặng, 3-state AI
│   └── DiaSinhEntity.java             — Địa Linh: NPC quản lý Phúc Địa
├── system/
│   ├── ThangTienManager.java          — ascension logic, stat boosts, Đạo Ngân check
│   ├── PhucDiaManager.java            — dimension teleport, guest management
│   └── PhucDiaSavedData.java          — per-dimension persistent data
├── event/
│   ├── CoTienEventHandler.java        — general events (inventory scan, Trấn Vũ, HUD)
│   └── PhucDiaEventHandler.java       — Phúc Địa events (Kiếp/Tai, Danh Hiệu, death/clone)
├── command/CoTienCommand.java         — /cotien command tree
└── client/                            — GUI screens, HUD overlay, client handler
```

## Tính năng đã hoàn thành

### Hệ thống Cổ Tiên (core)
Mod gốc max Ngũ Chuyển (zhuanshu=5.0). Addon thêm Lục Chuyển (zhuanshu=6.0).

**Thăng Tiên 4 phase**:
1. Phá Toái Tiên Khiếu — Levitation + bay lên
2. Nạp Khí — Hover (setNoGravity), 3 phút, Thiên Kiếp/Địa Tai bắt đầu
3. Ngưng Khiếu — flash particles
4. Cổ Tiên hoàn thành — zhuanshu=6.0, ×2 HP/gongjili/hunpo, assign Phúc Địa

**Điều kiện**: ≥2 `liupai_*dao` variables > 100,000 (Chuẩn Vô thượng Đại Tông Sư)  
**Fail/chết phase 2**: reset toàn bộ tu vi về 0 (zhuanshu, jieduan, kongqiao, zhenyuan)

### Phúc Địa (Blessed Land dimension)
- 4 grade dựa vào Nhân Khí: Hạ (<1k) / Trung (1k-9.9k) / Thượng (10k-99.9k) / Siêu (≥100k)
- Dimension riêng (`phuc_dia_1` đến `phuc_dia_4`) theo biome phù hợp
- GUI 3 tab: Tổng quan | Quản lý Khách | Hệ sinh thái
- Permission matrix 5 quyền: BUILD | CONTAINERS | COMBAT | CORE | MANAGE
- Thượng đẳng: Nguyên Thạch + Khối Tiên Nguyên auto-spawn mỗi 10 phút
- Hệ sinh thái: toggle ngày/đêm, mưa, hòa bình, Guzhenren mobs

### Thiên Kiếp & Địa Tai (6 loại mỗi)
Boss bar cảnh báo. Wave system. Trigger khi thăng tiên + định kỳ Siêu đẳng.  
Win → cộng liupai_tiandao / liupai_tudao | Fail → reset tu vi

### Địa Linh NPC (DiaSinhEntity)
- Storage kho đồ 4 cấp: 27/36/45/54 slots. Chi phí: 300/600/1200/2400 Tiên Nguyên
- Bond quest: 3 nhiệm vụ (gather / mine / claim orphan) → mở quyền đặt tên
- Orphan state: khi chủ chết → drop OrphanedNodeItem → Địa Linh thành "☠ Cô Hồn"
- Chấp Niệm: Shift+Click Địa Linh cô hồn → quest sang tên → thành chủ mới

### Thạch Nhân NPC (ThachNhanEntity)
- 3-state mining AI: STATE_0 (tìm quặng + navigate) → STATE_1 (đi đến) → STATE_2 (đào surface-only)
- Giới hạn theo grade Phúc Địa (grade = số Thạch Nhân tối đa)

### Danh Hiệu Tôn
- Điều kiện: thangTienPhase ≥ 4 AND phucDiaLevel ≥ 8
- Type auto: daode ≥ 0 → "Tiên Tôn" | daode < 0 → "Ma Tôn"
- Hiển thị Scoreboard team prefix (`cttm_<uuid8>`) — nameplate + chat
- 7 màu preset + hex #RRGGBB. GUI trong KhongKhieuScreen (phím K)

### Trấn Vũ Cổ
- Radius 50 block. Quan tài (display entity) theo player mỗi tick
- Block: EntityTeleportEvent + EntityTravelToDimensionEvent

### Đạo Ngân System
44 `liupai_*dao` variables trong GuzhenrenModVariables = tiến độ Đạo.  
Ngưỡng: 100,000 = Chuẩn Vô thượng Đại Tông Sư.

## Player Data

### Addon Data (CoTienData)
Truy cập: `player.getData(CoTienAttachments.CO_TIEN_DATA.get())`  
Sau khi sửa: `sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data)`

Các field quan trọng:
| Field | Mô tả |
|-------|-------|
| `thangTienPhase` | 0=không, 1=phá khiếu, 2=nạp khí, 3=ngưng, 4=cổ tiên |
| `phucDiaGrade` | 1-4 (hạ/trung/thượng/siêu đẳng) |
| `phucDiaSlot` | index dimension được gán |
| `tienNguyen` | Tiên Nguyên hiện có |
| `nhanKhi` | Nhân Khí tổng (công thức weighted theo tier gu) |
| `thienKhi` / `diaKhi` | Khí tích lũy khi thiền phase 2 |
| `dialinhStorageLevel` | 0-3 (cấp kho đồ Địa Linh) |
| `dialinhBondComplete` | true khi hoàn thành bond quest |
| `tonHieuName/Color/Enabled` | Danh Hiệu Tôn |
| `daode` | mirror từ GuzhenrenModVariables.daode (sync từ server) |

### Mod gốc Data (GuzhenrenModVariables)
Attachment: `neoforge:attachments."guzhenren:player_variables"`  
Truy cập: `player.getData(GuzhenrenModVariables.PLAYER_VARIABLES)`  
Sau khi sửa: `vars.markSyncDirty()`

Key fields: `zhuanshu` (chuyển số) | `jieduan` | `kongqiao` | `zhenyuan` | `zuida_zhenyuan` | `gongjili` | `zuida_hunpo` | `daode` | `liupai_*dao` (44 fields) | `benminggu`

## Commands

### Debug (op level 2)
`/cotien debug ascend|complete|reset|status|setnk|settn|setgrade|kiep|ditai|spawnores|xray|seal|unseal|questcomplete|questreset|buythachnhan`

### Admin (op level 2)
`/cotien setkongqiao <0-36>` | `setdaode` | `setqiyun` | `settizhi <0-15>` | `tizhi`

### Player (level 0, mọi người)
`/cotien dialinhname <tên>` | `tonhieu set/color/reset/info` | `acceptinvite <owner>`

## Code Rules

### Event Bus
```java
// Server-side
@EventBusSubscriber(modid = CoTienAddon.MODID)

// Client-side  
@EventBusSubscriber(modid = CoTienAddon.MODID, value = Dist.CLIENT)

// MOD bus (registry, keybind)
@EventBusSubscriber(modid = CoTienAddon.MODID, bus = EventBusSubscriber.Bus.MOD)
```

### ChestMenu (NeoForge 1.21.1)
```java
// KHÔNG có fourRows()/fiveRows() trong vanilla 1.21.1
new ChestMenu(MenuType.GENERIC_9x3, containerId, playerInventory, container, 3); // 27 slots
new ChestMenu(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6); // 54 slots
```

### Scoreboard Team Prefix
```java
ServerScoreboard sb = sp.server.getScoreboard();
String teamName = "cttm_" + sp.getUUID().toString().substring(0, 8);
PlayerTeam team = sb.getPlayerTeam(teamName);
if (team == null) team = sb.addPlayerTeam(teamName);
team.setPlayerPrefix(Component.literal("...").withStyle(...));
sb.addPlayerToTeam(sp.getScoreboardName(), team);
```

### Player joining dimension (không có PlayerChangedDimensionEvent)
```java
@SubscribeEvent
public static void onEntityJoin(EntityJoinLevelEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer sp)) return;
    if (event.getLevel().isClientSide()) return;
    // ...
}
```

### Delayed tasks
```java
net.guzhenren.GuzhenrenMod.queueServerWork(ticks, () -> { ... });
```

## Những điều KHÔNG làm

| ❌ Sai | ✅ Đúng |
|--------|---------|
| `minecraft:max_health` | `minecraft:generic.max_health` |
| `MobEffects.MOVEMENT_SLOWNESS` | `MobEffects.MOVEMENT_SLOWDOWN` |
| Levitation để hover | `setNoGravity(true)` + `setDeltaMovement(0,0,0)` |
| `ChestMenu.fourRows()` | `new ChestMenu(MenuType.GENERIC_9x4, ...)` |
| `sp.setCustomName()` cho multiplayer nameplate | Scoreboard team prefix |
| Mob AMBIENT để tấn công | Mob MONSTER (HUOYANXIONG, LEI_GUAN_TOU_LANG...) |
| `PlayerChangedDimensionEvent` | `EntityJoinLevelEvent` |
| `"fixed_time"` trong dimension JSON | Xóa fixed_time, `"natural": true` |
| Sửa file `decompiled_src/` | Chỉ đọc để tham khảo |

## Tìm class/method mod gốc

Trước khi dùng bất kỳ class nào của Guzhenren, **bắt buộc tra** — không được đoán:

```bash
# Trong decompiled_src
grep -r "ClassName\|fieldName" decompiled_src/net/guzhenren/ --include="*.java" -l

# Hoặc dùng vibe-hnindex MCP (nhanh hơn, semantic search)
```

Nơi tìm thường dùng:
- `GuzhenrenModVariables` fields → `decompiled_src/net/guzhenren/network/GuzhenrenModVariables.java`
- Entity types → `decompiled_src/net/guzhenren/init/GuzhenrenModEntities.java`
- Procedure logic → `decompiled_src/net/guzhenren/procedures/`
- Item IDs → `cochannhan/assets/guzhenren/models/item/`
- Item tags → `cochannhan/data/guzhenren/tags/item/`

## Items & Nomenclature

- `guzhenren:` prefix cho mọi item/entity/block của mod gốc
- `cotienaddon:` prefix cho addon
- `weilianhua` = dạng thô chưa luyện (KHÔNG phải tier 1)
- `yizhuan/erzhuan/sanzhuan/sizhuan/wuzhuan` = tier 1–5

## Known Issues

- Texture `orphaned_node` item chưa có PNG
- Nhân Khí tracking không hoạt động ở creative (item không bị consume → dùng `/cotien debug setnk`)
- GeckoLib "Unable to parse animation: death" = mod gốc, bỏ qua
