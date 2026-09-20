@echo off
rem Starts the Dance Studio console application.
rem The program detects the console encoding itself (use -Dapp.encoding=UTF-8 to force UTF-8).
set "JAVA_CMD=java"
where java >nul 2>nul && goto java_found
if not defined JAVA_HOME goto no_java
set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
:java_found
if not exist target\dance-studio.jar goto no_jar
"%JAVA_CMD%" -jar target\dance-studio.jar %*
goto :eof
:no_java
echo Java was not found. Install JDK 21 or newer, or set the JAVA_HOME variable.
exit /b 1
:no_jar
echo The project is not built yet. Run: mvn clean package
exit /b 1
