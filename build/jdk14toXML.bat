REM This would scan all the private classes and members too
REM c:\local\jdk1.3.x\bin\javadoc -private -excludeclass private -excludemember private 

REM Remember to set the sourcepath to find the Java source files on your system
setlocal
set SRCPATH=C:\jdktests\1.4b3\src

javadoc -J-Xmx256m -doclet jdiff.JDiff -docletpath ..\src -source 1.4 -apiname J2SE1.4 -sourcepath .;%SRCPATH% @jdk14pkglist.txt