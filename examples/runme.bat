REM This batch file will generate JavaDocs for each version of the 
REM SuperProduct source code. Then it generates XML from each version, 
REM and finally compares the two XML files to produce an HTML report describing
REM every change in the API between the two releases of SuperProduct.

REM Make sure we can find xerces.jar for XML parsing
setlocal 
set CLASSPATH=..\..\lib\xerces.jar;%CLASSPATH%

REM These are the packages in each version of the API
set OLDPKGS=com.acme.sp com.acme.util
set NEWPKGS=com.acme.sp com.acme.spextra

REM STEP ONE. Generate JavaDoc for the old API (version 1.0 of SuperProduct)
mkdir output\olddocs
cd output\olddocs
javadoc -sourcepath ..\..\SuperProduct1.0 -doctitle "SuperProduct 1.0 API Documentation" -windowtitle "SuperProduct 1.0 API Documentation" %OLDPKGS%
cd ..\..

REM STEP TWO. Generate JavaDoc for the new API (version 2.0 of SuperProduct)
mkdir output\newdocs
cd output\newdocs
javadoc -sourcepath ..\..\SuperProduct2.0 -doctitle "SuperProduct 2.0 API Documentation" -windowtitle "SuperProduct 2.0 API Documentation"  %NEWPKGS%
cd ..

REM STEP THREE. Generate XML for the old API.
javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\..\lib\jdiff.jar -apiname "SuperProduct 1.0" -sourcepath ..\SuperProduct1.0 %OLDPKGS%

REM STEP FOUR. Generate XML for the new API
javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\..\lib\jdiff.jar -apiname "SuperProduct 2.0" -sourcepath ..\SuperProduct2.0 %NEWPKGS%

REM STEP FIVE. Generate HTML report comparing the old and new APIs
javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\..\lib\jdiff.jar -d newdocs -oldapi "SuperProduct 1.0" -newapi "SuperProduct 2.0" -javadocold "../../olddocs/" -javadocnew "../../newdocs/" -sourcepath ..\..\lib Null

cd ..

REM Now open the file output\newdocs\changes.html in a browser