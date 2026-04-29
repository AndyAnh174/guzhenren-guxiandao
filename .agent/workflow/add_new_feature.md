# Workflow: Thêm tính năng mới

## Thêm Item mới

1. Tạo class item trong `item/XxxItem.java` extends `Item`
2. Đăng ký trong `CoTienItems.java`:
   ```java
   public static final DeferredHolder<Item, XxxItem> XXX =
           ITEMS.register("xxx", XxxItem::new);
   ```
3. Thêm vào creative tab trong `CoTienCreativeTabs.java` → `output.accept(...)`
4. Thêm tên trong `src/main/resources/assets/cotienaddon/lang/en_us.json`:
   ```json
   "item.cotienaddon.xxx": "Tên Hiển Thị"
   ```
5. Tạo model: `src/main/resources/assets/cotienaddon/models/item/xxx.json`
6. Tạo texture: `src/main/resources/assets/cotienaddon/textures/item/xxx.png` (16×16 RGBA)

## Thêm Packet mới

1. Tạo record trong `network/XxxPacket.java`:
   ```java
   public record XxxPacket(int field1, double field2) implements CustomPacketPayload {
       public static final Type<XxxPacket> TYPE = new Type<>(...);
       public static final StreamCodec<ByteBuf, XxxPacket> STREAM_CODEC = StreamCodec.composite(...);
   }
   ```
2. Đăng ký trong `CoTienNetwork.java` trong `registerPayloads()`:
   ```java
   reg.playToServer(XxxPacket.TYPE, XxxPacket.STREAM_CODEC, CoTienNetwork::handleXxx);
   ```
3. Viết handler `private static void handleXxx(XxxPacket pkt, IPayloadContext ctx)`
4. Gọi từ client: `PacketDistributor.sendToServer(new XxxPacket(...))`

## Thêm Command mới

Trong `CoTienCommand.java`, thêm vào cây lệnh:
```java
.then(Commands.literal("tenlenh")
    .executes(ctx -> { ... return 1; }))
```

## Thêm GUI Screen mới

1. Tạo `client/XxxScreen.java` extends `Screen`
2. Mở từ client: `Minecraft.getInstance().setScreen(new XxxScreen(...))`
3. Nếu cần data từ server: gửi packet → server sync `SyncCoTienPacket` → client mở screen trong `CoTienClientHandler.handleSyncPacket()`

## Thêm logic Server Tick

Thêm vào `CoTienEventHandler.java`:
```java
@SubscribeEvent
public static void onPlayerTick(PlayerTickEvent.Post event) {
    if (event.getEntity().level().isClientSide()) return;
    // server-side logic here
}
```

## Thêm item tag inject vào mod gốc

Tạo file: `src/main/resources/data/{mod_namespace}/tags/item/{tag_name}.json`
```json
{ "values": ["cotienaddon:item_id"] }
```
NeoForge tự merge với tag gốc khi load.
