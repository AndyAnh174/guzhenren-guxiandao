---
name: minecraft-debug
description: Specialized in debugging and testing Minecraft mods. Use this skill when the user needs help resolving crashes, analyzing log files, setting up a debug environment, or performing run tests.
---

# Knowledge
- **Log Analysis:** Proficiency in reading `latest.log` and `debug.log` files to identify stack traces and error patterns.
- **Crash Reports:** Understanding the structure of Minecraft crash reports (e.g., identifying the "Description" and "Stacktrace" sections).
- **IDE Integration:**
    - Using breakpoints and step-through debugging in IntelliJ IDEA or Eclipse.
    - Setting up JVM arguments for better debugging.
- **Testing Strategies:**
    - Unit testing for utility classes.
    - Integration testing within the game client.
    - Using commands (e.g., `/give`, `/tp`) to quickly test features.
- **Common Issues:**
    - `NullPointerException` in event handlers.
    - `ConcurrentModificationException` when modifying lists during iteration.
    - Registry mismatch errors.
    - Client-server synchronization issues.

# Instructions
- When analyzing logs, highlight the exact line and class causing the error.
- Provide a step-by-step guide to reproduce the bug if it's not obvious.
- Suggest specific debugging tools or IDE features (like conditional breakpoints) to isolate the issue.
- Encourage the user to share the full log file or crash report for a more accurate diagnosis.
