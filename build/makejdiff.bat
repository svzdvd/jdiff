cd ..\src
javac -classpath %JAVA_HOME%\lib\tools.jar;..\lib\xerces.jar;%CLASSPATH% jdiff\*.java
cd ..\build