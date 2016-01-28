The jt-ora module requires the oracle JDBC driver in order to compile.

Download the ojdbc5.jar driver from oracle and install local maven repository:

    mvn install:install-file -Dfile=ojdbc7.jar \
       -DgroupId=com.oracle -DartifactId=ojdbc7 \
       -Dversion=11.1.0.7.0 -Dpackaging=jar -DgeneratePom=true

You can then build using the oracle profile:

    mvn -Poracle