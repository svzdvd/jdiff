REM Old Javadoc is not at the main Sun site

mkdir ..\jdkchanges3
javadoc -J-Xmx256m -doclet jdiff.JDiff -docletpath ..\src -oldapi J2SE1.3 -d ..\jdkchanges3 -newapi J2SE1.3.1 -javadocnew "http://java.sun.com/j2se/1.3/docs/api/" ..\lib\Null.java
copy ..\lib\background.gif ..\jdkchanges3
copy ..\lib\black.gif ..\jdkchanges3\black.gif
