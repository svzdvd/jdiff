
REM C:\jakarta-ant-1.4.1\bin\ant %*

cd ..\src
javac -classpath %JAVA_HOME%\lib\tools.jar;..\lib\xerces.jar;%CLASSPATH% jdiff\*.java
cd ..\build