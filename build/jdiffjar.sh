#!/bin/bash

# Use the classdoc application from
# http://classdoc.sourceforge.net or http://www.jensgulden.de/, by 
# Jens Gulden, mail@jensgulden.de,
# to call a doclet such as jdiff on a .jar file rather than on source.
# This script can be used to run JDiff on two jar files.
#
# Usage: jdiffjar.sh <Old API name> <Old API jar absolute filename> <New API name> <New API jar absolute filename>
#
# e.g. jdiffjar SuperProduct1.0 /home/user/myDir/jdiff/test/old/SuperProduct1.0.jar SuperProduct2.0 /home/user/myDir/jdiff/test/new/SuperProduct2.0.jar 
# You can also scan multiple jar files using classdoc

# Assumes that JAVA_HOME has been set 
export PATH=$PATH:$JAVA_HOME/bin

JDIFF_HOME=/home/matt/personal/jdiff
# Change this to point to wherever classdoc is installed locally
# e.g. /home/user/myDir/classdoc.jar. By default, it is in the lib directory.
CLASSDOC=$JDIFF_HOME/lib/classdoc.jar
JDIFF=$JDIFF_HOME/lib/jdiff.jar:$JDIFF_HOME/lib/xerces.jar

# The first (old) jar to be scanned
INPUTNAME1=$1
INPUTJAR1=$2
# The second (new) jar to be scanned
INPUTNAME2=$3
INPUTJAR2=$4

# If extra jar files are required, add them to CLASSPATH

# STEP ONE. Generate Javadoc for the old API from the jar - no text
mkdir -p output/olddocs
cd output/olddocs
echo java -cp $INPUTJAR1:$CLASSDOC:$JAVA_HOME/lib/tools.jar:$CLASSPATH classdoc -docpath $INPUTJAR1

exit

cd ../..

# STEP TWO. Generate Javadoc for the new API from the jar - no text
mkdir output/newdocs
cd output/newdocs
java -cp $INPUTJAR2:$CLASSDOC:$JAVA_HOME/lib/tools.jar:$CLASSPATH classdoc -docpath $INPUTJAR2
cd ..

# Choose the level of comparison
DOCLEVEL=-private -excludeclass private -excludemember private

# STEP THREE. Generate XML for the old API.
java -cp $INPUTJAR1:$CLASSDOC:$JDIFF:$JAVA_HOME/lib/tools.jar:$CLASSPATH classdoc -docpath $INPUTJAR1 -doclet jdiff.JDiff -packagesonly -apiname $INPUTNAME1 $DOCLEVEL 

# STEP FOUR. Generate XML for the new API
java -cp $INPUTJAR2:$CLASSDOC:$JDIFF:$JAVA_HOME/lib/tools.jar:$CLASSPATH classdoc -docpath $INPUTJAR2 -doclet jdiff.JDiff -packagesonly -apiname $INPUTNAME2 $DOCLEVEL

# STEP FIVE. Generate HTML report comparing the old and new APIs
javadoc $DOCLEVEL -J-Xmx128m -doclet jdiff.JDiff -docletpath $JDIFF -d newdocs -stats -oldapi $INPUTNAME1 -newapi $INPUTNAME2 -javadocold "../../olddocs/" -javadocnew "../../newdocs/" $JDIFF_HOME/lib/Null.java

cp $JDIFF_HOME/lib/background.gif newdocs
cp $JDIFF_HOME/lib/black.gif newdocs
