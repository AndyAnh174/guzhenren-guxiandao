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

## Debug Commands (in-game, cần op/creative)

| Lệnh | Tác dụng |
|------|----------|
| `/cotien debug ascend` | Bắt đầu thăng tiên (bypass điều kiện) |
| `/cotien debug complete` | Force hoàn thành (phase 3 → 4) |
| `/cotien debug reset` | Reset phase về 0 |
| `/cotien debug status` | In toàn bộ CoTienData |
| `/cotien debug setnk <amount>` | Set Nhân Khí (ví dụ: setnk 1000 = Trung đẳng) |

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

## Texture Workflow (xóa phông + resize về 16×16)

Script mẫu: `remove_bg.py` tại root project (đang dùng cho phuc_dia_bg.png).

**Quy trình đúng** (tránh bị mờ hoặc viền trắng):

```python
from PIL import Image
from collections import deque

INPUT = "raw/item_name.png"   # ảnh gốc độ phân giải cao (512–2048px)
OUTPUT = "src/main/resources/assets/cotienaddon/textures/item/item_name.png"
TOLERANCE = 50  # tăng nếu bg không đồng đều

def color_diff(c1, c2):
    return max(abs(int(a) - int(b)) for a, b in zip(c1[:3], c2[:3]))

img = Image.open(INPUT).convert("RGBA")
pixels = img.load()
w, h = img.size

# 1. Sample bg từ điểm chắc chắn là nền (không lấy góc — hay bị sai)
bg_color = pixels[10, h // 2][:3]

# 2. Flood fill từ tất cả 4 cạnh
visited = set()
stack = [(x, y) for x in range(w) for y in [0, h-1]] + \
        [(x, y) for y in range(h) for x in [0, w-1]]

while stack:
    x, y = stack.pop()
    if (x, y) in visited or not (0 <= x < w and 0 <= y < h): continue
    px = pixels[x, y]
    visited.add((x, y))
    if px[3] == 0 or color_diff(px, bg_color) <= TOLERANCE:
        pixels[x, y] = (0, 0, 0, 0)
        stack += [(x+1,y),(x-1,y),(x,y+1),(x,y-1)]

# 3. Crop sát nội dung + padding vuông
bbox = img.getbbox()
cropped = img.crop(bbox)
cw, ch = cropped.size
side = max(cw, ch)
padded = Image.new("RGBA", (side, side), (0, 0, 0, 0))
padded.paste(cropped, ((side-cw)//2, (side-ch)//2))

# 4. Resize LANCZOS (chất lượng cao) → 16×16
resized = padded.resize((16, 16), Image.LANCZOS)

# 5. Hard threshold alpha — QUAN TRỌNG: tránh viền mờ do LANCZOS blend transparent
data = resized.getdata()
resized.putdata([(r, g, b, 0 if a < 100 else 255) for r, g, b, a in data])

resized.save(OUTPUT)
```

**Những lỗi hay gặp:**
- **Blurry 16×16**: dùng NEAREST resize từ ảnh lớn → chỉ lấy mỗi 128px thứ một. Fix: LANCZOS.
- **Viền mờ sau LANCZOS**: alpha trung gian (1–99) do blend. Fix: hard threshold `a < 100 → 0`.
- **Xóa nhầm màu của object**: tolerance quá cao hoặc sample sai điểm bg. Fix: giảm tolerance, chọn điểm sample chắc chắn là bg.
- **Ảnh gốc nền xám không phải transparent**: viewer thấy checker ≠ ảnh đã có alpha. Luôn convert("RGBA") và kiểm tra alpha channel thực.

## Known Issues
- Attribute IDs use OLD format: `minecraft:generic.max_health` (not `minecraft:max_health`)
- GeckoLib "Unable to parse animation: death" errors = mod gốc, bình thường, bỏ qua
- Nhân Khí tracking qua inventory snapshot — không hoạt động ở creative mode (items không bị consume)

## User Preferences
- User speaks Vietnamese, uses informal tone ("tui", "nè", "ko dc")
- Prefers concise bash commands with full absolute paths
- Caps lock = frustrated, fix immediately
- User email: anhvietho113@gmail.com
