@echo off
rem A batch file to run the JTS Test Builder

set CLASSPATH=
for %%i in (..\lib\*.*) do (
 set jarfile=%%i

 rem If we append to a variable inside the for, only the last entry will
 rem be kept. So append to the variable outside the for.
 rem See http://www.experts-exchange.com/Operating_Systems/MSDOS/Q_20561701.html.
 rem [Jon Aquino]

 call :setclass
)  

set MEMSIZE=256

start javaw -Xmx%MEMSIZE%M com.vividsolutions.jtstest.testbuilder.JTSTestBuilder

goto :eof

:setclass
set CLASSPATH=%jarfile%;%CLASSPATH%
set jarfile=

:eof