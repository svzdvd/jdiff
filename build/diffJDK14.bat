mkdir ..\jdkchanges
javadoc -J-Xmx256m -doclet jdiff.JDiff -docletpath ..\src -oldapi JDK1.3 -d ..\jdkchanges -stats -newapi JDK1.4 -javadocold "http://java.sun.com/j2se/1.3/docs/api/" -javadocnew "http://java.sun.com/j2se/1.4/docs/api/" ..\lib\Null.java
copy ..\lib\background.gif ..\jdkchanges
copy ..\lib\black.gif ..\jdkchanges\changes
