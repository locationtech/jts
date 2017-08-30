@echo off
rem Windows batch file to run the JTS Test Builder from the Maven target directory

set JAVA_OPTS=-Xms256M -Xmx1024M

REM to change L&F if desired.  Blank is default
REM JAVA_LOOKANDFEEL="-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"
set JAVA_LOOKANDFEEL=

REM Default app options can be added here
set APP_OPTS=%*

javaw %JAVA_OPTS% %JAVA_LOOKANDFEEL% -jar %~dp0\..\modules\app\target\JTSTestBuilder.jar %APP_OPTS%
