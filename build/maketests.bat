cd ..\test

REM Build the old API
javac -classpath %JAVA_HOME%\lib\tools.jar;..\lib\xerces.jar;%CLASSPATH% old\RemovedPackage\*.java old\ChangedPackage\*.java

REM Build the new API
javac -classpath %JAVA_HOME%\lib\tools.jar;..\lib\xerces.jar;%CLASSPATH% new\AddedPackage\*.java new\ChangedPackage\*.java

REM Create jar files of the each version
cd old
jar cf SuperProduct10.jar RemovedPackage\*.class ChangedPackage\*.class
cd ..\new
jar cf SuperProduct20.jar AddedPackage\*.class ChangedPackage\*.class

cd ..\..\build