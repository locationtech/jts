@echo off
REM A batch file to run the JTS Test Runner.  
REM Requirements: run the Maven build  (mvn clean install)

java -jar %~dp0\..\modules\tests\target\jts-tests-1.15.0-SNAPSHOT-jar-with-dependencies.jar %*

