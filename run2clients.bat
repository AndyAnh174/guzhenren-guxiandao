@echo off
set JAVA_HOME=C:\Users\ADMIN\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.10.7-hotspot
cd /d C:\moddev\guxiandao
start "Dev" cmd /k "set JAVA_HOME=C:\Users\ADMIN\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.10.7-hotspot && gradlew runClient --args=--username=Dev"
ping -n 10 127.0.0.1 >nul
start "Player2" cmd /k "set JAVA_HOME=C:\Users\ADMIN\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.10.7-hotspot && gradlew runClient --args=--username=Player2"
