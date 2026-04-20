# Guzhenren Addon/Mixin - NeoForge 1.21.1 (Gemini CLI)

## Current Work
- **Making a NeoForge addon/mixin** for the Guzhenren mod (1.21.1)
- Goal: extend/modify mod behavior via addon, NOT editing mod source directly.
- Decompiled mod source at: `C:\moddev\guxiandao\decompiled_src`
- Use the codebase search tools to investigate mod internals before guessing class/method names.

## Main Mod (Guzhenren)
- **Guzhenren** (Cổ Chân Nhân / 大爱修仙模组本体11.10版本) — cultivation mod.
- Item ID prefix: `guzhenren:` (e.g. `guzhenren:weilianhuadaduwa`, `guzhenren:xiwanggu_spawn_egg`).
- "weilianhua" prefix = "Chua luyen hoa" (unrefined) = nhat chuyen items.
- "erzhuan/sanzhuan/sizhuan/wuzhuan" in ID = nhi/tam/tu/ngu chuyen (2nd-5th tier).
- Mod dimensions: zhongzhou, beiyuan, donghai, nanjiang, ximo, yijixukong, jie_bi.

### Player Data (NeoForge Attachments)
- Path: `neoforge:attachments."guzhenren:player_variables"`
- Key variables:
  - `zhuanshu` — Chuyển số (cultivation level/tier).
  - `kongqiao` — Khiếu đã mở (apertures opened).
  - `zhenyuan` — Chân nguyên hiện tại (current true essence).
  - `zuida_zhenyuan` — Chân nguyên tối đa (max true essence).
  - `niantou` — Niệm đầu (thought power).
  - `niantou_rongliang` — Niệm đầu dung lượng (thought capacity).
  - `zuida_hunpo` — Hồn phách tối đa (max soul).
  - `shouyuan_ke` / `shouyuan_miao` / `shouyuan_fen` — Thọ nguyên (lifespan).
  - `gongjili` — Công kích lực (attack power).
  - `zfangyuli` — Phòng ngự lực (defense power).
  - `qiyun` — Khí vận (luck/fortune).
  - `renqi` — Nhân khí (reputation).
  - `benminggu` — Bản mệnh cổ (life gu bound).

## Known Issues
- Attribute IDs use OLD format: `minecraft:generic.max_health` (not `minecraft:max_health`).

## User Preferences & Rules
- **Language:** User speaks Vietnamese, uses informal tone ("tui", "nè", "ko dc").
- **Shell:** MUST use **PowerShell** for all terminal commands.
- **Pathing:** Prefer full absolute paths when operating on files.
- **Tone:** Be concise and direct.
- **Urgency:** Caps lock in user messages indicates frustration; prioritize fixing the issue immediately.
- **User Email:** anhvietho113@gmail.com
