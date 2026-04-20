# Items Registry Map

**Source File:** `decompiled_src/net/guzhenren/init/GuzhenrenModItems.java`
**Namespace:** `guzhenren:`

## 1. Item Naming Conventions
The mod uses specific prefixes to categorize items. Understanding these is key to identifying item families.

| Prefix / Keyword | Meaning | Context |
| :--- | :--- | :--- |
| `weilianhua` | Chưa luyện hóa (Unrefined) | **Riêng biệt, KHÔNG phải tier 1** — Cổ thô chưa tinh luyện, dạng nguyên liệu trước khi vào hệ thống chuyển. |
| `yizhuan` | Nhất chuyển | 1st Tier. |
| `erzhuan` | Nhị chuyển | 2nd Tier. |
| `sanzhuan` | Tam chuyển | 3rd Tier. |
| `sizhuan` | Tứ chuyển | 4th Tier. |
| `wuzhuan` | Ngũ chuyển | 5th Tier (max hiện tại trong mod gốc). |
| `gufang` | Cổ phương (Gu Recipe) | Công thức luyện Cổ. |
| `cangufang` | Tàn Cổ phương | Công thức không đầy đủ. |

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
