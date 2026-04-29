# Workflow: Build & Test

## Setup bắt buộc
```bash
export JAVA_HOME="/c/Users/ADMIN/AppData/Local/Programs/Eclipse Adoptium/jdk-21.0.10.7-hotspot"
```

## Các lệnh hay dùng

```bash
# Compile check nhanh (không cần chạy game)
JAVA_HOME="..." ./gradlew compileJava

# Chạy game client để test
JAVA_HOME="..." ./gradlew runClient

# Build jar để deploy
JAVA_HOME="..." ./gradlew build
```

Log file: `run/logs/latest.log`

## Debug in-game (cần op hoặc creative)

```
/cotien debug ascend          — bắt đầu thăng tiên (bypass điều kiện)
/cotien debug complete        — force hoàn thành (phase 3 → 4)
/cotien debug reset           — reset phase về 0
/cotien debug status          — in toàn bộ CoTienData ra chat
/cotien debug setnk <số>      — set Nhân Khí trực tiếp
                                vd: setnk 1000 = Trung đẳng, setnk 10000 = Thượng đẳng
```

## Test items mới
```
/give @s cotienaddon:dinh_tien_du   — Định Tiên Du Cổ
/give @s cotienaddon:tien_nguyen    — Tiên Nguyên
```

## Test bản mệnh cổ Không Đạo
1. `/give @s cotienaddon:dinh_tien_du`
2. Dùng anvil hoặc `/enchant @s guzhenren:ben_ming_gu 1`
3. Mở UI bản mệnh cổ, bỏ item vào slot → benminggu tự set = 45

## Khi compile lỗi
1. Đọc log lỗi — thường thiếu import hoặc sai tên class
2. Tra tên class đúng trong `decompiled_src/` trước khi sửa
3. Warning `[removal] bus()` = deprecated nhưng vẫn hoạt động, bỏ qua
4. Warning GeckoLib "Unable to parse animation: death" = bình thường của mod gốc, bỏ qua
