REM Use the classdoc application from
REM http://classdoc.sourceforge.net or http://www.jensgulden.de/, by 
REM Jens Gulden, mail@jensgulden.de,
REM to call a doclet such as jdiff on a .jar file rather than on source.
REM This script can be used to run JDiff on two jar files.
REM
REM Usage: jdiffjar <Old API name> <Old API jar absolute filename> <New API name> <New API jar absolute filename>
REM
REM e.g. jdiffjar SuperProduct1.0 C:\jdiff\test\old\SuperProduct1.0.jar SuperProduct2.0 C:\jdiff\test\new\SuperProduct2.0.jar 
REM You can also scan multiple jar files using classdoc

setlocal
set JDIFF_HOME=C:\jdiff
REM Change this to point to wherever classdoc is installed locally
REM e.g. C:\myDir\classdoc.jar. By default, it is in the lib directory.
set CLASSDOC=%JDIFF_HOME%\lib\classdoc.jar
set JDIFF=%JDIFF_HOME%\lib\jdiff.jar;%JDIFF_HOME%\lib\xerces.jar

REM The first (old) jar to be scanned
set INPUTNAME1=%1
set INPUTJAR1=%2
REM The second (new) jar to be scanned
set INPUTNAME2=%3
set INPUTJAR2=%4

REM STEP ONE. Generate Javadoc for the old API from the jar - no text
mkdir output\olddocs
cd output\olddocs
java -cp %INPUTJAR1%;%CLASSDOC%;%JAVA_HOME%\lib\tools.jar;%CLASSPATH% classdoc -docpath %INPUTJAR1%
cd ..\..

REM STEP TWO. Generate Javadoc for the new API from the jar - no text
mkdir output\newdocs
cd output\newdocs
java -cp %INPUTJAR2%;%CLASSDOC%;%JAVA_HOME%\lib\tools.jar;%CLASSPATH% classdoc -docpath %INPUTJAR2%
cd ..

REM Choose the level of comparison
set DOCLEVEL=-private -excludeclass private -excludemember private

REM STEP THREE. Generate XML for the old API.
java -cp %INPUTJAR1%;%CLASSDOC%;%JDIFF%;%JAVA_HOME%\lib\tools.jar;%CLASSPATH% classdoc -docpath %INPUTJAR1% -doclet jdiff.JDiff -packagesonly -apiname %INPUTNAME1% %DOCLEVEL% 

REM STEP FOUR. Generate XML for the new API
java -cp %INPUTJAR2%;%CLASSDOC%;%JDIFF%;%JAVA_HOME%\lib\tools.jar;%CLASSPATH% classdoc -docpath %INPUTJAR2% -doclet jdiff.JDiff -packagesonly -apiname %INPUTNAME2% %DOCLEVEL%

REM STEP FIVE. Generate HTML report comparing the old and new APIs
javadoc %DOCLEVEL% -J-Xmx128m -doclet jdiff.JDiff -docletpath %JDIFF% -d newdocs -stats -oldapi %INPUTNAME1% -newapi %INPUTNAME2% -javadocold "../../olddocs/" -javadocnew "../../newdocs/" %JDIFF_HOME%\lib\Null.java

copy %JDIFF_HOME%\lib\background.gif newdocs
copy %JDIFF_HOME%\lib\black.gif newdocs
