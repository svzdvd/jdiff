@echo off

setlocal
REM Set JAVA_HOME to the root of your local Java installation, e.g. C:\jdk1.3
REM Environment variables can be set here, or picked up from the environment.
if "x%JAVA_HOME%" == "x" set JAVA_HOME=C:\jdk1.3.x

REM You should not need to change anything below this line.
REM 
REM %~dp0 is the name of the directory where this script is located
REM If the value is not correct, set JDIFF_INSTALL, to C:\jdiff for example.
set DEFAULT_JDIFF_INSTALL=%~dp0
set DEFAULT_JDIFF_INSTALL=%DEFAULT_JDIFF_INSTALL%\..
if "x%JDIFF_INSTALL%" == "x" set JDIFF_INSTALL=%DEFAULT_JDIFF_INSTALL%

REM This adds xerces.jar and ant.jar to the classpath, in case they are not 
REM there already.
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%JDIFF_INSTALL%\lib\xerces.jar;%JDIFF_INSTALL%\lib\ant.jar;%JDIFF_INSTALL%\lib\jdiff.jar;%CLASSPATH%

java -classpath %CLASSPATH% jdiff.JDiff %*

goto done

:usage
echo.
echo jdiff [-buildfile <XML configuration file>]
echo.
echo e.g. jdiff   (uses the local build.xml file)
echo e.g. jdiff -buildfile C:\jdiff\examples\build.xml

:done
