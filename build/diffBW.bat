mkdir ..\bware
javadoc -J-Xmx256m -doclet jdiff.JDiff -docletpath ..\src -oldapi BW3x -d ..\bware -newapi BW401 -stats ..\lib\Null.java
copy ..\lib\background.gif ..\bware
copy ..\lib\black.gif ..\bware\changes\black.gif
