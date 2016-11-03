#!/bin/sh

if test "x$JTS_LIB_DIR" = "x"; then
	JTS_LIB_DIR=`dirname $0`/../lib/
fi

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
THE_CLASSPATH=
for i in `ls ${JTS_LIB_DIR}/*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done

#---------------------------#
# run the program           #
#---------------------------#
java -cp ".:${THE_CLASSPATH}" \
com.vividsolutions.jtstest.testrunner.TopologyTestApp $@
