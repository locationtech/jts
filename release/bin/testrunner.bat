@echo off
REM A batch file to run the JTS Test Runner.  
REM Requirements: Maven build must be run to create fat jar  (mvn clean install)

java -jar %~dp0\..\modules\tests\target\JTSTestRunner.jar %*

