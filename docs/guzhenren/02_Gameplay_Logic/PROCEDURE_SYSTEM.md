# Procedure & Class Naming System

**Target Folder:** `decompiled_src/net/guzhenren/procedures`
**Nature:** This folder contains thousands of generated classes. They follow a "Pattern-based" naming convention rather than a descriptive one.

## 1. The "Decoder Ring" (Naming Patterns)
Use this table to identify which gameplay module a class belongs to based on its prefix.

| Prefix | Module / Feature | Description |
| :--- | :--- | :--- |
| `Ylg` | **YuanLaoGuGui** | All logic related to the YuanLaoGu interface (e.g., `Ylg1Procedure`, `Ylg4Procedure`). |
| `Lg` | **LianGuGui** | Logic for the LianGu interface. |
| `Gfg` | **GuFangGui** | Logic for Gu Recipe/Crafting interface. |
| `Rwg` | **RenWuGui** | Logic for Quest/Task interface. |
| `WeiLianHua` | **Unrefined Items** | Logic for 1st tier items and their interactions. |
| `ErZhuan` / `SanZhuan` | **Tier Progression** | Logic for 2nd and 3rd tier advancements. |
| `Zhenyuan` / `Niantou` | **Resource Logic** | Procedures specifically handling essence or thought power calculation. |

## 2. How to trace a Class (The Workflow)
Since class names like `Ylg12Procedure` don't tell you *what* they do, use this tracing method:

**Path A: From UI $ightarrow$ Class**
`Screen Button ID` $ightarrow$ `NetworkMessage` $ightarrow$ `Procedure.execute()` $ightarrow$ **Target Class**.
*(Refer to `03_UI_UX_Matrix/YUAN_LAO_GU_GUI.md` for the map).*

**Path B: From Item $ightarrow$ Class**
`Item ID` $ightarrow$ `Item Class` $ightarrow$ `OnUse/OnRightClick` $ightarrow$ `Procedure.execute()` $ightarrow$ **Target Class**.

## 3. Core Logic Classes (Non-Procedure)
Unlike the procedures, these classes have descriptive names and are the "Brain" of the mod.

| Class Name | Role | Importance |
| :--- | :--- | :--- |
| `GuzhenrenMod` | Mod Entrypoint | Critical (Registry & Network setup) |
| `GuzhenrenModVariables` | Data Storage | Critical (All player stats) |
| `GuzhenrenModItems` | Item Registry | High (Finding item instances) |
| `GuzhenrenModEntities` | Entity Registry | High (Finding mob instances) |

## 4. AI Search Strategy
When searching for a class, **do not search by name** unless you have the prefix. Instead, search by **String/Keyword** inside the class:
- *Wrong:* Search for `Ylg5Procedure` (You might not know it's the one).
- *Right:* Search for `"Essence increased"` or `"Cultivation Level"` $ightarrow$ Find the procedure $ightarrow$ Identify the class name.
