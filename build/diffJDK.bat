mkdir ..\jdkchanges
javadoc -doclet jdiff.JDiff -docletpath ..\src -oldapi JDK1.2 -d ..\jdkchanges -newapi JDK1.3 -stats -javadocold "http://java.sun.com/j2se/1.2/docs/api/" -javadocnew "http://java.sun.com/j2se/1.3/docs/api/" ..\lib\Null.java
copy ..\lib\background.gif ..\jdkchanges
copy ..\lib\black.gif ..\jdkchanges\changes
