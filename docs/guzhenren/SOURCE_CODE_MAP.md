# Guzhenren Source Code Map (AI + Dev Handoff)

## 1) Muc tieu tai lieu
Tai lieu nay dung de:
- Ghi nhanh cac bien quan trong, class chinh, va file lien ket theo muc dich.
- Mo ta cau truc folder de AI/dev moi vao repo co the tim dung cho ngay.
- Lam source map cho viec phan tich addon/mixin, KHONG sua truc tiep source mod goc.

Pham vi chinh:
- Decompiled mod: decompiled_src/net/guzhenren
- Addon skeleton: src/main
- Du lieu tai nguyen/mixin tham chieu: cochannhan

## 2) Tong quan cau truc folder
### 2.1 Top-level trong repo
| Path | Vai tro |
| --- | --- |
| CLAUDE.md | Context nhanh cho du an addon/mixin (muc tieu, bien quan trong, luu y tuong thich). |
| decompiled_src | Source da decompile cua mod Guzhenren (doc de hieu logic). |
| src/main/java | Code addon skeleton hien tai (template). |
| src/main/resources | Tai nguyen addon skeleton. |
| src/main/templates | Template metadata (neoforge.mods.toml). |
| cochannhan | Data/assets/mixin config tham chieu cua mod side data. |
| docs/guzhenren | Thu muc tai lieu source map (file nay). |

### 2.2 Package map trong decompiled_src/net/guzhenren
| Package | So file Java (approx) | Muc dich |
| --- | ---: | --- |
| procedures | 6852 | Gameplay logic chinh, trigger theo event, item use, tick, combat, quest. |
| item | 3396 | Item classes va bien the theo tier/prefix (WeiLianHua, GuFang, CanGuFang...). |
| block | 972 | Block + tile/block entity + model/renderer/listener. |
| entity | 939 | Entity runtime, model, layer, renderer. |
| client | 828 | GUI screen, renderer, overlay, particle phia client. |
| potion | 282 | MobEffect/buff-debuff classes. |
| network | 113 | Button/Slot/Key messages, sync state, player variables sync. |
| world | 110 | Menu inventory, dimension, feature/worldgen. |
| init | 20 | Registry gateway cho blocks/items/entities/menus/effects... |
| command | 17 | Lenh command. |
| fluid | 2 | Fluid type/register. |
| utils | 1 | Utility class. |

Goi y uu tien doc neu can hieu gameplay:
1. procedures
2. network + world/inventory + client/gui
3. GuzhenrenModVariables (player data)
4. item/entity

## 3) Kien truc luong chinh
### 3.1 Boot/registry chain
| Step | Class/File | Vai tro |
| --- | --- | --- |
| Mod entry | decompiled_src/net/guzhenren/GuzhenrenMod.java | Dang ky toan bo registry va networking. |
| Registry gateway | decompiled_src/net/guzhenren/init/GuzhenrenMod*.java | Dang ky blocks/items/entities/menus/effects/screens... |
| Network register | decompiled_src/net/guzhenren/GuzhenrenMod.java + network/*Message.java | addNetworkMessage + playBidirectional payload. |

### 3.2 GUI chain mau (menu-screen-message-procedure)
| Layer | File | Vai tro |
| --- | --- | --- |
| Menu server/client container | decompiled_src/net/guzhenren/world/inventory/YuanLaoGuGuiMenu.java | Slot/container state, open/tick hooks, slot message send. |
| Screen client | decompiled_src/net/guzhenren/client/gui/YuanLaoGuGuiScreen.java | Nut bam UI, render labels, gui texture, gui -> network message. |
| Network button | decompiled_src/net/guzhenren/network/YuanLaoGuGuiButtonMessage.java | Nhan buttonID, map sang Ylg procedures. |
| Network slot | decompiled_src/net/guzhenren/network/YuanLaoGuGuiSlotMessage.java | Nhan slot changes, map slot event sang procedure. |
| Procedure layer | decompiled_src/net/guzhenren/procedures/Ylg*.java | Logic gameplay thuc te. |

### 3.3 Player data chain
| Layer | File | Vai tro |
| --- | --- | --- |
| Attachment define | decompiled_src/net/guzhenren/network/GuzhenrenModVariables.java | Khai bao player_variables attachment type. |
| Sync message | decompiled_src/net/guzhenren/network/GuzhenrenModVariables.java | PlayerVariablesSyncMessage + _syncDirty. |
| Serialize | decompiled_src/net/guzhenren/network/GuzhenrenModVariables.java | serializeNBT/deserializeNBT cho hang tram field. |

## 4) Bien quan trong cho AI/dev
Nguon chinh: decompiled_src/net/guzhenren/network/GuzhenrenModVariables.java
Attachment key: neoforge:attachments."guzhenren:player_variables"

| Bien | Y nghia | Muc dich dung nhanh |
| --- | --- | --- |
| zhuanshu | Muc chuyen/tu vi | Dieu kien tien trinh, mo tinh nang theo tier. |
| kongqiao | So khieu da mo | Mo rong nang luc va cac tinh nang lien quan. |
| zhenyuan | Chan nguyen hien tai | Tai nguyen runtime cho hanh dong/skill. |
| zuida_zhenyuan | Chan nguyen toi da | Gioi han cap phat/hoi phuc. |
| niantou | Niem dau hien tai | Tai nguyen tu duy/thu phap. |
| niantou_rongliang | Dung luong niem dau | Gioi han niantou. |
| zuida_hunpo | Tran hon phach toi da | Gioi han/he thong suc manh hon-phach. |
| shouyuan_ke | Tho nguyen theo gio | Theo doi tuoi tho (phan gio). |
| shouyuan_miao | Tho nguyen theo giay | Theo doi tuoi tho (phan giay). |
| shouyuan_fen | Tho nguyen theo phut | Theo doi tuoi tho (phan phut). |
| gongjili | Cong kich luc | Damage-related logic. |
| fangyuli | Phong ngu luc | Defense-related logic. |
| qiyun | Khi van | Luck/fortune logic. |
| renqi | Nhan khi | Danh tieng/quest/social gate. |
| benminggu | Ban menh co | Item/identity gate theo nhan vat. |
| _syncDirty | Co can sync du lieu | Neu true thi server day sync message ve client. |

Luu y:
- GuzhenrenModVariables co rat nhieu field khac. Bang tren la tap field uu tien cao de AI dung nhanh.
- Neu debug data sai giua client/server: theo doi _syncDirty + PlayerVariablesSyncMessage.

## 5) Class/File map theo muc dich
| Domain | Class | File | Muc dich |
| --- | --- | --- | --- |
| Core | GuzhenrenMod | decompiled_src/net/guzhenren/GuzhenrenMod.java | Entrypoint mod, register registry + networking + server work queue. |
| Data | GuzhenrenModVariables | decompiled_src/net/guzhenren/network/GuzhenrenModVariables.java | Attachment player/map/world variables, sync va NBT serialization. |
| UI state | MenuStateUpdateMessage | decompiled_src/net/guzhenren/network/MenuStateUpdateMessage.java | Dong bo menuState va screen state. |
| Menu registry | GuzhenrenModMenus | decompiled_src/net/guzhenren/init/GuzhenrenModMenus.java | Register MenuType va bridge MenuAccessor. |
| Screen registry | GuzhenrenModScreens | decompiled_src/net/guzhenren/init/GuzhenrenModScreens.java | Register menu -> screen phia client. |
| Pilot menu | YuanLaoGuGuiMenu | decompiled_src/net/guzhenren/world/inventory/YuanLaoGuGuiMenu.java | Container + slot handling + open/tick procedure hook. |
| Pilot screen | YuanLaoGuGuiScreen | decompiled_src/net/guzhenren/client/gui/YuanLaoGuGuiScreen.java | Render GUI + send button message. |
| Pilot button net | YuanLaoGuGuiButtonMessage | decompiled_src/net/guzhenren/network/YuanLaoGuGuiButtonMessage.java | buttonID -> Ylg2/3/6/7/8/9/10/11 procedures. |
| Pilot slot net | YuanLaoGuGuiSlotMessage | decompiled_src/net/guzhenren/network/YuanLaoGuGuiSlotMessage.java | slot event -> Ylg4 procedure. |
| Item registry | GuzhenrenModItems | decompiled_src/net/guzhenren/init/GuzhenrenModItems.java | Register rat nhieu item classes. |
| Entity registry | GuzhenrenModEntities | decompiled_src/net/guzhenren/init/GuzhenrenModEntities.java | Register entity types + spawn/attribute setup hooks. |
| Effect registry | GuzhenrenModMobEffects | decompiled_src/net/guzhenren/init/GuzhenrenModMobEffects.java | Register buff/debuff classes. |
| Dimension | ZhongzhouDimension | decompiled_src/net/guzhenren/world/dimension/ZhongzhouDimension.java | Handle player changed dimension + special effects registration. |
| Feature | WuZhuanChengZhen3Feature | decompiled_src/net/guzhenren/world/features/WuZhuanChengZhen3Feature.java | World feature place condition -> procedure gate. |
| Addon skeleton | ExampleMod | src/main/java/com/example/examplemod/ExampleMod.java | Current addon template entrypoint (chua migrate). |
| Mixin config reference | guzhenren.mixins.json | cochannhan/guzhenren.mixins.json | Mixin config reference package net.guzhenren.mixin. |

## 6) Phase 4 document: GUI-network-procedure matrix (Pilot YuanLaoGuGui)
### 6.1 Button matrix
Nguon:
- Screen: decompiled_src/net/guzhenren/client/gui/YuanLaoGuGuiScreen.java
- Network: decompiled_src/net/guzhenren/network/YuanLaoGuGuiButtonMessage.java

| buttonID | Trigger tu Screen | Server handler | Procedure duoc goi |
| ---: | --- | --- | --- |
| 0 | imagebutton_kongbai_ui | handleButtonAction | Ylg2Procedure.execute(LevelAccessor, Entity) |
| 1 | imagebutton_kongbai_ui1 | handleButtonAction | Ylg3Procedure.execute(LevelAccessor, Entity) |
| 2 | imagebutton_kongbai_ui2 | handleButtonAction | Ylg6Procedure.execute(Entity) |
| 3 | imagebutton_kongbai_ui3 | handleButtonAction | Ylg7Procedure.execute(Entity) |
| 4 | imagebutton_kongbai_ui4 | handleButtonAction | Ylg8Procedure.execute(Entity) |
| 5 | imagebutton_kongbai_ui5 | handleButtonAction | Ylg9Procedure.execute(Entity) |
| 6 | imagebutton_kongbai_ui6 | handleButtonAction | Ylg10Procedure.execute(Entity) |
| 7 | imagebutton_kongbai_ui7 | handleButtonAction | Ylg11Procedure.execute(Entity) |

### 6.2 Slot matrix
Nguon:
- Menu: decompiled_src/net/guzhenren/world/inventory/YuanLaoGuGuiMenu.java
- Network: decompiled_src/net/guzhenren/network/YuanLaoGuGuiSlotMessage.java

| slotID | changeType | Trigger | Procedure |
| ---: | ---: | --- | --- |
| 0 | 0 | slotChanged(...) tu SlotItemHandler slot 0 | Ylg4Procedure.execute(Entity) |

### 6.3 Open/Tick hooks trong Menu
| Event | File | Procedure |
| --- | --- | --- |
| PlayerContainerEvent.Open | YuanLaoGuGuiMenu.onContainerOpen | YuanLaoGuGuiGaiGUIDaKaiShiProcedure.execute(Entity) |
| PlayerTickEvent.Post khi dang mo menu | YuanLaoGuGuiMenu.onPlayerTick | YuanLaoGuGuiDangGaiGUIDaKaiShiMeiKeFaShengProcedure.execute(Entity) |

### 6.4 Label data hooks trong Screen
| Vi tri | Procedure |
| --- | --- |
| renderLabels line 1 text | Ylg1Procedure.execute(Entity) |
| renderLabels line 2 text | Ylg5Procedure.execute(Entity) |

## 7) Quy tac doc code nhanh cho AI
1. Neu loi GUI/button: doc theo thu tu Screen -> ButtonMessage/SlotMessage -> Procedure -> Variables.
2. Neu loi data stat: bat dau tu GuzhenrenModVariables, sau do tra procedures co doc/ghi field do.
3. Neu loi menu state: kiem tra MenuAccessor + MenuStateUpdateMessage.
4. Neu loi world/dimension: check world/dimension + world/features + procedure condition.
5. Neu thay class ten rat dai: uu tien tim class pattern cung prefix, thuong la generated family.

## 8) Lenh tim kiem goi y (fallback khi khong co index)
```powershell
rg "YuanLaoGuGui" "c:\moddev\guxiandao\decompiled_src\net\guzhenren"
rg "buttonID ==" "c:\moddev\guxiandao\decompiled_src\net\guzhenren\network"
rg "zhuanshu|zhenyuan|kongqiao|niantou|qiyun|renqi" "c:\moddev\guxiandao\decompiled_src\net\guzhenren\network\GuzhenrenModVariables.java"
rg "Procedure.execute" "c:\moddev\guxiandao\decompiled_src\net\guzhenren\network"
```

## 9) Do/Do not
Do:
- Doc decompiled_src de hieu logic va mapping.
- Ghi tai lieu truoc khi dong vao hook addon/mixin.
- Uu tien event-first strategy, mixin chi dung khi event khong du.

Do not:
- Khong sua truc tiep file trong decompiled_src (chi xem va map).
- Khong thay doi mapping/registry generated tru khi co ly do ro rang.

## 10) Trang thai va mo rong tiep
Trang thai hien tai:
- Da co source map tong quan.
- Da co pilot matrix YuanLaoGuGui day du cho button/slot/open/tick.

Viec can mo rong tiep:
1. Lap matrix tuong tu cho LianGugui, GuFanggui, RenWuGui.
2. Tao bien dictionary day du hon cho PlayerVariables (nhom combat, quest, economy, lifespan).
3. Them mot bang cross-reference item family <-> procedure family.
