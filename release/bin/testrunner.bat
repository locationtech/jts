@echo off
REM A batch file to run the JTS Test Runner.  

java -cp %~dp0\JTSTestRunner-1.15.0.jar org.locationtech.jtstest.testrunner.TopologyTestApp %*