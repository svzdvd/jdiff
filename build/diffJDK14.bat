setlocal
set outputdir=..\jdkchanges3
REM set options=-docchanges -stats

mkdir %outputdir%
javadoc -J-Xmx256m -doclet jdiff.JDiff -docletpath ..\src -oldapi J2SE1.3 -d %outputdir% -newapi J2SE1.4 %options% -javadocold "http://java.sun.com/j2se/1.3/docs/api/" -javadocnew "http://java.sun.com/j2se/1.4/docs/api/" ..\lib\Null.java
copy ..\lib\background.gif %outputdir%
copy ..\lib\black.gif %outputdir%
