# Quy tắc: Tìm trước, code sau

## Luôn tìm kiếm trước khi đoán

Trước khi dùng class/method/field nào của mod gốc, **bắt buộc** phải tra trong decompiled source.
Không được tự đoán tên class, enum, hay method của Guzhenren — sai sẽ compile lỗi.

### Cách tìm:
1. **MCP vibe-hnindex** (project `CoTrung`) — cách nhanh nhất:
   - Tìm theo keyword: tên procedure, tên field, tên class
   - Tìm theo semantic: mô tả hành vi muốn tìm
2. **Grep trực tiếp** trong `decompiled_src/net/guzhenren/`:
   - Tìm class: `grep -r "ClassName" decompiled_src/ --include="*.java" -l`
   - Tìm field: `grep -r "fieldName" decompiled_src/net/guzhenren/network/`

### Những thứ hay cần tra:
| Cần biết | Nơi tìm |
|----------|---------|
| GuzhenrenModVariables fields | `decompiled_src/net/guzhenren/network/GuzhenrenModVariables.java` |
| GuzhenrenModEntities | `decompiled_src/net/guzhenren/init/GuzhenrenModEntities.java` |
| GuzhenrenModItems | `decompiled_src/net/guzhenren/init/GuzhenrenModItems.java` |
| Procedure logic | `decompiled_src/net/guzhenren/procedures/` |
| Menu/slot validation | `decompiled_src/net/guzhenren/world/inventory/` |
| Item tag names | `cochannhan/data/guzhenren/tags/item/` |
| Item IDs | `cochannhan/assets/guzhenren/models/item/` |

## Không tự suy luận tên item
- "weilianhua" ≠ tier 1 (là dạng thô chưa luyện, prefix của mọi tier)
- Tier prefix: `yizhuan` / `erzhuan` / `sanzhuan` / `sizhuan` / `wuzhuan` = tier 1–5
- Item ID luôn prefix bằng `guzhenren:`
