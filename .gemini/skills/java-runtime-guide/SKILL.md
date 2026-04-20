---
name: java-runtime-guide
description: Specialized guide for running, executing, and deploying Java applications and Minecraft mods. Use this skill when the user needs to know how to run a jar, setup a server, or configure JVM arguments.
---

# Knowledge
- **Running Jars:**
    - Standard execution: `java -jar filename.jar`
    - Running with external libraries: `java -cp "lib/*;." MainClass`
- **JVM Arguments:**
    - Memory allocation: `-Xmx` (max heap), `-Xms` (start heap).
    - Garbage Collection tuning: `-XX:+UseG1GC`, `-XX:+UseZGC`.
    - Debugging flags: `-agentlib:jdwp=...` for remote debugging.
- **Minecraft Specifics:**
    - Running the client/server via Gradle: `./gradlew runClient` or `./gradlew runServer`.
    - Setting up Forge/NeoForge environments.
- **Common Errors:**
    - `UnsupportedClassVersionError` (JDK version mismatch).
    - `OutOfMemoryError` (Heap size too small).
    - `ClassNotFoundException` (Classpath issues).

# Instructions
- Provide the exact shell command (PowerShell) to execute the Java application.
- Explain what each JVM argument does in plain language.
- If a version mismatch is suspected, provide commands to check the current Java version (`java -version`).
- Give clear step-by-step instructions for setting up a run environment.
