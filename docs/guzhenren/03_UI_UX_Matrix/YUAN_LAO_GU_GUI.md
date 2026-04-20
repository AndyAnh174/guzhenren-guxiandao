# YuanLaoGuGui UI Matrix

**Purpose:** Main interface for [YuanLaoGuGui] functionality.
**Source Files:**
- Screen: `decompiled_src/net/guzhenren/client/gui/YuanLaoGuGuiScreen.java`
- Menu: `decompiled_src/net/guzhenren/world/inventory/YuanLaoGuGuiMenu.java`
- Network Messages: `YuanLaoGuGuiButtonMessage`, `YuanLaoGuGuiSlotMessage`

## 1. Button Interaction Matrix
Mapping from Client UI buttons to Server-side logic.

| buttonID | Trigger (Screen) | Network Handler | Procedure Called |
| ---: | --- | --- | --- |
| 0 | `imagebutton_kongbai_ui` | `handleButtonAction` | `Ylg2Procedure.execute(LevelAccessor, Entity)` |
| 1 | `imagebutton_kongbai_ui1` | `handleButtonAction` | `Ylg3Procedure.execute(LevelAccessor, Entity)` |
| 2 | `imagebutton_kongbai_ui2` | `handleButtonAction` | `Ylg6Procedure.execute(Entity)` |
| 3 | `imagebutton_kongbai_ui3` | `handleButtonAction` | `Ylg7Procedure.execute(Entity)` |
| 4 | `imagebutton_kongbai_ui4` | `handleButtonAction` | `Ylg8Procedure.execute(Entity)` |
| 5 | `imagebutton_kongbai_ui5` | `handleButtonAction` | `Ylg9Procedure.execute(Entity)` |
| 6 | `imagebutton_kongbai_ui6` | `handleButtonAction` | `Ylg10Procedure.execute(Entity)` |
| 7 | `imagebutton_kongbai_ui7` | `handleButtonAction` | `Ylg11Procedure.execute(Entity)` |

## 2. Slot Interaction Matrix
Mapping from Item Slot changes to Server-side logic.

| slotID | changeType | Trigger | Procedure |
| ---: | ---: | --- | --- |
| 0 | 0 | `slotChanged` in `SlotItemHandler` | `Ylg4Procedure.execute(Entity)` |

## 3. Lifecycle Hooks
Events triggered during the menu's existence.

| Event | Trigger Point | Procedure |
| --- | --- | --- |
| **Open** | `onContainerOpen` | `YuanLaoGuGuiGaiGUIDaKaiShiProcedure.execute(Entity)` |
| **Tick** | `onPlayerTick` | `YuanLaoGuGuiDangGaiGUIDaKaiShiMeiKeFaShengProcedure.execute(Entity)` |

## 4. Dynamic Labels
Data rendered on the screen based on real-time values.

| Label Line | Source Procedure |
| --- | --- |
| Line 1 Text | `Ylg1Procedure.execute(Entity)` |
| Line 2 Text | `Ylg5Procedure.execute(Entity)` |
