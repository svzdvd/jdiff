setlocal
set outputdir=..\jdkchanges2

mkdir %outputdir%
javadoc -J-Xmx256m -doclet jdiff.JDiff -docletpath ..\src -oldapi J2SE1.3 -d %outputdir% -newapi J2SE1.4 -javadocold "http://java.sun.com/j2se/1.3/docs/api/" -javadocnew "http://java.sun.com/j2se/1.4/docs/api/" ..\lib\Null.java
copy ..\lib\background.gif %outputdir%
copy ..\lib\black.gif %outputdir%\black.gif
