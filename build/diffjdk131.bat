REM Old Javadoc is not at the main Sun site

setlocal
set outputdir=..\jdkchanges5
set options=-docchanges -stats

mkdir %outputdir%
javadoc -J-Xmx256m -doclet jdiff.JDiff -docletpath ..\src -oldapi J2SE1.3 -d %outputdir% -newapi J2SE1.3.1 -javadocnew "http://java.sun.com/j2se/1.3/docs/api/" ..\lib\Null.java
copy ..\lib\background.gif %outputdir%
copy ..\lib\black.gif %outputdir%
