REM This batch file will generate Javadoc HTML for each version of the 
REM SuperProduct source code. Then it generates XML from each version, 
REM and finally compares the two XML files to produce an HTML report describing
REM every change in the API between the two releases of SuperProduct.

set BASE_URI=http://www.w3.org
REM You only need to uncomment this next line and change it to where JDiff 
REM lib is installed on your machine if you are not connected to the Internet.
REM set BASE_URI=file:///C:/jdiff/lib

REM Make sure we can find xerces.jar for XML parsing
setlocal 
set CLASSPATH=..\..\lib\xerces.jar;%CLASSPATH%

REM These are the packages in each version of the API
set OLDPKGS=com.acme.sp com.acme.util
set NEWPKGS=com.acme.sp com.acme.spextra

REM STEP ONE. Generate Javadoc for the old API (version 1.0 of SuperProduct)
mkdir sample_output\olddocs
cd sample_output\olddocs
javadoc -sourcepath ..\..\SuperProduct1.0 -doctitle "SuperProduct 1.0 API Documentation" -windowtitle "SuperProduct 1.0 API Documentation" %OLDPKGS%
cd ..\..

REM STEP TWO. Generate Javadoc for the new API (version 2.0 of SuperProduct)
mkdir sample_output\newdocs
cd sample_output\newdocs
javadoc -sourcepath ..\..\SuperProduct2.0 -doctitle "SuperProduct 2.0 API Documentation" -windowtitle "SuperProduct 2.0 API Documentation"  %NEWPKGS%
cd ..

REM STEP THREE. Generate XML for the old API.
javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\..\lib\jdiff.jar;..\..\lib\xerces.jar -apiname "SuperProduct 1.0" -baseURI "%BASE_URI%" -sourcepath ..\SuperProduct1.0 %OLDPKGS%

REM STEP FOUR. Generate XML for the new API
javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\..\lib\jdiff.jar;..\..\lib\xerces.jar -apiname "SuperProduct 2.0" -baseURI "%BASE_URI%" -sourcepath ..\SuperProduct2.0 %NEWPKGS%

REM STEP FIVE. Generate HTML report comparing the old and new APIs
javadoc -J-Xmx128m -doclet jdiff.JDiff -docletpath ..\..\lib\jdiff.jar;..\..\lib\xerces.jar -d newdocs -stats -oldapi "SuperProduct 1.0" -newapi "SuperProduct 2.0" -javadocold "../../olddocs/" -javadocnew "../../newdocs/" ..\..\lib\Null.java
copy ..\..\lib\background.gif newdocs
copy ..\..\lib\black.gif newdocs\black.gif

cd ..

REM Now open the file sample_output\newdocs\changes.html in a browser
