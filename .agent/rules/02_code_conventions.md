# Quy tắc code

## Data player
- Data addon lưu trong `CoTienData` via `CoTienAttachments.CO_TIEN_DATA.get()`
- Sau khi đọc và sửa **bắt buộc** phải gọi `sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data)` để persist
- Data mod gốc: `player.getData(GuzhenrenModVariables.PLAYER_VARIABLES)` → sau khi sửa gọi `vars.markSyncDirty()`

## Network Packets
- Mỗi packet là 1 Java `record` implements `CustomPacketPayload`
- Đặt trong package `network/`, đăng ký trong `CoTienNetwork.java`
- Client→Server: `reg.playToServer(...)` | Server→Client: `reg.playToClient(...)` | Cả hai: `reg.playBidirectional(...)`
- Gửi packet: `PacketDistributor.sendToServer(new XxxPacket(...))` (client) hoặc `PacketDistributor.sendToPlayer(sp, new XxxPacket(...))` (server)

## Event Bus
- Server-side event handler: `@EventBusSubscriber(modid = CoTienAddon.MODID)` — **KHÔNG** có `bus=MOD`
- Client-side event handler: thêm `value = Dist.CLIENT`
- MOD bus (đăng ký registry, keybind, payload): thêm `bus = EventBusSubscriber.Bus.MOD`

## Attributes & Effects
- Attribute ID format CŨ: `minecraft:generic.max_health` (KHÔNG dùng `minecraft:max_health`)
- Effect: `MobEffects.MOVEMENT_SLOWDOWN` (KHÔNG phải `MOVEMENT_SLOWNESS`)
- Effect: `MobEffects.LEVITATION` đúng

## Mob Entities (mod gốc)
- Lấy qua: `GuzhenrenModEntities.DIAN_LANG.get()`, `.XIONG.get()`, v.v.
- Sau khi spawn: `mob.setTarget(player)` để mob tấn công ngay
- **Mob có type AMBIENT không attack player** — phải dùng mob type MONSTER
  - ✅ HUOYANXIONG, LIEYANXIONG, LIAOYUANHUOXIONG, LEI_GUAN_TOU_LANG (MONSTER)
  - ❌ HUOYOUGUSHITI, XIONGLIGUSHITI, ZONG_XIONG_BEN_LI_GUSHITI (AMBIENT)

## Animation
```java
PacketDistributor.sendToPlayer(sp,
    new SetupAnimationsProcedure.GuzhenrenModAnimationMessage("dazuo3", sp.getId(), override));
```

## Item Tags (addon inject vào mod gốc)
- Tạo file: `src/main/resources/data/{namespace}/tags/item/{tag_path}.json`
- Ví dụ thêm item vào tag `guzhenren:guchong`: tạo `data/guzhenren/tags/item/guchong.json`
- NeoForge tự merge tag khi load — không cần Mixin

## Hover phase 2 (thiền định)
- Dùng `sp.setNoGravity(true)` + `sp.setDeltaMovement(0, 0, 0)` mỗi tick
- **KHÔNG dùng Levitation** — gây nhúng nhảy lên xuống không ổn định

## Delayed server tasks
```java
net.guzhenren.GuzhenrenMod.queueServerWork(ticks, () -> { ... });
```

## ChestMenu nhiều hàng (1.21.1)
Vanilla 1.21.1 KHÔNG có `.fourRows()`, `.fiveRows()`, `.sixRows()`. Dùng constructor trực tiếp:
```java
ChestMenu menu = new ChestMenu(MenuType.GENERIC_9x4, containerId, playerInventory, container, 4);
// MenuType: GENERIC_9x1 đến GENERIC_9x6 (1–6 hàng)
```

## Scoreboard Team Prefix (Danh Hiệu Tôn)
Cách duy nhất đổi nameplate + chat tag cho player trong multiplayer:
```java
ServerScoreboard sb = sp.server.getScoreboard();
String teamName = "cttm_" + sp.getUUID().toString().substring(0, 8);
PlayerTeam team = sb.getPlayerTeam(teamName);
if (team == null) team = sb.addPlayerTeam(teamName);
team.setPlayerPrefix(Component.literal("...").withStyle(...));
sb.addPlayerToTeam(sp.getScoreboardName(), team);
```
**KHÔNG dùng** `sp.setCustomName()` — chỉ hiện ở client local, không broadcast trong multiplayer.

## Player joining dimension
Không có `PlayerChangedDimensionEvent` trong NeoForge 1.21.1. Thay bằng:
```java
@SubscribeEvent
public static void onEntityJoin(EntityJoinLevelEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer sp)) return;
    if (event.getLevel().isClientSide()) return;
    // logic khi player join dimension
}
```

## Death-persistent data (PlayerEvent.Clone)
Khi player chết và respawn, NeoForge copy attachment từ old entity sang new entity VIA clone.  
Để cleanup data khi chết (không carry over):
```java
@SubscribeEvent
public static void onPlayerClone(PlayerEvent.Clone event) {
    if (!event.isWasDeath()) return;
    ServerPlayer newPlayer = (ServerPlayer) event.getEntity();
    CoTienData data = newPlayer.getData(CoTienAttachments.CO_TIEN_DATA.get());
    data.kiep_ticks = 0;  // clear boss bar state
    newPlayer.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
}
```

## Đạo Ngân (liupai_*dao variables)
44 biến trong `GuzhenrenModVariables`. Kiểm tra ngưỡng:
```java
public static DaoCheckResult checkDaoNganCondition(GuzhenrenModVariables.PlayerVariables gv) {
    double[] values = { gv.liupai_xingdao, gv.liupai_tiandao, /* ... all 44 */ };
    // count > 100_000, find top name/value
}
```
Thăng tiên cần count ≥ 2. Ngưỡng: 100,000 = Chuẩn Vô thượng Đại Tông Sư.
