# Full Player Variables Dictionary

**Source File:** `decompiled_src/net/guzhenren/network/GuzhenrenModVariables.java`
**Attachment Key:** `neoforge:attachments."guzhenren:player_variables"`

This document serves as the absolute reference for all persistent player data. 

---

## 1. Cultivation & Base Stats (The Core)
These variables define the player's power level and resource pools.

| Variable | Type | Meaning / Usage |
| :--- | :--- | :--- |
| `zhuanshu` | double | **Cultivation Tier (Chuyển số)** - Main progression gate. |
| `zhenyuan` | double | Current True Essence. |
| `zuida_zhenyuan` | double | Max True Essence. |
| `jingli` | double | Current Energy/Stamina. |
| `zuida_jingli` | double | Max Energy/Stamina. |
| `hunpo` | double | Current Soul Power. |
| `zuida_hunpo` | double | Max Soul Power. |
| `kongqiao` | double | Apertures opened. |
| `daode` | double | Morality/Virtue. |
| `qiyun` | double | Luck/Fortune. |
| `tizhi` | double | Constitution/Body strength. |
| `shouyuan` | double | Base Lifespan. |
| `shouyuan_ke` | double | Lifespan hours. |
| `shouyuan_miao` | double | Lifespan seconds. |
| `shouyuan_fen` | double | Lifespan minutes. |

## 2. The Dao System (Đạo Hệ)
The mod tracks progress in numerous "Daos". There are two main categories: `daohen` (Dao marks) and `liupai` (Six Sects).

### 2.1 Dao Marks (`daohen_...` / `dahen_...`)
*Most are `double` values.*
- **Elemental/Nature:** `bingxuedao` (Ice/Snow), `jindao` (Gold/Metal), `mudao` (Wood), `shuidao` (Water), `yandao` (Fire), `tudao` (Earth), `fengdao` (Wind), `guangdao` (Light), `andao` (Dark), `yingdao` (Shadow).
- **Conceptual:** `zhoudao` (Universe), `rendao` (Human), `tiandao` (Heaven), `qidao` (Qi), `nudao` (Female/Emotion), `zhidao` (Wisdom), `xingdao` (Star), `zhendao` (Truth), `liandao` (Refinement).
- **Specialized:** `huadao` (Flower), `toudao` (Theft), `yundao` (Cloud), `leidao` (Thunder), `xindao` (Heart), `yindao` (Yin), `gudao` (Gu), `xudao` (Void), `hundao` (Chaos), `jiandao` (Sword), `daodao` (Dao), `dandao` (Alchemy), `xuedao` (Blood), `dudao` (Poison), `huandao` (Illusion), `yuedao` (Moon), `mengdao` (Dream), `bingdao` (Ice), `bianhuadao` (Change).

### 2.2 Six Sects (`liupai_...`)
Similar to Dao Marks but specific to the "Six Sects" progression.
- Includes all above elements plus `qingmeidao` and `feixingdao`.

## 3. ShaZhao System (Sát Chiếu)
A complex system of "Shattering" or "Projection" involving specific Gu insects and durability.

| Group | Variables | Type |
| :--- | :--- | :--- |
| **ShaZhao 1-4** | `ShaZhao1`, `ShaZhao2`, `ShaZhao3`, `ShaZhao4` | `ItemStack` |
| **Gu Insects** | `ShaZhao[1-4]_GuChong[1-10]` | `ItemStack` |
| **Durability** | `ShaZhao[1-4]_GuChong[1-10]_NaiJiu` | `double` |

## 4. Economy & Trade (PaiMaiHang - Auction House)
Variables used to track auction house state and interactions.

| Variable | Type | Meaning |
| :--- | :--- | :--- |
| `PaiMaiHang_PaiPin` | `ItemStack` | Main auction item. |
| `PaiMaiHang_jiage` | double | Item price. |
| `PaiMaiHang_JingJia` | double | Current bid. |
| `PaiMaiHang_ShiJian` | double | Time remaining. |
| `PaiMaiHang_LiaoTian[1-14]` | String | Chat logs/messages. |
| `PaiMaiHang_JiPai[1-6]` | `ItemStack` | Machine/Tool items. |
| `PaiMaiHang_JiPai[1-6]_1` | String | Machine metadata. |

## 5. Quest & Lore (FengYuLou & RenWu)
Tracking player progress in the "FengYuLou" and general quests.

| Variable | Type | Meaning |
| :--- | :--- | :--- |
| `FengYuLouGui_RenWu[1-3]` | double | Quest state for FengYuLou GUI. |
| `FengYuLou[1-3]_RenWu1` | String | Quest ID/Name. |
| `FengYuLou[1-3]_NeiRong[1-5]` | String | Quest content/description. |
| `FengYuLou[1-3]_YaoQiu[1-3]` | String | Quest requirements. |
| `renwu[1-5]` | double | General quest progress. |
| `rwss` | double | Quest status. |
| `renqi` | double | Reputation/Renown. |

## 6. Combat & Technical Stats
| Variable | Type | Meaning |
| :--- | :--- | :--- |
| `gongjili` | double | Attack Power. |
| `fangyuli` | double | Defense Power. |
| `shengmingzhi` | double | Vitality/Life force. |
| `ZhanDouMoShi` | double | Combat Mode state. |
| `ax, ay, az` | double | Coordinate offsets for effects. |
| `cd` | double | General cooldown. |
| `Sanzhao_CD` | double | Specific cooldown for "Sanzhao" ability. |

## 7. Misc & Identity
| Variable | Type | Meaning |
| :--- | :--- | :--- |
| `xingbie` | double | Gender. |
| `zhongzu` | double | Race/Species. |
| `benminggu` | double | Bound Life Gu. |
| `guzhenren_ui` | boolean | UI state toggle. |
| `_syncDirty` | boolean | Internal flag to trigger Server $ightarrow$ Client sync. |
