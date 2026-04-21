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

# Compile check
JAVA_HOME="..." ./gradlew compileJava

# Chạy game (có GUI window)
JAVA_HOME="..." ./gradlew runClient

# Build jar
JAVA_HOME="..." ./gradlew build

# Log: run/logs/latest.log
```

## Debug Commands (in-game, cần op)

```
/cotien debug ascend         — bắt đầu thăng tiên (bypass điều kiện)
/cotien debug complete       — force hoàn thành phase 3→4
/cotien debug reset          — reset phase = 0
/cotien debug status         — in toàn bộ CoTienData
/cotien debug setnk <số>     — set Nhân Khí (vd: setnk 1000 = Trung đẳng)
```

## Feature: Hệ thống Cổ Tiên

Mod gốc max Ngũ Chuyển (zhuanshu=5.0). Addon thêm path Cổ Tiên (zhuanshu=6.0).

### Lore
- **Nhân Khí** = "thể tích cái lu" — tích lũy từ số cổ đã dùng (tier 1-5) + cổ phương crafted
- **Thiên Khí** = hút từ trời khi thiền (Y cao, bão, ban ngày)
- **Địa Khí** = hút từ đất khi thiền (Y thấp, quặng xung quanh)

### Thăng Tiên Flow (Phase 1→4)

| Phase | Tên | Mô tả |
|-------|-----|-------|
| 1 | Phá Toái Tiên Khiếu | Levitation 4, bay lên (80 ticks) |
| 2 | Nạp Khí | Hover trên không (setNoGravity), thiền 3 phút, mobs tấn công mỗi 30s |
| 3 | Ngưng Khiếu | Flash particles, delay 60 ticks |
| 4 | Cổ Tiên | zhuanshu=6.0, Phúc Địa được assign |

### Thiên Kiếp / Địa Tai (Phase 2, mỗi 30s)
- **Thiên Kiếp**: `DIAN_LANG` → `HAO_DIAN_LANG` → `LEI_DIAN_LANG` (theo thienKhi)
- **Địa Tai**: `XIONG` → `HONG_XIONG` → `DIAN_XIONG` (theo diaKhi)

### Phúc Địa — 4 Grade (dựa vào Nhân Khí)
| Grade | Nhân Khí | Chunk |
|-------|---------|-------|
| Hạ đẳng | < 1000 | 8x8 |
| Trung đẳng | 1000–9999 | 32x32 |
| Thượng đẳng | 10000–99999 | 128x128+ |
| Siêu đẳng | ≥ 100000 | Vô tận |

### Nhân Khí công thức
```
nk = tier1×1 + tier2×3 + tier3×9 + tier4×27 + tier5×81 + crafted×10
```
Tracking: scan inventory mỗi giây, detect khi gu biến mất khỏi inventory (không hoạt động ở creative).

## Cấu trúc file

```
src/main/java/com/andyanh/cotienaddon/
├── CoTienAddon.java
├── data/CoTienData.java                — player data, NBT serialize
├── init/
│   ├── CoTienAttachments.java
│   └── CoTienNetwork.java             — đăng ký + handle packets
├── network/                           — *Packet.java records
├── system/
│   ├── ThangTienManager.java          — ascension logic, animations, mob spawns
│   └── PhucDiaManager.java            — dimension management
├── event/CoTienEventHandler.java      — inventory scan, khi absorption tick
├── command/CoTienCommand.java         — /cotien commands
├── client/
│   ├── KhongKhieuScreen.java          — GUI Tiên Khiếu (phím K)
│   ├── PhucDiaScreen.java             — GUI Phúc Địa (phím P)
│   └── CoTienClientHandler.java
└── util/GuTierDetector.java           — detect tier từ item ID
```

## Player Data (mod gốc)

Attachment: `neoforge:attachments."guzhenren:player_variables"` (`GuzhenrenModVariables.PLAYER_VARIABLES`)

| Field | Ý nghĩa |
|-------|---------|
| `zhuanshu` | Chuyển số (1.0–5.0, addon thêm 6.0) |
| `jieduan` | Giai đoạn trong chuyển |
| `benminggu` | Bản mệnh cổ (cần > 0 để hoàn thành thăng tiên) |
| `kongqiao` | Số Khiếu đã mở |

## Code Conventions
- Sau khi sửa `CoTienData` phải gọi `sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data)`
- Server events: `@EventBusSubscriber(modid=MODID)` (không có `bus=MOD`)
- MOD bus (register, keybind): thêm `bus = EventBusSubscriber.Bus.MOD`
- Attribute format cũ: `minecraft:generic.max_health` (KHÔNG phải `minecraft:max_health`)
- Animation: `PacketDistributor.sendToPlayer(sp, new SetupAnimationsProcedure.GuzhenrenModAnimationMessage("dazuo3", sp.getId(), override))`
- Mob entities: `GuzhenrenModEntities.DIAN_LANG.get()`, `.XIONG.get()`, v.v.
- `MobEffects.MOVEMENT_SLOWDOWN` (KHÔNG phải `MOVEMENT_SLOWNESS`)
- Hover phase 2: dùng `sp.setNoGravity(true)` + `sp.setDeltaMovement(0,0,0)` thay vì Levitation

## Texture Workflow (xóa phông + resize 16×16)

**Quy trình chuẩn** — tránh ảnh mờ hoặc viền trắng:

1. **Flood fill xóa nền tại full resolution** (512–2048px gốc)
   - Sample màu bg từ `pixels[10, h//2]` (không lấy góc — hay bị sai)
   - Tolerance = 50 (tăng nếu bg không đồng đều)
   - Flood từ tất cả 4 cạnh; set alpha=0 cho px khớp bg

2. **Crop sát object** → `img.getbbox()` → pad về hình vuông

3. **Resize LANCZOS** → 16×16 (KHÔNG dùng NEAREST — mất quality)

4. **Hard alpha threshold** sau resize: `a < 100 → 0, else → 255`
   - Bắt buộc! LANCZOS blend tạo pixel semi-transparent ở viền → trông có viền mờ

**Lỗi thường gặp:**

| Triệu chứng | Nguyên nhân | Fix |
|-------------|-------------|-----|
| Ảnh 16×16 bị mờ/vỡ | Dùng NEAREST resize | Chuyển sang LANCZOS |
| Viền trắng/mờ sau resize | LANCZOS tạo alpha trung gian | Hard threshold sau resize |
| Xóa mất phần của vật thể | Tolerance quá cao hoặc sample sai điểm bg | Giảm tolerance, chọn điểm bg chắc |
| Nền tưởng transparent nhưng thực ra xám | Viewer hiện checker dù ảnh chưa có alpha | Luôn `.convert("RGBA")` và kiểm tra alpha thực |

Script đầy đủ: xem `CLAUDE.md → Texture Workflow`

## Tiến độ

### Phase 1 — Nền tảng ✅
- [x] Setup project, CoTienData attachment

### Phase 2 — Thăng Tiên ✅
- [x] Nhân Khí tracking (inventory snapshot)
- [x] Thiên/Địa Khí tick absorption
- [x] GUI Tiên Khiếu (phím K) + debug buttons
- [x] Ascension 4 phase + animation dazuo3 + particles
- [x] 3-minute phase 2 + Thiên Kiếp/Địa Tai mob spawns
- [x] Network packets

### Phase 3 — Phúc Địa 🔲
- [ ] Dimension per grade
- [ ] GUI 3 tab
- [ ] Permission matrix

### Phase 4 — Thiên Kiếp Địa Tai 🔲
### Phase 5 — Multiplayer & Địa Linh 🔲
