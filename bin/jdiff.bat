@echo off

setlocal
REM Environment variables can be set here or picked up from the environment.
REM Set JAVA_HOME to the root of your local Java installation, e.g. C:\jdk1.3
REM set JAVA_HOME=C:\j2sdk1.4.0-beta3
if "x%JAVA_HOME%" == "x" set JAVA_HOME=C:\local\jdk1.3.x

REM Set JDIFF_INSTALL to the location where JDiff is installed, e.g. C:\jdiff
if "x%JDIFF_INSTALL%" == "x" set JDIFF_INSTALL=C:\jdiff

REM You should not need to change anything below this line.
REM This adds xerces.jar and ant.jar to the classpath, in case they are not 
REM there already.
REM set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%JDIFF_INSTALL%\lib\xerces.jar;%JDIFF_INSTALL%\lib\ant.jar;%JDIFF_INSTALL%\lib\jdiff.jar;%CLASSPATH%
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%JDIFF_INSTALL%\lib\xerces.jar;%JDIFF_INSTALL%\lib\ant.jar;%JDIFF_INSTALL%\src;%CLASSPATH%

java -classpath %CLASSPATH% jdiff.JDiff %*

goto done

:usage
echo.
echo jdiff [-buildfile <XML configuration file>]
echo.
echo e.g. jdiff   (uses the local build.xml file)
echo e.g. jdiff -buildfile C:\jdiff\examples\build.xml

:done
