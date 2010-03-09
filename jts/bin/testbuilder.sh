#!/bin/sh

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
THE_CLASSPATH=
for i in `ls ../lib/*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done

#---------------------------#
# run the program           #
#---------------------------#
java -cp ".:${THE_CLASSPATH}"
com.vividsolutions.jtstest.testbuilder.JTSTestBuilder
