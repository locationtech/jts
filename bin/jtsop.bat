@echo off
rem Windows batch file to run the JTS TestBuilderOpCmd from the Maven target directory

set JAVA_OPTS=-Xms256M -Xmx2048M

REM Default app options can be added here
set APP_OPTS=%*

java %JAVA_OPTS% -cp %~dp0..\modules\app\target\JTSTestBuilder.jar org.locationtech.jtstest.cmd.JTSOpCmd %APP_OPTS% 
