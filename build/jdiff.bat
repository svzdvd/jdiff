REM Same as runtests.bat, but uses JDiff as a standalone Java application to 
REM avoid having to call Javadoc once for the old API, once for the new API
REM and once to compare the two APIs and generate the HTML report.

cd ..\test

REM Make sure we can find xerces.jar
setlocal 
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;..\lib\xerces.jar;..\lib\jdiff.jar;%CLASSPATH%
REM set CLASSPATH=%JAVA_HOME%\lib\tools.jar;..\lib\xerces.jar;..\src;%CLASSPATH%

REM These are the packages in each version of the API
set OLDPKGS=RemovedPackage ChangedPackage ChangedPackageDoc ChangedPackageDoc2
set NEWPKGS=AddedPackage ChangedPackage ChangedPackageDoc ChangedPackageDoc2

REM Generate Javadoc for the old API
mkdir olddocs
cd olddocs
javadoc -private -sourcepath ..\old -doctitle "JDiff Test Old API" -windowtitle "JDiff Test Old API" %OLDPKGS%
cd ..

REM Generate Javadoc for the new API
mkdir newdocs
cd newdocs
javadoc -private -sourcepath ..\new -doctitle "JDiff Test New API" -windowtitle "JDiff Test New API" %NEWPKGS%
cd ..

REM Generate an HTML report comparing the old and new APIs, using a single
REM Java application to call the JDiff Javadoc doclet.
java -classpath %CLASSPATH% jdiff.JDiff

REM TODO use a configuration file?
REM TODO define javadoc args, scanning args, report args or use @argfile @files?

REM java -classpath %CLASSPATH% jdiff.JDiff -private -excludeclass private -excludemember private -d newdocs -stats -oldapi "Old Test API" -sourcepathold old -javadocold "../../olddocs/" -newapi "New Test API" -sourcepathnew new -javadocnew "../../newdocs/"

copy ..\lib\background.gif newdocs
copy ..\lib\black.gif newdocs\changes\black.gif

REM Now open the file ..\test\newdocs\changes.html in a browser
