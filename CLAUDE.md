# Guzhenren Addon/Mixin - NeoForge 1.21.1

## Current Work
- **Making a NeoForge addon/mixin** for the Guzhenren mod (1.21.1)
- Goal: extend/modify mod behavior via addon, NOT editing mod source directly
- Decompiled mod source at: `C:\moddev\guxiandao\decompiled_src` (13,534 files, indexed in Qdrant)
- Use **vibe-hnindex MCP** (`mcp_ck_CoTrung` collection) to search mod internals — query it before guessing class/method names
- Qdrant: `http://localhost:6333`, Ollama: `http://222.253.80.30:11434` (bge-m3:567m)

## Main Mod (Guzhenren)
- **Guzhenren** (Cổ Chân Nhân / 大爱修仙模组本体11.10版本) — cultivation mod
- Mod data in local: `mods/server/coturng/` (extracted jar)
- Lang file: `coturng/assets/guzhenren/lang/vi_vn.json` (6047 lines, Vietnamese)
- Item ID prefix: `guzhenren:` (e.g. `guzhenren:weilianhuadaduwa`, `guzhenren:xiwanggu_spawn_egg`)
- "weilianhua" prefix = "Chua luyen hoa" (unrefined) = nhat chuyen items
- "erzhuan/sanzhuan/sizhuan/wuzhuan" in ID = nhi/tam/tu/ngu chuyen (2nd-5th tier)
- Mod dimensions: zhongzhou, beiyuan, donghai, nanjiang, ximo, yijixukong, jie_bi

### Player Data (NeoForge Attachments)
- Path: `neoforge:attachments."guzhenren:player_variables"`
- Key variables:
  - `zhuanshu` — Chuyển số (cultivation level/tier), e.g. 9.0d = 9th chuyển
  - `kongqiao` — Khiếu đã mở (apertures opened)
  - `zhenyuan` — Chân nguyên hiện tại (current true essence)
  - `zuida_zhenyuan` — Chân nguyên tối đa (max true essence)
  - `niantou` — Niệm đầu (thought power)
  - `niantou_rongliang` — Niệm đầu dung lượng (thought capacity)
  - `zuida_hunpo` — Hồn phách tối đa (max soul)
  - `shouyuan_ke` / `shouyuan_miao` / `shouyuan_fen` — Thọ nguyên (lifespan: hours/seconds/minutes)
  - `gongjili` — Công kích lực (attack power)
  - `fangyuli` — Phòng ngự lực (defense power)
  - `qiyun` — Khí vận (luck/fortune)
  - `renqi` — Nhân khí (reputation)
  - `benminggu` — Bản mệnh cổ (life gu bound)

## Known Issues
- Attribute IDs use OLD format: `minecraft:generic.max_health` (not `minecraft:max_health`)

## User Preferences
- User speaks Vietnamese, uses informal tone ("tui", "nè", "ko dc")
- Prefers concise bash commands with full absolute paths
- Caps lock = frustrated, fix immediately
- User email: anhvietho113@gmail.com
