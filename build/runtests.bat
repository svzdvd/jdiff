cd ..\test

REM Make sure we can find xerces.jar
setlocal 
set CLASSPATH=..\lib\xerces.jar;%CLASSPATH%

REM These are the packages in each version of the API
set OLDPKGS=RemovedPackage ChangedPackage ChangedPackageDoc
set NEWPKGS=AddedPackage ChangedPackage ChangedPackageDoc

REM Generate JavaDoc for the old API
mkdir olddocs
cd olddocs
javadoc -private -sourcepath ..\old -doctitle "JDiff Test Old API" -windowtitle "JDiff Test Old API" %OLDPKGS%
cd ..

REM Generate JavaDoc for the new API
mkdir newdocs
cd newdocs
javadoc -private -sourcepath ..\new -doctitle "JDiff Test New API" -windowtitle "JDiff Test New API" %NEWPKGS%
cd ..

REM Generate XML for the old API.
javadoc -private -excludeclass private -excludemember private -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\src -apiname "Old Test API" -sourcepath old %OLDPKGS%

REM Generate XML for the new API
javadoc -private -excludeclass private -excludemember private -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\src -apiname "New Test API" -sourcepath new %NEWPKGS%

REM Generate HTML report comparing the old and new APIs
javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\src -d newdocs -stats -oldapi "Old Test API" -newapi "New Test API" -javadocold "../../olddocs/" -javadocnew "../../newdocs/" -sourcepath ..\lib Null
copy ..\lib\background.gif newdocs

cd ..\build

REM Now open the file ..\test\newdocs\changes.html in a browser
