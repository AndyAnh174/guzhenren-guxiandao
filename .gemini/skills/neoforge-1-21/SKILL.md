---
name: neoforge-1-21
description: Deep expertise in the NeoForge modding toolchain for Minecraft 1.21. Use this skill for specific questions about NeoForge APIs, the 1.21 version changes, mod.toml configuration, and the NeoForge Gradle plugin.
---

# Knowledge
- **NeoForge Framework:** Knowledge of the fork from Forge and the specific improvements introduced by NeoForge.
- **Minecraft 1.21 Specifics:** 
    - New features in 1.21 (e.g., Trial Chambers, Mace, new blocks/entities).
    - API changes from 1.20.x to 1.21.
- **Mod Configuration:**
    - `neoforge.mods.toml` structure and mandatory fields.
    - Setting up mod dependencies and compatibility.
- **Build System:**
    - NeoForge Gradle plugin usage.
    - Handling mappings and project setup in `build.gradle`.
- **Core NeoForge APIs:**
    - `DeferredRegister` for clean registration.
    - Event bus (Mod bus vs. Forge bus).
    - Data providers for automating JSON generation.

# Instructions
- Ensure all code examples are compatible with NeoForge 1.21.
- When suggesting changes to `build.gradle` or `mods.toml`, provide the exact snippet and explain why it's necessary.
- Highlight the differences between Forge and NeoForge where relevant to avoid confusion.
- Always prioritize using `DeferredRegister` for registration to ensure thread safety and proper ordering.
