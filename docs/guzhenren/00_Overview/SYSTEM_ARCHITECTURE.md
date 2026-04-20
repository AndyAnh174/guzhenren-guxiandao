# Guzhenren System Architecture

## 1. High-Level Flow
The mod follows a classic NeoForge pattern:
**Boot** -> **Registry** -> **Networking** -> **Gameplay Logic (Procedures)**.

### 1.1 Execution Chain
- **Entry Point:** `net.guzhenren.GuzhenrenMod` handles initialization.
- **Registries:** Divided into `init` package (e.g., `GuzhenrenModItems`, `GuzhenrenModEntities`).
- **Networking:** Uses a custom packet system (`net.guzhenren.network`) to sync state between Server and Client.
- **Logic Layer:** Most gameplay logic is isolated in `net.guzhenren.procedures`, triggered by events or network packets.

## 2. Key Architectural Patterns
- **Attachment System:** Uses NeoForge Attachments (`guzhenren:player_variables`) to store persistent player data.
- **GUI-to-Procedure Bridge:** 
  `Screen (Client)` -> `NetworkMessage (Packet)` -> `Procedure.execute (Server)`.
- **Registry Gateways:** Centralized classes in the `init` package act as the single source of truth for all registered objects.
