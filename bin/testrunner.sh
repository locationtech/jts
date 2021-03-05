#!/bin/sh

if test "x$JTS_LIB_DIR" = "x"; then
	JTS_LIB_DIR=`dirname $0`/../modules/tests/target
fi

#---------------------------#
# run the program           #
#---------------------------#
java -jar ${JTS_LIB_DIR}/JTSTestRunner.jar "$@"
