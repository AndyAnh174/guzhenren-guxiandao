# Những điều KHÔNG được làm

## Code
- ❌ Dùng `MobEffects.MOVEMENT_SLOWNESS` → đúng là `MOVEMENT_SLOWDOWN`
- ❌ Dùng attribute `minecraft:max_health` → đúng là `minecraft:generic.max_health`
- ❌ Dùng Levitation để hover player → gây nhúng nhảy, dùng `setNoGravity` thay
- ❌ Tạo packet không đăng ký trong `CoTienNetwork.java`
- ❌ Sửa data nhưng quên gọi `sp.setData(...)` → data không persist sau restart
- ❌ Dùng `DEFAULT_NARRATION` trực tiếp (protected) → dùng lambda `supplier -> supplier.get()`
- ❌ Đoán tên class/method của mod gốc mà không tra source
- ❌ Dùng `ChestMenu.fourRows()` / `fiveRows()` / `sixRows()` — không tồn tại trong vanilla 1.21.1 → dùng constructor `new ChestMenu(MenuType.GENERIC_9xN, ...)` trực tiếp
- ❌ Dùng `sp.setCustomName()` để đổi tên player trong multiplayer → chỉ local, không broadcast → dùng Scoreboard team prefix
- ❌ Dùng mob type AMBIENT (HUOYOUGUSHITI, XIONGLIGUSHITI, ZONG_XIONG_BEN_LI...) làm mob tấn công → chúng không attack → dùng mob type MONSTER (HUOYANXIONG, LIEYANXIONG, LEI_GUAN_TOU_LANG...)
- ❌ Dùng `PlayerChangedDimensionEvent` → không tồn tại → dùng `EntityJoinLevelEvent` check `instanceof ServerPlayer`
- ❌ Dùng `renderTooltip` override trong anonymous Button → không phải valid override → implement tooltip logic khác

## File
- ❌ Sửa file trong `decompiled_src/`
- ❌ Sửa file trong `cochannhan/`
- ❌ Tạo file Java không đúng package (`com.andyanh.cotienaddon.*`)

## Texture
- ❌ Dùng NEAREST resize từ ảnh lớn → ảnh 16×16 bị vỡ/mờ
- ❌ Không hard-threshold alpha sau LANCZOS → viền mờ
- ❌ Sample màu bg từ góc ảnh → hay bị sai nếu object gần góc

## Logic game
- ❌ Tracking gu tiêu thụ qua event use item → mod gốc không dùng standard event
- ✅ Dùng inventory snapshot mỗi giây (so sánh số lượng gu trước/sau)
- ❌ Inventory snapshot không hoạt động ở creative mode (item không bị consume)
  → Test Nhân Khí bằng `/cotien debug setnk <số>`
- ❌ Đặt `"fixed_time"` trong dimension JSON → ngày/đêm không hoạt động
  → Xóa fixed_time, đặt `"natural": true`
- ❌ Dùng `\n` trong Component cho nameplate → không work → dùng 1 dòng format
- ❌ Set H của Screen trước `loadLayout()` nếu JSON có thể override H
  → Set `H = Math.max(H, minHeight)` AFTER gọi `loadLayout()`
