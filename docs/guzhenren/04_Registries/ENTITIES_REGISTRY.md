# Entities Registry Map

**Source File:** `decompiled_src/net/guzhenren/init/GuzhenrenModEntities.java`
**Namespace:** `guzhenren:`

## 1. Entity Categorization
Entities are generally divided into Monsters, Ambient, and Misc (NPCs/Special).

### 1.1 Monster Entities
High-threat entities usually following a family pattern (e.g., Lang/Xiong).

| Entity ID | Class | Category | Notes |
| :--- | :--- | :--- | :--- |
| `dian_lang` | `DianLangEntity` | Monster | Electric Wolf |
| `hao_dian_lang` | `HaoDianLangEntity` | Monster | High-Electric Wolf |
| `lei_dian_lang` | `LeiDianLangEntity` | Monster | Thunder-Electric Wolf |
| `tu_lang` | `TuLangEntity` | Monster | Earth Wolf |
| `hui_lang` | `HuiLangEntity` | Monster | Grey Wolf |
| `xiong` | `XiongEntity` | Monster | Bear |
| `hong_xiong` | `HongXiongEntity` | Monster | Red Bear |
| `hui_xiong` | `HuiXiongEntity` | Monster | Grey Bear |
| `dian_xiong` | `DianXiongEntity` | Monster | Electric Bear |

### 1.2 Misc / Special Entities
| Entity ID | Class | Category | Notes |
| :--- | :--- | :--- | :--- |
| `yue_ren_1` | `YueRen1Entity` | Misc | NPC/Special |
| `yue_wang_1` | `YueWang1Entity` | Misc | Boss/High-rank NPC |
| `shuijian` | `ShuijianEntity` | Misc | Specialized entity |

### 1.3 Ambient / Decorative
| Entity ID | Class | Category | Notes |
| :--- | :--- | :--- | :--- |
| `bai_shi_gu_shi_ti` | `BaiShiGuShiTiEntity` | Ambient | White Stone Gu Entity |
| `hei_shi_gu_shi_ti` | `HeiShiGuShiTiEntity` | Ambient | Black Stone Gu Entity |

## 2. Implementation Details
- **Attribute Setup:** Handled via `EntityAttributeCreationEvent` in `GuzhenrenModEntities`.
- **Spawn Logic:** Managed via `RegisterSpawnPlacementsEvent`.
