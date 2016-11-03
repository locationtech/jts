#!/bin/sh

#to change L&F if desired.  Blank is default
JAVA_LOOKANDFEEL="-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"
#JAVA_LOOKANDFEEL=""

JAVA_OPTS="-Xms256M -Xmx1024M"

APP_OPTS=""

if test "x$JTS_LIB_DIR" = "x"; then
        JTS_LIB_DIR=`dirname $0`/../lib/
fi

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
CP=
for i in `ls ${JTS_LIB_DIR}/*.jar`
do
  CP=${CP}:${i}
done

#---------------------------#
# run the program           #
#---------------------------#
MAIN=com.vividsolutions.jtstest.testbuilder.JTSTestBuilder
java -cp ".:${CP}" $JAVA_OPTS  $JAVA_LOOKANDFEEL $MAIN $APP_OPTS
