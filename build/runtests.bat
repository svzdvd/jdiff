cd ..\test

REM Make sure we can find xerces.jar
setlocal 
set CLASSPATH=..\lib\xerces.jar;%CLASSPATH%

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

REM Generate XML for the old API. 
javadoc -private -excludeclass private -excludemember private -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\src -apiname "Old Test API" -sourcepath old %OLDPKGS%

REM Generate XML for the new API. This uses local copies of the DTD files.
REM javadoc -private -excludeclass private -excludemember private -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\src -apiname "New Test API" -baseURI "file:///C:/jdiff/lib" -sourcepath new %NEWPKGS%
javadoc -private -excludeclass private -excludemember private -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\src -apiname "New Test API" -sourcepath new %NEWPKGS%

REM Generate an HTML report comparing the old and new APIs
javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\src -d newdocs -stats -oldapi "Old Test API" -newapi "New Test API" -javadocold "../../olddocs/" -javadocnew "../../newdocs/" ..\lib\Null.java
copy ..\lib\background.gif newdocs
copy ..\lib\black.gif newdocs\changes\black.gif

REM Generate another HTML report comparing the old and new APIs, but ignoring 
REM changes in documentation by using -nodocchanges
mkdir newdocs2
javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\src -d newdocs2 -nodocchanges -stats -oldapi "Old Test API" -newapi "New Test API" -javadocold "../../olddocs/" -javadocnew "../../newdocs/" ..\lib\Null.java
copy ..\lib\background.gif newdocs2
copy ..\lib\black.gif newdocs2\changes\black.gif

cd ..\build

REM Now open the file ..\test\newdocs\changes.html in a browser
REM and also open the file ..\test\newdocs2\changes.html in a browser
