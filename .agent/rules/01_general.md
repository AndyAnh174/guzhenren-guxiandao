# Quy tắc chung

## Dự án là gì
NeoForge 1.21.1 addon cho mod Guzhenren (Cổ Chân Nhân). KHÔNG chỉnh sửa mod gốc — chỉ extend qua addon.
- Mod ID addon: `cotienaddon` | Group: `com.andyanh.cotienaddon`
- Source addon: `src/main/java/com/andyanh/cotienaddon/`
- Mod gốc decompiled: `decompiled_src/net/guzhenren/` (chỉ đọc, KHÔNG sửa)

## Nguyên tắc tuyệt đối
1. **KHÔNG bao giờ sửa file trong `decompiled_src/`** — đây là source đọc tham khảo
2. **KHÔNG sửa file trong `cochannhan/`** — đây là assets của mod gốc
3. Mọi thay đổi chỉ được thực hiện trong `src/main/java/com/andyanh/cotienaddon/` và `src/main/resources/`
4. Muốn thay đổi hành vi mod gốc → dùng Mixin hoặc Event hook, không sửa thẳng

## Ngôn ngữ
- User nói tiếng Việt, trả lời tiếng Việt
- Giọng thân mật, ngắn gọn
- Tên biến/class giữ nguyên tiếng Anh/Pinyin theo convention của dự án
