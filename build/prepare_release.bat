REM Clean up ready to release jdiff as a zip file
REM Start in the top-level directory and make sure that JAVA_HOME is set
REM to the root of the current Java installation

cd C:\jdiff

del *~
del jdiff.jar
rm -rf jdkchanges

cd build
del *~
del jdiff.jar
del *.xml

cd ..\src
rm -rf *.xml jdiff.jar _notes changes* stylesheet* *~
del jdiff\*.class
javac -classpath %JAVA_HOME%\lib\tools.jar;..\lib\xerces.jar;%CLASSPATH% jdiff\*.java

REM Make the jar file
mkdir ..\lib
del ..\lib\*~
jar cvf ..\lib\jdiff.jar jdiff\*.class
del jdiff\*.class
del jdiff\*~
del jdiff.jar

cd ..\examples
del *~
rm -rf output
REM assumes that the jdiff.jar has been built already      
call runme
mv output sample_output
del SuperProduct1.0\com\acme\sp\*~
del SuperProduct1.0\com\acme\sp\*.class
del SuperProduct1.0\com\acme\util\*~
del SuperProduct1.0\com\acme\util\*.class
del SuperProduct2.0\com\acme\sp\*~
del SuperProduct2.0\com\acme\sp\*.class
del SuperProduct2.0\com\acme\spextra\*~
del SuperProduct2.0\com\acme\spextra\*.class

cd ..\test
copy ..\src\*.xsd .
rm -rf *.xml newdocs olddocs changes* stylesheet*

del old\RemovedPackage\*~
del old\ChangedPackage\*~
del new\ChangedPackage\*~
del new\AddedPackage\*~

del old\RemovedPackage\*.class
del old\ChangedPackage\*.class
del new\ChangedPackage\*.class
del new\AddedPackage\*.class

cd ..
del jdiff.zip

REM Now start explorer, choose "Add to Zip" and create jdiff.zip from the 
REM top-level, remembering to check the "Save full path info" box.
start .
