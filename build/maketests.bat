cd ..\test

REM Build the old API
javac -classpath %JAVA_HOME%\lib\tools.jar;..\lib\xerces.jar;%CLASSPATH% old\RemovedPackage\*.java old\ChangedPackage\*.java old\ChangedPackageDoc\*.java old\ChangedPackageDoc2\*.java old\*.java

REM Build the new API
javac -classpath %JAVA_HOME%\lib\tools.jar;..\lib\xerces.jar;%CLASSPATH% new\AddedPackage\*.java new\ChangedPackage\*.java new\ChangedPackageDoc\*.java new\ChangedPackageDoc2\*.java new\*.java

REM Create jar files of the each version
cd old
jar cf SuperProduct10.jar RemovedPackage\*.class ChangedPackage\*.class ChangedPackageDoc\*.class ChangedPackageDoc2\*.class

cd ..\new
jar cf SuperProduct20.jar AddedPackage\*.class ChangedPackage\*.class ChangedPackageDoc\*.class ChangedPackageDoc2\*.class

cd ..\..\build