# Items Registry Map

**Source File:** `decompiled_src/net/guzhenren/init/GuzhenrenModItems.java`
**Namespace:** `guzhenren:`

## 1. Item Naming Conventions
The mod uses specific prefixes to categorize items. Understanding these is key to identifying item families.

| Prefix / Keyword | Meaning | Context |
| :--- | :--- | :--- |
| `weilianhua` | Unrefined (Chưa luyện hóa) | 1st Tier / Basic items. |
| `erzhuan` | 2nd Tier (Nhị chuyển) | Mid-tier upgrades. |
| `sanzhuan` | 3rd Tier (Tam chuyển) | High-tier upgrades. |
| `sizhuan` | 4th Tier (Tứ chuyển) | Elite-tier upgrades. |
| `wuzhuan` | 5th Tier (Ngũ chuyển) | Top-tier / Legendary. |
| `gufang` | Gu Recipe (Cổ phương) | Recipes used for crafting Gu. |
| `cangufang` | Fragmented Recipe | Incomplete recipes. |

## 2. Key Item Categories
### 2.1 Progression Items
- **ShengZhuan / ShengJie:** Critical items for tier advancement.
- **JingJieTiSheng:** Boundary breakthrough items.

### 2.2 Gear Sets
- **FuShiBeiYuan (1, 2, 3):** Tiered armor sets (Helmet, Chestplate, Leggings, Boots).

## 3. Development Tip
When searching for items in code, use the prefixes above with `grep_search` to find all items of a specific tier.
Example: `rg "erzhuan" "C:\moddev\guxiandao\decompiled_src
et\guzhenren\init\GuzhenrenModItems.java"`
