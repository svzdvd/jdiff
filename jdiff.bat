@echo off

setlocal
REM Environment variables can be set here or picked up from the environment.
REM Set JAVA_HOME to the root of your local Java installation, e.g. C:\jdk1.3
REM set JAVA_HOME=C:\j2sdk1.4.0-beta3
if "x%JAVA_HOME%" == "x" set JAVA_HOME=C:\local\jdk1.3.x

REM Set JDIFF_HOME to the location where JDiff is installed, e.g. C:\jdiff
if "x%JDIFF_HOME%" == "x" set JDIFF_HOME=C:\jdiff

REM You should not need to change anything below this line
REM set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%JDIFF_HOME%\lib\xerces.jar;%JDIFF_HOME%\lib\jdiff.jar;%CLASSPATH%
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%JDIFF_HOME%\lib\xerces.jar;%JDIFF_HOME%\src;%CLASSPATH%

java -classpath %CLASSPATH% jdiff.JDiff %*

@echo on
copy %JDIFF_HOME%\lib\background.gif .
copy %JDIFF_HOME%\lib\black.gif changes\black.gif
@echo off

goto done

:usage
echo.
echo Change directory to where the Javadoc output for the new API is located.
echo Then type:
echo.
echo jdiff <old product name> <old source directory> <new product name> <new source directory>
echo.
echo e.g. jdiff SuperProduct1.0 C:\jdiff\examples\SuperProduct1.0 SuperProduct2.0 C:\jdiff\examples\SuperProduct2.0
pause

:done

